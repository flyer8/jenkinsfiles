#!/usr/bin/env groovy
pipeline {
    agent { label 'master_builder' }
    
    environment {
        RFS_VERSION = "${params.MARKET}.rfs.bin.${params.PREFIX}.${params.PACKVERSION}"
        RFS_IFACE = "${params.MARKET}.rfs.ifaces.${params.PREFIX}.${params.PACKVERSION}"
    }

    parameters {
        gitParameter name: 'BRANCH_TAG',
                     defaultValue: 'master',
                     description: 'Please select Branch',
                     type: 'PT_BRANCH_TAG',
                     quickFilterEnabled: true,
                     listSize: '5'
        extendedChoice(name: 'STAND', description: 'Select Stand for deploying', multiSelectDelimiter: ',',
                       quoteValue: false, saveJSONParameterToFile: false, type: 'PT_MULTI_SELECT',
                       defaultValue: 'tks24', value: 'tks23,tks24,tks25', visibleItemCount: 3)
        string(name: 'RFS_USERNAME', defaultValue: "rfs333", description: 'Please specify username')
        string(name: 'REBUS_USERNAME', defaultValue: "rebus333", description: 'Please specify username')
        string(name: 'RFS_NAME_GROUP', defaultValue: "astsadm", description: 'Please specify group name')
        choice(name: 'SYSTEM_TYPE', choices: ['curr', 'fond'], description: 'Тип системы. curr для RFS')
        string(name: 'MARKET', defaultValue: "", description: 'Please specify market, ex. curr')
        string(name: 'PREFIX', defaultValue: "", description: 'Please specify prefix, ex. "auto"')                
        string(name: 'PACKVERSION', defaultValue: "", description: 'Please specify version')
    }

    stages {
        stage("Deploy to TEST stand") {
            steps {
              cleanWs()
                  sh """
                    cp /home/jenkins/repos/testing_infra/rfs.yml /home/jenkins/repos/testing_infra/rfs.deploy.yml
                    cd /home/jenkins/repos/testing_infra

                    sed -i 's/rfsuser/${params.RFS_USERNAME}/g' rfs.deploy.yml
                    sed -i 's/astsadm/${params.RFS_NAME_GROUP}/g' rfs.deploy.yml
                    sed -i "s/'curr'/'${params.SYSTEM_TYPE}'/g" rfs.deploy.yml
                    sed -i 's/curr.rfs.bin.auto.XXX/${env.RFS_VERSION}/g' rfs.deploy.yml
                    sed -i 's/curr.rfs.ifaces.auto.XXX/${env.RFS_IFACE}/g' rfs.deploy.yml
                    sed -i 's/rebususer/${params.REBUS_USERNAME}/g' rfs.deploy.yml

                    ansible-playbook -i hosts_tks2X rfs.deploy.yml
                  """
                  sh "mv /home/jenkins/repos/testing_infra/rfs.deploy.yml ."
                  archiveArtifacts artifacts: 'rfs.deploy.yml', onlyIfSuccessful: false

                  sh """
                    echo -e '
                    RFS_USERNAME=${params.RFS_USERNAME}
                    REBUS_USERNAME=${params.REBUS_USERNAME}
                    RFS_NAME_GROUP=${params.RFS_NAME_GROUP}
                    SYSTEM_TYPE=${params.SYSTEM_TYPE}
                    MARKET=${params.MARKET}
                    PREFIX=${params.PREFIX}
                    PACKVERSION=${params.PACKVERSION}
                    RFS_VERSION=${params.MARKET}.rfs.bin.${params.PREFIX}.${params.PACKVERSION}
                    RFS_IFACE = ${params.MARKET}.rfs.ifaces.${params.PREFIX}.${params.PACKVERSION}
                    ' > pipeline.params
                  """
                  archiveArtifacts artifacts: 'pipeline.params', onlyIfSuccessful: false
                  sh "cp pipeline.params ../../rfs.deploy.dit/workspace || true"
            }
        }

        // stage("Deploy to DIT stand") {
        //     input {
        //         message "Approve deployment?"
        //         ok "Yes"
        //         parameters {
        //           string(name: 'STAND', defaultValue: 'tks23', description: 'Specify stand')
        //         }
        //     }
        //     steps {
        //         echo "Deploy to ${STAND}"
        //         sh """
        //           cd /home/jenkins/repos/testing_infra
        //           ansible-playbook -i hosts_${STAND} rfs.deploy.yml
        //           rm -f rfs.deploy.yml
        //         """
        //     }
        // }
    }
}