#!/usr/bin/env bash
# =============================================================================
# GraalVM Native Image Build Script with .env Loading
# =============================================================================
set -e

SERVICES=("account-service" "transaction-service"  "payment-service" )

echo "========================================================"
echo " Starting GraalVM Native Compilation for 5 Services"
echo "========================================================"

for SERVICE in "${SERVICES[@]}"; do
    echo ""
    echo "--------------------------------------------------------"
    echo " Building Native Binary for: ${SERVICE}"
    echo "--------------------------------------------------------"
    
    cd "${SERVICE}"
    
    # Load .env variables if .env exists
    if [ -f .env ]; then
        echo "Exporting environment variables from ${SERVICE}/.env..."
        export $(grep -v '^#' .env | xargs)
    fi
    
    # Run GraalVM Native Image Compilation
    ./mvnw native:compile -Pnative -DskipTests
    
    cd ..
    echo " Successfully built native binary: ${SERVICE}/target/${SERVICE}"
done

echo ""
echo "========================================================"
echo " All 5 GraalVM Native Binaries Built Successfully!"
echo "========================================================"
