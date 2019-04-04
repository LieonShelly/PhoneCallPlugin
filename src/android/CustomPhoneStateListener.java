package com.netson.safecampus.facedemo.detection;

import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 来去电监听
 */

public class CustomPhoneStateListener extends PhoneStateListener implements Serializable {
    private CallbackContext callbackContext;

    private long preTime = 0;

    private int preState = 0;

    public CustomPhoneStateListener(CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
    }

    @Override
    public void onServiceStateChanged(ServiceState serviceState) {
        super.onServiceStateChanged(serviceState);
        Log.d(PhoneListenService.TAG, "CustomPhoneStateListener onServiceStateChanged: " + serviceState);
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        Log.d(PhoneListenService.TAG, "CustomPhoneStateListener state: " + state + " incomingNumber: " + incomingNumber);
        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:      // 电话挂断

                long time = System.currentTimeMillis() - preTime;
                if (preState == TelephonyManager.CALL_STATE_RINGING) {
                    Log.i(PhoneListenService.TAG, "onCallStateChanged: 响铃时长（未接通挂断）=" + time / 1000 + "s");
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, "dialingTime" + time / 1000));
                } else if (preState == TelephonyManager.CALL_STATE_OFFHOOK) {
                    Log.i(PhoneListenService.TAG, "onCallStateChanged: 通话时长=" + time / 1000 + "s");
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, "talkTime" + time / 1000));
                }
                break;
            case TelephonyManager.CALL_STATE_RINGING:   // 电话响铃
                preTime = System.currentTimeMillis();
                preState = TelephonyManager.CALL_STATE_RINGING;
                Date date2 = new Date(System.currentTimeMillis());
                SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Log.i(PhoneListenService.TAG, "onCallStateChanged: 当前时间开始响铃" + simpleDateFormat2.format(date2));
                Log.i(PhoneListenService.TAG, "onCallStateChanged: 电话响铃");
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:    // 来电接通 或者 去电，去电接通  但是没法区分
                long offhookTime = System.currentTimeMillis();
                Log.i(PhoneListenService.TAG, "onCallStateChanged: 当前响铃到接通花费时间" + (offhookTime - preTime) / 1000 + "s");
                preState = TelephonyManager.CALL_STATE_OFFHOOK;
                preTime = offhookTime;
                Log.i(PhoneListenService.TAG, "onCallStateChanged: 电话接通");
                break;
        }
    }
}
