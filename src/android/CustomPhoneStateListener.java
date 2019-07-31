package phone.call.plugin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

// import org.apache.cordova.BuildHelper;
import org.apache.cordova.CallbackContext;
// import org.apache.cordova.CordovaPlugin;
// import org.apache.cordova.CordovaResourceApi;
// import org.apache.cordova.LOG;
// import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 来去电监听
 */


public class CustomPhoneStateListener extends PhoneStateListener implements Serializable {

    private long preTime = 0;
    private long dialingTime = 0;

    private int preState = 0;
    private boolean isListening = false; //是否正在回调
    private TelephonyManager mTelephonyManager;

    private PhoneCallTimeListener mTimeListener;

    private long callPhoneTime = 0;

    private Activity mActivity;
	
	private String mPhone = "";


    public CustomPhoneStateListener(Activity activity, String phone, PhoneCallTimeListener timeListener) {
		mActivity = activity;
		mPhone = phone;
        mTelephonyManager = (TelephonyManager) mActivity.getSystemService(activity.TELEPHONY_SERVICE);
        mTimeListener = timeListener;
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        Log.d(PhoneListenService.TAG, "CustomPhoneStateListener state: " + state + " incomingNumber: " + incomingNumber);
        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:      // 电话挂断
                long time = System.currentTimeMillis() - callPhoneTime;
                if (preState == TelephonyManager.CALL_STATE_RINGING) {
                    Log.i(PhoneListenService.TAG, "onCallStateChanged: 响铃时长（未接通挂断）=" + time / 1000 + "s");
                    String str = "{\"totalTime\":" + (time / 1000) + ",\"callTime\":" + getCallTime(time) + "}";
                    this.mTimeListener.dialingTime(str);
                } else if (preState == TelephonyManager.CALL_STATE_OFFHOOK) {
                    Log.i(PhoneListenService.TAG, "onCallStateChanged: 通话总时长=" + time / 1000 + "s");
                    if (dialingTime > 1000) {
                        dialingTime = 0;
                    }
                    String str = "{\"totalTime\":" + (time / 1000) + ",\"callTime\":" + getCallTime(time) + "}";
                    this.mTimeListener.talkingTime(str);
                    getCallTime(time);
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
                dialingTime = (offhookTime - preTime) / 1000;
                preTime = offhookTime;
                Log.i(PhoneListenService.TAG, "onCallStateChanged: 电话接通");
                break;
        }
    }

    /**
     * 启动监听
     *
     * @return
     */
    public boolean startListener() {
		callPhoneTime = System.currentTimeMillis();
        if (isListening) {
            return false;
        }
        isListening = true;
        mTelephonyManager.listen(this, PhoneStateListener.LISTEN_CALL_STATE);
        return true;
    }

    /**
     * 结束监听
     *
     * @return
     */
    public boolean stopListener() {
        if (!isListening) {
            return false;
        }
        isListening = false;
        mTelephonyManager.listen(this, PhoneStateListener.LISTEN_NONE);
        return true;
    }
	
	private String[] columns = {CallLog.Calls.NUMBER// 通话记录的电话号码
            , CallLog.Calls.DATE// 通话记录的日期
            , CallLog.Calls.DURATION// 通话时长
            , CallLog.Calls.TYPE};// 通话类型}

    private int getCallTime(long totalTime) {
        if (Build.VERSION.SDK_INT >= 23) {
            //1. 检测是否添加权限   PERMISSION_GRANTED  表示已经授权并可以使用
            if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                //手机为Android6.0的版本,未授权则动态请求授权
                //2. 申请请求授权权限
                //1. Activity
                // 2. 申请的权限名称
                // 3. 申请权限的 请求码
                ActivityCompat.requestPermissions(mActivity, new String[]
                        {Manifest.permission.READ_CALL_LOG//通话记录
                        }, 1005);
            } else {//手机为Android6.0的版本,权限已授权可以使用
                // 执行下一步
                initContacts(totalTime);
            }
        } else {//手机为Android6.0以前的版本，可以使用
            initContacts(totalTime);
        }
        return 0;
    }

    private void initContacts(long totalTime) {
        // 1.获得ContentResolver
        ContentResolver resolver = mActivity.getContentResolver();

        // 2.利用ContentResolver的query方法查询通话记录数据库
        /*
         * @param uri 需要查询的URI，（这个URI是ContentProvider提供的）
         * @param projection 需要查询的字段
         * @param selection sql语句where之后的语句
         * @param selectionArgs ?占位符代表的数据
         * @param sortOrder 排序方式
         */
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            // 按照时间逆序排列，最近打的最先显示
            Cursor cursor = resolver.query(CallLog.Calls.CONTENT_URI, columns, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);
            // 3.通过Cursor获得数据
            boolean flag = true;
            while (cursor.moveToNext() && flag) {
                int duration = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION));
                String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                long dateLong = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));

                int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
                @SuppressLint("SimpleDateFormat") String dayCurrent = new SimpleDateFormat("dd").format(new Date());
                @SuppressLint("SimpleDateFormat") String dayRecord = new SimpleDateFormat("dd").format(new Date(dateLong));

                if(mPhone".equals(number) && type == CallLog.Calls.OUTGOING_TYPE && Integer.parseInt(dayCurrent) == Integer.parseInt(dayRecord)){
                    flag = false;
                    Toast.makeText(mActivity, "通话总时长=" + totalTime / 1000 + "s  通话时长= " + duration, Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
