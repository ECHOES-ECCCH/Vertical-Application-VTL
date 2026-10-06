def pipelineContext = [:]
def shouldRunDeploy = false
def cron_string = BRANCH_NAME == "develop" ? "50 4 * * *" : ""
def projectName = "portal-bl"
pipeline {
    agent { node { label 'DL-Team' } }
    options { disableConcurrentBuilds() }
    tools {
        maven 'maven-3.5.0'
        jdk 'openjdk-11-dlteam'
    }
    triggers {
        cron(cron_string)
    }
    environment {
        registry = 'registry.apps.man.poznan.pl'
        registry_prefix = 'wlt'
        image_name = 'wlt-portal-bl'
    }
    stages {
        stage('Initialize') {
            when {
                branch "develop"
            }
            steps {
                sh '''
                    echo "using jdk-11-dlteam"
                    echo "java : "
                    echo "PATH = ${PATH}"
                    echo "M2_HOME = ${M2_HOME}"
                    echo "JAVA_HOME = ${JAVA_HOME}"
                '''
            }
        }
        stage('Start docker-compose') {
            when {
                branch "develop"
            }
            steps {
                script {
                    docker.withRegistry("https://${env.registry}", 'git-clepsydra-account') {
                        sh 'docker pull registry.apps.man.poznan.pl/wlt/wlt-iiif-tools:latest'
                        sh 'docker pull registry.apps.man.poznan.pl/wlt/wlt-ocr-tools:latest'
                        sh 'docker pull registry.apps.man.poznan.pl/wlt/wlt-ocr-training:latest'
                    }
                    sh 'sh docker-compose-dev.sh postgresql iiif-server iiif-tools ocr-tools ocr-training-db ocr-training-service'

                }
            }
        }
        stage('Run tests (Unit + Integration)') {
            when {
                branch "develop"
            }
            steps {
                sh '''
                        mvn -X verify
                        '''
            }
        }
        stage('build docker image') {
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
        always {
            shutdownContainers()
        }
        success {
            script {
                if (shouldRunDeploy) {
                    build job: 'wlt-deploy-services', parameters: [string(name: 'project_name', value: projectName), string(name: 'env', value: 'test'), string(name: 'image_tag', value: pipelineContext.commitId)]
                    build job: 'wlt-portal-bl-mvn-site'
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
                    build job: 'wlt-portal-bl-mvn-site'
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
def shutdownContainers() {
    sh '''
        docker-compose down -v
       '''
}