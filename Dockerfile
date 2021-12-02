FROM openjdk:18-jdk-alpine3.15

ARG redis_addr 
ENV REDIS_ADDR=$redis_addr
EXPOSE 3113

RUN apk add maven

WORKDIR /app
COPY ./ /app/

RUN mvn compile package

#CMD java -jar target/multi-paint-fat.jar
CMD /bin/sh