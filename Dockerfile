FROM maven:3.9.9-eclipse-temurin-25 AS builder
WORKDIR /workspace
COPY order/pom.xml order/pom.xml
COPY order/src order/src
COPY order-service/pom.xml order-service/pom.xml
COPY order-service/src order-service/src
RUN mvn -f order/pom.xml clean install -DskipTests && \
    mvn -f order-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=builder /workspace/order-service/target/order-service-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
