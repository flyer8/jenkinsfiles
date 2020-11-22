properties([
    parameters([
        gitParameter(branch: '',
                     branchFilter: 'origin/(.*)',
                     defaultValue: 'test',
                     description: 'Please select Branch',
                     name: 'BRANCH',
                     quickFilterEnabled: true,
                     listSize: '10',
                     selectedValue: 'NONE',
                     sortMode: 'NONE',
                     tagFilter: '*',
                     type: 'PT_BRANCH')
    ])
])

node ('slave-sshimanskiy') {

    stage('Clone repo') {
        git branch: "${params.BRANCH}", url: 'git@bitbucket.org:relative_localization/doors.git', credentialsId: 'jenkins-private-key'
    }

    stage('Build Docker-image') {
        def debianImg = docker.build("doors/debian:${env.BUILD_NUMBER}")
    }

    stage('Run Docker-image') {
        docker.image("doors/debian:${env.BUILD_NUMBER}").run("-p 80:80 --hostname doors-srv --name doors-${env.BUILD_NUMBER}")
    }

}
