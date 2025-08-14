#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default mode is "stable" (regular versions)
MODE="${1:-stable}"
GITHUB_TOKEN="${GITHUB_TOKEN:-}"
POM_FILE="pom.xml"

# Components that have "next" versions
NEXT_VERSION_COMPONENTS=(
    "identity.apps.console.version"
    "identity.apps.myaccount.version" 
    "identity.apps.core.version"
    "identity.server.api.version"
    "identity.user.api.version"
    "carbon.identity.framework.version"
    "identity.org.mgt.core.version"
    "identity.governance.version"
    "identity.org.mgt.version"
    "carbon.kernel.version"
    "identity.inbound.provisioning.scim2.version"
    "identity.inbound.auth.oauth.version"
    "org.wso2.identity.webhook.event.handlers.version"
    "org.wso2.identity.event.publishers.version"
)

if [[ ! -f "$POM_FILE" ]]; then
    echo -e "${RED}Error: pom.xml not found in current directory${NC}"
    exit 1
fi

# Function to check if component supports next versions
has_next_version() {
    local component="$1"
    for next_component in "${NEXT_VERSION_COMPONENTS[@]}"; do
        if [[ "$component" == "$next_component" ]]; then
            return 0
        fi
    done
    return 1
}

# Function to get latest version within the same major version from GitHub
get_latest_version_same_major() {
    local repo="$1"
    local current_version="$2"
    local version_type="$3"  # "stable" or "next"
    
    if [[ -z "$GITHUB_TOKEN" ]]; then
        echo ""
        return
    fi
    
    # Extract major version from current version
    local current_clean
    current_clean=$(echo "$current_version" | sed 's/^v//' | sed 's/-.*$//')
    local current_major
    current_major=$(echo "$current_clean" | cut -d. -f1)
    
    if [[ ! "$current_major" =~ ^[0-9]+$ ]]; then
        echo ""
        return
    fi
    
    # Get all tags and filter by version type and major version
    local filter_cmd=""
    if [[ "$version_type" == "next" ]]; then
        filter_cmd='select(.name | contains("next"))'
    else
        filter_cmd='select(.name | contains("next") | not)'
    fi
    
    local tag=$(curl -s -H "Authorization: token $GITHUB_TOKEN" \
        "https://api.github.com/repos/$repo/tags" | \
        jq -r ".[] | $filter_cmd | .name" | \
        sed 's/^v//' | \
        awk -F'[.-]' -v major="$current_major" '$1 == major' | \
        sort -V | tail -1)
    
    if [[ -n "$tag" && "$tag" != "null" ]]; then
        echo "$tag"
    else
        echo ""
    fi
}

# Function to get latest next release tag from GitHub
get_latest_next_release() {
    local repo="$1"
    local current_version="$2"
    
    if [[ -z "$current_version" ]]; then
        # Fallback to old behavior if no current version provided
        if [[ -z "$GITHUB_TOKEN" ]]; then
            echo ""
            return
        fi
        
        local tag=$(curl -s -H "Authorization: token $GITHUB_TOKEN" \
            "https://api.github.com/repos/$repo/tags" | \
            jq -r '.[] | select(.name | contains("next")) | .name' | \
            head -1)
        
        if [[ -n "$tag" && "$tag" != "null" ]]; then
            echo "${tag#v}"
        else
            echo ""
        fi
        return
    fi
    
    # Use the new function to get latest next version within same major
    get_latest_version_same_major "$repo" "$current_version" "next"
}

# Function to get specific identity-apps package version
get_identity_apps_version() {
    local package="$1"  # e.g., "console", "myaccount", "identity-apps-core"
    if [[ -z "$GITHUB_TOKEN" ]]; then
        echo ""
        return
    fi
    
    local tag=$(curl -s -H "Authorization: token $GITHUB_TOKEN" \
        "https://api.github.com/repos/wso2/identity-apps/releases" | \
        jq -r --arg pkg "@wso2is/$package" '.[] | select(.tag_name | startswith($pkg + "@")) | select(.tag_name | contains("next")) | .tag_name' | \
        head -1)
    
    if [[ -n "$tag" && "$tag" != "null" ]]; then
        # Extract version from tag like "@wso2is/console@2.69.1-next.0"
        echo "${tag##*@}"
    else
        echo ""
    fi
}

