
import constants_fapi as constants

FORMPOST_PROMPT_LOGIN = {
    "task": "Login",
    "match": constants.BASE_URL + "/authenticationendpoint/login*",
    "optional": True,
    "commands": [
        [
            "wait",
            "xpath",
            "/html/body/div[2]/main/div/div[2]/h3",
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
            "//*[@id=\"loginForm\"]/div[8]/div[1]/button"
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
            "//*[@id=\"loginForm\"]/div[8]/div[1]/button"
        ]
    ]
}

FORMPOST_LOGIN_WITHOUT_USERNAME = {
    "task": "Login",
    "match": constants.BASE_URL + "/authenticationendpoint/login*",
    "optional": True,
    "commands": [
        [
            "text",
            "id",
            "password",
            "admin"
        ],
        [
            "click",
            "xpath",
            "//*[@id=\"loginForm\"]/div[6]/div[1]/button"
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
            "/html/body/div[2]/main/div/div[2]/h3",
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
            "//*[@id=\"loginForm\"]/div[8]/div[1]/button"
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
            "Need help\? Contact us via",
            "update-image-placeholder"
        ]
    ]
}

VERIFY_COMPLETE_FORMPOST = {
    "task": "Verify Complete",
    "match": "*",
    "optional": False,
    "commands": [
         [
            "click",
            "id",
            "approve",
            "optional"
        ],
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
            "//*[@id=\"loginForm\"]/div[8]/div[1]/button"
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

CONSENT_DENY = {
    "task": "Consent",
    "match": constants.BASE_URL + "/authenticationendpoint/oauth2_consent*",
    "optional": True,
    "commands": [
        [
            "wait",
            "class",
            "ui fluid large button secondary",
            10
        ],
        [
            "click",
            "class",
            "ui fluid large button secondary"
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
            "//*[@id=\"loginForm\"]/div[8]/div[1]/button"
        ],
        [
            "wait",
            "contains",
            "test/callback",
            10
        ]
    ]
}

LOGIN_BASIC_WITHOUT_USERNAME = {
    "task": "Login",
    "match": constants.BASE_URL + "/authenticationendpoint/login*",
    "optional": True,
    "commands": [
        [
            "text",
            "id",
            "password",
            "admin"
        ],
        [
            "click",
            "xpath",
            "//*[@id=\"loginForm\"]/div[6]/div[1]/button"
        ],
        [
            "wait",
            "contains",
            "test/callback",
            10
        ]
    ]
}

WAIT_CONSENT = {
    "task": "Waiting for consent button",
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
            "approve",
            "optional"
        ],
        [
            "wait",
            "contains",
            "callback",
            10
        ]
    ]
}

VERIFY_COMPLETE = {
    "task": "Verify Complete",
    "match": "*",
    "optional": False,
    "commands": [
        [
            "click",
            "id",
            "approve",
            "optional"
        ],
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
                    WAIT_CONSENT,
                    VERIFY_COMPLETE

                ]
            }
        ],
        "override": {
            "fapi1-advanced-final-user-rejects-authentication":{
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            LOGIN_BASIC,
                            CONSENT_DENY,
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
                            WAIT_CONSENT,
                            WAIT_CONSENT,
                            WAIT_CONSENT,
                            VERIFY_COMPLETE

                        ]
                    }
                ]
            },
            "fapi1-advanced-final-ensure-registered-redirect-uri": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            VERIFY_ERROR
                        ]
                    }
                ]
            },
            "fapi1-advanced-final-ensure-redirect-uri-in-authorization-request": {
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
            },
            "oidcc-login-hint": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            LOGIN_BASIC_WITHOUT_USERNAME,
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
                            WAIT_CONSENT,
                            WAIT_CONSENT,
                            WAIT_CONSENT,
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
            },
            "oidcc-login-hint": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            LOGIN_BASIC_WITHOUT_USERNAME,
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
                            WAIT_CONSENT,
                            WAIT_CONSENT,
                            WAIT_CONSENT,
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
            },
            "oidcc-login-hint": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            LOGIN_BASIC_WITHOUT_USERNAME,
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
                            WAIT_CONSENT,
                            WAIT_CONSENT,
                            WAIT_CONSENT,
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
            },
            "oidcc-login-hint": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            FORMPOST_LOGIN_WITHOUT_USERNAME,
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
            },
            "oidcc-login-hint": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            FORMPOST_LOGIN_WITHOUT_USERNAME,
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
                            WAIT_CONSENT,
                            WAIT_CONSENT,
                            WAIT_CONSENT,
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
            },
            "oidcc-login-hint": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            FORMPOST_LOGIN_WITHOUT_USERNAME,
                            VERIFY_COMPLETE_FORMPOST
                        ]
                    }
                ]
            }
        }
    }

}
