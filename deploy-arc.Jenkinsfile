#!groovy
pipeline {

  agent {
    label 'ubuntu-60-11'
  }

  // triggers { pollSCM('H 20 * * *') }

  environment {
         BranchName = "${params.BRANCH_TAG}"
         AgentName = 'ubuntu-60-11'
         Execution = "${params.EXECUTE}"
         DB_locale = "${params.DB_LOCALE}"
  }

  parameters {
      string(name: 'IP_ADDR', defaultValue: "192.168.122.102", description: 'Please specify hosts IP address')
      choice(name: 'DB_LOCALE', choices: ["RU", "EN"], description: 'Database locale')
      extendedChoice  name: 'EXECUTE',
                      description: 'Select for executing',
                      multiSelectDelimiter: ',',
                      quoteValue: false,
                      saveJSONParameterToFile: false,
                      type: 'PT_MULTI_SELECT',
                      defaultValue: 'install',
                      value: 'install,update,uninstall',
                      visibleItemCount: 3
      gitParameter name: 'BRANCH_TAG',
                   branchFilter: 'origin/(.*)',
                   tagFilter: '*',
                   description: 'Please select Branch',
                   type: 'PT_BRANCH_TAG',
                   defaultValue: '4.6-docker',
                   quickFilterEnabled: true,
                   listSize: '5'
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
                        userRemoteConfigs: [[url: 'git@git.rvision.pro:rvn/r-vision.git', credentialsId: 'r-vision-deploy-key']]
                      ])
          }
      }

      stage('docker-stand install') {
          when {
              environment name: 'Execution', value: 'install'
          }
          steps {
              script {
                      if ("${params.DB_LOCALE}" == "EN") {
                        sh "sed -i 's/smp_locale=ru/smp_locale=en/g' .env.prod"
                        sh "sed -i 's/smp_dataDir=rvision_ru/smp_dataDir=defensys_en/g' .env.prod"
                      }
              }
              deploy('install')
          }
      }

      stage('docker-stand update') {
          when {
              environment name: 'Execution', value: 'update'
          }
          steps {
            script {
                    if ("${params.DB_LOCALE}" == "EN") {
                      sh "sed -i 's/smp_locale=ru/smp_locale=en/g' .env.prod"
                      sh "sed -i 's/smp_dataDir=rvision_ru/smp_dataDir=defensys_en/g' .env.prod"
                    }
            }
              deploy('update')
          }
      }
      stage('docker-stand uninstall') {
          when {
              environment name: 'Execution', value: 'uninstall'
          }
          steps {
              sh """
                ssh -o "StrictHostKeyChecking=no" root@${IP_ADDR} "cd /opt/rvn &&  docker-compose -f docker-compose.base.yml -f docker-compose.pg.yml -f docker-compose.prod.yml down || true && \
                docker system prune -a --volumes --force && \
                rm -rf /rvn-deploys/"
              """
          }
      }
  }
}

def deploy(Execution) {

  def EXEC_SCRIPT = ''

  if ("${params.EXECUTE}" == 'install') {
      EXEC_SCRIPT = 'archive-setup.sh'
  }

  if ("${params.EXECUTE}" == 'update') {
      EXEC_SCRIPT = 'archive-update.sh'
  }

  echo "===== deploy archive ====="
    sh """
    echo CI_REGISTRY=registry.rvision.pro > .env
    echo CI_PROJECT_NAMESPACE=rvn >> .env
    echo PROJECT_PREFIX=rvn >> .env
    echo CI_REGISTRY_IMAGE=registry.rvision.pro/rvn/r-vision >> .env
    echo CI_COMMIT_REF_SLUG="${params.BRANCH_TAG}" >> .env
    echo CI_COMMIT_REF_NAME="${params.BRANCH_TAG}" >> .env
    echo DEPLOY_DATA_DIR=/rvn-deploys/"${params.BRANCH_TAG}" >> .env
    echo COMPOSE_PRJ_NAME=rvn-"${params.BRANCH_TAG}" >> .env
    echo PG_VOLUME_NAME_PREFIX=/rvn-deploys/"${params.BRANCH_TAG}"/ >> .env
    echo HANDLED_REF_NAME=,${params.BRANCH_TAG}, >> .env
    echo DEPLOY_HOST=".*" >> .env

    cd arc-builder
    sudo mic cp rv-minio/rvn/ISO/centos-7-rvision-"${params.BRANCH_TAG}".iso iso/centos-7-rvision-"${params.BRANCH_TAG}".iso
    mkdir mount || true
    sudo mount -o loop iso/centos-7-rvision-"${params.BRANCH_TAG}".iso mount/
    ssh -o "StrictHostKeyChecking=no" root@${IP_ADDR} "mkdir /opt/rvn || true"
    scp -r mount/rvn/ root@${IP_ADDR}:/opt/
    sudo umount mount
    sudo rm -f iso/centos-7-rvision-"${params.BRANCH_TAG}".iso
    scp archive-setup.sh archive-update.sh root@${IP_ADDR}:/opt/rvn
    #scp ../docker-compose.base.yml ../docker-compose.pg.yml ../docker-compose.prod.yml ../.env.prod ../.env root@${IP_ADDR}:/opt/rvn
    scp ../.env.prod root@${IP_ADDR}:/opt/rvn
    ssh root@${IP_ADDR} "cd /opt/rvn && bash ${EXEC_SCRIPT}"
    """
}
