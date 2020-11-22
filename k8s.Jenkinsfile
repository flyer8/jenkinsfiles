node ('k8s') {
//node {
// DockerHub registry
def dockerRegistry = "index.docker.io/v1"
// 'docker-registry-login' - Jenkins Credentials ID for Docker Hub
    docker.withRegistry("https://${dockerRegistry}/", 'docker-registry-login') {

stage 'Checkout and build image'
    git branch: 'master', url: 'https://github.com/flyer8/pipe-for-kube.git'
    def nginxImg = docker.build("flyer8/nginx-lua")

stage 'Push image to Docker Hub registry'
    nginxImg.push()
 // Insecure alternative
 //sh 'docker login -u flyer8 -p F********'
 //sh 'docker push flyer8/nginx-lua'

// Deploying using package manager Helm
stage 'Deploying the chart in Kubernetes'
   sh 'helm delete --purge flyer8 || true'
   sh 'helm install --name flyer8 charts/nginx-lua/'
  }
}
