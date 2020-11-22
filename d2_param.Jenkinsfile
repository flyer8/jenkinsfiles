pipeline {
    agent {
      label 'doorsserver2'
    }
    triggers { pollSCM('H 10 * * *') }
    parameters {
        gitParameter name: 'BRANCH_TAG',
                     description: 'Please select Branch',
                     type: 'PT_BRANCH_TAG',
                     defaultValue: 'develop',
                     quickFilterEnabled: true,
                     listSize: '5'
        // string(name: 'PRIMARY_PORT', defaultValue: "5555", description: 'Please specify port')

    }

    stages {
        stage('Clone specified repo') {
            steps {
                checkout([$class: 'GitSCM',
                          branches: [[name: "${params.BRANCH_TAG}"]],
                          doGenerateSubmoduleConfigurations: false,
                          extensions: [],
                          gitTool: 'Default',
                          submoduleCfg: [],
                          userRemoteConfigs: [[url: 'git@bitbucket.org:relative_localization/admin-frontend.git', credentialsId: '57079151-792e-4976-82ca-2ee23716aa71']]
                        ])
            }
        }
        stage("Build Docker image") {
            steps {
                echo " ===== Building image ====="
                sh 'docker build -t 10.2.6.251:5555/doors/admin:$BUILD_NUMBER .'
            }
        }

        stage("Push Docker image to local Registry") {
            steps {
                echo " ===== Pushing image ====="
                //sh 'docker login -u admin -p admin 10.2.6.251:5555' // authentication in case of Nexus3 docker-private
                sh 'docker push 10.2.6.251:5555/doors/admin:$BUILD_NUMBER'
            }
        }

        stage("Run Docker image") {
            steps {
                echo " ===== Starting image ====="
                sh 'docker ps -f name=admin -aq | xargs docker stop | xargs docker rm || true'
                //sh 'docker rmi -f $(docker images | grep doors/admin ) || true'
                sh '''
                docker run -d -p 80:80 \
                --restart always \
                --hostname admin \
                --name admin-$BUILD_NUMBER \
                10.2.6.251:5555/doors/admin:$BUILD_NUMBER
                '''
            }
        }

    }
}
