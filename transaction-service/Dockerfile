# =============================================================================
# Dockerfile for GraalVM Native Binary Service: transaction-service
# Base: debian:bookworm-slim with GLIBC, zlib1g, and curl
# =============================================================================
FROM debian:bookworm-slim

RUN apt-get update && \
    apt-get install -y --no-install-recommends ca-certificates zlib1g curl && \
    rm -rf /var/lib/apt/lists/*

RUN groupadd -r appgroup && useradd -r -g appgroup appuser

WORKDIR /app

COPY --chown=appuser:appgroup target/transaction-service /app/application

USER appuser

EXPOSE 8084

ENTRYPOINT ["/app/application"]
