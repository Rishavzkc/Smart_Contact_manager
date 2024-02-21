# Stage 1: Build the Spring Boot application
FROM maven:3.8.4-openjdk-11 AS builder

# Set the working directory in the container
WORKDIR /app

# Copy the Maven project file
COPY pom.xml .

# Copy the source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Create the final Docker image
FROM openjdk:11-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file from the builder stage to the final image
COPY --from=builder /app/target/smartcontactmanager-0.0.1-SNAPSHOT.jar .

# Stage 3: Setup MySQL database service
FROM mysql AS mysql_service

# Define environment variables for MySQL database
ENV MYSQL_ROOT_PASSWORD=root

# Expose port 3306
EXPOSE 3306

# Mount a volume for persistent storage
VOLUME /var/lib/mysql

# Stage 4: Final image for the Spring Boot application
FROM openjdk:11-jdk-slim

# Define environment variables for MySQL database connection
ENV SPRING_DATASOURCE_URL=jdbc:mysql://mysql_service:3306/smartcontact?createDatabaseIfNotExist=true
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=root

# Expose port 8284
EXPOSE 8284

# Copy the JAR file from the previous stage to the final image
COPY --from=builder /app/target/smartcontactmanager-0.0.1-SNAPSHOT.jar .

# Run the JAR file
CMD ["java", "-jar", "smartcontactmanager-0.0.1-SNAPSHOT.jar"]
