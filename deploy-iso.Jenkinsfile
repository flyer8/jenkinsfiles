#!groovy
pipeline {

  agent {
    label 'ubuntu-60-11'
  }

  // triggers { pollSCM('H 20 * * *') }

  environment {
        BranchName = "${params.BRANCH_TAG}"
        Os_ISO = "${params.OS_ISO}"
        AgentName = 'ubuntu-60-11'
  }

  parameters {
    extendedChoice name: 'OS_ISO',
                   description: 'Select for Linux OS ISO',
                   multiSelectDelimiter: ',',
                   quoteValue: false,
                   saveJSONParameterToFile: false,
                   type: 'PT_MULTI_SELECT',
                   defaultValue: 'Centos-7-RVision',
                   value: 'Centos-7-RVision,Astra-Orel-2.12',
                   visibleItemCount: 3
      string(name: 'VM_NAME', defaultValue: "rstand", description: 'Please specify VM name')
      string(name: 'IP_ADDR', defaultValue: "10.99.60.105", description: 'Please specify IP address')
      string(name: 'CPUS', defaultValue: "4", description: 'Please specify CPUs')
      choice(name: 'RAM', choices: '4096\n8192\n12288\n16384', description: 'Select memory size')
      string(name: 'DISK', defaultValue: "40", description: 'Please specify HDD size')
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
      stage("Install Centos 7") {
        when {
            environment name: 'Os_ISO', value: 'Centos-7-RVision'
        }
        steps {
              sh """
              (virsh shutdown "${VM_NAME}" && virsh destroy "${VM_NAME}" && sleep 3 &&  virsh undefine "${VM_NAME}" || true)
              sudo rm -rf /var/lib/libvirt/images/"${VM_NAME}".img || true
              sudo mic cp rv-minio/rvn/ISO/centos-7-rvision-"${params.BRANCH_TAG}".iso /var/lib/libvirt/images/centos-7-rvision-"${params.BRANCH_TAG}".iso
              sudo -u root virt-install \
              --name "${VM_NAME}" \
              --os-type=linux \
              --os-variant=rhel7 \
              --vcpus ${CPUS} \
              --memory "${RAM}" \
              --disk path=/var/lib/libvirt/images/"${VM_NAME}".img,bus=ide,size=${DISK} \
              --network type=direct,source=eno1,source_mode=bridge \
              --graphics vnc,listen=0.0.0.0  \
              --cdrom /var/lib/libvirt/images/centos-7-rvision-"${params.BRANCH_TAG}".iso
              """
        }
      }

      stage("Install Astra Orel") {
        when {
            environment name: 'Os_ISO', value: 'Astra-Orel-2.12'
        }
        steps {
              sh """
              cd iso-deb-builder
              sudo mic cp rv-minio/rvn/ISO/orel-2.12.22-04.12.2019_09.06.iso .
              sed -i 's/10.99.60.103/${params.IP_ADDR}/g' preseed-orel.cfg
              sudo ./preseed_creator.sh -i orel-2.12.22-04.12.2019_09.06.iso -p preseed-orel.cfg -o /var/lib/libvirt/images/rv-orel212.iso
              sudo rm -f orel-2.12.22-04.12.2019_09.06.iso
              (virsh shutdown "${VM_NAME}" && virsh destroy "${VM_NAME}" && sleep 3 &&  virsh undefine "${VM_NAME}" || true)
              sudo rm -rf /var/lib/libvirt/images/"${VM_NAME}".img || true
              sudo -u root virt-install \
              --name "${VM_NAME}" \
              --os-type=linux \
              --vcpus ${CPUS} \
              --memory "${RAM}" \
              --disk path=/var/lib/libvirt/images/"${VM_NAME}".img,bus=ide,size=${DISK} \
              --network type=direct,source=eno1,source_mode=bridge \
              --graphics vnc,listen=0.0.0.0  \
              --cdrom /var/lib/libvirt/images/rv-orel212.iso
              """
        }
      }

  }
}
