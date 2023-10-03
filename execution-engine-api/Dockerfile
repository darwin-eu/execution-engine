FROM maven:3.9.4-eclipse-temurin-17-alpine AS BUILDER

WORKDIR /builder

COPY pom.xml .
RUN mvn dependency:go-offline

COPY lombok.config .
COPY src/main ./src/main
RUN mvn package -DskipTests



FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
RUN mkdir "files"

COPY --from=BUILDER /builder/target/execution-engine-0.2.0.jar /app/engine.jar

ENTRYPOINT ["java", "-jar", "engine.jar"]
EXPOSE 8083
