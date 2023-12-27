pipeline {
  agent {
    label 'node1.jk'
  }
  stages {
    stage('Clone VProfile Project') {
      steps {
        checkout scmGit(branches: [[name: "${params.BRANCH}"]], extensions: [], userRemoteConfigs: [[credentialsId: 'Yaseenins-Arch', url: 'git@gitlab.yaseenins.com:yaseenins_devops/vprofile.git']])
        script {
          git_revision_id = sh(script: 'git rev-parse --short=8 HEAD', returnStdout:true).trim()
        }
      }
    }
    stage('Artifactory Config') {
      steps {
        script {
          rtServer (
            id: 'yaseenins',
            credentialsId: 'yaseenins-art'
          )
        }
      }
    }
    stage('Maven Build') {
      steps {
        script {
          ansiColor('xterm') {
            sh "mvn clean package -s ${WORKSPACE}/settings.xml"
          }
        }
      }
    }
    stage('Sonar Scan') {
      when {
        expression {
          return params.SonarScan == true
        }
      }
      environment {
        SONAR_TOKEN = credentials('Sonar-Token')
      }
      steps {
        script {
          ansiColor('xterm') {
            sh "mvn clean verify sonar:sonar -Dsonar.projectKey=VProfile -Dsonar.host.url=https://sonar.yaseenins.com -Dsonar.login=\$SONAR_TOKEN -s ${WORKSPACE}/settings.xml"
          }
        }
      }
    }
    stage('Artifact Upload') {
      when {
        expression {
          return params.Deploy == true
        }
      }
      steps {
        script { 
          ansiColor('xterm') {
            dir("${WORKSPACE}/target") {
              String artTarget = "VProfile/Webapps/VProfile-Release-" + new Date().format('yyyyMMdd') +"-${git_revision_id}" +"-${env.BUILD_NUMBER}.war"
              rtUpload(
                serverId: 'yaseenins',
                spec: """{
                  "files": [
                    {
                      "pattern": "*.war",
                      "target": "$artTarget"
                    }
                  ]
                }"""
              )
            }
          }
        }
      }
    }
    stage('Deploy to Tomcat') {
      when {
        expression {
          return params.Deploy == true
        }
      }
      environment {
        TOMCAT_CREDS = credentials('Tomcat-Creds')
      }
      steps {
        script {
          ansiColor {
            def warFile = findFiles(glob: 'target/*.war').collect { it.toString() }.first()
            sh """
              curl -T ${warFile} 'https://tomcat.yaseenins.com/manager/text/deploy?path=/vprofile&update=true' -u \${TOMCAT_CREDS_USR}:\${TOMCAT_CREDS_PSW}
            """
          }
        }
      }
    }
  }
  post {
      failure {
        cleanWs()
      }
      success {
        cleanWs()
      }
    }
    options {
      buildDiscarder(logRotator(numToKeepStr: '5'))
      timeout(time: 25, unit: 'MINUTES')
    }
    parameters {
      string(name: 'BRANCH', defaultValue: 'main', description: '')
      booleanParam(name: 'Deploy', defaultValue: false, description: '')
      booleanParam(name: 'SonarScan', defaultValue: false, description: '')
    }
}
