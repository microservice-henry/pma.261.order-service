FROM eclipse-temurin:25-jre
COPY target/order-service-1.0.0.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
