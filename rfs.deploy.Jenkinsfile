#!/usr/bin/env groovy
pipeline {
    agent { label 'master_builder' }
    options { ansiColor('xterm') }
    
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
                  createPlaybook()
                  sh """
                    cp rfs.deploy.yml /home/jenkins/repos/testing_infra/rfs.deploy.yml
                    cd /home/jenkins/repos/testing_infra
                    ansible-playbook -i hosts_${params.STAND} rfs.deploy.yml
                  """
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
                    ' > pipeline.params
                  """
                  archiveArtifacts artifacts: 'pipeline.params', onlyIfSuccessful: false
                  sh "cp pipeline.params ../../rfs.deploy.dit/workspace"
            }
        }

        stage("Deploy to DIT stand") {
            input {
                message "Approve deployment?"
                ok "Yes"
                parameters {
                  string(name: 'STAND', defaultValue: 'tks23', description: 'Specify stand')
                }
            }
            steps {
                echo "Deploy to ${STAND}"
                sh """
                  cd /home/jenkins/repos/testing_infra
                  ansible-playbook -i hosts_${STAND} rfs.deploy.yml
                  rm -f rfs.deploy.yml
                """
            }
        }
    }
}

def createPlaybook() {
sh """
cat <<EOF > rfs.deploy.yml
---
- hosts: all                 # Only RFS host, группа RFS указана в инвентори

  vars_files:
    - creds_vars.yml

  vars:                      # Параметры развертывания
    #RFS параметры
    rfs_username: '${params.RFS_USERNAME}'   # Имя пользователя для RFS. Если оно равно имени rebus_username, то RFS хост должен быть отельно от всех хостов стенда
    rfs_name_group: '${params.RFS_NAME_GROUP}'
    rfs_workspace: '/sd/{{ rfs_username }}'

    priv_key: '{{ rfs_username }}_rsa'       # Имя приватного ssh ключа. Должен находится в директории keys. Будет скопирован на RFS хост в ~/.ssh для созданного пользователя.
    pub_key: '{{ rfs_username }}_rsa.pub'    # Имя публичного ssh ключа. Приватный и публичный ключ можно сгенерировать при помощу ssh-keygen или использовать существующие (если пользователь создан)

    system_type: '${params.SYSTEM_TYPE}'  # Тип системы. 'curr' для RFS
    rfs_version: '${env.RFS_VERSION}' # Версия бинарника системы для установки, например, на выходе: curr.rfs.bin.auto.315.rfs.10084f1
    rfs_iface: '${env.RFS_IFACE}' # Версия интерфейса системы для установки, curr.rfs.ifaces.auto.315.rfs.10084f1

    tarballsDir: '/home/jenkins/repos/testing_infra/tarballsDir' # Временная директория для хранения tarballs перед установкой
    #rfs_ftp: 'ftp://{{ ftp_creds }}@tks2'           # Откуда брать mx_installer
    rfs_nexus: 'http://{{ nexus_creds }}@10.50.1.71:8081/repository'  # Откуда брать сборку

    #CURR.REBUS.stand параметры валютного стенда, к которому добавляется RFS
    rebus_username: '${params.REBUS_USERNAME}'    # Используется в формировании названия сервисов и в конфигах
    #rebus_base_port: 18000                       # Порты можно задать вручную, тогда все строчки ниже закоментировать
    rebus_workspace: '/sd/{{ rebus_username }}'   # Для запроса портов: для определения ASTSDIR
    network_segment: 'tks.spt.tev'                # Для запроса портов: cегмент
    standsregistry_server: 'tks2'                 # Для запроса портов: реестр стендов
    #cluster_te: ['hostname']                     # Для запроса портов: для определения TEhost, должен быть определен в inventory

  roles:
    #- norfs                       # Роль для удaления RFS стенда, аккуратно, все созданное в rfs_prepare для RFS, касающееся валютного стенда удаляется тоже
    - rfs_prepare                 # Подготовить RFS юзера, его ssh-окружение, поставить необходимые пакеты
    - rfs_get_packages            # Подготовить пакеты для развертывания
    - { role: rfs_update, ansible_user: '{{ rfs_username }}' }  # Развернуть и Настроить для использования
EOF
"""
}