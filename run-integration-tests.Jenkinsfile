pipeline {

    agent { node { label 'DL-Team' } }

    tools {
        maven 'maven-3.5.0'
        jdk 'openjdk-11-dlteam'
    }
    environment {
        registry = 'registry.apps.man.poznan.pl'
    }
    stages {
        stage('Initialize') {
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
            steps {
                script {
                    docker.withRegistry("https://${env.registry}", 'git-clepsydra-account') {
                        sh 'docker pull registry.apps.man.poznan.pl/wlt/wlt-iiif-tools:latest'
                        sh 'docker pull registry.apps.man.poznan.pl/wlt/wlt-ocr-tools:latest'
                    }
                    sh 'docker-compose up --build -d postgresql iiif-server iiif-tools ocr-tools'

                }
            }
        }
        stage('Run tests') {
            steps {
                sh '''
                        mvn -X verify
                        '''
            }
        }
    }
    post {

        always {
            shutdownContainers()
        }
        failure {
            emailext(
                    subject: "Integration tests failed on Jenkins. '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                    body: """FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':\n\nCheck console output at '${
                        env.BUILD_URL
                    }'\n${env.JOB_NAME} [${env.BUILD_NUMBER}]
                         """,
                    recipientProviders: [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']]
            )
        }
        unstable {
            emailext(
                    subject: "Integration tests failed on Jenkins are UNSTABLE. Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                    body: """UNSTABLE: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':\n\nCheck console output at '${
                        env.BUILD_URL
                    }'\n${env.JOB_NAME} [${env.BUILD_NUMBER}]
                         """,
                    recipientProviders: [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']]
            )
        }
        fixed {
            emailext(
                    subject: "Integration tests on Jenkins are BACK TO NORMAL. Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
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
        docker-compose stop
        docker-compose rm -f
       '''
}
