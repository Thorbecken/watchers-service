# first mvn package to get a .jar for the docker file to handle

# in local cmd
# docker build -t watchers-service .

# to run
# docker run -d -p 8080:8080 --name=watchers-service watchers-service:0.0.3-SNAPSHOT

FROM maven:3.8.5-openjdk-18-slim AS build
WORKDIR /home/app
COPY . /home/app/
#RUN mvn -f /home/app/watchers-data/pom.xml -s /usr/share/maven/ref/settings-docker.xml dependency:go-offline
#RUN mvn -f /home/app/watchers-data/pom.xml -s /usr/share/maven/ref/settings-docker.xml install
#RUN mvn -f /home/app/watchers-application/pom.xml -s /usr/share/maven/ref/settings-docker.xml dependency:go-offline -DexcludeArtifactIds=watchers-data
#RUN mvn -f /home/app/watchers-application/pom.xml -s /usr/share/maven/ref/settings-docker.xml install -DskipTests -DfinalName=watchers-service
RUN mvn -f /home/app/pom.xml install -DskipTests

FROM openjdk:17-alpine
VOLUME /tmp
COPY --from=build home/app/watchers-application/target/watchers-application-0.0.3-SNAPSHOT.jar watchers.jar
ENV WATCHERS_SAVE_PATH=/tmp/
RUN sh -c 'touch /watchers.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/watchers.jar"]