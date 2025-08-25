#!/bin/bash

# Azure App Service startup script for Spring Boot application

echo "Starting Projo Backend Application..."

# Set JVM options for production
export JAVA_OPTS="-Xms512m -Xmx1024m -Dspring.profiles.active=production"

# Start the application
java $JAVA_OPTS -jar /home/site/wwwroot/target/projo-backend-0.0.1-SNAPSHOT.jar
