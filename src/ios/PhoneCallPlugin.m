//
//  PhoneCallPlugin.m
//  PhoneCall
//
//  Created by lieon on 2019/3/21.
//  Copyright © 2019 lieon. All rights reserved.
//

#import "PhoneCallPlugin.h"
#import <UIKit/UIKit.h>

@implementation PhoneCallPlugin

- (instancetype)init {
    if (self = [super init]) {
        self.callObserver = [[CXCallObserver alloc]init];
        [self.callObserver setDelegate:self queue:dispatch_get_main_queue()];
    }
    return self;
}

- (void)callWithCommand: (CDVInvokedUrlCommand*)command  {
    [self.commandDelegate runInBackground:^{
        
        CDVPluginResult* pluginResult = nil;
        NSString * number = @"10010";
        [[UIApplication sharedApplication]openURL:[NSURL URLWithString:[NSString stringWithFormat:@"tel://%@", number]] options:@{} completionHandler:^(BOOL success) {
            
        }];
        self.callback = ^(PhoneCallStatus status) {
            NSLog(@"%ld",status);
            if (status == -1 || status == -2 ){
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[NSString stringWithFormat:@"%ld", status]];
            } else {
                 pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:[NSString stringWithFormat:@"%ld", status]];
            }
             [self.commandDelegate sendPluginResult:pluginResult callbackId: command.callbackId];
        };
    }];
    
  
}

- (void)callObserver:(CXCallObserver *)callObserver callChanged:(CXCall *)call {
    PhoneCallStatus status = PhoneCallStatusUnknown;
    if (call.isOutgoing) {
        status = PhoneCallStatuOutgoing;
        NSLog(@"电话播出");
        if (call.hasConnected) {
            status = PhoneCallStatusHasConnected;
            NSLog(@"电话接通");
        }
        if (call.hasEnded) {
            status = PhoneCallStatusHasEnded;
            NSLog(@"电话挂断");
        }
        if (call.isOnHold) {
            status = PhoneCallStatusIsOnHold;
            NSLog(@"无人接听挂断");
        }
    } else {
        status = PhoneCallStatusError;
        NSLog(@"电话error");
    }
    self.callback(status);
}
@end
