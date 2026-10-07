FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY web ./web
RUN mvn --batch-mode --no-transfer-progress package

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app
RUN mkdir -p /var/data && chown 10001:10001 /app /var/data
COPY --from=build --chown=10001:10001 /app/target/MiniBankingSystem-1.0.0.jar /app/app.jar

USER 10001:10001
ENV HOST=0.0.0.0
ENV PORT=10000
ENV GENBANK_DB_URL="jdbc:h2:file:/var/data/banking_data;MODE=MySQL;DB_CLOSE_DELAY=-1"
EXPOSE 10000

CMD ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
