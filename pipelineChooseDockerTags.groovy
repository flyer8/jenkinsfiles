/*
 *  Displays input dropdown select containing docker image tags
 *  Manage Jenkins->Script Approval (required)
 */

node ('slave-sshimanskiy') {
    stage('chooseDockerTags') {
        def image = "doors/debian"
        def url = "10.1.0.74:5000/v2/${image}/tags/list"
        def list = getDockerImageTags(url)
        list = sortReverse(list)
        def versions = list.join("\n")
        def userInput = input(
         id: 'userInput', message: 'Promote:', parameters: [
                [$class: 'ChoiceParameterDefinition', choices: versions, description: 'Tag', name: 'version']
         ]
        )
    }
}

@NonCPS
def sortReverse(list) {
    list.reverse()
}

def getDockerImageTags(url) {
    def myjson = getUrl(url)
    def json = jsonParse(myjson);
    def tags = json.tags
    tags
}

def jsonParse(json) {
    new groovy.json.JsonSlurper().parseText(json)
}

def getUrl(url) {
    sh(returnStdout: true, script: "curl -s ${url} 2>&1 | tee result.json")
    def data = readFile('result.json').trim()
    data
}
