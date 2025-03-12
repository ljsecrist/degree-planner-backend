# Use an official Java runtime as a parent image
FROM eclipse-temurin:21-jdk

# Set the working directory inside the container
WORKDIR /app

# Copy the project files into the container
COPY . /app

# Install dependencies and build the project
RUN ./mvnw clean package

# Expose the port your backend runs on
EXPOSE 8080

# Command to run the backend
CMD ["java", "-jar", "target/backend-0.0.1-SNAPSHOT.jar"]