pipeline {
    agent {
      label 'slave-sshimanskiy'
    }
    parameters {
        gitParameter name: 'BRANCH_TAG',
                     description: 'Please select Branch',
                     type: 'PT_BRANCH_TAG',
                     defaultValue: 'AC-370-test',
                     quickFilterEnabled: true,
                     listSize: '10'
    }
    stages {
        stage('Checkout SCM') {
            steps {
                checkout([$class: 'GitSCM',
                          branches: [[name: "${params.BRANCH_TAG}"]],
                          doGenerateSubmoduleConfigurations: false,
                          extensions: [],
                          gitTool: 'Default',
                          submoduleCfg: [],
                          userRemoteConfigs: [[url: 'git@bitbucket.org:relative_localization/doors.git', credentialsId: 'jenkins-private-key']]
                        ])
            }
        }
        stage("Build Docker image") {
            steps {
                echo " ===== Building image ====="
                sh 'docker build -t doors/debian:$BUILD_NUMBER .'
            }
        }
        stage("Run Docker image") {
            steps {
                echo " ===== Starting image ====="
                sh 'docker run -d -P --hostname doors-srv --name doors-$BUILD_NUMBER doors/debian:$BUILD_NUMBER'
            }
        }

    }
}
