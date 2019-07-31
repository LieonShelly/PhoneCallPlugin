package phone.call.plugin;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * 来去电监听服务
 */

public class PhoneListenService extends Service {

    public static final String TAG = "test123";

    public static final String ACTION_REGISTER_LISTENER = "action_register_listener";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand action: " + intent.getAction() + " flags: " + flags + " startId: " + startId);
        PhoneStateReceiver phoneStateReceiver = new PhoneStateReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_REGISTER_LISTENER);
        registerReceiver(phoneStateReceiver, intentFilter);
        CustomPhoneStateListener customPhoneStateListener = (CustomPhoneStateListener) intent.getSerializableExtra("listener");
        String action = intent.getAction();
        if (action.equals(ACTION_REGISTER_LISTENER) && customPhoneStateListener != null) {
            registerPhoneStateListener(customPhoneStateListener);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void registerPhoneStateListener(CustomPhoneStateListener customPhoneStateListener) {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            telephonyManager.listen(customPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind action: " + intent.getAction());
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind action: " + intent.getAction());
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind action: " + intent.getAction());
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }
}
