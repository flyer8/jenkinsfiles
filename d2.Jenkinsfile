pipeline {
    agent {
      label 'doorsserver2'
    }
    triggers { pollSCM('H 10 * * *') }

    stages {
        stage('Checkout repo') {
            steps {
                checkout scm
            }
        }

        stage("Build Docker image") {
            steps {
                echo " ===== Building image ====="
                sh 'docker build -t 10.2.6.251:5555/doors/admin-test:$BUILD_NUMBER -f pipe-admin-frontend/Dockerfile .'
            }
        }

        stage("Push Docker image to local Registry") {
            steps {
                echo " ===== Pushing image ====="
                //sh 'docker login -u admin -p admin 10.2.6.251:5555' // authentication in case of Nexus3 docker-private
                sh 'docker push 10.2.6.251:5555/doors/admin-test:$BUILD_NUMBER'
            }
        }

        stage("Run Docker image") {
            steps {
                echo " ===== Starting image ====="
                sh 'docker ps -f name=admin-test -aq | xargs docker stop | xargs docker rm || true'
                //sh 'docker rmi -f $(docker images | grep doors/admin-test ) || true'
                sh '''
                docker run -d -p 88:80 \
                --restart always \
                --hostname admin-test \
                --name admin-test$BUILD_NUMBER \
                10.2.6.251:5555/doors/admin-test:$BUILD_NUMBER
                '''
            }
        }

    }
}
