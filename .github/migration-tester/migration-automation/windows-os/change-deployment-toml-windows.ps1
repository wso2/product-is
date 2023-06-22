# Define color variables
$GREEN = "$(Write-Host -ForegroundColor Green -NoNewline)"
$RESET = "$(Write-Host -NoNewline)"

# Get the value of the inputs
$currentVersion = $args[2]
$database = $args[4]
$os = $args[5]

# Source env file
chmod +x env.sh
source ./env.sh
Write-Host "==> ${GREEN}Env file sourced successfully${RESET}"

# Function to perform cat operation based on conditions
function perform_cat {
    param(
        [string]$cat_file,
        [string]$deployment_file
    )

    if ($database -eq "mysql" -and $os -eq "ubuntu" -and $currentVersion -eq $cat_file) {
        Get-Content "$DEPLOYMENT_AUTOMATION_MYSQL_MAC_IS_$cat_file" > $deployment_file
        Write-Host "Deployment file '$deployment_file' changed."
    }
    elseif ($database -eq "mssql" -and $os -eq "ubuntu" -and $currentVersion -eq $cat_file) {
        Get-Content "$DEPLOYMENT_AUTOMATION_MSSQL_MAC_IS_$cat_file" > $deployment_file
        Write-Host "Deployment file '$deployment_file' changed."
    }
    elseif ($database -eq "postgres" -and $os -eq "ubuntu" -and $currentVersion -eq $cat_file) {
        Get-Content "$DEPLOYMENT_AUTOMATION_POSTGRE_MAC_IS_$cat_file" > $deployment_file
        Write-Host "Deployment file '$deployment_file' changed."
    }
}

# Iterate over deployment files
Get-ChildItem -Path $DEPLOYMENT_PATH_MAC -Recurse -Filter 'deployment.toml' | ForEach-Object {
    perform_cat "5.9" $_.FullName
    perform_cat "5.10" $_.FullName
    perform_cat "5.11" $_.FullName
    perform_cat "6.0" $_.FullName
    perform_cat "6.1" $_.FullName
    perform_cat "6.2" $_.FullName
}

