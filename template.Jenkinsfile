pipeline {
  agent {
    docker 'kat/base_v0'
  }
// далее
  stages {
    stage ("do something") {
      steps {
        echo "Hi Kate"
        sh "docker ps"
      }
    }
  }
}
