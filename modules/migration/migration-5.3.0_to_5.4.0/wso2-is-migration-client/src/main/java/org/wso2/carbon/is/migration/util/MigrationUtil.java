package org.wso2.carbon.is.migration.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.is.migration.MigrationException;

import java.io.File;

public class MigrationUtil {

    private static final Log log = LogFactory.getLog(MigrationUtil.class);

    public static String getMigrationResource(String resourcePath, String fileName)
            throws MigrationException {

        if(StringUtils.isBlank(resourcePath) || StringUtils.isBlank(fileName) ){
            String errorMessage = "Migration resources path or file name can't be empty." ;
            log.error(errorMessage);
            throw new MigrationException(errorMessage);
        }

        String carbonHome = System.getProperty(Constants.CARBON_HOME);
        return carbonHome + File.separator + resourcePath +
               File.separator + fileName ;
    }
}
