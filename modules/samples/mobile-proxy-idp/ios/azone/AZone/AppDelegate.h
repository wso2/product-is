//
//  AppDelegate.h
//  AZone
//
//  Created by Shanmugarajah on 7/1/14.
//  Copyright (c) 2014 WSO2. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "IdPProxy.h"

@interface AppDelegate : UIResponder <UIApplicationDelegate>

@property (strong, nonatomic) UIWindow *window;

@property (strong, nonatomic) IdPProxy *wso2Proxy ;

@end
