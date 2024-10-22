package org.wso2.identity.integration.test.rest.api.server.notification.template.v1;

public class Constants {
    
    public static final String BASE_PATH = "/api/server/v1";
    public static final String EMAIL_TEMPLATES_PATH = "/notification/email";
    public static final String SMS_TEMPLATES_PATH = "/notification/sms";
    public static final String RESET_TEMPLATE_TYPE_PATH = "/notification/reset-template-type";
    public static final String TEMPLATE_TYPES_PATH = "/template-types";
    public static final String ORG_TEMPLATES_PATH = "/org-templates";
    public static final String APP_TEMPLATES_PATH = "/app-templates";
    public static final String SYSTEM_TEMPLATES_PATH = "/system-templates";
    public static final String PATH_SEPARATOR = "/";
    public static final String CHANNEL_EMAIL = "EMAIL";
    public static final String CHANNEL_SMS = "SMS";

    public static final String ATTRIBUTE_BODY = "body";
    public static final String ATTRIBUTE_CONTENT_TYPE = "contentType";
    public static final String ATTRIBUTE_DISPLAY_NAME = "displayName";
    public static final String ATTRIBUTE_FOOTER = "footer";
    public static final String ATTRIBUTE_ID = "id";
    public static final String ATTRIBUTE_LOCALE = "locale";
    public static final String ATTRIBUTE_SELF = "self";
    public static final String ATTRIBUTE_SUBJECT = "subject";

    public static final String PLACE_HOLDER_BODY = "{{body}}";
    public static final String PLACE_HOLDER_CONTENT_TYPE = "{{contentType}}";
    public static final String PLACE_HOLDER_DISPLAY_NAME = "{{displayName}}";
    public static final String PLACE_HOLDER_FOOTER = "{{footer}}";
    public static final String PLACE_HOLDER_LOCALE = "{{locale}}";
    public static final String PLACE_HOLDER_SUBJECT = "{{subject}}";
    public static final String PLACE_HOLDER_TEMPLATE_TYPE_ID = "{{templateTypeId}}";
    public static final String PLACE_HOLDER_CHANNEL = "{{channel}}";

    public static final String COLLECTION_QUERY_BY_ID_TEMPLATE = "find{ it.id == '%s' }.";
    public static final String COLLECTION_QUERY_BY_LOCALE_TEMPLATE = "find{ it.locale == '%s' }.";

    public static final String SAMPLE_APPLICATION_UUID = "159341d6-bc1e-4445-982d-43d4df707b9a";

    public static final String LOCALE_EN_US = "en_US";
}
