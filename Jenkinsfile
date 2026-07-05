pipeline {
    agent any

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    environment {
        SONAR_PROJECT_KEY = 'ms-customer'
        SONAR_HOST_URL = 'http://sonarqube:9000'
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
                    sh 'mvn sonar:sonar -Dsonar.projectKey=${SONAR_PROJECT_KEY}'
                }
            }
        }

        stage('Quality Gate') {
            steps {
                echo '⏳ Esperando resultado del Quality Gate...'
                timeout(time: 15, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: false
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
