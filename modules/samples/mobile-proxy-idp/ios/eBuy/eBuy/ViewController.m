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

#import "ViewController.h"
#import "AppDelegate.h"

@interface ViewController ()

@end

@implementation ViewController


- (void) onAuthenticationSuccess : (User *) user
{
    [self.buttonLogin setTitle:user.name forState:UIControlStateNormal];
    
}

- (void) onAuthenticationFailure : (NSString *) errorMessage
{
    UIAlertView * alert =[[UIAlertView alloc ] initWithTitle:@"eBuy"
                                                     message:errorMessage
                                                    delegate:self
                                           cancelButtonTitle:@"Cancel"
                                           otherButtonTitles: nil];
    [alert show];
}


- (void)viewDidLoad
{
    [super viewDidLoad];
    AppDelegate *appDelegate  = [[UIApplication sharedApplication] delegate];
    IdPProxy *idpProxy = [appDelegate wso2Proxy];
   
    [idpProxy initialize : @"nZQREUlViaeVJDb78HF37GWwfYMa"  client_secret: @"BNcJ4hIzG58vCS8RHywCpWe3Wkoa" scope: @"custom" callee: @"eBuy" idp_server:@"http://10.100.0.189:9763"];
    
    idpProxy.delegate = self ;
}



- (IBAction)click_LoginButton:(id)sender {
    AppDelegate *appDelegate  = [[UIApplication sharedApplication] delegate];
    IdPProxy *idpProxy = [appDelegate wso2Proxy];
    [idpProxy authenticateUser];
}


- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
