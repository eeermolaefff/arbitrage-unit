#
# Build stage
#
FROM maven:3.9.4-eclipse-temurin-17 AS build
COPY src /home/app/src
COPY pom.xml /home/app

RUN mvn install:install-file \
       -Dfile=home/app/src/database-alpha.jar \
       -DgroupId=com.stambul \
       -DartifactId=database \
       -Dversion=alpha \
       -Dpackaging=jar \
       -DgeneratePom=true

RUN mvn -f /home/app/pom.xml clean package


#
# Package stage
#
FROM openjdk:22-slim
COPY --from=build /home/app/target/scanner-alpha-jar-with-dependencies.jar /home/app/scanner-alpha-jar-with-dependencies.jar

WORKDIR /home/app
CMD ["java", "-cp", "scanner-alpha-jar-with-dependencies.jar", "com.stambul.scanner.Application"]