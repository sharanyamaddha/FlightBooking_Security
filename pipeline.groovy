pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    stages {

        stage('Clone Repo') {
            steps {
                git branch: 'main', url: 'https://github.com/sharanyamaddha/FlightBooking_Security.git'
            }
        }

        stage('Build All Services') {
            parallel {

                stage('Config Server') {
                    steps { dir('config-server'){ bat 'mvn package -DskipTests' } }
                }

                stage('Service Registry') {
                    steps { dir('service-registry'){ bat 'mvn package -DskipTests' } }
                }

                stage('Booking Service') {
                    steps { dir('BookingService'){ bat 'mvn package -DskipTests' } }
                }

                stage('Auth Service') {
                    steps { dir('auth-service'){ bat 'mvn package -DskipTests' } }
                }

                stage('Flight Service') {
                    steps { dir('flight-service'){ bat 'mvn package -DskipTests' } }
                }

                stage('API Gateway') {
                    steps { dir('api-gateway'){ bat 'mvn package -DskipTests' } }
                }

                stage('Notification Service') {
                    steps { dir('notification-service'){ bat 'mvn package -DskipTests' } }
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                bat 'docker build -t config-server ./config-server'
                bat 'docker build -t service-registry ./service-registry'
                bat 'docker build -t booking-service ./BookingService'
                bat 'docker build -t auth-service ./auth-service'
                bat 'docker build -t flight-service ./flight-service'
                bat 'docker build -t api-gateway ./api-gateway'
                bat 'docker build -t notification-service ./notification-service'
            }
        }
    }

    post {
        success {
            archiveArtifacts artifacts: '**/target/*.jar'
        }
    }
}
