/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.provider.common.model;

import java.io.Serializable;

public abstract class IdentityProperty implements Serializable {

    private static final long serialVersionUID = 2423059969331364604L;

    // have removed value attribute from this list because this object is mainly used to populate the UI
    // need to define a new object to insert property values to DB
    private String name;
    private String label;
    private String description;
    private int displayOrder;
    private String inputType;
    private String dataType;
    private String defaultValue;
    private boolean isConfidential;
    private boolean isRequired;
    private boolean isAdvanced;

    protected IdentityProperty(IdentityPropertyBuilder builder) {
        this.name = builder.name;
        this.label = builder.displayName;
        this.description = builder.description;
        this.displayOrder = builder.displayOrder;
        this.inputType = builder.inputType;
        this.dataType = builder.dataType;
        this.defaultValue = builder.defaultValue;
        this.isConfidential = builder.isConfidential;
        this.isRequired = builder.isRequired;
        this.isAdvanced = builder.isAdvanced;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public String getInputType() {
        return inputType;
    }

    public String getDataType() {
        return dataType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean isConfidential() {
        return isConfidential;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public boolean isAdvanced() {
        return isAdvanced;
    }

    public abstract class IdentityPropertyBuilder {

        private String name;
        private String displayName;
        private String description;
        private int displayOrder;
        private String inputType;
        private String dataType;
        private String defaultValue;
        private boolean isConfidential;
        private boolean isRequired;
        private boolean isAdvanced;

        public IdentityPropertyBuilder(String name, String dataType) {

            this.name = name;
            this.dataType = dataType;
        }

        public IdentityPropertyBuilder setDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public IdentityPropertyBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        public IdentityPropertyBuilder setDisplayOrder(int displayOrder) {
            this.displayOrder = displayOrder;
            return this;
        }

        public IdentityPropertyBuilder setInputType(String inputType) {
            this.inputType = inputType;
            return this;
        }

        public IdentityPropertyBuilder setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public IdentityPropertyBuilder setConfidential(boolean confidential) {
            isConfidential = confidential;
            return this;
        }

        public IdentityPropertyBuilder setRequired(boolean isRequired) {
            this.isRequired = isRequired;
            return this;
        }

        public IdentityPropertyBuilder setAdvanced(boolean isAdvanced) {
            this.isAdvanced = isAdvanced;
            return this;
        }

        public abstract IdentityProperty build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IdentityProperty)) {
            return false;
        }

        IdentityProperty that = (IdentityProperty) o;

        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
