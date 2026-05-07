FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/juzhi-api-1.0.0.jar app.jar

EXPOSE 8080

ENV TZ=Asia/Shanghai

ENTRYPOINT ["java", "-jar", "app.jar"]
