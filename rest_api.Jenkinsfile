pipeline {
    agent {
            label 'slave-sshimanskiy'
    }

    environment {
        TEST_DIR = 'debug'
    }

    stages {
        stage('Test') {
            steps {
                    sh '''
                        docker run -t --rm \
                        -v $(pwd):/doors_tests \
                        -v $(pwd)/debug/allure_result:/doors_tests/debug/allure_result \
                        -w /doors_tests \
                        python:3.7.7 bash -c "pip3 install -r requirements.txt && \
                        cd debug/ && \
                        /usr/local/bin/py.test --durations=3 -q --ff --tb=line --alluredir=allure_result debug.py" || true
                       '''
                    sh 'sudo chown -R jenkins:jenkins debug/allure_result/'
            }

            post {
                always {
                    allure includeProperties: false, jdk: '', results: [[path: 'debug/allure_result']]
                }
            }
        }
    }
}
