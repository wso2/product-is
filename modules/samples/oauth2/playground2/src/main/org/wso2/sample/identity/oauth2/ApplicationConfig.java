package org.wso2.sample.identity.oauth2;

import org.apache.commons.lang.StringUtils;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Sample OAuth2 application config that read from file based.
 */
public class ApplicationConfig {

    private static Logger LOGGER = Logger.getLogger(ApplicationConfig.class.getName());

    public static final String CONSUMER_KEY = "ConsumerKey";
    public static final String CONSUMER_SECRET = "ConsumerSecret";
    public static final String SCOPE = "Scope";
    public static final String CALLBACK_URL = "CallbackURL";

    public static final String IS_HOST_NAME = "IdentityServerHostName";
    public static final String IS_PORT = "IdentityServerPort";
    public static final String AUTHORIZE_ENDPOINT_CONTEXT = "AuthorizeEndpointContext";
    public static final String ACCESS_TOKEN_ENDPOINT_CONTEXT = "AccessTokenEndpointContext";
    public static final String LOGOUT_ENDPOINT_CONTEXT = "LogoutEndpointContext";
    public static final String USER_INFO_ENDPOINT_CONTEXT = "UserInfoEndpointContext";
    public static final String SESSION_IFRAME_ENDPOINT_CONTEXT = "SessionIFrameEndpointContext";

    private static Properties properties = new Properties();

    public static Properties getProperties() {
        return properties;
    }

    public static void setProperties(Properties properties) {
        ApplicationConfig.properties = properties;
    }

    public static String getConsumerKey() {
        String value = "";
        if (StringUtils.isNotEmpty(getProperties().getProperty(CONSUMER_KEY))) {
            value = getProperties().getProperty(CONSUMER_KEY);
        }
        return value;
    }

    public static String getConsumerSecret() {
        String value = "";
        if (StringUtils.isNotEmpty(getProperties().getProperty(CONSUMER_SECRET))) {
            value = getProperties().getProperty(CONSUMER_SECRET);
        }
        return value;
    }

    public static String getScope() {
        String value = "";
        if (StringUtils.isNotEmpty(getProperties().getProperty(SCOPE))) {
            value = getProperties().getProperty(SCOPE);
        }
        return value;
    }

    public static String getCallbackUrl() {
        String value = "";
        if (StringUtils.isNotEmpty(getProperties().getProperty(CALLBACK_URL))) {
            value = getProperties().getProperty(CALLBACK_URL);
        }
        return value;
    }

    public static String getAuthorizeEndpointContext() {
        return buildURL(getProperties().getProperty(AUTHORIZE_ENDPOINT_CONTEXT));
    }

    public static String getAccessTokenEndpointContext() {
        return buildURL(getProperties().getProperty(ACCESS_TOKEN_ENDPOINT_CONTEXT));
    }

    public static String getLogoutEndpointContext() {
        return buildURL(getProperties().getProperty(LOGOUT_ENDPOINT_CONTEXT));
    }

    public static String getUserInforEndpointContext() {
        return buildURL(getProperties().getProperty(USER_INFO_ENDPOINT_CONTEXT));
    }

    public static String getSessionIframeEndpointContext() {
        String iframeURL = buildURL(getProperties().getProperty(SESSION_IFRAME_ENDPOINT_CONTEXT));
        if (StringUtils.isNotEmpty(iframeURL) && iframeURL.endsWith("client_id=")) {
            iframeURL += getConsumerKey();
        }
        return iframeURL;
    }

    public static String getISHostName() {
        String value = "localhost";
        if (StringUtils.isNotEmpty(getProperties().getProperty(IS_HOST_NAME))) {
            value = getProperties().getProperty(IS_HOST_NAME);
        }
        return value;
    }

    public static int getISPort() {
        int value = 0;
        try {
            int tmpValue = Integer.parseInt(getProperties().getProperty(IS_PORT));
            if (tmpValue > 0) {
                value = tmpValue;
            }
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid input for Identity Server PORT.");
        }
        return value;
    }

    private static String buildURL(String context) {
        String buildURL = "";
        if (StringUtils.isNotEmpty(context)) {
            if (!context.startsWith("https")) {
                String endpointURL = "https://" + getISHostName();
                if (getISPort() != 0) {
                    endpointURL += ":" + getISPort();
                }
                if (context.startsWith("/")) {
                    buildURL = endpointURL + context;
                } else {
                    buildURL += endpointURL + "/" + context;
                }
            } else {
                buildURL = context;
            }
        }
        return buildURL;
    }
}
