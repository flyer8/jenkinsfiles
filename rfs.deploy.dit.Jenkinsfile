#!/usr/bin/env groovy
pipeline {

    agent {
      label 'master_builder'
    }

    stages {
        stage('Deploy to DIT-stand with Ansible') {
            steps {
              withCredentials([usernamePassword(credentialsId: 'nexus-creds', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASSWORD')]) {
                copyArtifacts fingerprintArtifacts: true, projectName: 'rfs.deploy', selector: lastCompleted()
                // Reading params from a file
                script {
                    def props = readProperties file: 'pipeline.params'
                    def market= props['MARKET']
                    def prefix= props['PREFIX']
                    def packversion= props['PACKVERSION']

                    def rfsbin = "rfs.bin"
                    def rfsiface = "rfs.ifaces"

                    env.RFS_USERNAME = props.RFS_USERNAME
                    env.REBUS_USERNAME = props.REBUS_USERNAME
                    env.RFS_NAME_GROUP = props.RFS_NAME_GROUP
                    env.SYSTEM_TYPE = props.SYSTEM_TYPE
                    env.MARKET = props.MARKET
                    env.PREFIX = props.PREFIX
                    env.PACKVERSION = props.PACKVERSION

                  properties([
                    parameters([
                      string(name: 'RFS_VERSION', defaultValue: "$market.$rfsbin.$prefix.$packversion", description: 'Please specify bin Version'),
                      string(name: 'RFS_IFACE', defaultValue: "$market.$rfsiface.$prefix.$packversion", description: 'Please specify iface Version'),
                      extendedChoice(name: 'STAND', description: 'Select Stand for deploying', multiSelectDelimiter: ',',
                                      quoteValue: false, saveJSONParameterToFile: false, type: 'PT_MULTI_SELECT',
                                      defaultValue: 'tks23', value: 'tks23,tks24,tks25', visibleItemCount: 3)
                    ])
                  ])
                }
                // Run playbook
                  createPlaybook()
                  sh """
                  cp rfs.deploy.dit.yml /home/jenkins/repos/testing_infra/rfs.deploy.dit.yml
                  cd /home/jenkins/repos/testing_infra
                  ansible-playbook -i hosts_${params.STAND} rfs.deploy.dit.yml
                  rm -f rfs.deploy.dit.yml
                  """
                  archiveArtifacts artifacts: 'rfs.deploy.dit.yml'
              }
            }
        }
    }
}

def createPlaybook() {
sh """
cat <<EOF > rfs.deploy.dit.yml
---
- hosts: all
  
  vars_files:
    - creds_vars.yml

  vars:
    # RFS parameters   
    rfs_username: '$RFS_USERNAME'
    rfs_name_group: '$RFS_NAME_GROUP'
    rfs_workspace: '/sd/{{ rfs_username }}'

    priv_key: '{{ rfs_username }}_rsa'
    pub_key: '{{ rfs_username }}_rsa.pub'

    system_type: '$SYSTEM_TYPE'
    rfs_version: '${params.RFS_VERSION}'
    rfs_iface: '${params.RFS_IFACE}'

    tarballsDir: '/home/jenkins/repos/testing_infra/tarballsDir'
    #rfs_ftp: 'ftp://{{ ftp_creds }}@tks2'
    rfs_nexus: 'http://{{ nexus_creds }}@10.50.1.71:8081/repository'

    # CURR.REBUS.stand parameters for RFS adding
    rebus_username: '$REBUS_USERNAME'
    #rebus_base_port: 18000
    rebus_workspace: '/sd/{{ rebus_username }}'
    network_segment: 'tks.spt.tev'
    standsregistry_server: 'tks2'
    #cluster_te: ['hostname']

  roles:
    - norfs
    - rfs_prepare
    - rfs_get_packages
    - { role: rfs_update, ansible_user: '{{ rfs_username }}' }
EOF
"""
}