# Function to compare versions and check if it's a major version change
is_major_version_bump() {
    local current="$1"
    local new="$2"
    
    # Remove any non-numeric prefixes and suffixes like 'v' or '-next', '-SNAPSHOT'
    local current_clean=$(echo "$current" | sed 's/^v//' | sed 's/-.*$//')
    local new_clean=$(echo "$new" | sed 's/^v//' | sed 's/-.*$//')
    
    # Extract major version (first number)
    local current_major=$(echo "$current_clean" | cut -d. -f1)
    local new_major=$(echo "$new_clean" | cut -d. -f1)
    
    # Check if both are numeric
    if [[ ! "$current_major" =~ ^[0-9]+$ ]] || [[ ! "$new_major" =~ ^[0-9]+$ ]]; then
        echo -e "${YELLOW}⚠️  Unable to parse version numbers: $current -> $new${NC}"
        return 1  # Assume it's a major change if we can't parse
    fi
    
    # Return 0 (true) if major version increased
    [[ $new_major -gt $current_major ]]
}

# Function to update pom.xml property with version checking and repo info
update_pom_property_with_repo() {
    local prop="$1"
    local version="$2"
    local repo="$3"  # GitHub repo like "wso2/carbon-identity-framework"
    local version_type="${4:-stable}"  # "stable" or "next"
    
    if [[ -n "$version" ]]; then
        # Get current version for comparison
        local current_version
        current_version=$(grep "<$prop>" "$POM_FILE" | sed "s/.*<$prop>\(.*\)<\/$prop>.*/\1/" | tr -d ' ')
        
        # Check for major version bump
        if is_major_version_bump "$current_version" "$version"; then
            echo -e "${YELLOW}⚠️  Major version bump detected for $prop: $current_version -> $version${NC}"
            echo -e "${BLUE}🔍 Searching for latest version within major version $(echo "$current_version" | cut -d. -f1)...${NC}"
            
            # Try to get latest version within same major
            local same_major_version
            same_major_version=$(get_latest_version_same_major "$repo" "$current_version" "$version_type")
            
            if [[ -n "$same_major_version" && "$same_major_version" != "$current_version" ]]; then
                echo -e "${GREEN}✅ Found safer update for $prop: $current_version -> $same_major_version${NC}"
                # Update with the safer version
                sed -i.tmp "s|<$prop>.*</$prop>|<$prop>$same_major_version</$prop>|g" "$POM_FILE"
                rm "$POM_FILE.tmp"
                return 0
            else
                echo -e "${YELLOW}⚠️  No newer version found within major version, keeping: $current_version${NC}"
                return 0
            fi
        fi
        
        # Update the property using sed (no major version bump)
        sed -i.tmp "s|<$prop>.*</$prop>|<$prop>$version</$prop>|g" "$POM_FILE"
        rm "$POM_FILE.tmp"
        
        echo -e "${GREEN}✅ Updated $prop from $current_version to $version${NC}"
        return 0
    else
        echo -e "${YELLOW}⚠️  No version found for $prop - skipping${NC}"
        return 0
    fi
}

# Function to update pom.xml property with version checking (legacy version)
update_pom_property() {
    local prop="$1"
    local version="$2"
    
    if [[ -n "$version" ]]; then
        # Get current version for comparison
        local current_version
        current_version=$(grep "<$prop>" "$POM_FILE" | sed "s/.*<$prop>\(.*\)<\/$prop>.*/\1/" | tr -d ' ')
        
        # Check for major version bump
        if is_major_version_bump "$current_version" "$version"; then
            echo -e "${RED}⚠️  Skipping major version bump for $prop: $current_version -> $version${NC}"
            echo -e "${YELLOW}    Major version updates require manual review (use update_pom_property_with_repo for smart updates)${NC}"
            return 0
        fi
        
        # Update the property using sed
        sed -i.tmp "s|<$prop>.*</$prop>|<$prop>$version</$prop>|g" "$POM_FILE"
        rm "$POM_FILE.tmp"
        
        echo -e "${GREEN}✅ Updated $prop from $current_version to $version${NC}"
        return 0
    else
        echo -e "${YELLOW}⚠️  No version found for $prop - skipping${NC}"
        return 0
    fi
}

