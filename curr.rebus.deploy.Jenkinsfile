// для выполнения необходимо установить Jenkins плагин: // https://plugins.jenkins.io/extended-choice-parameter/

pipeline {
    agent {
      label 'CI2'
    }

    environment {
        PARAM_PATH = "/sd/jenkins/ci/jobs/curr.rebus.deploy/rebus_el7.params"
    }

    stages {
        stage('Deploy to stand with Ansible') {
            steps {
              withCredentials([file(credentialsId: 'ansible_vault_password', variable: 'VAULT_PASSWORD_FILE')]) {
                // copyArtifacts fingerprintArtifacts: true, projectName: 'curr.rebus.build', selector: lastCompleted()
                script {
                  properties([
                    parameters([
                      string(name: 'REBUS_USERNAME', defaultValue: "rebus333", description: 'Please specify username'),

                      extendedChoice(name: 'REBUS_VERSION', description: 'Specify rebus version', defaultPropertyFile: "${env.PARAM_PATH}",
                                      defaultPropertyKey: 'REBUS_VERSION', type: 'PT_TEXTBOX', visibleItemCount: 1),

                      extendedChoice(name: 'INVENTORY', description: 'Select Inventory file for deploying', multiSelectDelimiter: ',', quoteValue: false,
                                     type: 'PT_MULTI_SELECT', defaultValue: 'hosts_rebus333_tks2X', value: 'hosts_rebus333_tks2X,hosts_tks2X,hosts_forts_cur', visibleItemCount: 3)

                      // extendedChoice(name: 'REBUS_NAME_GROUP', description: 'Specify group', defaultPropertyFile: "${env.PARAM_PATH}",
                      //                 defaultPropertyKey: 'REBUS_NAME_GROUP', type: 'PT_TEXTBOX', visibleItemCount: 1),
                      // extendedChoice(name: 'SYSTEM_TYPE', description: 'Specify System type', defaultPropertyFile: "${env.PARAM_PATH}",
                      //                 defaultPropertyKey: 'SYSTEM_TYPE', type: 'PT_TEXTBOX', visibleItemCount: 1),
                      // extendedChoice(name: 'STAND', description: 'Select Stand for deploying', multiSelectDelimiter: ',',
                      //                 quoteValue: false, type: 'PT_MULTI_SELECT', defaultValue: 'tks23', value: 'tks23,tks24,tks25', visibleItemCount: 3)
                    ])
                  ])
                }

                sh """
echo -e '---
rebus_username: ${params.REBUS_USERNAME}
system_type: 'curr'
rebus_version: ${params.REBUS_VERSION}
' > extra_vars.curr.rebus.yml
                """
            archiveArtifacts artifacts: 'extra_vars.curr.rebus.yml', onlyIfSuccessful: true

                sh "scp $VAULT_PASSWORD_FILE $ANSIBLE_SERVER:/home/jenkins/repos/testing_infra/.ansible_vault_pass || true"
                sh "scp extra_vars.curr.rebus.yml $ANSIBLE_SERVER:/home/jenkins/repos/testing_infra/extra_vars.curr.rebus.yml || true"
                sh """
                  ssh $ANSIBLE_SERVER "cd /home/jenkins/repos/testing_infra && git pull && \
                  ansible-playbook -i ${params.INVENTORY} rebus.yml --extra-vars "@extra_vars.curr.rebus.yml" --vault-password-file .ansible_vault_pass && \
                  rm -f extra_vars.curr.rebus.yml && rm -f .ansible_vault_pass"
                """
              }
            }
        }
    }
}
