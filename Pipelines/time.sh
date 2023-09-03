#!/bin/bash
timestamp=$(date +%M)
if [ `expr $timestamp % 2` == 0 ]
then
	exit 0
else
  sleep 1
	exit 1
fi

pipeline {
    agent any
    
    stages {
        stage('Build') {
            steps {
                // Your build steps here
            }
        }
    }
    
    post {
        always {
            script {
                def versionsContent = readFile('versions.txt')
                def versionsTable = versionsContent.readLines().collect { line ->
                    def parts = line.split(':')
                    "<tr><td>${parts[0]}</td><td>${parts[1]}</td></tr>"
                }.join('\n')

                def emailBody = """
                    <html>
                    <body>
                        <h2>Table of Contents - Versions</h2>
                        <table border="1">
                            <tr>
                                <th>Module</th>
                                <th>Version</th>
                            </tr>
                            ${versionsTable}
                        </table>
                    </body>
                    </html>
                """

                emailext body: emailBody,
                         subject: "Build Notification",
                         to: "recipient@example.com",
                         mimeType: 'text/html'
            }
        }
    }
}
```

Replace `"recipient@example.com"` with the actual recipient email address. Also, make sure the `versions.txt` file is present in the workspace directory when the pipeline runs. The code reads each line from the `versions.txt` file, splits it using `:` as a delimiter, and generates an HTML table row for each version entry. The resulting HTML content is then used as the email body with the `emailext` step.

Please adapt and modify the code according to your specific needs and Jenkins configuration.