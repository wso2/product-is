//
//  ViewController.h
//  AZone
//
//  Created by Shanmugarajah on 7/1/14.
//  Copyright (c) 2014 WSO2. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "IdPProxy.h"

@interface ViewController : UIViewController <IdPProxyDelegate>
@property (weak, nonatomic) IBOutlet UIButton *buttonLogin;
- (IBAction)tap:(id)sender;
@end
