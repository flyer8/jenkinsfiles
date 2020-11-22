pipeline {
    agent {
        kubernetes {
            label 'staffing-worker'
            defaultContainer 'jnlp'
            yaml """
apiVersion: v1
kind: Pod
metadata:
  labels:
    label: staffing-worker
spec:
  nodeSelector:
    node-role.kubernetes.io/jenkins-workers: jenkins-workers
  containers:
  - name: python-app
    image: matthewfeickert/docker-python3-ubuntu
    command:
    - cat
    tty: true
"""
        }
    }

    parameters {
      string(name: 'branch', defaultValue: 'master', description: 'Target branch')
    }

    triggers { cron('H 4 * * *') }

    stages {
        stage('Checkout') {
          steps {
            checkout([
                $class: 'GitSCM',
                branches: [[name: "${params.branch}"]],
                extensions: [],
                userRemoteConfigs: [[
                  credentialsId: 'e9b12804-d1fb-4b49-a5a0-af0414e72972',
                  url: 'git@bitbucket.org:quantori/org-skill-matrix-service.git'
                ]]
            ])
          }
        }

        stage("Run python app") {
            steps {
                container('python-app') {
                sh '''
                  echo "=== Hi guys! ==="
                  python3 --version
                  export SKILLS_HOST=https://org-skill-matrix-prod.azurewebsites.net/
                  pwd
                  # pip3 install -r requirements.txt
                  pip3 install requests datetime applicationinsights google-cloud-storage wheel
                  python3 data_export_task.py
                '''
                }
            }
        }
    }
}
