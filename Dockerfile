############################################################################
# Adopt Open JDK 11 Build (176 MB)
#
# build from project root dir with: docker build -t chat:1.0.0-SNAPSHOT .
# run with: docker run -p 8080:8080 -d chat:1.0.0-SNAPSHOT
############################################################################
FROM eclipse-temurin:17-jre-alpine
LABEL maintainer="tait1337"

# App
WORKDIR /app
COPY ./build/libs/chat-1.0.0-SNAPSHOT.jar ./app.jar
EXPOSE $PORT
ENTRYPOINT ["java", "-jar", "app.jar"]