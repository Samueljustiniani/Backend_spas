# Stage 1: Build
FROM maven:3.9.0-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8085
COPY Wallet_A7OUG9ZHFSCPFJ2S /app/Wallet_A7OUG9ZHFSCPFJ2S
ENTRYPOINT ["java", "-jar", "app.jar"]


# docker build -t juan321/backend-oracle:1.0 .

# docker run -d --name back-end -p 8085:8085 juan321/backend-oracle:1.0

# docker rm -f $(docker ps -aq)
# docker rmi -f $(docker images -aq)