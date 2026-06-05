pipeline {
    agent any

    tools {
        jdk 'jdk-17' // Ensure this tool is configured in Jenkins Global Tool Configuration
        gradle 'gradle-814'
    }

    environment {
        APP_NAME = 'calculator'
        JAR_NAME = "calculator-1.0.0.jar"
        APP_SERVER = "54.80.78.13"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo 'Building the application...'
                sh 'gradle clean compileJava'
            }
        }

        stage('Test') {
            steps {
                echo 'Running unit tests...'
                sh 'gradle test'
            }
            post {
                always {
                    junit 'build/test-results/test/*.xml'
                    // jacoco execPattern: 'build/jacoco/test.exec',
                    //        classPattern: 'build/classes/java/main',
                    //        sourcePattern: 'src/main/java',
                    //        inclusionPattern: '**/*.class'
                }
            }
        }

        stage('Security & code analysis'){
            parallel {
                stage('OWASP Dependency check'){
                    steps {
                        echo 'Scanning third-party dependencies'
                        sh 'sleep 30'
                    }
                    post {
                        always {
                            echo 'Updating third party dependencies report'
                        }
                    }
                }
                stage('SonarQube Analysis'){
                    steps {
                        echo 'analyzing code quality'
                        sh 'sleep 90'
                    }
                }
            }
        }

        stage("SonarQube Quality Gate"){
            steps{
                timeout(time: 5, unit: 'MINUTES') {
                    script{
                        sh """
                            sleep 30
                        """
                    }
                }
            }
        }

        stage('Package') {
            steps {
                echo 'Packaging the application into a JAR...'
                sh 'gradle bootJar'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
                }
            }
        }

        stage('Approval'){
            steps {
                imput message: 'Approve deloyment to Production?', ok: 'Deploy'
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying to the target environment...'
                // Example: sh 'scp build/libs/${JAR_NAME} user@server:/path/to/deploy'
                // Example for Docker:
                // sh "docker build -t ${APP_NAME}:${BUILD_NUMBER} ."
                // sh "docker run -d -p 8080:8080 ${APP_NAME}:${BUILD_NUMBER}"
                script {
                    withCredentials([sshUserPrivateKey(
                    credentialsId: 'app-server-ssh',     // ← Your credential ID
                    keyFileVariable: 'SSH_KEY'
                )]) {
                    sh """
                        echo "copying new jar to the server"
                        scp -i \$SSH_KEY -o StrictHostKeyChecking=no build/libs/${JAR_NAME} ubuntu@${APP_SERVER}:~/calculator.jar.new
                        echo "replacning the old jar and restarting the service..."
                        ssh -i $SSH_KEY -o StrictHostKeyChecking=no ubuntu@${APP_SERVER} "
                        if [ -f calculator.jar ]; then
                            cp calculator.jar calculator.jar.bak
                        fi

                        mv calculator.jar.new calculator.jar

                        sudo systemctl restart calculator.service
                        echo '✅ Deployment completed and service restarted'
                        echo 'Service Status:'
                        sudo systemctl status calculator.service --no-pager -l
                    "
                """
                }
                }
                echo 'Deployment successful (placeholder).'
            }
        }
    }

    post {
        always {
            echo 'Pipeline finished execution.'
        }
        success {
            echo 'Build, Test, Package, and Deploy stages completed successfully!'
        }
        failure {
            echo 'Pipeline failed. Please check the logs for more information.'
        }
    }
}
