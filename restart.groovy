pipeline {
  agent any
  stages {
    stage(parameters) {
      steps {
        script {
          properties([
            parameters([
              [$class: 'ChoiceParameter', 
                choiceType: 'PT_RADIO', 
                filterLength: 1, 
                filterable: false, 
                name: 'REALTIME',  
                script: [
                  $class: 'GroovyScript', 
                  fallbackScript: [
                    classpath: [], 
                    oldScript: '', 
                    sandbox: false, 
                    script: ''
                  ], 
                  script: [
                    classpath: [], 
                    oldScript: '', 
                    sandbox: true, 
                    script: 'return[\'yes\']'
                  ]
                ]
              ],
              choice(choices: ['restart', 'start', 'stop'], name: 'Action'),
              [$class: 'CascadeChoiceParameter', 
                choiceType: 'PT_SINGLE_SELECT', 
                filterLength: 1, 
                filterable: true, 
                name: 'Environment', 
                referencedParameters: 'REALTIME', 
                script: [
                  $class: 'GroovyScript', 
                  fallbackScript:[
                    classpath: [], 
                    oldScript: '', 
                    sandbox: false, 
                    script: ''
                  ], 
                  script: [
                    classpath: [], 
                    oldScript: '', 
                    sandbox: true, 
                    script: '''
                      if (REALTIME.equals(\'yes\')){
                        return["dev1", "uat1"]
                      }
                      else {
                        return["dev1", "dev2", "uat1","uat2"]
                      }
                    '''
                  ]
                ]
              ], 
              [$class: 'CascadeChoiceParameter', 
                choiceType: 'PT_SINGLE_SELECT', 
                filterLength: 1, 
                filterable: true, 
                name: 'Artifact',
                referencedParameters: 'REALTIME', 
                script: [
                  $class: 'GroovyScript', 
                  fallbackScript: [
                    classpath: [], 
                    oldScript: '', 
                    sandbox: false, 
                    script: ''
                  ], 
                  script: [
                    classpath: [], 
                    oldScript: '', 
                    sandbox: true, 
                    script: '''
                      if (REALTIME.equals(\'yes\')){
                        return["sep.jar", "pcsearch.jar"]
                      }
                      else {
                        return["batchdseng.jar", "batchsep.jar", "batchorch.jar","batchpcsearch.jar"]
                      }
                    '''
                  ]
                ]
              ]
            ])
          ])
        }
      }  
    }
    stage('Echo') {
      steps {
        script {
          if ( params.REALTIME == "yes") {
            echo "Realtime"
          }
          echo "${Artifact}"
          echo "${params.Environment}"
        }
      }
    }
  }
}
