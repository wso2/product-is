//
//  ViewController.m
//  AZone
//
//  Created by Shanmugarajah on 7/1/14.
//  Copyright (c) 2014 WSO2. All rights reserved.
//

#import "ViewController.h"

#import "IdPProxy.h"
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
    UIAlertView * alert =[[UIAlertView alloc ] initWithTitle:@"OK Dailog"
                                                     message:errorMessage
                                                    delegate:self
                                           cancelButtonTitle:@"Cancel"
                                           otherButtonTitles: nil];
    [alert addButtonWithTitle:@"On Authentication Failure"];
    [alert show];
}
- (void) onTokenAcquiredSuccess : (Token *) token
{
    
}
- (void) onTokenAcquiredFailure : (NSString *) errorMessage
{
    
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    AppDelegate *appDelegate  = [[UIApplication sharedApplication] delegate];
    IdPProxy *idpProxy = [appDelegate wso2Proxy];
    
    [idpProxy initialize : @"nZQREUlViaeVJDb78HF37GWwfYMa"  client_secret: @"BNcJ4hIzG58vCS8RHywCpWe3Wkoa" scope: @"custom" callee: @"eBuy" idp_server:@"http://10.100.0.189:9763"];
    
    idpProxy.delegate = self ;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

- (IBAction)tap:(id)sender {
    AppDelegate *appDelegate  = [[UIApplication sharedApplication] delegate];
    IdPProxy *idpProxy = [appDelegate wso2Proxy];
    [idpProxy authenticateUser];
}


@end
