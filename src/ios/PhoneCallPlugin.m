//
//  PhoneCallPlugin.m
//  PhoneCall
//
//  Created by lieon on 2019/3/21.
//  Copyright © 2019 lieon. All rights reserved.
//

#import "PhoneCallPlugin.h"
#import <UIKit/UIKit.h>

@interface PhoneCallPlugin()
{
    NSInteger fireTime;
    NSInteger connectTime;
    NSInteger endTime;
    NSInteger  onholdTime;
    NSInteger connectTimeTotal;
    NSInteger currentTime;
}
@end

@implementation PhoneCallPlugin


- (void)setAppBadgeNumber:(CDVInvokedUrlCommand*)command {
    NSInteger value = [command.arguments objectAtIndex:0];
    [[UIApplication sharedApplication] setApplicationIconBadgeNumber: (NSInteger)value];
}


- (void)callWithCommand: (CDVInvokedUrlCommand*)command  {
    self.callObserver = [[CXCallObserver alloc]init];
    [self.callObserver setDelegate:self queue:dispatch_get_main_queue()];
    NSString* number = [command.arguments objectAtIndex:0];
    [[UIApplication sharedApplication]openURL:[NSURL URLWithString:[NSString stringWithFormat:@"tel://%@", number]] options:@{} completionHandler:^(BOOL success) {
        NSLog(@"completionHandler-success");
        
    }];
    self.callback = ^(NSInteger dialingTime, NSInteger talkTime) {
        NSLog(@"dialingTime: %ld--talkTime:%ld",dialingTime, talkTime);
        NSString * message = [NSString  stringWithFormat:@"%ld-%ld",dialingTime, talkTime];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:message];
        [self.commandDelegate sendPluginResult:pluginResult callbackId: command.callbackId];
    };
}


- (void)test {
    self.callObserver = [[CXCallObserver alloc]init];
    [self.callObserver setDelegate:self queue:dispatch_get_main_queue()];
    [[UIApplication sharedApplication]openURL:[NSURL URLWithString:[NSString stringWithFormat:@"tel://%s", "15608066283"]] options:@{} completionHandler:^(BOOL success) {
        NSLog(@"completionHandler-success");
        
    }];
    self.callback = ^(NSInteger dialingTime, NSInteger talkTime) {
        NSLog(@"dialingTime: %ld--talkTime:%ld",dialingTime, talkTime);
        NSString * message = [NSString  stringWithFormat:@"%ld-%ld",dialingTime, talkTime];
        //        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:message];
        //        [self.commandDelegate sendPluginResult:pluginResult callbackId: command.callbackId];
    };
}


- (void)callObserver:(CXCallObserver *)callObserver callChanged:(CXCall *)call {
    
    if (call.isOutgoing) {
        if(fireTime == 0) {
            fireTime = [NSDate new].timeIntervalSince1970;
        }
        NSLog(@"电话播出: %ld", fireTime);
        if (call.hasConnected) {
            if (connectTime == 0) {
                connectTime = [NSDate new].timeIntervalSince1970;
            }
            connectTimeTotal = connectTime - fireTime;
            NSLog(@"电话接通的时间点: %ld", connectTime);
            NSLog(@"播出---->接通的时间段: %ld", connectTimeTotal);
        }
        if (call.hasEnded) {
            if (endTime == 0) {
                 endTime = [NSDate new].timeIntervalSince1970;
            }
            if (connectTime > 0) {
                NSInteger endTimeTotal = endTime - connectTime;
                NSLog(@"电话挂断: %ld", endTime);
                NSLog(@"接通---->挂断的时间段: %ld", endTimeTotal);
                self.callback(connectTimeTotal, endTimeTotal);
            } else {
                NSInteger endTimeTotal = endTime - fireTime;
                NSLog(@"电话挂断: %ld", endTime);
                NSLog(@"接通---->挂断的时间段: %ld", endTimeTotal);
                self.callback(endTimeTotal, 0);
            }
            endTime = 0;
            connectTime = 0;
            fireTime = 0;
            currentTime = 0;
        }
        if (call.isOnHold) {
            if (onholdTime == 0) {
                 onholdTime = [NSDate new].timeIntervalSince1970;
            }
            NSInteger onholdTotal = onholdTime - fireTime;
            NSLog(@"无人接听挂断: %ld", onholdTime);
            NSLog(@"播出 --> 无人接听挂断的时间段: %ld", onholdTotal);
            endTime = 0;
            connectTime = 0;
            fireTime = 0;
            currentTime = 0;
            self.callback(onholdTotal, 0);
        }
    } else {
        endTime = 0;
        connectTime = 0;
        fireTime = 0;
        currentTime = 0;
        NSLog(@"电话error");
        self.callback(0, 0);
    }
}


- (void)dealloc {
    NSLog(@"--PhoneCallPlugin--dealloc----");
}
@end
