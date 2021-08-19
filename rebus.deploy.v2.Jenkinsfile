/*
для выполнения необходимо установить следующий Jenkins плагины:
https://plugins.jenkins.io/uno-choice/
https://plugins.jenkins.io/extended-choice-parameter/
https://plugins.jenkins.io/git-parameter/
*/
pipeline {
    agent {
      label 'master'
    }

    stages {
        stage('Clone testing_infra repo') {
          steps {
            dir('testing_infra') {
              git branch: "${params.ANSIBLE_GIT_BRANCH}", url: 'git@gitlab.web-tech.moex.com:astsdev/testing_infra.git'
            }
          }
        }

        stage('Deploy to stand with Ansible') {
            steps {
              withCredentials([
                file(credentialsId: 'ansible_vault_password', variable: 'VAULT_PASSWORD_FILE'),
                usernamePassword(credentialsId: 'nexus-creds', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASSWORD'),
                usernamePassword(credentialsId: 'ftp-jenkins', usernameVariable: 'FTP_USER', passwordVariable: 'FTP_PASSWORD')
                ]) {
                script {
                  properties([
                    parameters([
                      gitParameter(name: 'ANSIBLE_GIT_BRANCH',
                                   defaultValue: 'master',
                                   description: 'Select branch in repo testing_infra',
                                   type: 'PT_BRANCH_TAG',
                                   quickFilterEnabled: true,
                                   listSize: '5',
                                   branchFilter: 'origin/(.*)',
                                   useRepository: '.*testing_infra.git'),

                      booleanParam(defaultValue: true, description: 'Use ansible-vault password for deploying', name: 'ANSIBLE_VAULT'),

                      [$class: 'ChoiceParameter',
                          name: 'STAND',
                          choiceType: 'PT_MULTI_SELECT',
                          description: 'Select the Playbook from the List',
                          filterLength: 1, filterable: false,
                          script: [
                              $class: 'GroovyScript',
                              fallbackScript: [classpath: [], sandbox: false,
                                  script:
                                      "return['Could not get playbook']"
                              ],
                              script: [classpath: [], sandbox: false,
                                  script:
                                      "return['rebus333_tks23.yml:selected','rebus-dit.yml','rebus-prod.yml']"
                              ]
                          ]
                      ],

                      [$class: 'ChoiceParameter',
                          name: 'FTP_EXTRA_VARS',
                          choiceType: 'PT_MULTI_SELECT',
                          description: 'Select file',
                          filterLength: 1, filterable: true,
                          randomName: 'choice-parameter-10782710957930078',
                          script:
                              [$class: 'GroovyScript',
                              fallbackScript: [classpath: [], sandbox: false,
                                  script: ''],
                              script: [classpath: [], sandbox: false,
                                  script: '''
                                    import org.apache.commons.net.ftp.FTPClient
                                    import org.apache.commons.net.ftp.FTPFile

                                    def path = "asts/extra_vars/"

                                    println("Fetching reverse sorted list of ftp files from $path");
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
                                    BuildMap
                                  ''']
                             ]
                      ],
                      // extendedChoice(name: 'REBUS_USERNAME', description: 'Specify username', defaultPropertyFile: "${env.PARAM_PATH}",
                      //                 defaultPropertyKey: 'REBUS_USERNAME', type: 'PT_TEXTBOX', visibleItemCount: 1),
                      // extendedChoice(name: 'INVENTORY', description: 'Select Inventory file for deploying', multiSelectDelimiter: ',', quoteValue: false,
                      //                type: 'PT_MULTI_SELECT', defaultValue: 'hosts_rebus333_tks2X', value: 'hosts_rebus333_tks2X,hosts_tks2X,hosts_forts_cur', visibleItemCount: 3)
                      ]),
                    ])

                  }
                  deploy () // вызов функции деплоймента
                  cleanWs() // удалить каталог workspace
              }
            }
        }
    }
}

// функция деплоймента
def deploy () {
    def INVENTORY = ''
    if ("${params.STAND}" == 'rebus333_tks23.yml') {
        INVENTORY = "hosts_rebus333_tks23"
    }
    else if ("${params.STAND}" == 'rebus-dit.yml') {
        INVENTORY = "hosts_forts_cur"
    }
    else if ("${params.STAND}" == 'rebus-prod.yml') {
        INVENTORY = "hosts_prod"
    }

    sh "curl -O -u $FTP_USER:$FTP_PASSWORD ftp://10.50.1.12/asts/extra_vars/${params.FTP_EXTRA_VARS}"
    // sh "curl -O -u $FTP_USER:$FTP_PASSWORD ftp://10.50.1.12/asts/extra_vars/extra_vars.curr.rebus.2891.yml" // temp
    // sh "curl -O -u $NEXUS_USER:$NEXUS_PASSWORD http://10.50.1.71:8081/repository/asts/extra_vars/${params.FTP_EXTRA_VARS}"
    archiveArtifacts artifacts: "${params.FTP_EXTRA_VARS}", onlyIfSuccessful: false

    sh """
    cp ${params.FTP_EXTRA_VARS} testing_infra/${params.FTP_EXTRA_VARS}
    cd testing_infra
    if [[ "${params.ANSIBLE_VAULT}" == "true" ]]; then
      ansible-playbook -i stands/${INVENTORY} stands/${params.STAND} --extra-vars "@${params.FTP_EXTRA_VARS}" --vault-password-file $VAULT_PASSWORD_FILE
    else
      ansible-playbook -i stands/${INVENTORY} stands/${params.STAND} --extra-vars "@${params.FTP_EXTRA_VARS}"
    fi
    rm -f ${params.FTP_EXTRA_VARS}
    """
}
