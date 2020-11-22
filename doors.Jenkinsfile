  pipeline {

    agent {
      label 'doorsserver2'
    }

    // triggers { pollSCM('H 20 * * *') }

    environment {
           BranchName = "${params.BRANCH_TAG}"
           RegistryURL = '10.2.6.251:5555'
           ImageName = 'doors/base'
           ContainerName = 'base'
           AgentName = 'doorsserver2'
    }

    parameters {
        gitParameter name: 'BRANCH_TAG',
                     description: 'Please select Branch',
                     type: 'PT_BRANCH_TAG',
                     defaultValue: 'AC-370-test',
                     quickFilterEnabled: true,
                     listSize: '10'
        // string(name: 'PRIMARY_PORT', defaultValue: "5000", description: 'Please specify port')
        // string(name: 'DOORS_DIR', defaultValue: "/mnt/hdd", description: 'Please specify path for DOORS_DIR')
        // string(name: 'INSTALL_PATH', defaultValue: "/home/jenkins/server_deploy", description: 'Please specify path for INSTALL_PATH')
    }

    stages {
        // stage('Clone specified repo') {
        //     steps {
        //         checkout([$class: 'GitSCM',
        //                   branches: [[name: "${params.BRANCH_TAG}"]],
        //                   doGenerateSubmoduleConfigurations: false,
        //                   extensions: [],
        //                   gitTool: 'Default',
        //                   submoduleCfg: [],
        //                   userRemoteConfigs: [[url: 'git@bitbucket.org:relative_localization/doors.git', credentialsId: 'jenkins-private-key']]
        //                 ])
        //     }
        // }
        stage("Build Docker image") {
          steps {
              echo "===== Building image ====="
                sh "docker build --build-arg user=jenkins --build-arg uid=\$(id -u jenkins) -t ${RegistryURL}/${ImageName}:$BUILD_NUMBER -f pipe-doors/base.Dockerfile ."
          }
        }

        stage("Push Docker image to local Registry") {
            steps {
              echo "===== Pushing image ====="
              //sh 'docker login -u admin -p admin ${RegistryURL}' // authentication in case of docker-private
                sh "docker push ${RegistryURL}/${ImageName}:$BUILD_NUMBER"
            }
        }

        stage("Run Docker image") {
          steps {
                sh "docker ps -f name=${ContainerName} -aq | xargs docker stop | xargs docker rm || true"
                sh "docker rmi -f \$(docker images ${RegistryURL}/${ImageName} -q) || true"
              echo " ===== Starting image ====="
              // should be add some port for applicatopn like -p 5000:5000
                sh """
                docker run -d --restart always \
                --hostname ${ContainerName} --name ${ContainerName}-$BUILD_NUMBER \
                ${RegistryURL}/${ImageName}:$BUILD_NUMBER
                """
              sh "docker ps -l"
            }
          }
    }
}
