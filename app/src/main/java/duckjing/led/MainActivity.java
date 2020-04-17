package duckjing.led;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.BatteryManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "DuckJing";

    private Button                btClose;
    private Button                btOpen;
    private ChargeAndLockReceiver receiver;
    private EditText              edTime;
    private EditText              edNum;
    private Button                bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btOpen = findViewById(R.id.bt_open);
        btClose = findViewById(R.id.bt_close);
        btClose.setOnClickListener(this);
        btOpen.setOnClickListener(this);
//        初始化工具类

        edTime = findViewById(R.id.ed_time);
        edNum = findViewById(R.id.ed_num);
        bt = findViewById(R.id.bt_commit);
        initEd();
        Button btQQ = findViewById(R.id.bt_go_qq);
        btQQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinQQGroup("ldaMP27KHjFrLLmqL-ByIi78L3qxXrJC");
            }
        });


    }

    private void initEd() {
        edTime.setHint("当前显示时长:" + SPUtils.getTime() + "秒");
        edNum.setHint("当前频率时长:" + SPUtils.getNum() + "秒");
        bt.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(edNum.getText().toString()) | TextUtils.isEmpty(edTime.getText().toString())) {
                    ShowUtils.showToast("fuck you");
                    return;
                }

                Log.d("DuckJing", "onClick: num:" + edTime.getText().toString());

                SPUtils.writeDate("time", Integer.valueOf(edTime.getText().toString()));
                SPUtils.writeDate("num", Integer.valueOf(edNum.getText().toString()));

                if (Integer.valueOf(edTime.getText().toString()) >= Integer.valueOf(edNum.getText().toString())) {
                    ShowUtils.showToast("fuck you");
                    edNum.setText("");
                    edTime.setText("");

                    return;
                }
                ShowUtils.showToast("成功");
                edNum.setText("");
                edTime.setText("");

                edTime.setHint("当前显示时长:" + SPUtils.getTime() + "秒");
                edNum.setHint("当前频率时长:" + SPUtils.getNum() + "秒");
            }
        });
    }

    /****************
     *
     * 发起添加群流程。群号：Bug灯-K30(1061421764) 的 key 为： ldaMP27KHjFrLLmqL-ByIi78L3qxXrJC
     * 调用 joinQQGroup(ldaMP27KHjFrLLmqL-ByIi78L3qxXrJC) 即可发起手Q客户端申请加群 Bug灯-K30(1061421764)
     *
     * @param key 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回fals表示呼起失败
     ******************/
    public boolean joinQQGroup(String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            return false;
        }
    }

    @Override
    public void onClick(View view) {

//        如果没获得权限 就GG
        if (!isEnabledNotification()) {
            ShowUtils.showToast("你在等待爱情?");
            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
            return;
        }
//打开
        if (view == btOpen) {
            open();
        } else {
            SPUtils.setIsOpen(false);
            ShowUtils.getIn().setLight(0);//关闭
            if (receiver != null) {
                unregisterReceiver(receiver);
            }
            ShowUtils.showToast("关闭成功");
        }
    }

    private void open() {

        SPUtils.setIsOpen(true);

//          注册广播
        IntentFilter filter = new IntentFilter();
//        充电广播
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
//        锁屏广播
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        receiver = new ChargeAndLockReceiver();
        registerReceiver(receiver, filter);

//            获取当前充电状态
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplication().registerReceiver(null, iFilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        ShowUtils.getIn().setCharge(isCharging);

        ShowUtils.showToast("打开成功");
            /*PowerManager pm = (PowerManager) getApplication().getSystemService(Context.POWER_SERVICE);
            boolean isScreenOn = pm.isInteractive();//如果为true，则表示屏幕“亮”了，否则屏幕“暗”了。*/


    }


    /**
     * 判断是否打开了通知监听权限
     *
     * @return 感谢dalao
     */
    private boolean isEnabledNotification() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;

    }


}
