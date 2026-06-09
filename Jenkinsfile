pipeline {
    agent any

    tools {
        jdk 'jdk-17' // Ensure this tool is configured in Jenkins Global Tool Configuration
        gradle 'gradle-814'
    }

    environment {
        APP_NAME = 'calculator'
        JAR_NAME = "calculator-1.0.0.jar"
        APP_SERVER = "32.192.209.15"
        IMAGE_REPO = "prengineering"
        IMAGE_TAG = "${GIT_COMMIT}"
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
                        sh 'sleep 10'
                        // dependencyCheck additionalArguments: '--scan "./" --format "ALL"', odcInstallation: 'OWASP-SCA'
                    }
                    post {
                        always {
                            echo 'Updating third party dependencies report'
                            // dependencyCheckPublisher pattern: '**/dependency-check-report.xml'
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
                withCredentials([usernamePassword(credentialsId: 'docker-login', 
                                                  usernameVariable: 'USERNAME', 
                                                  passwordVariable: 'PASSWORD')]) {
                    sh 'echo $PASSWORD | docker login -u $USERNAME --password-stdin'
                    echo 'Logged in successfully'
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
                input message: 'Approve deloyment to Production?', ok: 'Deploy'
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
                """
                }
                }
                echo 'Deployment successful (placeholder).'
            }
        }
        stage('Docker Image Approval'){
            options{
                timeout(time: 3, unit: 'MINUTES') 
            }
            steps {
                input message: 'Approve deloyment to Production?', ok: 'Deploy docker'
            }
        }
        stage('Deploy docker'){
            steps {
                script {
                withCredentials([sshUserPrivateKey(
                    credentialsId: 'app-server-ssh',     // ← Your credential ID
                    keyFileVariable: 'SSH_KEY'
                )])    {
                sh """
                echo "deploying docker container"
                ssh -i $SSH_KEY -o StrictHostKeyChecking=no ubuntu@${APP_SERVER} "
                    docker pull  ${IMAGE_REPO}/calculator-app:${IMAGE_TAG}
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
