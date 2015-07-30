/**
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

#import "IdPProxy.h"

@implementation IdPProxy


- (NSDictionary *)parseQueryString:(NSString *)query {
    NSMutableDictionary *dict = [[NSMutableDictionary alloc] initWithCapacity:3];
    NSArray *pairs = [query componentsSeparatedByString:@"&"];
    for (NSString *pair in pairs) {
        NSArray *elements = [pair componentsSeparatedByString:@"="];
        NSString *key = [[elements objectAtIndex:0] stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
        NSString *val = [[elements objectAtIndex:1] stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
        [dict setObject:val forKey:key];
    }
    return dict;
}


- (void) receivedFromIdPProxy : (NSString *) data sourceApplication: (NSString *) app
{
    // Check the signature of the calling application
    if ([app isEqualToString:IDPPROXY_APP])
    {
        NSDictionary *dicParameter = [self parseQueryString:data];
        if ([[dicParameter objectForKey:@"status"] isEqualToString:@"1"])
        {
            self.auth_code = [dicParameter objectForKey:@"auth_code"];
            
            [self getTokenByAuthCode];
            
            if (self.token != nil)
               [self.delegate onAuthenticationSuccess:self.token.user];
            else
               [self.delegate onAuthenticationFailure:IDPPROXY_ERROR];
        }
        else
            [self.delegate onAuthenticationFailure:IDPPROXY_ERROR];
    }
}

// initialize the variables
- (void) initialize : (NSString *) client_id client_secret: (NSString *) client_secret scope: (NSString *) scope callee: (NSString *) callee idp_server:(NSString *) idp_server
{
    self.client_id = client_id;
    self.client_secret = client_secret;
    self.scope = scope ;
    self.callee = callee;
    self.idp_server = idp_server ;
}

// Call IdPProxy App and get the Authorization Code
- (BOOL) authenticateUser {
    if (self.client_id != nil && self.client_secret != nil && self.idp_server != nil && self.callee != nil)
    {
        NSString *idpProxyURL = [NSString stringWithFormat:IDPPROXY_URL,self.client_id,self.scope,self.callee];
        [[UIApplication sharedApplication] openURL:[NSURL URLWithString:idpProxyURL]];
        return YES ;
    }
    else
        return NO ;
}

// Retrieve Access and Refresh Token using auth code
- (void) getTokenByAuthCode {
    
    NSString *oauthURL = [NSString stringWithFormat:OAUTH_URL,self.idp_server];
    
    // Send a synchronous request
    NSURLRequest * urlRequest = [NSURLRequest requestWithURL:[NSURL URLWithString: oauthURL]];
    
    NSMutableURLRequest *mutableRequest = [urlRequest mutableCopy];
    [mutableRequest setHTTPMethod:@"POST"];
    NSString *bodyContents = [NSString stringWithFormat:OAUTH_REQUEST,self.auth_code,REDIRECT_URI];
    [mutableRequest setHTTPBody:[bodyContents dataUsingEncoding:NSUTF8StringEncoding]];
    
    // Create a plaintext string in the format clientID:clientSecret
    NSString *clientAppCredential = [NSString stringWithFormat:@"%@:%@", self.client_id, self.client_secret];
    
    // Convert clientAppCredential to Base64 format
    NSString *base64EncodedString = [[clientAppCredential dataUsingEncoding:NSUTF8StringEncoding] base64EncodedStringWithOptions:0];
    
    // Create the contents of the header
    NSString *authHeader = [@"Basic " stringByAppendingFormat:@"%@", base64EncodedString];
    [mutableRequest addValue:authHeader forHTTPHeaderField:@"Authorization"];
    
    NSURLResponse * response = nil;
    NSError * error = nil;
    
    NSData *data = [NSURLConnection sendSynchronousRequest:mutableRequest returningResponse:&response error:&error];
    NSString *ret = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
    NSLog(@"ret=%@", ret);
    
    if (error == nil)
    {
        self.token =  [self tokenObjectFromJSON: data];
    }
}

- (Token *) tokenObjectFromJSON : (NSData *) data {
    
    NSDate *dateNow = [NSDate date];
    NSDictionary *jsonObject=[NSJSONSerialization
                              JSONObjectWithData:data
                              options:NSJSONReadingMutableLeaves
                              error:nil];
    
    Token *token = [[Token alloc] init];
    token.access_token = [jsonObject objectForKey: @"access_token"];
    token.refresh_token = [jsonObject objectForKey: @"refresh_token"];
    int expiryTime = [[jsonObject objectForKey: @"expires_in"] intValue];
    time_t unixTime = (time_t) [dateNow timeIntervalSince1970];
    unixTime += expiryTime;
    token.expiry_date = [NSDate dateWithTimeIntervalSince1970:unixTime];
    
    token.user = [self userObjectFromJWT:[jsonObject objectForKey: @"id_token"]] ;
    return token ;
}

- (User *) userObjectFromJWT : (NSString *) base64JWTString
{
    
    NSArray *arrBase64JSON = [base64JWTString componentsSeparatedByString:@"."];
    
    
    NSString *base64HeaderJSON = [[arrBase64JSON objectAtIndex:0] stringByReplacingOccurrencesOfString:@"\r\n" withString:@""];
    
    NSData *base64HeaderJSONData = [[NSData alloc] initWithBase64EncodedString:base64HeaderJSON options:0];
    
    NSDictionary *jsonHeaderObject=[NSJSONSerialization
                              JSONObjectWithData:base64HeaderJSONData
                              options:NSJSONReadingMutableLeaves
                              error:nil];
    
    NSString *algJWT = [jsonHeaderObject objectForKey:@"alg"];
    NSString *base64ClaimJSON ;
    if (![algJWT isEqualToString:@"none"])
    {
        NSString *base64ClaimJSON_signed = [arrBase64JSON objectAtIndex:1];
        base64ClaimJSON = [base64ClaimJSON_signed stringByReplacingOccurrencesOfString:@"\r\n" withString:@""];
    }
    else
    {
        base64ClaimJSON = [[arrBase64JSON objectAtIndex:1] stringByReplacingOccurrencesOfString:@"\r\n" withString:@""];
    }
    
    NSData *base64ClaimJSONData = [[NSData alloc] initWithBase64EncodedString:base64ClaimJSON options:0];
    
    NSDictionary *jsonBodyObject=[NSJSONSerialization
                              JSONObjectWithData:base64ClaimJSONData
                              options:NSJSONReadingMutableLeaves
                              error:nil];
    
    User *user = [[User alloc] init];
    user.name = [jsonBodyObject objectForKey: @"sub"];
    return user ;
}

- (BOOL) isTokenValid {
    NSDate *dateNow = [NSDate date];
    if (dateNow < self.token.expiry_date) {
        return true;
    }
    return false;
}

@end
