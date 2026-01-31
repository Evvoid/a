#!/bin/bash

# PVP Arena Plugin Build Script

echo "Building PVP Arena Plugin..."

cd "$(dirname "$0")"

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed!"
    echo "Please install Maven: https://maven.apache.org/install.html"
    exit 1
fi

# Clean and build
mvn clean package

if [ $? -eq 0 ]; then
    echo ""
    echo "Build successful!"
    echo "Plugin JAR location: target/PVPArenaPlugin-1.0-SNAPSHOT.jar"
    echo ""
    echo "Installation:"
    echo "1. Copy the JAR file to your server's plugins/ folder"
    echo "2. Create or ensure you have a world named 'pvp'"
    echo "3. Restart your server"
    echo "4. Configure config.yml to your preferences"
else
    echo ""
    echo "Build failed! Check the errors above."
    exit 1
fi
