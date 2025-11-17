FROM bellsoft/liberica-openjdk-alpine:23-cds
LABEL authors="dilansachi"
COPY build/libs/ex201-log-reader-1.0-SNAPSHOT.jar ./

ENTRYPOINT ["java", "-jar", "./ex201-log-reader-1.0-SNAPSHOT.jar"]
