FROM harbor.devbridge.net/sourcery-academy/eclipse-temurin:21-jdk AS builder

ADD . /code
RUN chmod +x /code/gradlew
RUN cd code && ./gradlew build

FROM harbor.devbridge.net/sourcery-academy/eclipse-temurin:21-jre
COPY --from=builder /code/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
