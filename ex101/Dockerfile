FROM bellsoft/liberica-openjdk-alpine:23-cds
LABEL authors="dilansachi"
COPY build/libs/ex101-1.0-SNAPSHOT.jar ./

ENTRYPOINT ["java", "-jar", "./ex101-1.0-SNAPSHOT.jar"]

#docker build -t dilansachi/devopswk8s-ex101 .