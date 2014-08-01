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

#import <UIKit/UIKit.h>

@interface ViewController : UIViewController <UITextFieldDelegate,UIImagePickerControllerDelegate>

#define REDIRECT_URI @"http://wso2.com"

#define IDP_SERVER_URL_TEMPLATE @"https://%@:%@"

@property (strong, nonatomic) NSString *idp_server;
@property (assign, nonatomic) BOOL isSelfStarted;



-(void) showAuthenticationUI  ;

-(IBAction) click_ProfileImage : (id) sender  ;
-(IBAction) click_Save : (id) sender  ;

-(IBAction) click_Settings : (id) sender  ;
@end
