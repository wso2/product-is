@echo off
SetLocal EnableDelayedExpansion
REM ----------------------------------------------------------------------------
REM  Copyright 2020 WSO2, Inc. http://www.wso2.org
REM
REM  Licensed under the Apache License, Version 2.0 (the "License");
REM  you may not use this file except in compliance with the License.
REM  You may obtain a copy of the License at
REM
REM      http://www.apache.org/licenses/LICENSE-2.0
REM
REM  Unless required by applicable law or agreed to in writing, software
REM  distributed under the License is distributed on an "AS IS" BASIS,
REM  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM  See the License for the specific language governing permissions and
REM  limitations under the License.

REM ===============script starts here ===============================================
cd test-classes\test-utils

if exist cypress.json (
    echo "Running identity apps cypress integration tests..."
    cypress cache clear
    sleep 25
    npm run cy:headless
) else (
    echo "Failed to find the $file file."
    exit 1
)
