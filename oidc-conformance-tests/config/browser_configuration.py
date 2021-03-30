# ----------------------------------------------------------------------------
#  Copyright 2021 WSO2, Inc. http://www.wso2.org
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

import constants

CONFIG = {
    "basic": {
        "browser": [
            {
                "match": constants.BASE_URL + "/oauth2/authorize*",
                "tasks": [
                    {
                        "task": "Login",
                        "match": constants.BASE_URL + "/authenticationendpoint/login*",
                        "optional": True,
                        "commands": [
                            [
                                "text",
                                "id",
                                "usernameUserInput",
                                "admin"
                            ],
                            [
                                "text",
                                "id",
                                "password",
                                "admin"
                            ],
                            [
                                "click",
                                "xpath",
                                "/html/body/main/div/div[2]/div/form/div[9]/div[2]/button"
                            ],
                            [
                                "wait",
                                "contains",
                                "test/callback",
                                10
                            ]
                        ]
                    },
                    {
                        "task": "Verify",
                        "match": "https://localhost.emobix.co.uk:8443/test/a/test/callback*"
                    }
                ]
            }
        ],
        "override": {
            "oidcc-refresh-token": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Login",
                                "match": constants.BASE_URL + "/authenticationendpoint/login*",
                                "optional": True,
                                "commands": [
                                    [
                                        "text",
                                        "id",
                                        "usernameUserInput",
                                        "admin"
                                    ],
                                    [
                                        "text",
                                        "id",
                                        "password",
                                        "admin"
                                    ],
                                    [
                                        "click",
                                        "xpath",
                                        "/html/body/main/div/div[2]/div/form/div[9]/div[2]/button"
                                    ],
                                    [
                                        "wait",
                                        "contains",
                                        "oauth2_consent",
                                        10
                                    ]
                                ]
                            },
                            {
                                "task": "Consent",
                                "match": constants.BASE_URL + "/authenticationendpoint/oauth2_consent*",
                                "optional": True,
                                "commands": [
                                    [
                                        "wait",
                                        "id",
                                        "approve",
                                        10
                                    ],
                                    [
                                        "click",
                                        "id",
                                        "approve"
                                    ],
                                    [
                                        "wait",
                                        "contains",
                                        "callback",
                                        10
                                    ]
                                ]
                            },
                            {
                                "task": "Verify",
                                "match": "https://localhost.emobix.co.uk:8443/test/a/test/callback*"
                            }
                        ]
                    }
                ]
            },
            "oidcc-ensure-registered-redirect-uri": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Verify error page",
                                "match": constants.BASE_URL + "/authenticationendpoint/oauth2_error.do*",
                                "commands": [
                                    [
                                        "wait",
                                        "xpath",
                                        "//*",
                                        10,
                                        "Identity Server",
                                        "update-image-placeholder"
                                    ]
                                ]
                            }
                        ]
                    }
                ]
            },
            "oidcc-ensure-request-object-with-redirect-uri": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Verify error",
                                "match": constants.BASE_URL + "/authenticationendpoint/oauth2_error.do*",
                                "commands": [
                                    [
                                        "wait",
                                        "xpath",
                                        "//*",
                                        10,
                                        "Identity Server",
                                        "update-image-placeholder"
                                    ]
                                ]
                            }
                        ]
                    }
                ]
            },
            "oidcc-prompt-login": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Login",
                                "match": constants.BASE_URL + "/authenticationendpoint/login*",
                                "optional": True,
                                "commands": [
                                    [
                                        "wait",
                                        "xpath",
                                        "/html/body/main/div/div[2]/h3",
                                        10,
                                        "Sign In",
                                        "update-image-placeholder-optional"
                                    ],
                                    [
                                        "text",
                                        "id",
                                        "usernameUserInput",
                                        "admin"
                                    ],
                                    [
                                        "text",
                                        "id",
                                        "password",
                                        "admin"
                                    ],
                                    [
                                        "click",
                                        "xpath",
                                        "/html/body/main/div/div[2]/div/form/div[9]/div[2]/button"
                                    ],
                                    [
                                        "wait",
                                        "contains",
                                        "test/callback",
                                        10
                                    ]
                                ]
                            },
                            {
                                "task": "Consent",
                                "match": constants.BASE_URL + "/authenticationendpoint/oauth2_consent*",
                                "optional": True,
                                "commands": [
                                    [
                                        "wait",
                                        "id",
                                        "approve",
                                        10
                                    ],
                                    [
                                        "click",
                                        "id",
                                        "rememberApproval",
                                        "optional"
                                    ],
                                    [
                                        "click",
                                        "id",
                                        "approve"
                                    ],
                                    [
                                        "wait",
                                        "contains",
                                        "callback",
                                        10
                                    ]
                                ]
                            },
                            {
                                "task": "Verify",
                                "match": "https://localhost.emobix.co.uk:8443/test/a/test/callback*"
                            }
                        ]
                    }
                ]
            },
            "oidcc-max-age-1": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Login",
                                "match": constants.BASE_URL + "/authenticationendpoint/login*",
                                "optional": True,
                                "commands": [
                                    [
                                        "wait",
                                        "xpath",
                                        "/html/body/main/div/div[2]/h3",
                                        10,
                                        "Sign In",
                                        "update-image-placeholder-optional"
                                    ],
                                    [
                                        "text",
                                        "id",
                                        "usernameUserInput",
                                        "admin"
                                    ],
                                    [
                                        "text",
                                        "id",
                                        "password",
                                        "admin"
                                    ],
                                    [
                                        "click",
                                        "xpath",
                                        "/html/body/main/div/div[2]/div/form/div[9]/div[2]/button"
                                    ],
                                    [
                                        "wait",
                                        "contains",
                                        "test/callback",
                                        10
                                    ]
                                ]
                            },
                            {
                                "task": "Consent",
                                "match": constants.BASE_URL + "/authenticationendpoint/oauth2_consent*",
                                "optional": True,
                                "commands": [
                                    [
                                        "wait",
                                        "id",
                                        "approve",
                                        10
                                    ],
                                    [
                                        "click",
                                        "id",
                                        "rememberApproval",
                                        "optional"
                                    ],
                                    [
                                        "click",
                                        "id",
                                        "approve"
                                    ],
                                    [
                                        "wait",
                                        "contains",
                                        "callback",
                                        10
                                    ]
                                ]
                            },
                            {
                                "task": "Verify",
                                "match": "https://localhost.emobix.co.uk:8443/test/a/test/callback*"
                            }
                        ]
                    }
                ]
            }
        }
    },
    "implicit": {
        "browser": [
            {
                "match": constants.BASE_URL + "/oauth2/authorize*",
                "tasks": [
                    {
                        "task": "Login",
                        "match": constants.BASE_URL + "/authenticationendpoint/login*",
                        "optional": True,
                        "commands": [
                            [
                                "text",
                                "id",
                                "usernameUserInput",
                                "admin"
                            ],
                            [
                                "text",
                                "id",
                                "password",
                                "admin"
                            ],
                            [
                                "click",
                                "xpath",
                                "/html/body/main/div/div[2]/div/form/div[9]/div[2]/button"
                            ],
                            [
                                "wait",
                                "contains",
                                "test/callback",
                                10
                            ]
                        ]
                    },
                    {
                        "task": "Verify",
                        "match": "https://localhost.emobix.co.uk:8443/test/a/test/callback*"
                    }
                ]
            }
        ],
        "override": {
            "oidcc-ensure-request-with-valid-pkce-succeeds": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Login",
                                "match": constants.BASE_URL + "/authenticationendpoint/login*",
                                "optional": True,
                                "commands": [
                                    [
                                        "text",
                                        "id",
                                        "usernameUserInput",
                                        "admin"
                                    ],
                                    [
                                        "text",
                                        "id",
                                        "password",
                                        "admin"
                                    ],
                                    [
                                        "click",
                                        "xpath",
                                        "/html/body/main/div/div[2]/div/form/div[9]/div[2]/button"
                                    ],
                                    [
                                        "wait",
                                        "contains",
                                        "test/callback",
                                        10
                                    ]
                                ]
                            },
                            {
                                "task": "Consent",
                                "match": constants.BASE_URL + "/authenticationendpoint/oauth2_consent*",
                                "optional": True,
                                "commands": [
                                    [
                                        "wait",
                                        "id",
                                        "approve",
                                        10
                                    ],
                                    [
                                        "click",
                                        "id",
                                        "approve"
                                    ],
                                    [
                                        "wait",
                                        "contains",
                                        "callback",
                                        10
                                    ]
                                ]
                            },
                            {
                                "task": "Verify",
                                "match": "https://localhost.emobix.co.uk:8443/test/a/test/callback*"
                            }
                        ]
                    }
                ]
            },
            "oidcc-refresh-token": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Login",
                                "match": constants.BASE_URL + "/authenticationendpoint/login*",
                                "optional": True,
                                "commands": [
                                    [
                                        "text",
                                        "id",
                                        "usernameUserInput",
                                        "admin"
                                    ],
                                    [
                                        "text",
                                        "id",
                                        "password",
                                        "admin"
                                    ],
                                    [
                                        "click",
                                        "xpath",
                                        "/html/body/main/div/div[2]/div/form/div[9]/div[2]/button"
                                    ],
                                    [
                                        "wait",
                                        "contains",
                                        "test/callback",
                                        10
                                    ]
                                ]
                            },
                            {
                                "task": "Consent",
                                "match": constants.BASE_URL + "/authenticationendpoint/oauth2_consent*",
                                "optional": True,
                                "commands": [
                                    [
                                        "wait",
                                        "id",
                                        "approve",
                                        10
                                    ],
                                    [
                                        "click",
                                        "id",
                                        "approve"
                                    ],
                                    [
                                        "wait",
                                        "contains",
                                        "callback",
                                        10
                                    ]
                                ]
                            },
                            {
                                "task": "Verify",
                                "match": "https://localhost.emobix.co.uk:8443/test/a/test/callback*"
                            }
                        ]
                    }
                ]
            },
            "oidcc-ensure-registered-redirect-uri": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Verify error page",
                                "match": constants.BASE_URL + "/authenticationendpoint/oauth2_error.do*",
                                "commands": [
                                    [
                                        "wait",
                                        "xpath",
                                        "//*",
                                        10,
                                        "Identity Server",
                                        "update-image-placeholder"
                                    ]
                                ]
                            }
                        ]
                    }
                ]
            },
            "oidcc-ensure-request-object-with-redirect-uri": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Verify error",
                                "match": constants.BASE_URL + "/authenticationendpoint/oauth2_error.do*",
                                "commands": [
                                    [
                                        "wait",
                                        "xpath",
                                        "//*",
                                        10,
                                        "Identity Server",
                                        "update-image-placeholder"
                                    ]
                                ]
                            }
                        ]
                    }
                ]
            },
            "oidcc-prompt-login": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Login",
                                "match": constants.BASE_URL + "/authenticationendpoint/login*",
                                "optional": True,
                                "commands": [
                                    [
                                        "wait",
                                        "xpath",
                                        "/html/body/main/div/div[2]/h3",
                                        10,
                                        "Sign In",
                                        "update-image-placeholder-optional"
                                    ],
                                    [
                                        "text",
                                        "id",
                                        "usernameUserInput",
                                        "admin"
                                    ],
                                    [
                                        "text",
                                        "id",
                                        "password",
                                        "admin"
                                    ],
                                    [
                                        "click",
                                        "xpath",
                                        "/html/body/main/div/div[2]/div/form/div[9]/div[2]/button"
                                    ],
                                    [
                                        "wait",
                                        "contains",
                                        "test/callback",
                                        10
                                    ]
                                ]
                            },
                            {
                                "task": "Consent",
                                "match": constants.BASE_URL + "/authenticationendpoint/oauth2_consent*",
                                "optional": True,
                                "commands": [
                                    [
                                        "wait",
                                        "id",
                                        "approve",
                                        10
                                    ],
                                    [
                                        "click",
                                        "id",
                                        "rememberApproval",
                                        "optional"
                                    ],
                                    [
                                        "click",
                                        "id",
                                        "approve"
                                    ],
                                    [
                                        "wait",
                                        "contains",
                                        "callback",
                                        10
                                    ]
                                ]
                            },
                            {
                                "task": "Verify",
                                "match": "https://localhost.emobix.co.uk:8443/test/a/test/callback*"
                            }
                        ]
                    }
                ]
            },
            "oidcc-max-age-1": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Login",
                                "match": constants.BASE_URL + "/authenticationendpoint/login*",
                                "optional": True,
                                "commands": [
                                    [
                                        "wait",
                                        "xpath",
                                        "/html/body/main/div/div[2]/h3",
                                        10,
                                        "Sign In",
                                        "update-image-placeholder-optional"
                                    ],
                                    [
                                        "text",
                                        "id",
                                        "usernameUserInput",
                                        "admin"
                                    ],
                                    [
                                        "text",
                                        "id",
                                        "password",
                                        "admin"
                                    ],
                                    [
                                        "click",
                                        "xpath",
                                        "/html/body/main/div/div[2]/div/form/div[9]/div[2]/button"
                                    ],
                                    [
                                        "wait",
                                        "contains",
                                        "test/callback",
                                        10
                                    ]
                                ]
                            },
                            {
                                "task": "Consent",
                                "match": constants.BASE_URL + "/authenticationendpoint/oauth2_consent*",
                                "optional": True,
                                "commands": [
                                    [
                                        "wait",
                                        "id",
                                        "approve",
                                        10
                                    ],
                                    [
                                        "click",
                                        "id",
                                        "rememberApproval",
                                        "optional"
                                    ],
                                    [
                                        "click",
                                        "id",
                                        "approve"
                                    ],
                                    [
                                        "wait",
                                        "contains",
                                        "callback",
                                        10
                                    ]
                                ]
                            },
                            {
                                "task": "Verify",
                                "match": "https://localhost.emobix.co.uk:8443/test/a/test/callback*"
                            }
                        ]
                    }
                ]
            }
        }
    },
    "hybrid": {
        "browser": [
            {
                "match": constants.BASE_URL + "/oauth2/authorize*",
                "tasks": [
                    {
                        "task": "Login",
                        "match": constants.BASE_URL + "/authenticationendpoint/login*",
                        "optional": True,
                        "commands": [
                            [
                                "text",
                                "id",
                                "usernameUserInput",
                                "admin"
                            ],
                            [
                                "text",
                                "id",
                                "password",
                                "admin"
                            ],
                            [
                                "click",
                                "xpath",
                                "/html/body/main/div/div[2]/div/form/div[9]/div[2]/button"
                            ],
                            [
                                "wait",
                                "contains",
                                "test/callback",
                                10
                            ]
                        ]
                    },
                    {
                        "task": "Verify",
                        "match": "https://localhost.emobix.co.uk:8443/test/a/test/callback*"
                    }
                ]
            }
        ],
        "override": {
            "oidcc-ensure-request-with-valid-pkce-succeeds": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Login",
                                "match": constants.BASE_URL + "/authenticationendpoint/login*",
                                "optional": True,
                                "commands": [
                                    [
                                        "text",
                                        "id",
                                        "usernameUserInput",
                                        "admin"
                                    ],
                                    [
                                        "text",
                                        "id",
                                        "password",
                                        "admin"
                                    ],
                                    [
                                        "click",
                                        "xpath",
                                        "/html/body/main/div/div[2]/div/form/div[9]/div[2]/button"
                                    ],
                                    [
                                        "wait",
                                        "contains",
                                        "test/callback",
                                        10
                                    ]
                                ]
                            },
                            {
                                "task": "Consent",
                                "match": constants.BASE_URL + "/authenticationendpoint/oauth2_consent*",
                                "optional": True,
                                "commands": [
                                    [
                                        "wait",
                                        "id",
                                        "approve",
                                        10
                                    ],
                                    [
                                        "click",
                                        "id",
                                        "approve"
                                    ],
                                    [
                                        "wait",
                                        "contains",
                                        "callback",
                                        10
                                    ]
                                ]
                            },
                            {
                                "task": "Verify",
                                "match": "https://localhost.emobix.co.uk:8443/test/a/test/callback*"
                            }
                        ]
                    }
                ]
            },
            "oidcc-refresh-token": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Login",
                                "match": constants.BASE_URL + "/authenticationendpoint/login*",
                                "optional": True,
                                "commands": [
                                    [
                                        "text",
                                        "id",
                                        "usernameUserInput",
                                        "admin"
                                    ],
                                    [
                                        "text",
                                        "id",
                                        "password",
                                        "admin"
                                    ],
                                    [
                                        "click",
                                        "xpath",
                                        "/html/body/main/div/div[2]/div/form/div[9]/div[2]/button"
                                    ],
                                    [
                                        "wait",
                                        "contains",
                                        "oauth2_consent",
                                        10
                                    ]
                                ]
                            },
                            {
                                "task": "Consent",
                                "match": constants.BASE_URL + "/authenticationendpoint/oauth2_consent*",
                                "optional": True,
                                "commands": [
                                    [
                                        "wait",
                                        "id",
                                        "approve",
                                        10
                                    ],
                                    [
                                        "click",
                                        "id",
                                        "approve"
                                    ],
                                    [
                                        "wait",
                                        "contains",
                                        "callback",
                                        10
                                    ]
                                ]
                            },
                            {
                                "task": "Verify",
                                "match": "https://localhost.emobix.co.uk:8443/test/a/test/callback*"
                            }
                        ]
                    }
                ]
            },
            "oidcc-ensure-registered-redirect-uri": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Verify error page",
                                "match": constants.BASE_URL + "/authenticationendpoint/oauth2_error.do*",
                                "commands": [
                                    [
                                        "wait",
                                        "xpath",
                                        "//*",
                                        10,
                                        "Identity Server",
                                        "update-image-placeholder"
                                    ]
                                ]
                            }
                        ]
                    }
                ]
            },
            "oidcc-ensure-request-object-with-redirect-uri": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Verify error",
                                "match": constants.BASE_URL + "/authenticationendpoint/oauth2_error.do*",
                                "commands": [
                                    [
                                        "wait",
                                        "xpath",
                                        "//*",
                                        10,
                                        "Identity Server",
                                        "update-image-placeholder"
                                    ]
                                ]
                            }
                        ]
                    }
                ]
            },
            "oidcc-prompt-login": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Login",
                                "match": constants.BASE_URL + "/authenticationendpoint/login*",
                                "optional": True,
                                "commands": [
                                    [
                                        "wait",
                                        "xpath",
                                        "/html/body/main/div/div[2]/h3",
                                        10,
                                        "Sign In",
                                        "update-image-placeholder-optional"
                                    ],
                                    [
                                        "text",
                                        "id",
                                        "usernameUserInput",
                                        "admin"
                                    ],
                                    [
                                        "text",
                                        "id",
                                        "password",
                                        "admin"
                                    ],
                                    [
                                        "click",
                                        "xpath",
                                        "/html/body/main/div/div[2]/div/form/div[9]/div[2]/button"
                                    ],
                                    [
                                        "wait",
                                        "contains",
                                        "test/callback",
                                        10
                                    ]
                                ]
                            },
                            {
                                "task": "Consent",
                                "match": constants.BASE_URL + "/authenticationendpoint/oauth2_consent*",
                                "optional": True,
                                "commands": [
                                    [
                                        "wait",
                                        "id",
                                        "approve",
                                        10
                                    ],
                                    [
                                        "click",
                                        "id",
                                        "rememberApproval",
                                        "optional"
                                    ],
                                    [
                                        "click",
                                        "id",
                                        "approve"
                                    ],
                                    [
                                        "wait",
                                        "contains",
                                        "callback",
                                        10
                                    ]
                                ]
                            },
                            {
                                "task": "Verify",
                                "match": "https://localhost.emobix.co.uk:8443/test/a/test/callback*"
                            }
                        ]
                    }
                ]
            },
            "oidcc-max-age-1": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Login",
                                "match": constants.BASE_URL + "/authenticationendpoint/login*",
                                "optional": True,
                                "commands": [
                                    [
                                        "wait",
                                        "xpath",
                                        "/html/body/main/div/div[2]/h3",
                                        10,
                                        "Sign In",
                                        "update-image-placeholder-optional"
                                    ],
                                    [
                                        "text",
                                        "id",
                                        "usernameUserInput",
                                        "admin"
                                    ],
                                    [
                                        "text",
                                        "id",
                                        "password",
                                        "admin"
                                    ],
                                    [
                                        "click",
                                        "xpath",
                                        "/html/body/main/div/div[2]/div/form/div[9]/div[2]/button"
                                    ],
                                    [
                                        "wait",
                                        "contains",
                                        "test/callback",
                                        10
                                    ]
                                ]
                            },
                            {
                                "task": "Consent",
                                "match": constants.BASE_URL + "/authenticationendpoint/oauth2_consent*",
                                "optional": True,
                                "commands": [
                                    [
                                        "wait",
                                        "id",
                                        "approve",
                                        10
                                    ],
                                    [
                                        "click",
                                        "id",
                                        "rememberApproval",
                                        "optional"
                                    ],
                                    [
                                        "click",
                                        "id",
                                        "approve"
                                    ],
                                    [
                                        "wait",
                                        "contains",
                                        "callback",
                                        10
                                    ]
                                ]
                            },
                            {
                                "task": "Verify",
                                "match": "https://localhost.emobix.co.uk:8443/test/a/test/callback*"
                            }
                        ]
                    }
                ]
            }
        }
    },
    "formpost-basic": {
        "browser": [
            {
                "match": constants.BASE_URL + "/oauth2/authorize*",
                "tasks": [
                    {
                        "task": "Login",
                        "match": constants.BASE_URL + "/authenticationendpoint/login*",
                        "optional": True,
                        "commands": [
                            [
                                "text",
                                "id",
                                "usernameUserInput",
                                "admin"
                            ],
                            [
                                "text",
                                "id",
                                "password",
                                "admin"
                            ],
                            [
                                "click",
                                "xpath",
                                "/html/body/main/div/div[2]/div/form/div[9]/div[2]/button"
                            ]
                        ]
                    }
                ]
            }
        ],
        "override": {
            "oidcc-prompt-none-not-logged-in": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Verify error page",
                                "match": "https://localhost.emobix.co.uk:8443/test/a/test/callback?error*"
                            }
                        ]
                    }
                ]
            },
            "oidcc-response-type-missing": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Verify error page",
                                "match": "https://localhost.emobix.co.uk:8443/test/a/test/callback?error*"
                            }
                        ]
                    }
                ]
            },
            "oidcc-refresh-token": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Login",
                                "match": constants.BASE_URL + "/authenticationendpoint/login*",
                                "optional": True,
                                "commands": [
                                    [
                                        "text",
                                        "id",
                                        "usernameUserInput",
                                        "admin"
                                    ],
                                    [
                                        "text",
                                        "id",
                                        "password",
                                        "admin"
                                    ],
                                    [
                                        "click",
                                        "xpath",
                                        "/html/body/main/div/div[2]/div/form/div[9]/div[2]/button"
                                    ],
                                    [
                                        "wait",
                                        "contains",
                                        "oauth2_consent",
                                        10
                                    ]
                                ]
                            },
                            {
                                "task": "Consent",
                                "match": constants.BASE_URL + "/authenticationendpoint/oauth2_consent*",
                                "optional": True,
                                "commands": [
                                    [
                                        "wait",
                                        "id",
                                        "approve",
                                        10
                                    ],
                                    [
                                        "click",
                                        "id",
                                        "approve"
                                    ]
                                ]
                            }
                        ]
                    }
                ]
            },
            "oidcc-ensure-registered-redirect-uri": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Verify error page",
                                "match": constants.BASE_URL + "/authenticationendpoint/oauth2_error.do*",
                                "commands": [
                                    [
                                        "wait",
                                        "xpath",
                                        "//*",
                                        10,
                                        "Identity Server",
                                        "update-image-placeholder"
                                    ]
                                ]
                            }
                        ]
                    }
                ]
            },
            "oidcc-ensure-request-object-with-redirect-uri": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Verify error",
                                "match": constants.BASE_URL + "/authenticationendpoint/oauth2_error.do*",
                                "commands": [
                                    [
                                        "wait",
                                        "xpath",
                                        "//*",
                                        10,
                                        "Identity Server",
                                        "update-image-placeholder"
                                    ]
                                ]
                            }
                        ]
                    }
                ]
            },
            "oidcc-prompt-login": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Login",
                                "match": constants.BASE_URL + "/authenticationendpoint/login*",
                                "optional": True,
                                "commands": [
                                    [
                                        "wait",
                                        "xpath",
                                        "/html/body/main/div/div[2]/h3",
                                        10,
                                        "Sign In",
                                        "update-image-placeholder-optional"
                                    ],
                                    [
                                        "text",
                                        "id",
                                        "usernameUserInput",
                                        "admin"
                                    ],
                                    [
                                        "text",
                                        "id",
                                        "password",
                                        "admin"
                                    ],
                                    [
                                        "click",
                                        "xpath",
                                        "/html/body/main/div/div[2]/div/form/div[9]/div[2]/button"
                                    ]
                                ]
                            }
                        ]
                    }
                ]
            },
            "oidcc-max-age-1": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Login",
                                "match": constants.BASE_URL + "/authenticationendpoint/login*",
                                "optional": True,
                                "commands": [
                                    [
                                        "wait",
                                        "xpath",
                                        "/html/body/main/div/div[2]/h3",
                                        10,
                                        "Sign In",
                                        "update-image-placeholder-optional"
                                    ],
                                    [
                                        "text",
                                        "id",
                                        "usernameUserInput",
                                        "admin"
                                    ],
                                    [
                                        "text",
                                        "id",
                                        "password",
                                        "admin"
                                    ],
                                    [
                                        "click",
                                        "xpath",
                                        "/html/body/main/div/div[2]/div/form/div[9]/div[2]/button"
                                    ]
                                ]
                            }
                        ]
                    }
                ]
            }
        }
    },
    "formpost-implicit": {
        "browser": [
            {
                "match": constants.BASE_URL + "/oauth2/authorize*",
                "tasks": [
                    {
                        "task": "Login",
                        "match": constants.BASE_URL + "/authenticationendpoint/login*",
                        "optional": True,
                        "commands": [
                            [
                                "text",
                                "id",
                                "usernameUserInput",
                                "admin"
                            ],
                            [
                                "text",
                                "id",
                                "password",
                                "admin"
                            ],
                            [
                                "click",
                                "xpath",
                                "/html/body/main/div/div[2]/div/form/div[9]/div[2]/button"
                            ]
                        ]
                    }
                ]
            }
        ],
        "override": {
            "oidcc-prompt-none-not-logged-in": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Verify error page",
                                "match": "https://localhost.emobix.co.uk:8443/test/a/test/callback#error*"
                            }
                        ]
                    }
                ]
            },
            "oidcc-response-type-missing": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Verify error page",
                                "match": "https://localhost.emobix.co.uk:8443/test/a/test/callback?error*"
                            }
                        ]
                    }
                ]
            },
            "oidcc-refresh-token": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Login",
                                "match": constants.BASE_URL + "/authenticationendpoint/login*",
                                "optional": True,
                                "commands": [
                                    [
                                        "text",
                                        "id",
                                        "usernameUserInput",
                                        "admin"
                                    ],
                                    [
                                        "text",
                                        "id",
                                        "password",
                                        "admin"
                                    ],
                                    [
                                        "click",
                                        "xpath",
                                        "/html/body/main/div/div[2]/div/form/div[9]/div[2]/button"
                                    ]
                                ]
                            }
                        ]
                    }
                ]
            },
            "oidcc-ensure-registered-redirect-uri": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Verify error page",
                                "match": constants.BASE_URL + "/authenticationendpoint/oauth2_error.do*",
                                "commands": [
                                    [
                                        "wait",
                                        "xpath",
                                        "//*",
                                        10,
                                        "Identity Server",
                                        "update-image-placeholder"
                                    ]
                                ]
                            }
                        ]
                    }
                ]
            },
            "oidcc-ensure-request-without-nonce-fails": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Verify error page",
                                "match": "https://localhost.emobix.co.uk:8443/test/a/test/callback#error_description*",
                                "commands": [
                                    [
                                        "wait",
                                        "xpath",
                                        "//*",
                                        10,
                                        "Identity Server",
                                        "update-image-placeholder"
                                    ]
                                ]
                            }
                        ]
                    }
                ]
            },
            "oidcc-ensure-request-object-with-redirect-uri": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Verify error",
                                "match": constants.BASE_URL + "/authenticationendpoint/oauth2_error.do*",
                                "commands": [
                                    [
                                        "wait",
                                        "xpath",
                                        "//*",
                                        10,
                                        "Identity Server",
                                        "update-image-placeholder"
                                    ]
                                ]
                            }
                        ]
                    }
                ]
            },
            "oidcc-prompt-login": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Login",
                                "match": constants.BASE_URL + "/authenticationendpoint/login*",
                                "optional": True,
                                "commands": [
                                    [
                                        "wait",
                                        "xpath",
                                        "/html/body/main/div/div[2]/h3",
                                        10,
                                        "Sign In",
                                        "update-image-placeholder-optional"
                                    ],
                                    [
                                        "text",
                                        "id",
                                        "usernameUserInput",
                                        "admin"
                                    ],
                                    [
                                        "text",
                                        "id",
                                        "password",
                                        "admin"
                                    ],
                                    [
                                        "click",
                                        "xpath",
                                        "/html/body/main/div/div[2]/div/form/div[9]/div[2]/button"
                                    ]
                                ]
                            }
                        ]
                    }
                ]
            },
            "oidcc-max-age-1": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Login",
                                "match": constants.BASE_URL + "/authenticationendpoint/login*",
                                "optional": True,
                                "commands": [
                                    [
                                        "wait",
                                        "xpath",
                                        "/html/body/main/div/div[2]/h3",
                                        10,
                                        "Sign In",
                                        "update-image-placeholder-optional"
                                    ],
                                    [
                                        "text",
                                        "id",
                                        "usernameUserInput",
                                        "admin"
                                    ],
                                    [
                                        "text",
                                        "id",
                                        "password",
                                        "admin"
                                    ],
                                    [
                                        "click",
                                        "xpath",
                                        "/html/body/main/div/div[2]/div/form/div[9]/div[2]/button"
                                    ]
                                ]
                            }
                        ]
                    }
                ]
            }
        }
    },
    "formpost-hybrid": {
        "browser": [
            {
                "match": constants.BASE_URL + "/oauth2/authorize*",
                "tasks": [
                    {
                        "task": "Login",
                        "match": constants.BASE_URL + "/authenticationendpoint/login*",
                        "optional": True,
                        "commands": [
                            [
                                "text",
                                "id",
                                "usernameUserInput",
                                "admin"
                            ],
                            [
                                "text",
                                "id",
                                "password",
                                "admin"
                            ],
                            [
                                "click",
                                "xpath",
                                "/html/body/main/div/div[2]/div/form/div[9]/div[2]/button"
                            ]
                        ]
                    }
                ]
            }
        ],
        "override": {
            "oidcc-prompt-none-not-logged-in": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Verify error page",
                                "match": "https://localhost.emobix.co.uk:8443/test/a/test/callback#error*"
                            }
                        ]
                    }
                ]
            },
            "oidcc-response-type-missing": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Verify error page",
                                "match": "https://localhost.emobix.co.uk:8443/test/a/test/callback?error*"
                            }
                        ]
                    }
                ]
            },
            "oidcc-refresh-token": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Login",
                                "match": constants.BASE_URL + "/authenticationendpoint/login*",
                                "optional": True,
                                "commands": [
                                    [
                                        "text",
                                        "id",
                                        "usernameUserInput",
                                        "admin"
                                    ],
                                    [
                                        "text",
                                        "id",
                                        "password",
                                        "admin"
                                    ],
                                    [
                                        "click",
                                        "xpath",
                                        "/html/body/main/div/div[2]/div/form/div[9]/div[2]/button"
                                    ],
                                    [
                                        "wait",
                                        "contains",
                                        "oauth2_consent",
                                        10
                                    ]
                                ]
                            },
                            {
                                "task": "Consent",
                                "match": constants.BASE_URL + "/authenticationendpoint/oauth2_consent*",
                                "optional": True,
                                "commands": [
                                    [
                                        "wait",
                                        "id",
                                        "approve",
                                        10
                                    ],
                                    [
                                        "click",
                                        "id",
                                        "approve"
                                    ]
                                ]
                            }
                        ]
                    }
                ]
            },
            "oidcc-ensure-registered-redirect-uri": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Verify error page",
                                "match": constants.BASE_URL + "/authenticationendpoint/oauth2_error.do*",
                                "commands": [
                                    [
                                        "wait",
                                        "xpath",
                                        "//*",
                                        10,
                                        "Identity Server",
                                        "update-image-placeholder"
                                    ]
                                ]
                            }
                        ]
                    }
                ]
            },
            "oidcc-ensure-request-without-nonce-fails": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Verify error page",
                                "match": "https://localhost.emobix.co.uk:8443/test/a/test/callback*",
                                "commands": [
                                    [
                                        "wait",
                                        "xpath",
                                        "//*",
                                        10,
                                        "Identity Server",
                                        "update-image-placeholder"
                                    ]
                                ]
                            }
                        ]
                    }
                ]
            },
            "oidcc-ensure-request-object-with-redirect-uri": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Verify error",
                                "match": constants.BASE_URL + "/authenticationendpoint/oauth2_error.do*",
                                "commands": [
                                    [
                                        "wait",
                                        "xpath",
                                        "//*",
                                        10,
                                        "Identity Server",
                                        "update-image-placeholder"
                                    ]
                                ]
                            }
                        ]
                    }
                ]
            },
            "oidcc-prompt-login": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Login",
                                "match": constants.BASE_URL + "/authenticationendpoint/login*",
                                "optional": True,
                                "commands": [
                                    [
                                        "wait",
                                        "xpath",
                                        "/html/body/main/div/div[2]/h3",
                                        10,
                                        "Sign In",
                                        "update-image-placeholder-optional"
                                    ],
                                    [
                                        "text",
                                        "id",
                                        "usernameUserInput",
                                        "admin"
                                    ],
                                    [
                                        "text",
                                        "id",
                                        "password",
                                        "admin"
                                    ],
                                    [
                                        "click",
                                        "xpath",
                                        "/html/body/main/div/div[2]/div/form/div[9]/div[2]/button"
                                    ]
                                ]
                            }
                        ]
                    }
                ]
            },
            "oidcc-max-age-1": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            {
                                "task": "Login",
                                "match": constants.BASE_URL + "/authenticationendpoint/login*",
                                "optional": True,
                                "commands": [
                                    [
                                        "wait",
                                        "xpath",
                                        "/html/body/main/div/div[2]/h3",
                                        10,
                                        "Sign In",
                                        "update-image-placeholder-optional"
                                    ],
                                    [
                                        "text",
                                        "id",
                                        "usernameUserInput",
                                        "admin"
                                    ],
                                    [
                                        "text",
                                        "id",
                                        "password",
                                        "admin"
                                    ],
                                    [
                                        "click",
                                        "xpath",
                                        "/html/body/main/div/div[2]/div/form/div[9]/div[2]/button"
                                    ]
                                ]
                            }
                        ]
                    }
                ]
            }
        }
    }

}
