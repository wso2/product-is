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

#import <Foundation/Foundation.h>
#import "User.h"
#import "Token.h"
#import "IdPProxyDelegate.h"

#define IDPPROXY_URL @"WSO2IdPProxy://idp?client_id=%@&idp_server=%@&callee=%@"
#define IDPPROXY_APP @"org.wso2.IdPProxy"

#define IDPPROXY_ERROR @"IdPProxy Server Error"
#define REDIRECT_URI @"http://wso2.com"

#define OAUTH_URL @"%@/oauth2/token"
#define OAUTH_REQUEST @"grant_type=authorization_code&scope=openid&code=%@&redirect_uri=%@"

@interface IdPProxy : NSObject

@property (strong, nonatomic) NSString *client_id;
@property (strong, nonatomic) NSString *client_secret;
@property (strong, nonatomic) NSString *scope;
@property (strong, nonatomic) NSString *callee;
@property (strong, nonatomic) NSString *auth_code;
@property (strong, nonatomic) Token *token;
@property (strong, nonatomic) NSString *idp_server;


@property (weak, nonatomic) id<IdPProxyDelegate> delegate;

- (void) initialize : (NSString *) client_id client_secret: (NSString *) client_secret scope: (NSString *) scope callee: (NSString *) callee idp_server:(NSString *) idp_server;
- (BOOL) authenticateUser;
- (void) receivedFromIdPProxy : (NSString *) data sourceApplication: (NSString *) app ;
- (BOOL) isTokenValid;

@end
