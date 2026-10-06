def pipelineContext = [:]
def shouldRunDeploy = false
def cron_string = BRANCH_NAME == "develop" ? "10 4 * * *" : ""
def projectName = "iiif-tools"

pipeline {
    agent { node { label 'DL-Team' } }

    triggers {
        cron(cron_string)
    }
    environment {
        registry = 'registry.apps.man.poznan.pl'
        registry_prefix = 'wlt'
        image_name = 'wlt-iiif-tools'
    }
    stages {
        stage('build') {
            steps {
                script {
                    pipelineContext.commitId = sh(returnStdout: true, script: "git describe --always").trim()
                    pipelineContext.dockerContainer = docker.build("${registry_prefix}/${image_name}:${pipelineContext.commitId}")
                }
            }
        }
        stage('push image to registry') {
            when {
                branch "develop"
            }
            steps {
                script {
                    docker.withRegistry("https://${env.registry}", 'git-clepsydra-account') {
                        pipelineContext.dockerContainer.push()
                        pipelineContext.dockerContainer.push("latest")
                    }
                    shouldRunDeploy = true
                }
            }
        }
    }
    post {
        success {
            script {
                if (shouldRunDeploy) {
                    build job: 'wlt-deploy-services', parameters: [string(name: 'project_name', value: projectName), string(name: 'env', value: 'test'), string(name: 'image_tag', value: pipelineContext.commitId)]
                    build job: 'wlt-iiif-tools-mvn-site'
                }
            }
        }
        failure {
            emailext(
                    subject: "Jenkins build FAILED Job. '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                    body: """FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':\n\nCheck console output at '${
                        env.BUILD_URL
                    }'\n${env.JOB_NAME} [${env.BUILD_NUMBER}]
                        """,
                    recipientProviders: [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']]
            )
        }
        unstable {
            emailext(
                    subject: "Jenkins build UNSTABLE. Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                    body: """UNSTABLE: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':\n\nCheck console output at '${
                        env.BUILD_URL
                    }'\n${env.JOB_NAME} [${env.BUILD_NUMBER}]
                        """,
                    recipientProviders: [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']]
            )
        }
        fixed {
            script {
                if (shouldRunDeploy) {
                    build job: 'wlt-deploy-services', parameters: [string(name: 'project_name', value: projectName), string(name: 'env', value: 'test'), string(name: 'image_tag', value: pipelineContext.commitId)]
                    build job: 'wlt-iiif-tools-mvn-site'
                }
            }
            emailext(
                    subject: "Jenkins build BACK TO NORMAL. Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                    body: """BACK TO NORMAL: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':\n\nCheck console output at '${
                        env.BUILD_URL
                    }'\n${env.JOB_NAME} [${env.BUILD_NUMBER}]
                        """,
                    recipientProviders: [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']]
            )
        }
    }
}
