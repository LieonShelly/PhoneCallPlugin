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
}
@end

@implementation PhoneCallPlugin

- (void)callWithCommand: (CDVInvokedUrlCommand*)command  {
    self.callObserver = [[CXCallObserver alloc]init];
    self.timer = [NSTimer timerWithTimeInterval:1 target:self selector:@selector(timerAction) userInfo:nil repeats:true];
    [[NSRunLoop currentRunLoop]addTimer:self.timer forMode:NSRunLoopCommonModes];
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

- (void)timerAction {
    self.currentTime = self.currentTime + 1;
    NSLog(@"%ld", self.currentTime);
}

- (void)callObserver:(CXCallObserver *)callObserver callChanged:(CXCall *)call {
    
    if (call.isOutgoing) {
        if (fireTime == 0) {
            fireTime = self.currentTime;
        }
        NSLog(@"电话播出: %ld", fireTime);
        if (call.hasConnected) {
            if (connectTime == 0) {
                connectTime = self.currentTime;
            }
            connectTimeTotal = connectTime - fireTime;
            NSLog(@"电话接通的时间点: %ld", connectTime);
            NSLog(@"播出---->接通的时间段: %ld", connectTimeTotal);
        }
        if (call.hasEnded) {
            if (endTime == 0) {
                 endTime = self.currentTime;
            }
            NSInteger endTimeTotal = endTime - connectTime;
            NSLog(@"电话挂断: %ld", endTime);
            NSLog(@"接通---->挂断的时间段: %ld", endTimeTotal);
            [self.timer invalidate];
            endTime = 0;
            connectTime = 0;
            fireTime = 0;
            self.currentTime = 0;
            self.callback(connectTimeTotal, endTimeTotal);
        }
        if (call.isOnHold) {
            if (onholdTime == 0) {
                 onholdTime = self.currentTime;
            }
            NSInteger onholdTotal = onholdTime - fireTime;
            NSLog(@"无人接听挂断: %ld", onholdTime);
            NSLog(@"播出 --> 无人接听挂断的时间段: %ld", onholdTotal);
            [self.timer invalidate];
            endTime = 0;
            connectTime = 0;
            fireTime = 0;
            self.currentTime = 0;
            self.callback(onholdTotal, 0);
        }
    } else {
        [self.timer invalidate];
        endTime = 0;
        connectTime = 0;
        fireTime = 0;
        self.currentTime = 0;
        NSLog(@"电话error");
    }
}


- (void)dealloc {
    NSLog(@"--PhoneCallPlugin--dealloc----");
}
@end
