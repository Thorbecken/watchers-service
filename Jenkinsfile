// https://www.youtube.com/watch?v=HaGeSq-SB9E

pipeline {
    environment  {
        JAVA_TOOL_OPTIONS = "-Duser.home=/var/maven"
    }

    agent {
        // https://devopscube.com/docker-containers-as-build-slaves-jenkins/
        // needs Docker plugin, Docker Pipeline and Pipeline Maven Integration
        // docker desktop uses tcp://localhost:2375 instead of the standard 2376
        // add docker in global configuration of Jenkins in http://localhost:8081/configureClouds/
        // add docker in tool configuration of Jenkins in http://localhost:8081/configureTools/
        // change git to C:\Program Files\Git\bin\git.exe in http://localhost:8081/configureTools/
        docker {
            // same as in dockerfile
            image "maven:3.8.4-openjdk-17-slim"
            label "docker"
            // change /tmp/maven to the directory you want or create it with the following command: mkdir -p /tmp/maven
            args "-v C:\\temp\\mvn:/var/maven/.m2 -e MAVEN_CONFIG=/var/maven/.m2"
        }
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
            // workspace cleanup plugin in Jenkins store
            cleanWs()
        }
    }
}