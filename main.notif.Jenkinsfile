pipeline {
    agent none

    options {
      buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '10', daysToKeepStr: '', numToKeepStr: '10')
      timestamps()
    }

    triggers { pollSCM('H 9 * * *') }

    environment {
      BranchName = "${params.BRANCH_TAG}"
      AgentProd = 'doorsserver'
      AgentDev = 'doorsserver2'
    }

    parameters {
        gitParameter name: 'BRANCH_TAG',
                     branchFilter: 'origin/(.*)',
                     tagFilter: '*',
                     description: """
                                  <h2>Please select Branch:</h2>
                                  <p>master - for deploying to doorsserver;</p>
                                  <p>other branches - for deploying to doorsserver2</p>
                                  """,
                     type: 'PT_BRANCH_TAG',
                     defaultValue: 'develop',
                     quickFilterEnabled: true,
                     listSize: '5'
        // string(name: 'PRIMARY_PORT', defaultValue: "5555", description: 'Please specify port')
    }

    stages {
        stage('Deploy Docker-image in Dev') {
            agent { label "${AgentDev}" }
            when {
                beforeAgent true
                not { environment name: 'BranchName', value: 'master' }
            }
            steps {
                deploy('develop')
            }
            post {
                always {
                  sh 'hostname'
                }
                success {
                  sendHangouts("SUCCESS")
                  // sendSlack("SUCCESS")
                  // sendEmail("Successful")
                }
                failure {
                  sendHangouts("FAILURE")
                }
            }
        }

        stage('Deploy Docker-image in Prod') {
            agent { label "${AgentProd}" }
            when {
                beforeAgent true
                environment name: 'BranchName', value: 'master'
            }
            steps {
                deploy('master')
            }
            post {
                always { sh 'hostname' }
                success {
                  sendHangouts("SUCCESS")
                }
                failure {
                  sendHangouts("FAILURE")
                }
            }
        }
    }

}

def deploy(BranchName) {

  def IMAGE_NAME = ''
  def CONTAINER_NAME = ''
  def DOCKERFILE_NAME= ''
  def REGISTRY_URL = '10.2.6.251:5555'

  if ("${BranchName}" != 'master') {
      IMAGE_NAME = 'doors/admin-dev-test'
      CONTAINER_NAME = 'admin-dev-test'
      DOCKERFILE_NAME = 'dev.Dockerfile'
  }

  else if ("${BranchName}" == 'master') {
      IMAGE_NAME = 'doors/admin-prod-test'
      CONTAINER_NAME = 'admin-prod-test'
      DOCKERFILE_NAME = 'prod.Dockerfile'
  }
        echo "===== Building image ====="
          sh "docker build -t ${REGISTRY_URL}/${IMAGE_NAME}:$BUILD_NUMBER -f pipe-admin-frontend/${DOCKERFILE_NAME} ."
        echo "===== Pushing image ====="
        //sh 'docker login -u admin -p admin ${REGISTRY_URL}' // authentication in private docker-registry
          sh "docker push ${REGISTRY_URL}/${IMAGE_NAME}:$BUILD_NUMBER"
          sh "docker ps -f name=${CONTAINER_NAME} -aq | xargs docker stop | xargs docker rm || true"
          sh "docker rmi -f \$(docker images ${REGISTRY_URL}/${IMAGE_NAME} -q) || true"
        echo " ===== Starting image ====="
          sh """
          docker run -d -p 88:80 --restart always \
          --hostname ${CONTAINER_NAME} --name ${CONTAINER_NAME}-$BUILD_NUMBER \
          ${REGISTRY_URL}/${IMAGE_NAME}:$BUILD_NUMBER
          """
          sh "docker ps -l"
}

def sendEmail(status) {
    mail (to: "Sergey.Shimanskiy@lanit-tercom.com",
          subject: status + " - ${currentBuild.fullDisplayName} - Branch: $GIT_BRANCH)",
          body: "Changes:\n " + "\n\n Check console output at: $BUILD_URL/console" + "\n")
}

def sendSlack(status) {
    slackSend channel: '#doors_adminka',
              message: status + " - Job: '${env.JOB_NAME}' Build: #${env.BUILD_NUMBER} Branch: $GIT_BRANCH '(${env.BUILD_URL})'",
              teamDomain: 'lanit-cvs',
              tokenCredentialId: 'lanit-cvs-slack-token'
}

def sendHangouts(status) {
    hangoutsNotify message: status + " - Job: '${env.JOB_NAME}'\nBuild: #${env.BUILD_NUMBER}\nBranch: $GIT_BRANCH (${env.BUILD_URL})",
                   token: "JgOwQYBa74mgOihDgIkbiXJp3",
                   threadByJob: false
}
