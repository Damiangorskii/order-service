FROM openjdk:17-jdk

WORKDIR /app

LABEL maintainer="damian" \
      version="1.0" \
      description="Docker image for the order-service"

COPY target/order-service-0.0.1-SNAPSHOT.jar /app/order-service.jar

EXPOSE 8085

CMD ["java", "-jar", "/app/order-service.jar"]