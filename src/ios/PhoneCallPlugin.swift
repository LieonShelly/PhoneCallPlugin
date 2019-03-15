//
//  HanggeSwiftPlugin.swift
//  HelloWorld
//
//  Created by hangge on 16/4/19.
//
//

import Foundation
import CallKit

@objc(PhoneCallPlugin) class PhoneCallPlugin : CDVPlugin, CXCallObserverDelegate {
    
    enum PhoneCallStatus: Int {
        case outgoing = 0
        case hasConnected = 1
        case hasEnded = 2
        case isOnHold = 3
        case error = -1
        case unknown = -2
    }
    fileprivate var callObserver = CXCallObserver()
    var statusCallback: ((Int) -> Void)?
    
    override init() {
        super.init()
        callObserver.setDelegate(self, queue: DispatchQueue.main)
    }
    
    func call(_ command: CDVInvokedUrlCommand) {
        let number = command.arguments[0] as? String
        UIApplication.shared.open(URL(string: "tel://\(number!)")!, options: [:], completionHandler: nil)
        statusCallback = { status in
            var pluginResult:CDVPluginResult?
            if status == -1 || status == -2 {
                pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "\(status)")
            } else {
                pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "\(status)")
            }
            self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        }
    }
    
    func callObserver(_ callObserver: CXCallObserver, callChanged call: CXCall) {
        var phoneCallStatus = PhoneCallStatus.unknown
        if call.isOutgoing {
            phoneCallStatus = .outgoing
            print("电话播出")
            
            if call.hasConnected {
                print("电话接通")
                phoneCallStatus = .hasConnected
            }
            if call.hasEnded {
                print("电话挂断")
                phoneCallStatus = .hasEnded
            }
            if call.isOnHold {
                print("无人接听挂断")
                phoneCallStatus = .isOnHold
            }
        } else {
            print("other error")
            phoneCallStatus = .error
        }
        statusCallback?(phoneCallStatus.rawValue)
    }
    
}
