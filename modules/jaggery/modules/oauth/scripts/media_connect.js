var twitterAPI = function(apiKey, secret) {
    return twitterProvider = {
        "oauth_version" : "1",
        "authorization_url" : "https://api.twitter.com/oauth/authorize",
        "access_token_url" : "https://api.twitter.com/oauth/access_token",
        "request_token_url" : "https://api.twitter.com/oauth/request_token",
        "api_key" : apiKey,
        "api_secret" : secret
    };
};

var likedinAPI = function(apiKey, secret) {
    return linkedinProvider = {
        "oauth_version" : "1",
        "authorization_url" : "https://www.linkedin.com/uas/oauth/authorize",
        "access_token_url" : "https://api.linkedin.com/uas/oauth/accessToken",
        "request_token_url" : "https://api.linkedin.com/uas/oauth/requestToken",
        "api_key" : apiKey,
        "api_secret" : secret
    };
};