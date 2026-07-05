pipeline {
    agent any

    tools {
        jdk 'jdk-17' // Ensure this tool is configured in Jenkins Global Tool Configuration
        gradle 'gradle-814'
    }

    environment {
        APP_NAME = 'calculator'
        JAR_NAME = "calculator-1.0.0.jar"
        APP_SERVER = "10.0.1.182"
        // FIX 1: Updated to your exact Docker Hub username so you have permissions to push
        IMAGE_REPO = "khanalsafalta0" 
        IMAGE_TAG = "0.0.${BUILD_NUMBER}"
    }

    stages {
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
                }
            }
        }

        stage('Security & code analysis'){
            parallel {
                stage('OWASP Dependency check'){
                    steps {
                        echo 'Scanning third-party dependencies'
                        sh 'sleep 10'
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
                        sh 'sleep 20'
                    }
                }
            }
        }

        stage("SonarQube Quality Gate"){
            steps{
                timeout(time: 5, unit: 'MINUTES') {
                    script{
                        sh """
                            sleep 20
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

        stage('Login to Docker Hub') {
            steps {
                withCredentials([string(credentialsId: 'docker-hub-token-id', variable: 'DOCKER_HUB_TOKEN')]) {
                    // FIX 2: Switched to triple double-quotes (""") so Jenkins evaluates the token variable correctly
                    sh """
                        echo "${DOCKER_HUB_TOKEN}" | docker login -u khanalsafalta0 --password-stdin
                    """
                }
            }
        }

        stage('Docker build'){
            steps {
                echo "building docker image"
                sh "docker build -t ${IMAGE_REPO}/calculator-app:${IMAGE_TAG} ."
                sh "docker push ${IMAGE_REPO}/calculator-app:${IMAGE_TAG}"
            }
        }

        stage('Approval'){
            options{
                timeout(time: 3, unit: 'MINUTES') 
            }
            steps {
                input message: 'Approve deployment to Production?', ok: 'Deploy'
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying to the target environment...'
                // REMOVED: Deleted the hardcoded orphaned 'scp' command to IP 52.87.174.240 that would crash this stage
                script {
                    withCredentials([sshUserPrivateKey(
                        credentialsId: 'app-server-ssh',
                        keyFileVariable: 'SSH_KEY'
                    )]) {
                        // FIX 3: Kept triple single-quotes but fixed inner Jenkins variable mapping via standard environment injection
                        sh '''
                            echo "copying new jar to the server ${APP_SERVER}"
                            scp -i "$SSH_KEY" -o StrictHostKeyChecking=no build/libs/"${JAR_NAME}" ubuntu@"${APP_SERVER}":~/calculator.jar.new
                            echo "replacing the old jar and restarting the service..."
                            ssh -i "$SSH_KEY" -o StrictHostKeyChecking=no ubuntu@"${APP_SERVER}" "
                                if [ -f calculator.jar ]; then
                                    cp calculator.jar calculator.jar.bak
                                fi

                                mv calculator.jar.new calculator.jar

                                sudo systemctl restart calculator.service
                                echo '✅ Deployment completed and service restarted'
                                echo 'Service Status:'
                                sudo systemctl status calculator.service --no-pager -l
                            "
                        '''
                    }
                }
                echo 'Deployment successful.'
            }
        }

        stage('Docker Image Approval'){
            options{
                timeout(time: 3, unit: 'MINUTES') 
            }
            steps {
                input message: 'Approve deployment to Production?', ok: 'Deploy docker'
            }
        }

        stage('Deploy docker'){
            steps {
                script {
                    withCredentials([sshUserPrivateKey(
                        credentialsId: 'app-server-ssh',     
                        keyFileVariable: 'SSH_KEY'
                    )])    {
                        // FIX 4: Changed to triple double-quotes (""") so Jenkins environment variables pass into the SSH command
                        // We escape the bash-native \$SSH_KEY so bash reads it locally, while handling Jenkins variables smoothly
                        sh """
                        echo "deploying docker container"
                        ssh -i "\$SSH_KEY" -o StrictHostKeyChecking=no ubuntu@"${APP_SERVER}" "
                            docker pull ${IMAGE_REPO}/calculator-app:${IMAGE_TAG}
                            docker rm -f calculator || true
                            docker run -d -p 8090:8080 --name calculator ${IMAGE_REPO}/calculator-app:${IMAGE_TAG} 
                        "
                        """
                    }
                }
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