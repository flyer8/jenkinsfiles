/*
для выполнения необходимо установить Jenkins плагины:
https://plugins.jenkins.io/uno-choice/
https://plugins.jenkins.io/extended-choice-parameter/
*/
pipeline {
    agent {
      label 'CI2'
    }

    environment {
        PARAM_PATH = "/sd/jenkins/ci/jobs/curr.rebus.deploy/rebus_el7.params"
    }

    stages {
        stage('Deploy to DIT-stand with Ansible') {
            steps {
              withCredentials([
                file(credentialsId: 'ansible_vault_password', variable: 'VAULT_PASSWORD_FILE'),
                usernamePassword(credentialsId: 'nexus-creds', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASSWORD'),
                usernamePassword(credentialsId: 'ftp-creds', usernameVariable: 'FTP_USER', passwordVariable: 'FTP_PASSWORD')
                ]) {
                // copyArtifacts fingerprintArtifacts: true, projectName: 'curr.rebus.build', selector: lastCompleted()
                script {
                  properties([
                    parameters([
                      [$class: 'ChoiceParameter', name: 'FTP_EXTRA_VARS', choiceType: 'PT_MULTI_SELECT', description: 'Select file',
                      filterLength: 1, filterable: false, randomName: 'choice-parameter-10782710957930078',
                      script: [$class: 'GroovyScript', fallbackScript: [classpath: [], sandbox: false, script: ''],
                      script: [classpath: [], sandbox: false, script: '''
                        import org.apache.commons.net.ftp.FTPClient
                        import org.apache.commons.net.ftp.FTPFile

                        def path = "asts/extra_vars/"

                        println("Fetching reverse sorted list of ftp folders from $path");
                        FTPClient ftpClient = new FTPClient()
                        ftpClient.connect "10.50.1.12",21
                        ftpClient.enterLocalPassiveMode()
                        ftpClient.login "jenkins", "Jenk1ns"
                        ftpClient.changeWorkingDirectory(path)
                        FTPFile[] entries = ftpClient.listFiles().sort { it.getTimestamp().getTime() }.reverse()
                        def BuildMap  = [:]
                        for (FTPFile file : entries) {
                            BuildMap.put(file.getName(),file.getName() )
                        }

                        ftpClient.logout()
                        ftpClient.disconnect()
                        println("Done!");
                        BuildMap
                      ''']
                         ]
                      ],
                      // extendedChoice(name: 'REBUS_USERNAME', description: 'Specify Rebus username', defaultPropertyFile: "${env.PARAM_PATH}",
                      //                 defaultPropertyKey: 'REBUS_USERNAME', type: 'PT_TEXTBOX', visibleItemCount: 1),
                      // extendedChoice(name: 'SYSTEM_TYPE', description: 'Specify System type', defaultPropertyFile: "${env.PARAM_PATH}",
                      //                 defaultPropertyKey: 'SYSTEM_TYPE', type: 'PT_TEXTBOX', visibleItemCount: 1),
                      // extendedChoice(name: 'REBUS_VERSION', description: 'Specify rebus version', defaultPropertyFile: "${env.PARAM_PATH}",
                      //                 defaultPropertyKey: 'REBUS_VERSION', type: 'PT_TEXTBOX', visibleItemCount: 1),
                      // extendedChoice(name: 'STAND', description: 'Select Stand for deploying', multiSelectDelimiter: ',',
                      //                 quoteValue: false, type: 'PT_MULTI_SELECT', defaultValue: 'tks23', value: 'tks23,tks24,tks25', visibleItemCount: 3)
                      ]),
                  ])
                }

                sh "curl -O -u $FTP_USER:$FTP_PASSWORD ftp://10.50.1.12/asts/extra_vars/${params.FTP_EXTRA_VARS}"
                // sh "curl -O -u $NEXUS_USER:$NEXUS_PASSWORD http://10.50.1.71:8081/repository/asts/extra_vars/${params.FTP_EXTRA_VARS}"

                archiveArtifacts artifacts: "${params.FTP_EXTRA_VARS}", onlyIfSuccessful: true

                sh "scp $VAULT_PASSWORD_FILE $ANSIBLE_SERVER:/home/jenkins/repos/testing_infra/.ansible_vault_pass || true"
                sh "scp ${params.FTP_EXTRA_VARS} $ANSIBLE_SERVER:/home/jenkins/repos/testing_infra/${params.FTP_EXTRA_VARS} || true"
                sh '''
                  ssh $ANSIBLE_SERVER "cd /home/jenkins/repos/testing_infra && git pull && \
                  ansible-playbook -i hosts_rebus333_tks2X rebus.yml --extra-vars "@${params.FTP_EXTRA_VARS}" --vault-password-file .ansible_vault_pass && \
                  rm -f ${params.FTP_EXTRA_VARS} && rm -f .ansible_vault_pass"
                '''
              }
            }
        }
    }
}
