pipeline {
    agent none

    options {
      buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '10', daysToKeepStr: '', numToKeepStr: '10')
      timestamps()
    }

    triggers { pollSCM('H 9 * * *') }

    environment {
           BranchName = "${params.BRANCH_TAG}"
           RegistryURL = '10.2.6.251:5555'
           ImageNameProd = 'doors/admin-prod'
           ImageNameDev = 'doors/admin-test'
           ContainerNameProd = 'admin-prod'
           ContainerNameDev = 'admin-dev'
           AgentProd = 'doorsserver'
           AgentDev = 'doorsserver2'
    }

    parameters {
        gitParameter name: 'BRANCH_TAG',
                     branchFilter: 'origin/(.*)',
                     tagFilter: '*',
                     description: 'Please select Branch',
                     type: 'PT_BRANCH_TAG',
                     defaultValue: 'develop',
                     quickFilterEnabled: true,
                     listSize: '5'
        // string(name: 'PRIMARY_PORT', defaultValue: "5555", description: 'Please specify port')
    }

    stages {
    // === DEVELOP DEPLOYING ===
        stage('Clone specified repo in Dev') {
            agent { label "${AgentDev}" }
            when {
                beforeAgent true
                not { environment name: 'BranchName', value: 'master' }
            }
            steps {
                // checkout scm
                checkout([$class: 'GitSCM',
                          branches: [[name: "${params.BRANCH_TAG}"]],
                          doGenerateSubmoduleConfigurations: false,
                          extensions: [
                                        [$class: 'CleanBeforeCheckout'],
                                        [$class: 'SubmoduleOption',
                                          disableSubmodules: false,
                                          parentCredentials: true,
                                          recursiveSubmodules: true,
                                          reference: '',
                                          trackingSubmodules: true]
                                      ],
                          gitTool: 'Default',
                          submoduleCfg: [],
                          userRemoteConfigs: [[url: 'git@bitbucket.org:relative_localization/admin-frontend.git', credentialsId: '57079151-792e-4976-82ca-2ee23716aa71']]
                        ])
                // git branch: "${params.BRANCH_TAG}", url: 'git@bitbucket.org:relative_localization/admin-frontend.git', credentialsId: '57079151-792e-4976-82ca-2ee23716aa71'
                // sh "git checkout '${params.BRANCH_TAG}'"
            }
        }
        stage("Build & Push Docker image in Dev") {
            agent { label "${AgentDev}" }
            when {
                beforeAgent true
                not { environment name: 'BranchName', value: 'master' }
            }
            steps {
                echo "===== Building image ====="
                sh "docker build -t ${RegistryURL}/${ImageNameDev}:$BUILD_NUMBER -f pipe-admin-frontend/dev.Dockerfile ."
                echo "===== Pushing image ====="
                //sh 'docker login -u admin -p admin ${RegistryURL}' // authentication in case of Nexus3 docker-private
                sh "docker push ${RegistryURL}/${ImageNameDev}:$BUILD_NUMBER"
            }
        }
        stage("Run Docker image in Dev") {
            agent { label "${AgentDev}" }
            when {
                beforeAgent true
                not { environment name: 'BranchName', value: 'master' }
            }
            steps {
                sh "docker ps -f name=${ContainerNameDev} -aq | xargs docker stop | xargs docker rm || true"
                sh "docker rmi -f \$(docker images ${RegistryURL}/${ImageNameDev} -q) || true"
                echo " ===== Starting image ====="
                sh """
                docker run -d -p 87:80 --restart always \
                --hostname ${ContainerNameDev} --name ${ContainerNameDev}-$BUILD_NUMBER \
                ${RegistryURL}/${ImageNameDev}:$BUILD_NUMBER
                """
                sh "docker ps -l"
                // Mailer notification
                step ([$class: 'Mailer', notifyEveryUnstableBuild: true, recipients: 'sergey.shimanskiy@vergendo.com', sendToIndividuals: false])
            }
        }

    // === PRODUCTION DEPLOYING ===
        stage('Clone specified repo in Prod') {
            agent { label "${AgentProd}" }
            when {
                beforeAgent true
                    environment name: 'BranchName', value: 'master'
            }
            steps {
                echo " ===== Cloning repo ====="
              // checkout scm
                checkout([$class: 'GitSCM',
                          branches: [[name: "${params.BRANCH_TAG}"]],
                          doGenerateSubmoduleConfigurations: false,
                          extensions: [
                                        [$class: 'CleanBeforeCheckout'],
                                        [$class: 'SubmoduleOption',
                                          disableSubmodules: false,
                                          parentCredentials: true,
                                          recursiveSubmodules: true,
                                          reference: '',
                                          trackingSubmodules: true]
                          ],
                          gitTool: 'Default',
                          submoduleCfg: [],
                          userRemoteConfigs: [[url: 'git@bitbucket.org:relative_localization/admin-frontend.git', credentialsId: '57079151-792e-4976-82ca-2ee23716aa71']]
                        ])
            }
        }
        stage("Build & Push Docker image in Prod") {
            agent { label "${AgentProd}" }
            when {
                beforeAgent true
                    environment name: 'BranchName', value: 'master'
            }
            steps {
                echo " ===== Building image ====="
                sh "docker build -t ${RegistryURL}/${ImageNameProd}:$BUILD_NUMBER -f pipe-admin-frontend/prod.Dockerfile ."
                echo " ===== Pushing image ====="
                //sh 'docker login -u admin -p admin ${RegistryURL}' // authentication in case of Nexus3 docker-private
                sh "docker push ${RegistryURL}/${ImageNameProd}:$BUILD_NUMBER"
            }
        }

        stage("Run Docker image in Prod") {
            agent { label "${AgentProd}" }
            when {
                beforeAgent true
                    environment name: 'BranchName', value: 'master'
            }
            steps {
                sh "docker ps -f name=${ContainerNameProd} -aq | xargs docker stop | xargs docker rm || true"
                sh "docker rmi -f \$(docker images ${RegistryURL}/${ImageNameProd} -q) || true"
                echo " ===== Starting image ====="
                sh """
                docker run -d -p 88:80 --restart always \
                --hostname ${ContainerNameProd} --name ${ContainerNameProd}-$BUILD_NUMBER \
                ${RegistryURL}/${ImageNameProd}:$BUILD_NUMBER
                """
                sh "docker ps -l"
            }
        }

    }
}
