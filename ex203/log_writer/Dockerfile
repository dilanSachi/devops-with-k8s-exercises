FROM bellsoft/liberica-openjdk-alpine:23-cds
LABEL authors="dilansachi"
COPY build/libs/ex203-log-writer-1.0-SNAPSHOT.jar ./

ENTRYPOINT ["java", "-jar", "./ex203-log-writer-1.0-SNAPSHOT.jar"]
