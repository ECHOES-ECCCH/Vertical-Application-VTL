def defaultProfile = "test"
def playbookVars = [:]

pipeline {
    agent { node { label 'DL-Team' } }

    parameters {
        choice(name: 'env', choices: ['test', 'uwr-test', 'local', 'prod'], description: 'Environment where project should be deployed (test/prod)')
        string(name: 'spName', defaultValue: '', description: 'SP name')
        string(name: 'adfsUrl', defaultValue: '', description: 'ADFS url')
        string(name: 'imageTag', defaultValue: '', description: 'Docker image tag')
    }
    stages {
        stage('init') {
            steps {
                script {
                    if (!params.imageTag?.trim()) {
                        tag = "latest"
                    } else {
                        tag = params.imageTag
                    }
                    playbookVars.tag = getTagForEnv(params.env, tag)
                    playbookVars.spName = params.spName
                    playbookVars.adfsName = params.adfsUrl
                }
            }

        }
        stage('build SP image') {
            steps {
                buildImageWithParams(params.project_name, playbookVars, defaultProfile)
            }
        }
    }
}

def getTagForEnv(env, tag){
    tag + "-" + env
}

def buildImageWithParams(project, playbookVars, env) {
    def playbook = "etc/sp/generate-sp-image/generate-sp-image.yml"
    def inventory = "etc/sp/generate-sp-image/inventory.${env}.ini"
    ansiColor('xterm') {
        withCredentials([usernamePassword(credentialsId: 'git-clepsydra-account', passwordVariable: 'pass', usernameVariable: 'user')]) {
            if (playbookVars != null) {
                if (playbookVars.spName?.trim() && playbookVars.adfsName?.trim()) {
                    ansiblePlaybook(
                            playbook: playbook,
                            inventory: inventory,
                            colorized: true,
                            extraVars: [
                                    registryUser    : "${user}",
                                    registryPassword: "${pass}",
                                    adfsName        : playbookVars.adfsName,
                                    spName          : playbookVars.spName
                            ]
                    )
                }
                else if (!playbookVars.spName?.trim() && playbookVars.adfsName?.trim()) {
                    ansiblePlaybook(
                            playbook: playbook,
                            inventory: inventory,
                            colorized: true,
                            extraVars: [
                                    registryUser    : "${user}",
                                    registryPassword: "${pass}",
                                    adfsName        : playbookVars.adfsName,
                            ]
                    )
                }
                else if (playbookVars.spName?.trim() && !playbookVars.adfsName?.trim()) {
                    ansiblePlaybook(
                            playbook: playbook,
                            inventory: inventory,
                            colorized: true,
                            extraVars: [
                                    registryUser    : "${user}",
                                    registryPassword: "${pass}",
                                    spName          : playbookVars.spName
                            ]
                    )
                } else {
                    ansiblePlaybook(
                            playbook: playbook,
                            inventory: inventory,
                            colorized: true,
                            extraVars: [
                                    registryUser    : "${user}",
                                    registryPassword: "${pass}",
                                    tag             : playbookVars.tag
                            ]
                    )
                }
            }
        }
    }
}