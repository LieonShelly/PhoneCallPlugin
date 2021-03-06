//
//  PhoneCallPlugin.h
//  PhoneCall
//
//  Created by lieon on 2019/3/21.
//  Copyright © 2019 lieon. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CallKit/CallKit.h>
#import <Cordova/CDVPlugin.h>

NS_ASSUME_NONNULL_BEGIN

typedef enum : NSUInteger {
    PhoneCallStatusUnknown = -2,
    PhoneCallStatusError = -1,
    PhoneCallStatuOutgoing = 0,
    PhoneCallStatusHasConnected = 1,
    PhoneCallStatusHasEnded = 2,
    PhoneCallStatusIsOnHold = 3
} PhoneCallStatus;

@interface PhoneCallPlugin : CDVPlugin<CXCallObserverDelegate> // NSObject<CXCallObserverDelegate>
@property (nonatomic, strong) CXCallObserver *callObserver;
@property (nonatomic, copy) void(^callback)(NSInteger dialingTime, NSInteger talkTime);

- (void)test;

- (void)callWithCommand: (CDVInvokedUrlCommand*)command;

- (void)setAppBadgeNumber:(CDVInvokedUrlCommand*)command;

@end

NS_ASSUME_NONNULL_END
