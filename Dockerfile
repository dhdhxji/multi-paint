FROM openjdk:18-jdk-alpine3.15

EXPOSE 3113

RUN apk add maven

WORKDIR /app
COPY ./ /app/

RUN mvn compile package

CMD java -jar target/multi-paint-fat.jar