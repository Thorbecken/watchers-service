# first mvn package to get a .jar for the docker file to handle

# in local cmd
# docker build -t watchers-service .

# to run
# docker run -d -p 8080:8080 --name=watchers-service watchers-service:0.0.3-SNAPSHOT

FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /home/app
COPY . /home/app
RUN mvn -f /home/app/pom.xml clean package -DskipTests -DfinalName=watchers-service

FROM openjdk:17-alpine
VOLUME /tmp
COPY --from=build  home/app/target/watchers-service.jar watchers.jar
ENV WATCHERS_SAVE_PATH=/tmp
RUN sh -c 'touch /watchers.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/watchers.jar"]