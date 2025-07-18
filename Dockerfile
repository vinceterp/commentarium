FROM openjdk:21-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Expose the port the app runs on
EXPOSE 8080

# Copy the Maven project files
COPY pom.xml .
COPY src ./src
# Install Maven and build the application

RUN apt-get update && \
    apt-get install -y maven && \
    mvn clean package -DskipTests

# Copy the built JAR file to the container
COPY target/commentarium-0.0.1-SNAPSHOT.jar commentarium.jar

# Copy .env file to the container
# COPY .env .env

# Export environment variables from .env before running the app
CMD ["/bin/sh", "-c", "export $(cat .env | xargs) && java -jar commentarium.jar"]