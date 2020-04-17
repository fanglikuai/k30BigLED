package duckjing.led;

import android.app.Notification;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class MessageGetService extends NotificationListenerService {
    String TAG = "DuckJing";

    static ArrayList<String> list = new ArrayList<>();

    //    通知来了
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        Log.d(TAG, "通知:"+sbn.getPackageName()+"tag:"+sbn.getNotification().extras.getString(Notification.EXTRA_TEXT,"没有文本"));
        if (sbn.isClearable()) {

            if (SPUtils.getIsOpen()) {
                Log.d(TAG, " 可以移除");
//                屏幕不亮的状态
                if (!ShowUtils.getIn().isScreenState()) {
                    list.add(sbn.getPackageName());
                    Log.d("DuckJing", "onNotificationPosted: 开始发送通知");

                    ShowUtils.getIn().showLight(ShowUtils.TYPE_MESSAGE);
                }

            }
        }

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

        Log.d(TAG, "onNotificationRemoved: 通知删除");
        if (list.contains(sbn.getPackageName())) {
            list.remove(sbn.getPackageName());
            Log.d(TAG, "进入: 通知删除");
        }

    }

    public ArrayList<String> getList() {
        return list;
    }


    @Override
    public void onListenerConnected() {
        ShowUtils.getIn().setService(this);
        Log.d(TAG, "onListenerConnected: 绑定服务");
        open();
    }

    private void open() {
/*        ShowUtils.getIn().setContext(getApplicationContext());
        SPUtils.setContext(getApplicationContext());
        ShowUtils.getIn().initScreenState();*/

        SPUtils.setIsOpen(true);
//        startService(new Intent(this, MessageGetService.class));

//          注册广播
        IntentFilter filter = new IntentFilter();
//        充电广播
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
//        锁屏广播
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        ChargeAndLockReceiver receiver = new ChargeAndLockReceiver();
        registerReceiver(receiver, filter);

//            获取当前充电状态
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplication().registerReceiver(null, iFilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        ShowUtils.getIn().setCharge(isCharging);

        Log.d("DuckJing", "open: 充电状态："+isCharging);
//        Toast.makeText(this, "打开成功", Toast.LENGTH_SHORT).show();
            /*PowerManager pm = (PowerManager) getApplication().getSystemService(Context.POWER_SERVICE);
            boolean isScreenOn = pm.isInteractive();//如果为true，则表示屏幕“亮”了，否则屏幕“暗”了。*/


    }


}
