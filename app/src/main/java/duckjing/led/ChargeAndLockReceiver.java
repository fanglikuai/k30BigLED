package duckjing.led;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;

public class ChargeAndLockReceiver extends BroadcastReceiver {

    private static final String TAG = "DuckJing";

    @Override
    public void onReceive(Context context, Intent intent) {
//        判断是否打开
        if (SPUtils.getIsOpen()) {
            String action = intent.getAction();
            switch (action) {
                case Intent.ACTION_POWER_CONNECTED://接通电源
                    ShowUtils.getIn().setCharge(true);
                    break;
                case Intent.ACTION_POWER_DISCONNECTED://拔出电源
                    ShowUtils.getIn().setCharge(false);
                    break;

      /*      case Intent.ACTION_SCREEN_ON:
                Log.d(TAG, "onReceive: ACTION_SCREEN_ON");
                ShowUtils.getIn().setScreenState(true);
//                Log.d("DuckJing", "onReceive: shoudao");
                break;*/

                case Intent.ACTION_USER_PRESENT:
//         解锁
                    Log.d(TAG, "onReceive: ACTION_USER_PRESENT");
                    ShowUtils.getIn().setScreenState(true);
//                Log.d("DuckJing", "onReceive: shoudao");
                    break;

                case Intent.ACTION_SCREEN_OFF:
//                似乎不同
                    Log.d(TAG, "onReceive: ACTION_SCREEN_OFF");
                    ShowUtils.getIn().setScreenState(false);
//                ShowUtils.getIn(context).setScreenState(false);
                    break;


            }
        }
    }

}
