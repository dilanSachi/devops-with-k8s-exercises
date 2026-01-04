FROM bellsoft/liberica-openjdk-alpine:23-cds
LABEL authors="dilansachi"
COPY build/libs/ex402-log-writer-1.0-SNAPSHOT.jar ./

ENTRYPOINT ["java", "-jar", "./ex402-log-writer-1.0-SNAPSHOT.jar"]