# Function to update next versions for specific components
update_next_versions() {
    if [[ -z "$GITHUB_TOKEN" ]]; then
        echo -e "${RED}Warning: GITHUB_TOKEN not set. Cannot fetch next versions from GitHub.${NC}"
        echo -e "${YELLOW}Only Maven-discoverable dependencies will be updated.${NC}"
        return
    fi

    echo -e "${BLUE}📦 Fetching identity-apps GitHub release versions...${NC}"

    # 1. identity-apps packages from GitHub releases (special handling)
    echo -e "${BLUE}Checking wso2/identity-apps...${NC}"
    local console_version=$(get_identity_apps_version "console")
    local myaccount_version=$(get_identity_apps_version "myaccount")
    local core_version=$(get_identity_apps_version "identity-apps-core")

    update_pom_property "identity.apps.console.version" "$console_version"
    update_pom_property "identity.apps.myaccount.version" "$myaccount_version"
    update_pom_property "identity.apps.core.version" "$core_version"

    echo ""
    echo -e "${BLUE}📦 Fetching GitHub release versions...${NC}"

    # 2. identity-api-server
    echo -e "${BLUE}Checking wso2/identity-api-server...${NC}"
    local current_api_server=$(grep "<identity.server.api.version>" "$POM_FILE" | sed "s/.*<identity.server.api.version>\(.*\)<\/identity.server.api.version>.*/\1/" | tr -d ' ')
    local api_server_version=$(get_latest_next_release "wso2/identity-api-server" "$current_api_server")
    update_pom_property "identity.server.api.version" "$api_server_version"

    # 3. identity-api-user
    echo -e "${BLUE}Checking wso2/identity-api-user...${NC}"
    local current_api_user=$(grep "<identity.user.api.version>" "$POM_FILE" | sed "s/.*<identity.user.api.version>\(.*\)<\/identity.user.api.version>.*/\1/" | tr -d ' ')
    local api_user_version=$(get_latest_next_release "wso2/identity-api-user" "$current_api_user")
    update_pom_property "identity.user.api.version" "$api_user_version"

    # 4. carbon-identity-framework
    echo -e "${BLUE}Checking wso2/carbon-identity-framework...${NC}"
    local current_framework=$(grep "<carbon.identity.framework.version>" "$POM_FILE" | sed "s/.*<carbon.identity.framework.version>\(.*\)<\/carbon.identity.framework.version>.*/\1/" | tr -d ' ')
    local framework_version=$(get_latest_next_release "wso2/carbon-identity-framework" "$current_framework")
    update_pom_property "carbon.identity.framework.version" "$framework_version"

    # 5. identity-organization-management-core
    echo -e "${BLUE}Checking wso2/identity-organization-management-core...${NC}"
    local org_mgt_core_version=$(get_latest_next_release "wso2/identity-organization-management-core")
    update_pom_property "identity.org.mgt.core.version" "$org_mgt_core_version"

    # 6. identity-governance
    echo -e "${BLUE}Checking wso2-extensions/identity-governance...${NC}"
    local governance_version=$(get_latest_next_release "wso2-extensions/identity-governance")
    update_pom_property "identity.governance.version" "$governance_version"

    # 7. identity-organization-management
    echo -e "${BLUE}Checking wso2-extensions/identity-organization-management...${NC}"
    local org_mgt_version=$(get_latest_next_release "wso2-extensions/identity-organization-management")
    update_pom_property "identity.org.mgt.version" "$org_mgt_version"

    # 8. carbon-kernel
    echo -e "${BLUE}Checking wso2/carbon-kernel...${NC}"
    local kernel_version=$(get_latest_next_release "wso2/carbon-kernel")
    update_pom_property "carbon.kernel.version" "$kernel_version"

    # 9. identity-inbound-provisioning-scim2
    echo -e "${BLUE}Checking wso2-extensions/identity-inbound-provisioning-scim2...${NC}"
    local scim2_version=$(get_latest_next_release "wso2-extensions/identity-inbound-provisioning-scim2")
    update_pom_property "identity.inbound.provisioning.scim2.version" "$scim2_version"

    # 10. identity-inbound-auth-oauth
    echo -e "${BLUE}Checking wso2-extensions/identity-inbound-auth-oauth...${NC}"
    local oauth_version=$(get_latest_next_release "wso2-extensions/identity-inbound-auth-oauth")
    update_pom_property "identity.inbound.auth.oauth.version" "$oauth_version"

    # 11. identity-webhook-event-handlers
    echo -e "${BLUE}Checking wso2-extensions/identity-webhook-event-handlers...${NC}"
    local webhook_version=$(get_latest_next_release "wso2-extensions/identity-webhook-event-handlers")
    update_pom_property "org.wso2.identity.webhook.event.handlers.version" "$webhook_version"

    # 12. identity-event-publishers
    echo -e "${BLUE}Checking wso2-extensions/identity-event-publishers...${NC}"
    local publishers_version=$(get_latest_next_release "wso2-extensions/identity-event-publishers")
    update_pom_property "org.wso2.identity.event.publishers.version" "$publishers_version"
}

