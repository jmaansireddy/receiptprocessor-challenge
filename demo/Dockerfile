# Use a Java runtime as a base image
FROM openjdk:11-jre-slim

# Set the working directory in the container
WORKDIR /app

# Copy the packaged jar file into the container
COPY target/receipt-processor.jar /app

# Specify the command to run your Spring Boot application
CMD ["java", "-jar", "receipt-processor.jar"]
