pipeline {
    agent any

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    environment {
        SONAR_PROJECT_KEY = 'ms-customer'
        SONAR_HOST_URL = 'http://sonarqube:9000'

        ACR_NAME = 'bootcamplabacr.azurecr.io'
        IMAGE_NAME = 'ms-customer'
    }

    stages {
        stage('Checkout') {
            steps {
                echo '🔄 Clonando repositorio...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo '🔨 Compilando proyecto...'
                sh 'mvn clean compile'
            }
        }

        stage('Tests') {
            steps {
                echo '✅ Ejecutando tests...'
                sh 'mvn test'
            }
        }

        stage('Code Quality - Checkstyle') {
            steps {
                echo '🎨 Validando estilo de código...'
                sh 'mvn checkstyle:check'
            }
        }

        stage('Package') {
            steps {
                echo '📦 Empaquetando JAR...'
                sh 'mvn package -DskipTests'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                echo '🔍 Enviando análisis a SonarQube...'
                withSonarQubeEnv('SonarQube') {
                    sh 'mvn clean verify jacoco:report sonar:sonar -Dsonar.projectKey=${SONAR_PROJECT_KEY}'
                }
            }
        }

        stage('Quality Gate') {
            steps {
                echo '⏳ Esperando resultado del Quality Gate...'
                timeout(time: 15, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                script {

                    env.BASE_VERSION = sh(
                        script: "mvn help:evaluate -Dexpression=project.version -q -DforceStdout",
                        returnStdout: true
                    ).trim()

                    env.GIT_SHA = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()

                    env.IMAGE_VERSION = "${env.BASE_VERSION}-${env.BUILD_NUMBER}"

                    withCredentials([
                        usernamePassword(
                            credentialsId: 'azure-acr',
                            usernameVariable: 'ACR_USER',
                            passwordVariable: 'ACR_PASSWORD'
                        )
                    ]) {

                        sh """
                            echo $ACR_PASSWORD | docker login ${ACR_NAME} \
                                --username $ACR_USER \
                                --password-stdin

                            docker build \
                              -t ${ACR_NAME}/${IMAGE_NAME}:latest \
                              -t ${ACR_NAME}/${IMAGE_NAME}:${IMAGE_VERSION} \
                              -t ${ACR_NAME}/${IMAGE_NAME}:${GIT_SHA} \
                              .

                            docker push ${ACR_NAME}/${IMAGE_NAME}:latest
                            docker push ${ACR_NAME}/${IMAGE_NAME}:${IMAGE_VERSION}
                            docker push ${ACR_NAME}/${IMAGE_NAME}:${GIT_SHA}
                        """
                    }
                }
            }
        }
    }

    post {
        always {
            echo '📊 Generando reportes...'
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
            archiveArtifacts allowEmptyArchive: true, artifacts: 'target/site/jacoco/**'
        }
        success {
            echo '✅ Pipeline ejecutado exitosamente'
        }
        failure {
            echo '❌ Pipeline falló'
        }
    }
}
