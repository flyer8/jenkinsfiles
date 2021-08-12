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
echo -e '---
rfs_username: ${params.RFS_USERNAME}
rebus_username: ${params.REBUS_USERNAME}
rfs_name_group: ${params.RFS_NAME_GROUP}
system_type: ${params.SYSTEM_TYPE}
rfs_version: ${env.RFS_VERSION}
' > extra_vars.rfs.yml
                  """
              archiveArtifacts artifacts: 'extra_vars.rfs.yml', onlyIfSuccessful: false

                  sh """
                    cp extra_vars.rfs.yml /home/jenkins/repos/testing_infra/extra_vars.rfs.yml
                    cd /home/jenkins/repos/testing_infra
                    ansible-playbook -i hosts_tks2X rfs.yml --extra-vars "@extra_vars.rfs.yml"
                    rm -f extra_vars.rfs.yml
                  """

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
                  archiveArtifacts artifacts: 'pipeline.params', onlyIfSuccessful: true
                  sh "cp pipeline.params ../../rfs.deploy.dit/workspace || true"
            }
        }
    }
}
