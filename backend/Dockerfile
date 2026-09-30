FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY mvnw .
COPY .mvn ./.mvn
COPY pom.xml .
COPY src ./src

RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests

RUN ls target/*.jar # For debugging
RUN cp target/*.jar app.jar # Copy the executable JAR

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]