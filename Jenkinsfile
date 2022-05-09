// https://www.youtube.com/watch?v=HaGeSq-SB9E

pipeline {
    environment  {
        JAVA_TOOL_OPTIONS = "-Duser.home=/var/maven"
    }

    agent {
        // same as in dockerfile
        image "maven:3.8.4-openjdk-17-slim"
        label "docker"
        // change /tmp/maven to the directory you want or create it with the following command: mkdir -p /tmp/maven
        args "-v /tmp/maven:/var/maven/.m2 -e MAVEN_CONFIG=/var/maven/.m2"
    }

    stages {

//         stage('Checkout code'){
//             steps{
//                 sh "mvn -version"
//                 sh "mvn clean install"
//             }
//         }
        stage('Run unit tests'){
            steps{
                sh "mvn test"
            }
        }
        stage('Build jar'){
            steps{
                sh "mvn install -DskipTests"
            }
        }
        stage('Build Docker image'){
            steps{
                sh "docker build -t watchers-service:0.0.3-WATCH-14 ."
            }
        }
        stage('Run Docker image'){
            steps{
                sh "docker run -d -p 8080:8080 --name=watchers-service watchers-service:0.0.3-WATCH-14"
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}