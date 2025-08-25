# Azure App Service Configuration Guide

## How to Use These Environment Variable Files

### Option 1: Azure Portal (Recommended for beginners)

1. **Open Azure Portal** and navigate to your App Service
2. Go to **Configuration** > **Application settings**
3. Open the `azure-env-variables.env` file and copy each line
4. For each environment variable:
   - Click **+ New application setting**
   - Enter the **Name** (part before =)
   - Enter the **Value** (part after =)
   - Click **OK**
5. Click **Save** at the top to apply all changes

### Option 2: Azure CLI (Bulk Import)

1. **Install Azure CLI** if not already installed
2. **Login to Azure**:
   ```bash
   az login
   ```
3. **Update the script** `azure-cli-setup.sh`:
   - Replace `your-resource-group` with your actual resource group name
   - Replace `your-app-name` with your actual app service name
4. **Run the script**:
   ```bash
   chmod +x azure-cli-setup.sh
   ./azure-cli-setup.sh
   ```

### Option 3: Azure CLI with JSON (Alternative)

Use the `azure-env-variables.json` file:

```bash
az webapp config appsettings set \
  --resource-group your-resource-group \
  --name your-app-name \
  --settings @azure-env-variables.json
```

## Important Security Notes

⚠️ **These files contain sensitive information!**

1. **DO NOT commit these files to Git**
2. **Delete these files after setting up Azure** or store them securely
3. **Use Azure Key Vault** for production secrets (recommended)
4. **Rotate secrets regularly** (JWT secret, database passwords, etc.)

## Verification

After setting up the environment variables:

1. **Check in Azure Portal**: Configuration > Application settings
2. **Test the application**: Navigate to your Azure app URL
3. **Check logs**: Go to Log stream or App Service logs
4. **Health check**: Visit `https://your-app.azurewebsites.net/actuator/health`

## Files Included

- `azure-env-variables.json` - JSON format for Azure CLI import
- `azure-env-variables.env` - Key-value format for manual entry
- `azure-cli-setup.sh` - Complete Azure CLI script
- `azure-config-guide.md` - This guide

## Next Steps

1. Set up environment variables using one of the methods above
2. Deploy your application to Azure App Service
3. Test the deployment
4. Set up monitoring and alerts
5. Configure custom domain (optional)

## Troubleshooting

- **Application won't start**: Check Application settings are correctly set
- **Database connection issues**: Verify DATABASE_URL is correct
- **Cache errors**: Verify REDIS_URL is correct and Redis service is running
- **Email not working**: Check MAIL\_\* settings and Gmail app password