# Function to get latest stable release tag from GitHub
get_latest_stable_release() {
    local repo="$1"
    local current_version="$2"
    
    if [[ -z "$current_version" ]]; then
        # Fallback to old behavior if no current version provided
        if [[ -z "$GITHUB_TOKEN" ]]; then
            echo ""
            return
        fi
        
        local tag=$(curl -s -H "Authorization: token $GITHUB_TOKEN" \
            "https://api.github.com/repos/$repo/tags" | \
            jq -r '.[] | select(.name | contains("next") | not) | .name' | \
            head -1)
        
        if [[ -n "$tag" && "$tag" != "null" ]]; then
            echo "${tag#v}"
        else
            echo ""
        fi
        return
    fi
    
    # Use the new function to get latest stable version within same major
    get_latest_version_same_major "$repo" "$current_version" "stable"
}

# Function to get specific identity-apps package stable version
get_identity_apps_stable_version() {
    local package="$1"  # e.g., "console", "myaccount", "identity-apps-core"
    if [[ -z "$GITHUB_TOKEN" ]]; then
        echo ""
        return
    fi
    
    local tag=$(curl -s -H "Authorization: token $GITHUB_TOKEN" \
        "https://api.github.com/repos/wso2/identity-apps/releases" | \
        jq -r --arg pkg "@wso2is/$package" '.[] | select(.tag_name | startswith($pkg + "@")) | select(.tag_name | contains("next") | not) | .tag_name' | \
        head -1)
    
    if [[ -n "$tag" && "$tag" != "null" ]]; then
        # Extract version from tag like "@wso2is/console@2.69.1"
        echo "${tag##*@}"
    else
        echo ""
    fi
}

