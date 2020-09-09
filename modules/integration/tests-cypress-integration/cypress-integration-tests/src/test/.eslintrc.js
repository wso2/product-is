/*
 *Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */

module.exports = {
    env: {
        browser: true,
        es6: true,
        jest: true,
        node: true
    },
    extends: [
        "plugin:cypress/recommended",
        "eslint:recommended"
    ],
    parserOptions: {
        ecmaVersion: 6,
        sourceType: "module"
    },
    plugins: ["import"],
    rules: {
        "comma-dangle": ["warn", "never"],
        "cypress/no-unnecessary-waiting": ["off"],
        "eol-last": "error",
        "import/order": [
            "warn",
            {
                "alphabetize": {
                    caseInsensitive: true,
                    order: "asc"
                },
                "groups": ["builtin", "external", "index", "sibling", "parent", "internal"]
            }
        ],
        "indent": ["warn", 4],
        "max-len": ["warn", { "code": 120 }],
        "no-console": "warn",
        "no-duplicate-imports": "warn",
        "no-unused-vars": ["warn", { "argsIgnorePattern": "^_" }],
        "object-curly-spacing": ["warn", "always"],
        "quotes": ["warn", "double"],
        "semi": ["warn", "always"],
        "sort-imports": ["warn", {
            "ignoreCase": false,
            "ignoreDeclarationSort": true,
            "ignoreMemberSort": false
        }],
        "sort-keys": ["warn", "asc", { "caseSensitive": true, "minKeys": 2, "natural": false }]
    }
};

