
import constants_fapi as constants

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
            "fapi-wso2is/callback",
            10
        ]
    ]
}

LOGIN_WITH_DELAY = {
    "task": "Login",
    "match": constants.BASE_URL + "/authenticationendpoint/login*",
    "optional": True,
    "commands": [
        [
            "sleep",
            "20000"
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
            "fapi-wso2is/callback",
            10
        ]
    ]
}

LOGIN_BEFORE_CONSENT = {
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
            "/authenticationendpoint/oauth2_consent",
            10
        ]
    ]
}

JUST_SCREENSHOT = {
    "task": "Screenshot",
    "match": constants.BASE_URL + "/authenticationendpoint/login*",
    "optional": True,
    "commands": [
        [
            "wait",
            "id",
            "usernameUserInput",
            10
        ]
    ]
}

CONSENT_APPROVE = {
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

LOGIN_BEFORE_CONSENT_USER1 = {
    "task": "Login",
    "match": constants.BASE_URL + "/authenticationendpoint/login*",
    "optional": True,
    "commands": [
        [
            "text",
            "id",
            "usernameUserInput",
            "user1"
        ],
        [
            "text",
            "id",
            "password",
            "User1@password"
        ],
        [
            "click",
            "xpath",
            "//*[@id=\"loginForm\"]/div[8]/div[1]/button"
        ],
        [
            "wait",
            "contains",
            "/authenticationendpoint/oauth2_consent",
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
            "id",
            "consent",
            10
        ],
        [
            "click",
            "xpath",
            "//*[@id=\"profile\"]/div[2]/div[5]/input[2]"
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
                    VERIFY_COMPLETE

                ]
            }
        ],
        "override": {
            "fapi1-advanced-final": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            LOGIN_BEFORE_CONSENT,
                            CONSENT_APPROVE,
                            VERIFY_COMPLETE

                        ]
                    }
                ]
            },
            "fapi1-advanced-final-user-rejects-authentication": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            LOGIN_BEFORE_CONSENT_USER1,
                            CONSENT_DENY,
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
            "fapi1-advanced-final-par-ensure-reused-request-uri-prior-to-auth-completion-succeeds": {
                "browser": [
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "match-limit": 1,
                        "tasks": [
                            JUST_SCREENSHOT
                        ]
                    },
                    {
                        "match": constants.BASE_URL + "/oauth2/authorize*",
                        "tasks": [
                            LOGIN_BEFORE_CONSENT,
                            CONSENT_APPROVE,
                            VERIFY_COMPLETE
                        ]
                    }
                ]
            },
        }
    },
}
