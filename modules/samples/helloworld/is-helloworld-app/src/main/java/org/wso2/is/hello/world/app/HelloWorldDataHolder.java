/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.is.hello.world.app;

import java.nio.file.Path;
import java.util.Properties;

/**
 * Data holder of the application.
 */
public class HelloWorldDataHolder {

    private static HelloWorldDataHolder instance = new HelloWorldDataHolder();
    private Properties properties = new Properties();
    private Path propertyPath;

    private HelloWorldDataHolder() {

    }

    public static HelloWorldDataHolder getInstance() {

        return instance;
    }

    public Properties getProperties() {

        return properties;
    }

    public void setProperties(Properties properties) {

        this.properties = properties;
    }

    public Path getPropertyPath() {

        return propertyPath;
    }

    public void setPropertyFilePath(Path propertyPath) {

        this.propertyPath = propertyPath;
    }
}
