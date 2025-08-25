# Azure Deployment Guide

## Environment Variables Configuration

When deploying to Azure App Service, you need to configure the following environment variables in the Azure portal:

### Database Configuration
- `DATABASE_URL`: Your PostgreSQL connection string
  - Example: `jdbc:postgresql://your-db-server.postgres.database.azure.com:5432/projo?user=username&password=password&sslmode=require`

### JWT Configuration
- `JWT_SECRET`: A secure secret key (minimum 512 bits)
  - Generate with: `openssl rand -base64 64`
- `JWT_EXPIRATION`: Token expiration time in milliseconds (default: 86400000 = 24 hours)

### Redis Configuration
- `REDIS_URL`: Your Redis connection string
  - Azure Redis: `redis://default:password@your-cache.redis.cache.windows.net:6380?ssl=true`

### Email Configuration
- `MAIL_HOST`: SMTP server (default: smtp.gmail.com)
- `MAIL_PORT`: SMTP port (default: 587)
- `MAIL_USERNAME`: Your email address
- `MAIL_PASSWORD`: Your email app password
- `EMAIL_FROM`: From email address

### Optional Configuration
- `PORT`: Server port (default: 8080)
- `LOG_LEVEL`: Logging level (default: INFO)
- `OTP_EXPIRY`: OTP expiration in minutes (default: 10)
- `REMINDER_CRON`: Cron expression for reminders (default: 0 0 9 * * ?)
- `REMINDER_ENABLED`: Enable/disable reminders (default: true)
- `SCHEDULING_POOL_SIZE`: Scheduling thread pool size (default: 5)
- `MAX_FILE_SIZE`: Maximum file upload size (default: 10MB)
- `MAX_REQUEST_SIZE`: Maximum request size (default: 10MB)

## Azure App Service Configuration Steps

### 1. Create App Service
```bash
# Login to Azure CLI
az login

# Create resource group
az group create --name projo-rg --location "East US"

# Create App Service plan
az appservice plan create --name projo-plan --resource-group projo-rg --sku B1 --is-linux

# Create web app
az webapp create --resource-group projo-rg --plan projo-plan --name projo-backend-app --runtime "JAVA|17-java17"
```

### 2. Configure Environment Variables
```bash
# Set environment variables
az webapp config appsettings set --resource-group projo-rg --name projo-backend-app --settings \
  DATABASE_URL="your-database-url" \
  JWT_SECRET="your-jwt-secret" \
  REDIS_URL="your-redis-url" \
  MAIL_USERNAME="your-email" \
  MAIL_PASSWORD="your-app-password" \
  EMAIL_FROM="your-email"
```

### 3. Configure Deployment
```bash
# Configure deployment source
az webapp deployment source config --resource-group projo-rg --name projo-backend-app --repo-url https://github.com/Arkadipta-Kundu/projo-backend --branch main --manual-integration
```

### 4. Configure Java Runtime
```bash
# Set Java version and startup file
az webapp config set --resource-group projo-rg --name projo-backend-app --java-version 17 --startup-file "java -jar /home/site/wwwroot/target/projo-backend-0.0.1-SNAPSHOT.jar"
```

## Local Development Setup

1. Copy the template file:
   ```bash
   cp src/main/resources/application.properties.template src/main/resources/application.properties
   ```

2. Update the `application.properties` file with your local configuration values.

3. Make sure `application.properties` is in your `.gitignore` to prevent committing sensitive data.

## Security Best Practices

1. **Never commit sensitive data** like passwords, API keys, or connection strings to Git
2. **Use environment variables** for all sensitive configuration
3. **Use app passwords** for Gmail instead of your actual password
4. **Generate strong JWT secrets** using `openssl rand -base64 64`
5. **Enable SSL/TLS** for all external connections (database, Redis, email)
6. **Regularly rotate secrets** and passwords
7. **Use Azure Key Vault** for additional security in production

## Monitoring and Logging

- Enable Application Insights for monitoring
- Use Azure Log Analytics for centralized logging
- Configure health checks at `/actuator/health`
- Monitor metrics at `/actuator/metrics`

## Database Migration

For production deployments, consider:
- Setting `spring.jpa.hibernate.ddl-auto=validate` instead of `update`
- Using Flyway or Liquibase for database migrations
- Creating database backups before deployments