# Function to update stable versions for specific components
update_stable_versions() {
    if [[ -z "$GITHUB_TOKEN" ]]; then
        echo -e "${YELLOW}Warning: GITHUB_TOKEN not set. Cannot fetch latest stable versions from GitHub.${NC}"
        echo -e "${YELLOW}Will only use version reversion to ensure no next versions remain.${NC}"
        return
    fi

    echo -e "${BLUE}📦 Fetching latest stable versions from GitHub...${NC}"

    # 1. identity-apps packages from GitHub releases (special handling)
    echo -e "${BLUE}Checking wso2/identity-apps for stable versions...${NC}"
    local console_version=$(get_identity_apps_stable_version "console")
    local myaccount_version=$(get_identity_apps_stable_version "myaccount")
    local core_version=$(get_identity_apps_stable_version "identity-apps-core")

    update_pom_property "identity.apps.console.version" "$console_version"
    update_pom_property "identity.apps.myaccount.version" "$myaccount_version"
    update_pom_property "identity.apps.core.version" "$core_version"

    echo ""
    echo -e "${BLUE}📦 Fetching stable GitHub release versions...${NC}"

    # 2. identity-api-server
    echo -e "${BLUE}Checking wso2/identity-api-server for stable versions...${NC}"
    local current_api_server=$(grep "<identity.server.api.version>" "$POM_FILE" | sed "s/.*<identity.server.api.version>\(.*\)<\/identity.server.api.version>.*/\1/" | tr -d ' ')
    local api_server_version=$(get_latest_stable_release "wso2/identity-api-server" "$current_api_server")
    update_pom_property "identity.server.api.version" "$api_server_version"

    # 3. identity-api-user
    echo -e "${BLUE}Checking wso2/identity-api-user for stable versions...${NC}"
    local current_api_user=$(grep "<identity.user.api.version>" "$POM_FILE" | sed "s/.*<identity.user.api.version>\(.*\)<\/identity.user.api.version>.*/\1/" | tr -d ' ')
    local api_user_version=$(get_latest_stable_release "wso2/identity-api-user" "$current_api_user")
    update_pom_property "identity.user.api.version" "$api_user_version"

    # 4. carbon-identity-framework
    echo -e "${BLUE}Checking wso2/carbon-identity-framework for stable versions...${NC}"
    local current_framework=$(grep "<carbon.identity.framework.version>" "$POM_FILE" | sed "s/.*<carbon.identity.framework.version>\(.*\)<\/carbon.identity.framework.version>.*/\1/" | tr -d ' ')
    local framework_version=$(get_latest_stable_release "wso2/carbon-identity-framework" "$current_framework")
    update_pom_property "carbon.identity.framework.version" "$framework_version"

    # 5. identity-organization-management-core
    echo -e "${BLUE}Checking wso2/identity-organization-management-core for stable versions...${NC}"
    local org_mgt_core_version=$(get_latest_stable_release "wso2/identity-organization-management-core")
    update_pom_property "identity.org.mgt.core.version" "$org_mgt_core_version"

    # 6. identity-governance
    echo -e "${BLUE}Checking wso2-extensions/identity-governance for stable versions...${NC}"
    local governance_version=$(get_latest_stable_release "wso2-extensions/identity-governance")
    update_pom_property_with_repo "identity.governance.version" "$governance_version" "wso2-extensions/identity-governance" "stable"

    # 7. identity-organization-management
    echo -e "${BLUE}Checking wso2-extensions/identity-organization-management for stable versions...${NC}"
    local org_mgt_version=$(get_latest_stable_release "wso2-extensions/identity-organization-management")
    update_pom_property "identity.org.mgt.version" "$org_mgt_version"

    # 8. carbon-kernel
    echo -e "${BLUE}Checking wso2/carbon-kernel for stable versions...${NC}"
    local kernel_version=$(get_latest_stable_release "wso2/carbon-kernel")
    update_pom_property "carbon.kernel.version" "$kernel_version"

    # 9. identity-inbound-provisioning-scim2
    echo -e "${BLUE}Checking wso2-extensions/identity-inbound-provisioning-scim2 for stable versions...${NC}"
    local scim2_version=$(get_latest_stable_release "wso2-extensions/identity-inbound-provisioning-scim2")
    update_pom_property "identity.inbound.provisioning.scim2.version" "$scim2_version"

    # 10. identity-inbound-auth-oauth
    echo -e "${BLUE}Checking wso2-extensions/identity-inbound-auth-oauth for stable versions...${NC}"
    local oauth_version=$(get_latest_stable_release "wso2-extensions/identity-inbound-auth-oauth")
    update_pom_property "identity.inbound.auth.oauth.version" "$oauth_version"

    # 11. identity-webhook-event-handlers
    echo -e "${BLUE}Checking wso2-extensions/identity-webhook-event-handlers for stable versions...${NC}"
    local webhook_version=$(get_latest_stable_release "wso2-extensions/identity-webhook-event-handlers")
    update_pom_property "org.wso2.identity.webhook.event.handlers.version" "$webhook_version"

    # 12. identity-event-publishers
    echo -e "${BLUE}Checking wso2-extensions/identity-event-publishers for stable versions...${NC}"
    local publishers_version=$(get_latest_stable_release "wso2-extensions/identity-event-publishers")
    update_pom_property "org.wso2.identity.event.publishers.version" "$publishers_version"
}

