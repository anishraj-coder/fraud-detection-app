# =============================================================================
# Dockerfile for JVM JAR Service: config-server
# =============================================================================
FROM eclipse-temurin:21-jre-alpine

# Create non-root system group and user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Single-layer copy with direct ownership assignment to eliminate layer duplication
COPY --chown=appuser:appgroup target/*.jar app.jar

USER appuser

EXPOSE 8888

# JVM memory and performance options suitable for containers
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
