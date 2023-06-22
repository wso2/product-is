#!/bin/bash

MIGRATION_PAT=$1

export GITHUB_REPO_OWNER="wso2-extensions"
export GITHUB_REPO_NAME="identity-migration-resources"
export TAG="1.0.225"
export ASSET_FILE_NAME_WITH_EXT="wso2is-migration-$TAG.zip"

github_tag_url=$(curl --silent --show-error \
  --header "Authorization: token $MIGRATION_PAT" \
  --header "Accept: application/json, application/vnd.github.v3+json" \
  --request GET "https://api.github.com/repos/$GITHUB_REPO_OWNER/$GITHUB_REPO_NAME/releases?per_page=100&draft=false" |
  jq -r '.url')
echo "Discovered the github_tag_url: ${github_tag_url}"

github_tag_id=$(curl --silent --show-error \
  --header "Authorization: token $MIGRATION_PAT" \
  --header "Accept: application/json, application/vnd.github.v3+json" \
  --request GET "$github_tag_url" | 
  jq --raw-output "first(.[] | select(.tag_name==\"v$TAG\")).id")
echo "Discovered the tag_id: ${github_tag_id}"
echo "Creating the download link..."

# Get the download URL of the desired asset
download_url=$(curl --silent --show-error \
  --header "Authorization: token $MIGRATION_PAT" \
  --header "Accept: application/json, application/vnd.github.v3+json" \
  --location --request "GET" \
  "https://api.github.com/repos/$GITHUB_REPO_OWNER/$GITHUB_REPO_NAME/releases/$github_tag_id" |
  jq --raw-output ".assets[] | select(.name==\"${ASSET_FILE_NAME_WITH_EXT}\").url")
echo "Found resource download URL: $download_url"
echo "Discovering the S3 bucket URL for the resource..."

redirect_url=$(curl --silent --show-error \
  --header "Accept: application/octet-stream" \
  --header "Authorization: token $MIGRATION_PAT" \
  --request GET --write-out "%{redirect_url}" \
  "$download_url")
echo "Download is starting now..."

# Finally, download the actual binary
curl -LJO --show-error \
  --output "./utils/migration-client" \
  --request GET \
  "$redirect_url"
echo "Binary $ASSET_FILE_NAME_WITH_EXT download is completed."

wget -qq "$ASSET_FILE_NAME_WITH_EXT"
wait $!
echo "Unzipped downloaded migration client"
ls -a

