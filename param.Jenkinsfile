pipeline {
    agent {
      label ('slave-sshimanskiy')
    }

    environment {
        SSH_PRV_KEY = credentials('jenkins-private-key')
    }

    parameters {
        gitParameter name: 'BRANCH_TAG',
                     type: 'PT_BRANCH_TAG',
                     defaultValue: 'test'
    }
    stages {
        stage('Stage Select Branch') {
            steps {
                withCredentials(bindings: [sshUserPrivateKey(credentialsId: 'jenkins-private-key', keyFileVariable: 'SSH_PRV_KEY')]) {
                checkout([$class: 'GitSCM',
                          branches: [[name: "${params.BRANCH_TAG}"]],
                          doGenerateSubmoduleConfigurations: false,
                          extensions: [],
                          gitTool: 'Default',
                          submoduleCfg: [],
                          userRemoteConfigs: [[url: 'https://bitbucket.org/relative_localization/doors.git']]
                        ])
                }
            }
        }
    }
}
