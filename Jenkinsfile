// https://www.youtube.com/watch?v=HaGeSq-SB9E

pipeline {
    environment  {
        JAVA_TOOL_OPTIONS = "-Duser.home=/var/maven"
    }


        // https://devopscube.com/docker-containers-as-build-slaves-jenkins/
        // needs Docker plugin, Docker Pipeline and Pipeline Maven Integration
        // docker desktop uses tcp://localhost:2375 instead of the standard 2376
        // add docker in global configuration of Jenkins in http://localhost:8081/configureClouds/
        // add docker in tool configuration of Jenkins in http://localhost:8081/configureTools/
        // change git to C:\Program Files\Git\bin\git.exe in http://localhost:8081/configureTools/
//     agent {
//             docker {
//                 // same as in dockerfile
//                 image "maven:3.8.4-openjdk-17-slim"
//                 label "docker"
//                 // change /tmp/maven to the directory you want or create it with the following command: mkdir -p /tmp/maven
// //                 args "-v ~/workspace/temp/maven:/var/maven/.m2 -e MAVEN_CONFIG=/var/maven/.m2"
//                 args "-v ~/workspace/temp/maven:/root/.m2 -e MAVEN_CONFIG=/var/maven/.m2"
//                 }
//             }
        agent any

        tools {
            maven "3.8.3"
        }


    stages {

        stage('Run unit tests'){
            steps{
                bat "mvn test"
            }
        }
        stage('Build jar'){
            steps{
                bat "mvn install -DskipTests"
            }
        }
        stage('Build Docker image'){
            steps{
                bat "docker build -t watchers-service:0.0.3-SNAPSHOT ."
            }
        }
        stage('Run Docker image'){
            steps{
                bat "docker stop watchers-service"
                bat "docker rm watchers-service"
                bat "docker run -d -p 8080:8080 --name=watchers-service watchers-service:0.0.3-SNAPSHOT"
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