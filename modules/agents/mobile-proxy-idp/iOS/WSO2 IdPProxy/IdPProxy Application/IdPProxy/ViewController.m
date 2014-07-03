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


@interface ViewController () <UIWebViewDelegate>
@property (weak, nonatomic) IBOutlet UIWebView *webView;
@property (nonatomic) UIImagePickerController *imagePickerController;
@property (weak, nonatomic) IBOutlet UITextField *txtAddress;
@property (weak, nonatomic) IBOutlet UITextField *txtPort;
@property (weak, nonatomic) IBOutlet UIButton *btnSave;
@property (weak, nonatomic) IBOutlet UIButton *btnContinue;
@property (weak, nonatomic) IBOutlet UIButton *btnSettings;

@property (weak, nonatomic) IBOutlet UIButton *imageButton;

@property (weak, nonatomic) IBOutlet UIImageView *imageProfile;

@end

@implementation ViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    self.idp_server = [self getIdPServerAddress];
    self.webView.delegate = self ;
    
    self.isSelfStarted = NO ;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:YES];
}


-(NSString *) getIdPServerAddress
{
    NSString *bundlePathofPlist = [[NSBundle mainBundle]pathForResource:@"Settings" ofType:@"plist"];
    NSDictionary *dict = [NSDictionary dictionaryWithContentsOfFile:bundlePathofPlist];
    NSString* idp_server = [dict valueForKey:@"IdPServer"];
    NSString* port = [dict valueForKey:@"Port"];
    
    NSData* image = [dict valueForKey:@"Image"];
    
    _txtAddress.text = idp_server ;
    _txtPort.text = port ;
    
    NSString *idp_server_url = nil ;
    if (![idp_server isEqualToString:@""] && ![port isEqualToString:@""])
        idp_server_url = [NSString stringWithFormat:IDP_SERVER_URL_TEMPLATE,idp_server,port];
    
    if ([image bytes] > 0)
    {
        self.imageButton.imageView.image = [UIImage imageWithData:image];
    }
    
    
    return idp_server_url ;
}

// Show the authentication screen from the IdP Server
-(void) showAuthenticationUI
{
    self.isSelfStarted = YES ;
    if (self.idp_server != nil)
    {
       self.webView.hidden = FALSE;
       self.btnSettings.hidden = FALSE ;
       self.btnSave.hidden = TRUE ;
        
       // Get the parameters from the dictionary
       AppDelegate *appDelegate = [[UIApplication sharedApplication] delegate] ;
       NSDictionary *dic = [appDelegate dicParameters];
    
       NSString *client_id =[dic objectForKey:@"client_id"] ;
       NSString *scope = [dic objectForKey:@"scope"] ;
    
       NSString *urlTemplate = @"%@/oauth2/authorize?client_id=%@&redirect_uri=%@&response_type=code&scope=openid";
       NSString *urlLink = [NSString stringWithFormat:urlTemplate,self.idp_server,client_id,REDIRECT_URI,scope];
       NSURL *urlAuthorize = [NSURL URLWithString:urlLink];
    
       NSURLRequest *request = [NSURLRequest requestWithURL:urlAuthorize];
       [self.webView loadRequest:request];
    }
}


#pragma mark - UIWebViewDelegate
- (BOOL)webView:(UIWebView *)webView shouldStartLoadWithRequest:(NSURLRequest *)request navigationType:(UIWebViewNavigationType)navigationType
{
    // Check if the URL is the Redirect URI 
    if([[request.URL absoluteString] hasPrefix:REDIRECT_URI])
    {
        // Get the value of the Authorization Code from the Redirect URI query value
        NSDictionary *dicParameters = [self parseQueryString:request.URL.query];
        NSString *auth_token = [dicParameters objectForKey:@"code"];
        
        // Get the reference to the parameters called by the calle application
        AppDelegate *appDelegate = [[UIApplication sharedApplication] delegate] ;
        NSDictionary *dic = [appDelegate dicParameters];
        NSString *server =[dic objectForKey:@"callee"] ;
        
        // Set the status based on the value of Authorization Code
        NSString  *status ;
        if (auth_token != nil)
            status = @"status=1";
        else
            status = @"status=0";
     
        // Pass the Authorization to the calle application
        NSString *urlTemplate = @"%@://sdk?%@&auth_code=%@";
        NSString *urlLink = [NSString stringWithFormat:urlTemplate,server,status,auth_token];
        [[UIApplication sharedApplication] openURL:[NSURL URLWithString:urlLink]];
        return NO;
    }
    else
    {
        return YES ;
    }
}

// Convert the query parameters into Key Value pair
- (NSDictionary *)parseQueryString:(NSString *)query {
    NSMutableDictionary *dict = [[NSMutableDictionary alloc] initWithCapacity:1];
    NSArray *pairs = [query componentsSeparatedByString:@"&"];
    for (NSString *pair in pairs) {
        NSArray *elements = [pair componentsSeparatedByString:@"="];
        NSString *key = [[elements objectAtIndex:0] stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
        NSString *val = [[elements objectAtIndex:1] stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
        [dict setObject:val forKey:key];
    }
    return dict;
}


-(IBAction) click_ProfileImage : (id) sender
{
   [self showImagePickerForSourceType:UIImagePickerControllerSourceTypePhotoLibrary];
}


- (void)showImagePickerForSourceType:(UIImagePickerControllerSourceType)sourceType
{
    UIImagePickerController *imagePicker = [[UIImagePickerController alloc] init];
    imagePicker.delegate = self;
    [self presentViewController:imagePicker animated:YES completion:nil];
}


-(IBAction) click_Save : (id) sender
{
    [self.txtAddress resignFirstResponder];
    [self.txtPort resignFirstResponder];
    
    NSString *bundlePathofPlist = [[NSBundle mainBundle]pathForResource:@"Settings" ofType:@"plist"];
    NSMutableDictionary *dict = [NSMutableDictionary dictionaryWithContentsOfFile:bundlePathofPlist];
    
    if (_txtAddress.text != nil )
    {
       [dict setValue:[_txtAddress text] forKey:@"IdPServer"];
       [dict setValue:[_txtPort text] forKey:@"Port"];
        
       NSData *pngData = UIImagePNGRepresentation(self.imageButton.imageView.image);
       [dict setValue:pngData forKey:@"Image"];
        
       [dict writeToFile:bundlePathofPlist atomically: TRUE];
        
       self.idp_server = [self getIdPServerAddress];
        
       if (self.isSelfStarted == YES)
       {
            self.idp_server = [self getIdPServerAddress];
            self.webView.hidden = FALSE;
            self.btnSettings.hidden = FALSE ;
            self.btnSave.hidden = TRUE ;
            [self showAuthenticationUI];
       }
       else
       {
            UIAlertView * alert =[[UIAlertView alloc ] initWithTitle:@"IdPProxy"
                                                         message:@"Settings saved successfully"
                                                        delegate:self
                                               cancelButtonTitle:@"Cancel"
                                               otherButtonTitles: nil];
            [alert show];
       }
    }
}

-(IBAction) click_Settings : (id) sender
{
    self.webView.hidden = TRUE;
    self.btnSettings.hidden = TRUE ;
    self.btnSave.hidden = FALSE ;
}

#pragma mark - UIImagePickerControllerDelegate


- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingImage:(UIImage *)selectedImage editingInfo:(NSDictionary *)editingInfo {
	
	[self.imageButton setImage:selectedImage forState:UIControlStateNormal];
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker {
    
    [self dismissViewControllerAnimated:YES completion:nil];
}


@end
