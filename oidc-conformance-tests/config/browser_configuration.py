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

FORMPOST_PROMPT_LOGIN = {
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
            "username",
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

FORMPOST_CONSENT = {
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

FORMPOST_LOGIN = {
    "task": "Login",
    "match": constants.BASE_URL + "/authenticationendpoint/login*",
    "optional": True,
    "commands": [
        [
            "text",
            "id",
            "username",
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

PROMPT_CONSENT = {
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
}

PROMPT_LOGIN = {
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
            "username",
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
}

VERIFY_ERROR = {
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

VERIFY_COMPLETE_FORMPOST = {
    "task": "Verify Complete",
    "match": "*",
    "optional": True,
    "commands": [
        [
            "wait",
            "id",
            "submission_complete",
            10
        ]
    ]
}

LOGIN_REFRESH_TOKEN = {
    "task": "Login",
    "match": constants.BASE_URL + "/authenticationendpoint/login*",
    "optional": True,
    "commands": [
        [
            "text",
            "id",
            "username",
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
}

CONSENT = {
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
}

LOGIN_BASIC = {
    "task": "Login",
    "match": constants.BASE_URL + "/authenticationendpoint/login*",
    "optional": True,
    "commands": [
        [
            "text",
            "id",
            "username",
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
}

VERIFY_COMPLETE = {
    "task": "Verify Complete",
    "match": "https://*/test/a/*/callback*",
    "optional": True,
    "commands": [
        [
            "wait",
            "id",
            "submission_complete",
            10
        ]
    ]
}

CONFIG = {
    "basic": {
        "browser": [
            {
                "match": constants.BASE_URL + "/oauth2/authorize*",
                "tasks": [
                    LOGIN_BASIC,
                    VERIFY_COMPLETE

                ]
            }
        ],
        "override": {
            "oidcc-refresh-token": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            LOGIN_REFRESH_TOKEN,
                            CONSENT,
                            VERIFY_COMPLETE

                        ]
                    }
                ]
            },
            "oidcc-ensure-registered-redirect-uri": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            VERIFY_ERROR
                        ]
                    }
                ]
            },
            "oidcc-ensure-request-object-with-redirect-uri": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            VERIFY_ERROR
                        ]
                    }
                ]
            },
            "oidcc-prompt-login": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            PROMPT_LOGIN,
                            PROMPT_CONSENT,
                            VERIFY_COMPLETE

                        ]
                    }
                ]
            },
            "oidcc-max-age-1": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            PROMPT_LOGIN,
                            PROMPT_CONSENT,
                            VERIFY_COMPLETE

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
                    LOGIN_BASIC,
                    VERIFY_COMPLETE
                ]
            }
        ],
        "override": {
            "oidcc-ensure-request-with-valid-pkce-succeeds": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            LOGIN_BASIC,
                            CONSENT,
                            VERIFY_COMPLETE
                        ]
                    }
                ]
            },
            "oidcc-refresh-token": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            LOGIN_BASIC,
                            CONSENT,
                            VERIFY_COMPLETE
                        ]
                    }
                ]
            },
            "oidcc-ensure-registered-redirect-uri": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            VERIFY_ERROR
                        ]
                    }
                ]
            },
            "oidcc-ensure-request-object-with-redirect-uri": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            VERIFY_ERROR
                        ]
                    }
                ]
            },
            "oidcc-prompt-login": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            PROMPT_LOGIN,
                            PROMPT_CONSENT,
                            VERIFY_COMPLETE
                        ]
                    }
                ]
            },
            "oidcc-max-age-1": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            PROMPT_LOGIN,
                            PROMPT_CONSENT,
                            VERIFY_COMPLETE
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
                    LOGIN_BASIC,
                    VERIFY_COMPLETE
                ]
            }
        ],
        "override": {
            "oidcc-ensure-request-with-valid-pkce-succeeds": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            LOGIN_BASIC,
                            CONSENT,
                            VERIFY_COMPLETE
                        ]
                    }
                ]
            },
            "oidcc-refresh-token": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            LOGIN_REFRESH_TOKEN,
                            CONSENT,
                            VERIFY_COMPLETE
                        ]
                    }
                ]
            },
            "oidcc-ensure-registered-redirect-uri": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            VERIFY_ERROR
                        ]
                    }
                ]
            },
            "oidcc-ensure-request-object-with-redirect-uri": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            VERIFY_ERROR
                        ]
                    }
                ]
            },
            "oidcc-prompt-login": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            PROMPT_LOGIN,
                            PROMPT_CONSENT,
                            VERIFY_COMPLETE
                        ]
                    }
                ]
            },
            "oidcc-max-age-1": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            PROMPT_LOGIN,
                            PROMPT_CONSENT,
                            VERIFY_COMPLETE
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
                    FORMPOST_LOGIN,
                    VERIFY_COMPLETE_FORMPOST
                ]
            }
        ],
        "override": {
            "oidcc-prompt-none-not-logged-in": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            VERIFY_COMPLETE_FORMPOST
                        ]
                    }
                ]
            },
            "oidcc-prompt-none-logged-in": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            FORMPOST_LOGIN,
                            VERIFY_COMPLETE_FORMPOST
                        ]
                    }
                ]
            },
            "oidcc-response-type-missing": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            VERIFY_COMPLETE_FORMPOST
                        ]
                    }
                ]
            },
            "oidcc-refresh-token": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            LOGIN_REFRESH_TOKEN,
                            FORMPOST_CONSENT,
                            VERIFY_COMPLETE_FORMPOST
                        ]
                    }
                ]
            },
            "oidcc-ensure-registered-redirect-uri": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            VERIFY_ERROR
                        ]
                    }
                ]
            },
            "oidcc-ensure-request-object-with-redirect-uri": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            VERIFY_ERROR
                        ]
                    }
                ]
            },
            "oidcc-prompt-login": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            FORMPOST_PROMPT_LOGIN,
                            VERIFY_COMPLETE_FORMPOST
                        ]
                    }
                ]
            },
            "oidcc-max-age-1": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            FORMPOST_PROMPT_LOGIN,
                            VERIFY_COMPLETE_FORMPOST
                        ]
                    }
                ]
            },
            "oidcc-max-age-10000": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            FORMPOST_LOGIN,
                            VERIFY_COMPLETE_FORMPOST
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
                    FORMPOST_LOGIN,
                    VERIFY_COMPLETE_FORMPOST
                ]
            }
        ],
        "override": {
            "oidcc-prompt-none-not-logged-in": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            VERIFY_COMPLETE
                        ]
                    }
                ]
            },
            "oidcc-prompt-none-logged-in": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            FORMPOST_LOGIN,
                            VERIFY_COMPLETE_FORMPOST
                        ]
                    }
                ]
            },
            "oidcc-response-type-missing": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            VERIFY_COMPLETE_FORMPOST
                        ]
                    }
                ]
            },
            "oidcc-refresh-token": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            FORMPOST_LOGIN,
                            VERIFY_COMPLETE_FORMPOST
                        ]
                    }
                ]
            },
            "oidcc-ensure-registered-redirect-uri": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            VERIFY_ERROR
                        ]
                    }
                ]
            },
            "oidcc-ensure-request-without-nonce-fails": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                           VERIFY_COMPLETE_FORMPOST
                        ]
                    }
                ]
            },
            "oidcc-ensure-request-object-with-redirect-uri": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            VERIFY_ERROR
                        ]
                    }
                ]
            },
            "oidcc-prompt-login": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            FORMPOST_PROMPT_LOGIN,
                            VERIFY_COMPLETE_FORMPOST
                        ]
                    }
                ]
            },
            "oidcc-max-age-1": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            FORMPOST_PROMPT_LOGIN,
                            VERIFY_COMPLETE_FORMPOST
                        ]
                    }
                ]
            },
            "oidcc-max-age-10000": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            FORMPOST_LOGIN,
                            VERIFY_COMPLETE_FORMPOST

                        ]
                    }
                ]
            },
            "oidcc-id-token-hint": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            FORMPOST_LOGIN,
                            VERIFY_COMPLETE_FORMPOST
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
                    FORMPOST_LOGIN,
                    VERIFY_COMPLETE_FORMPOST
                ]
            }
        ],
        "override": {
            "oidcc-prompt-none-not-logged-in": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            VERIFY_COMPLETE_FORMPOST
                        ]
                    }
                ]
            },
            "oidcc-prompt-none-logged-in": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            FORMPOST_LOGIN,
                            VERIFY_COMPLETE_FORMPOST
                        ]
                    }
                ]
            },
            "oidcc-response-type-missing": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            VERIFY_COMPLETE_FORMPOST
                        ]
                    }
                ]
            },
            "oidcc-refresh-token": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            LOGIN_REFRESH_TOKEN,
                            FORMPOST_CONSENT,
                            VERIFY_COMPLETE_FORMPOST
                        ]
                    }
                ]
            },
            "oidcc-ensure-registered-redirect-uri": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            VERIFY_ERROR
                        ]
                    }
                ]
            },
            "oidcc-ensure-request-without-nonce-fails": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            VERIFY_COMPLETE_FORMPOST
                        ]
                    }
                ]
            },
            "oidcc-ensure-request-object-with-redirect-uri": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            VERIFY_ERROR
                        ]
                    }
                ]
            },
            "oidcc-prompt-login": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            FORMPOST_PROMPT_LOGIN,
                            VERIFY_COMPLETE_FORMPOST
                        ]
                    }
                ]
            },
            "oidcc-max-age-1": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            FORMPOST_PROMPT_LOGIN,
                            VERIFY_COMPLETE_FORMPOST
                        ]
                    }
                ]
            },
            "oidcc-max-age-10000": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            FORMPOST_LOGIN,
                            VERIFY_COMPLETE_FORMPOST

                        ]
                    }
                ]
            },
            "oidcc-id-token-hint": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            FORMPOST_LOGIN,
                            VERIFY_COMPLETE_FORMPOST
                        ]
                    }
                ]
            }
        }
    }

}
