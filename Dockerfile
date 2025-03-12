# Use an official Java runtime as a parent image
FROM eclipse-temurin:21-jdk

# Install Maven
RUN apt-get update && apt-get install -y maven

# Set the working directory inside the container
WORKDIR /app

# Copy only the backend folder's contents into the container
COPY backend /app

# Set the correct working directory where pom.xml is located
WORKDIR /app

# Build the project
RUN mvn clean package

# Expose the port your backend runs on
EXPOSE 8080

# Command to run the backend
CMD ["java", "-jar", "target/backend-0.0.1-SNAPSHOT.jar"]
