FROM amazoncorretto:17-alpine

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew
RUN ./gradlew clean build --refresh-dependencies -x test

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
