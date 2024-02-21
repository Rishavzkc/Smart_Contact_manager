pipeline {
    agent any
    tools {
        jdk 'jdk11'
        maven 'maven4'
    }
    


    stages {
        stage('Git Checkout') {
            steps {
                git changelog: false, poll: false, url: 'https://github.com/Rishavzkc/Smart_Contact_manager.git'
            }
        }
        stage('Unit Testing') {
            steps {
                bat 'mvn test'
            }
        }
        stage('Integration Testing') {
            steps {
                bat 'mvn verify -DskipTests'
            }
        }
        stage('Build') {
            steps {
                bat 'mvn clean install'
            }
        }
        stage('OWASP Scan') {
            steps {
                script {
                    dependencyCheck additionalArguments: '--scan ./',
                                    odcInstallation: 'dependency-check'
                    dependencyCheckPublisher pattern: '**/dependency-check-report.xml'
                }
            }
        }
         stage('Sonar Analysis') {
            steps {
                script{
                    withSonarQubeEnv(credentialsId: 'sonar-cred') {
                   bat "mvn clean package sonar:sonar"
                        }
                }
            }
        }
        // stage('Quality Gate Status'){
        //     steps{
        //         script{
        //           waitForQualityGate abortPipeline: false, credentialsId: 'sonar-cred'
        //         }
        //     }
        // }
        stage('Docker Image Build'){
            steps{
                script{
                   bat 'docker image build -t $JOB_NAME:v1.$BUILD_ID .'
                   bat 'docker image tag $JOB_NAME:v1.$BUILD_ID rishavzkc/$JOB_NAME:v1.$BUILD_ID'
                   bat 'docker image tag $JOB_NAME:v1.$BUILD_ID rishavzkc/$JOB_NAME:latest'
                }
            }
        }
}
}
