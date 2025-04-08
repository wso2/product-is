/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.identity.integration.test.serviceextensions.model;

import java.util.List;
import java.util.Objects;

/**
 * This class models the Allowed Operation sent in the request payload to the API endpoint of a particular action.
 */
public class AllowedOperation {

    private Operation op;

    private List<String> paths;

    public Operation getOp() {

        return op;
    }

    public void setOp(Operation op) {

        this.op = op;
    }

    public List<String> getPaths() {

        return paths;
    }

    public void setPaths(List<String> paths) {

        this.paths = paths;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AllowedOperation that = (AllowedOperation) o;
        return op == that.op &&
                Objects.equals(paths, that.paths);
    }

    @Override
    public int hashCode() {

        return Objects.hash(op, paths);
    }
}
