# Multi-Attribute Login Enhancement

## Integration Location
This JavaScript enhancement has been placed in the WSO2 Identity Server dashboard JavaScript directory:
```
modules/features/org.wso2.identity.jaggery.apps.feature/src/main/resources/dashboard/js/
```

## Purpose
Enhances the WSO2 Identity Server management console by replacing manual claim URI input with a user-friendly dropdown selector for multi-attribute login configuration.

## Integration with WSO2 IS
This JavaScript file will be deployed as part of the dashboard feature and can be:

1. **Automatically loaded** in relevant JSP pages that handle multi-attribute login configuration
2. **Included via JSP** in management console pages:
   ```jsp
   <script src="dashboard/js/multi-attribute-enhancement.js"></script>
   ```
3. **Bundled** with other dashboard JavaScript files during the build process

## How it Works
- Detects multi-attribute login configuration pages
- Fetches claims via WSO2 IS REST API: `/api/server/v1/claim-dialects/local/claims`
- Transforms text input to dropdown with claim selection
- Maintains backward compatibility with existing form submission

## Architecture Fit
This approach properly integrates with WSO2 IS architecture:
- **Backend**: Java services provide REST API endpoints
- **Frontend**: JavaScript enhances user experience progressively
- **Deployment**: Part of the standard WSO2 IS feature deployment