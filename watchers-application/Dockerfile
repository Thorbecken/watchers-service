# first mvn package to get a .jar for the docker file to handle

# in local cmd
# docker build -t watchers-docker .

# to runmvn mvn
# docker run -d -p 8080:8080 watchers-docker

FROM centos

RUN yum install -y java

VOLUME /tmp
ADD /target/watchers-application-0.0.2-SNAPSHOT.jar watchers.jar
ENV WATCHERS_SAVE_PATH=/tmp
RUN sh -c 'touch /watchers.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/watchers.jar"]