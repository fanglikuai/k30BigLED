package duckjing.led;

import android.app.Application;
import android.util.Log;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ShowUtils.getIn().setContext(getApplicationContext());
        SPUtils.setContext(getApplicationContext());
        ShowUtils.getIn().initScreenState();

        Log.d("DuckJing", "onCreate: 初始化");
    }
}