# Function to revert next versions to their previous stable versions
revert_next_versions() {
    echo -e "${BLUE}🔄 Checking for and reverting any next versions...${NC}"
    
    # Check if any next versions exist and revert them
    local reverted_count=0
    for component in "${NEXT_VERSION_COMPONENTS[@]}"; do
        # Get current value
        local current_value
        current_value=$(grep "<$component>" "$POM_FILE" | sed "s/.*<$component>\(.*\)<\/$component>.*/\1/" | tr -d ' ')
        
        # Check if current value contains "next"
        if [[ "$current_value" == *"next"* ]]; then
            echo -e "${YELLOW}⚠️  Found next version in $component: $current_value${NC}"
            
            # Extract the base version without next suffix
            local base_version
            base_version=$(echo "$current_value" | sed 's/-next.*$//')
            
            # Try to get the latest stable version from GitHub for this component
            local repo=""
            case "$component" in
                "identity.apps.core.version")
                    repo="wso2/identity-apps"
                    ;;
                "console.version")
                    repo="wso2/identity-apps"
                    ;;
                "myaccount.version")
                    repo="wso2/identity-apps"
                    ;;
            esac
            
            if [[ -n "$repo" ]]; then
                local latest_stable
                latest_stable=$(get_latest_version_same_major "$repo" "$base_version" "stable")
                
                if [[ -n "$latest_stable" ]]; then
                    echo -e "${BLUE}    Reverting $component from $current_value to $latest_stable${NC}"
                    update_pom_property "$component" "$latest_stable"
                    ((reverted_count++))
                else
                    echo -e "${BLUE}    Reverting $component from $current_value to $base_version${NC}"
                    update_pom_property "$component" "$base_version"
                    ((reverted_count++))
                fi
            else
                # For components without GitHub repos, just remove the next suffix
                echo -e "${BLUE}    Reverting $component from $current_value to $base_version${NC}"
                update_pom_property "$component" "$base_version"
                ((reverted_count++))
            fi
        fi
    done
    
    if [[ $reverted_count -gt 0 ]]; then
        echo -e "${GREEN}✅ Reverted $reverted_count next versions to stable versions${NC}"
    else
        echo -e "${GREEN}✅ No next versions found${NC}"
    fi
}

# Main execution
if [[ "$MODE" == "next" ]]; then
    echo -e "${BLUE}🚀 Starting NEXT version updates...${NC}"
    echo ""
    
    # Only update next versions for specific components
    update_next_versions
    
elif [[ "$MODE" == "stable" ]]; then
    echo -e "${BLUE}🚀 Starting STABLE version updates...${NC}"
    echo ""
    
    # For stable mode, first revert any next versions that may have been picked up by Maven
    revert_next_versions
    
    # Then update to latest stable versions
    update_stable_versions
    
else
    echo -e "${RED}Error: Invalid mode '$MODE'. Use 'stable' or 'next'${NC}"
    echo "Usage: $0 [stable|next]"
    exit 1
fi

echo ""
echo -e "${GREEN}🎉 Version update process completed!${NC}"

# Show changes if git is available
if command -v git &> /dev/null && git rev-parse --git-dir > /dev/null 2>&1; then
    echo ""
    echo -e "${BLUE}📋 Changes made:${NC}"
    git diff HEAD -- "$POM_FILE" | grep "^[+-]" | grep -E "(identity\.|carbon\.|org\.wso2)" || echo "No changes detected"
fi

echo ""
if [[ "$MODE" == "stable" ]]; then
    echo -e "${YELLOW}Note: Stable mode completed. Maven versions:update-properties should handle most dependencies automatically.${NC}"
else
    echo -e "${YELLOW}Note: Next mode completed. Please review the changes before committing.${NC}"
fi
