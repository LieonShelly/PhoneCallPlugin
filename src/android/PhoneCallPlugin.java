package phone.call.plugin;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

// import org.apache.cordova.BuildHelper;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
// import org.apache.cordova.CordovaResourceApi;
import android.util.Log;

import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;


import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

import java.util.List;

public class PhoneCallPlugin extends CordovaPlugin {
    public static final int CALL_REQ_CODE = 0;
    public static final int PERMISSION_DENIED_ERROR = 20;
    public static String[] PERMISSION = new String[]{Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE};
//    public static final String CALL_PHONE = Manifest.permission.CALL_PHONE;

    private CallbackContext callbackContext;        // The callback context from which we were invoked.
    private JSONArray executeArgs;
    private CustomPhoneStateListener listener;

    protected void getCallPermission(int requestCode) {
        cordova.requestPermissions(this, requestCode, PERMISSION);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        this.executeArgs = args;

        if (action.equals("callWithCommand")) {
            if (cordova.hasPermission(PERMISSION[0]) && cordova.hasPermission(PERMISSION[1])) {
                callPhone(executeArgs);
            } else {
                getCallPermission(CALL_REQ_CODE);
            }
        } else if (action.equals("isCallSupported")) {
            this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, isTelephonyEnabled()));
        } else {
            return false;
        }

        return true;
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR));
                return;
            }
        }
        switch (requestCode) {
            case CALL_REQ_CODE:
                callPhone(executeArgs);
                break;
        }
    }

    private void callPhone(JSONArray args) throws JSONException {
        String number = args.getString(0);

        if (!number.startsWith("tel:")) {
            number = String.format("tel:%s", number);
        }
        try {
            Intent intent = new Intent(isTelephonyEnabled() ? Intent.ACTION_CALL : Intent.ACTION_VIEW);
            PhoneCallTimeListener callback = new PhoneCallTimeListener() {
                @Override
                public void dialingTime(String time) {
                    Log.d("call---->", time);
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, time));
                    listener.stopListener();
                }

                @Override
                public void talkingTime(String time) {
                    Log.d("call----->", time);
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, time));
                    listener.stopListener();
                }
            };

            listener = new CustomPhoneStateListener(cordova.getActivity(), number.substring(4), callback);
            listener.startListener();
            intent.setData(Uri.parse(number));
            intent.setPackage(getDialerPackage(intent));
            cordova.getActivity().startActivity(intent);

        } catch (Exception e) {

            callbackContext.error("CouldNotCallPhoneNumber" + e);
        }
    }


    private boolean isTelephonyEnabled() {
        TelephonyManager tm = (TelephonyManager) cordova.getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        return tm != null && tm.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE;
    }

    private String getDialerPackage(Intent intent) {
        PackageManager packageManager = (PackageManager) cordova.getActivity().getPackageManager();
        List activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        for (int i = 0; i < activities.size(); i++) {
            if (activities.get(i).toString().toLowerCase().contains("com.android.server.telecom")) {
                return "com.android.server.telecom";
            }
            if (activities.get(i).toString().toLowerCase().contains("com.android.phone")) {
                return "com.android.phone";
            } else if (activities.get(i).toString().toLowerCase().contains("call")) {
                return activities.get(i).toString().split("[ ]")[1].split("[/]")[0];
            }
        }
        return "";
    }
}
