FROM maven as build
WORKDIR /app
COPY . .
RUN mvn install



FROM openjdk:11
WORKDIR /app
COPY target/smartcontactmanager-0.0.1-SNAPSHOT.jar /app/
# Define environment variables for MySQL database connection
ENV SPRING_DATASOURCE_URL=jdbc:mysql://dbservice:3306/smartcontact?createDatabaseIfNotExist=true
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=root

EXPOSE  8284
CMD [ "java","-jar","smartcontactmanager-0.0.1-SNAPSHOT.jar" ]


FROM mysql
ENV MYSQL_ROOT_PASSWORD=root
# Expose port 3306
EXPOSE 3306
# Mount a volume for persistent storage
VOLUME /var/lib/mysql

