pipeline {
  agent {
     label 'linux-slave1'
  }

  options {
    buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '3')
    disableConcurrentBuilds()
    timestamps()
  }


   triggers {
     bitbucketPush()
   }

 environment {
        BranchName = "${BRANCH_NAME}"
        DockerRegistryURL = 'mydocker.repo.servername'
        DockerImageName = 'myapp'
        HashCommit = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
        BuildTimestamp = "${BUILD_TIMESTAMP}"
 }

  stages {

    stage('Unit-tests') {
      steps {
        echo 'Unit-testing...'
        }
      }

    stage('Integration-tests') {
      steps {
        echo 'Integration-testing...'
        }
      }


    stage('Docker build') {
      steps {
             sh "docker build -t ${DockerRegistryURL}/${DockerImageName}:${BranchName}-${BuildTimestamp}-${HashCommit} ."
      }
    }

    stage('Docker push') {
      steps {
         withCredentials([usernamePassword(credentialsId: 'docker-login-password-authentification', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')])
          {
              sh "docker login --username ${DOCKER_USER} --password ${DOCKER_PASSWORD} https://${DockerRegistryURL}"
              sh "docker push ${DockerRegistryURL}/${DockerImageName}:${BranchName}-${BuildTimestamp}-${HashCommit}"
              sh "docker rmi -f ${DockerRegistryURL}/${DockerImageName}:${BranchName}-${BuildTimestamp}-${HashCommit}"
          }
      }
    }


     stage('Deploy - Master') {
        when {  environment name: 'BranchName', value: 'master' }
          steps {
              deploy('master')
          }
        }

     stage('Deploy - Staging') {
        when {  environment name: 'BranchName', value: 'staging' }
          steps {
              deploy('staging')
          }
        }

   }


}

def deploy(BranchName) {

        def DOCKER_SWARM_MANAGER_NODE = ''


        if ("${BranchName}" == 'master') {

          DOCKER_SWARM_MANAGER_NODE = "swarm-manager-production.mydomain.com"

sh '''
cat << 'EOF' >> .env
SERVICE_NAME=login
HOST=https://login.mydomain.com
VAULT_ROLE_ID=login
EOF
'''


        }

        else if ("${BranchName}" == 'staging') {

          DOCKER_SWARM_MANAGER_NODE = "swarm-manager-staging.mydomain.com"

sh'''
cat << 'EOF' >> .env
SERVICE_NAME=login-staging
HOST=https://login-staging.mydomain.com
VAULT_ROLE_ID=login-staging
EOF
'''
        }

        else {
                 currentBuild.result = 'ABORTED'
                 error('Aborting Build: branch isn\'t correct')
        }

         withCredentials([sshUserPrivateKey(credentialsId: 'ssh-connection-to-swarm', keyFileVariable: 'SWARM_KEY', passphraseVariable: '', usernameVariable: 'SWARM_USER')]) {
         sh "ssh -p2222 -i ${SWARM_KEY} -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -NL localhost:2371:/var/run/docker.sock ${SWARM_USER}@${DOCKER_SWARM_MANAGER_NODE} &"
          withDockerRegistry(credentialsId: 'docker-login-password-authentification', url: 'https://${DockerRegistryURL}') {
          sh "docker -H localhost:2371 stack deploy -c docker-compose-cats.yaml --with-registry-auth cats"
          }
        }


}
