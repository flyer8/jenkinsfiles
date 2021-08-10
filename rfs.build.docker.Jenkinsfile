def build() {
    sh """#!/bin/bash -ex
    echo "[\$(date)] Start building"
    rpm -qa|grep -e "boost.*1\\.57"|xargs yum remove -y
    yum install -y boost1.65.1-1
    yum install -y soci-oracle
    git clone --recursive -b \$BRANCH git@gitlab.web-tech.moex.com:astsdev/cricket.git /rfs
    cd /rfs
    if [[ ! -z \$COMMIT ]]; then
      git checkout \$COMMIT
      git submodule update
    fi
    mkdir -p /rfs/build
    cd /rfs/build
    echo \$(git rev-parse --short HEAD) >\$WORKSPACE/REVSHORT
    source /opt/rh/devtoolset-?/enable
    export LD_LIBRARY_PATH=/usr/lib/oracle/11.2/client64/lib/:\$LD_LIBRARY_PATH
    [[ "\$OPTIMISATION" == "true" ]] && OPT="-DOPT_ALL=ON" || OPT=""
    [[ "\$PROFILE" == "true" ]] && GPROF="-DWITH_GPROF=ON" || GPROF=""
    [[ ! -z \$UPPERBUILDNO ]] && export BUILD_NUMBER=\$UPPERBUILDNO
    #export LC_ALL=en_US.UTF-8
    cmake .. \$CMAKEOPTS \$OPT \$GPROF -G Ninja
    ninja-build -j12
    echo "[\$(date)] Finish building"
    cd ..
    PACKAGER=\$(curl ftp://jenkins:Jenk1ns@10.50.1.12/mx_packager/latest 2>/dev/null)
    curl -O ftp://jenkins:Jenk1ns@10.50.1.12/mx_packager/\$PACKAGER
    chmod 755 \$PACKAGER
    [[ -z \$PACKVERSION ]] && PACKVERSION=\$BUILD_NUMBER.\$BRANCH.\$(<\$WORKSPACE/REVSHORT)
    ./\$PACKAGER \$MARKET rfs \$BUILDTYPE \$PACKVERSION --repo \$WORKSPACE
    """
}

node('master_builder') {
    try {
      withCredentials([usernamePassword(credentialsId: 'nexus-creds', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASSWORD')]) {
        cleanWs()
        stage('Build RFS') {
            docker.image("ci6:8082/rhel\$RHEL-builder").inside('-u root') {
                build()
            }
        }

        stage('Publish package to Nexus') {
            sh """#!/bin/bash -ex
            if [[ "\$FTP_UPLOAD" == "true" ]]; then
              curl -u $NEXUS_USER:$NEXUS_PASSWORD -T "{\$(echo \$WORKSPACE/\$MARKET.rfs.*.tar.gz | tr ' ' ',')}" http://10.50.1.71:8081/repository/asts/\$REPOFTP\$RHEL/
            fi
            """
            archiveArtifacts '*.tar.gz'
        }

        // stage('Publish package to FTP') {
        //     sh """#!/bin/bash -ex
        //     if [[ "\$FTP_UPLOAD" == "true" ]]; then
        //       curl -T "{\$(echo \$WORKSPACE/\$MARKET.rfs.*.tar.gz | tr ' ' ',')}" ftp://jenkins:Jenk1ns@10.50.1.12/asts/\$REPOFTP\$RHEL/
        //     fi
        //     """
        //     archiveArtifacts '*.tar.gz'
        // }
      }
    } finally {
        //println "${currentBuild.currentResult}"
        emailext(body: '${DEFAULT_CONTENT}', mimeType: 'text/html',
            replyTo: '$DEFAULT_REPLYTO', subject: '${DEFAULT_SUBJECT}',
            to: '$DEFAULT_RECIPIENTS'
        )
    }
}
