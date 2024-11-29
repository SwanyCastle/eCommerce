# Build stage
FROM amazoncorretto:17 AS build

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew
RUN ./gradlew clean build --no-daemon --refresh-dependencies -x test

# Run stage
FROM amazoncorretto:17

COPY --from=build /app/build/libs/*.jar app.jar

RUN chmod +x app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
