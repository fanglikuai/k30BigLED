package duckjing.led;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 显示呼吸灯
 */
public class ShowUtils {

    private DataOutputStream dos;
    private ExecutorService  pool;//线程池

    private static ShowUtils utils;

    private boolean isCharge = false;//是否正在充电


    private String TAG = "DuckJing";

    public static  int TYPE_MESSAGE = 1;//类型为通知
    private static int TYPE_CHARGE  = 2;//类型为充电


    private static Context c;
    /**
     * 呼吸灯的亮度
     */
    private int type_Led_max = 255;
    /**
     * 呼吸灯最小
     */
    private int type_led_min = 0;


    public boolean isScreenState() {
        return screenState;
    }

    private boolean                  screenState;//屏幕状态
    private ScheduledExecutorService service;//定时器
    private MessageGetService        se;//获取通知列表


    public static ShowUtils getIn() {
        if (utils == null) {
            utils = new ShowUtils();

        }
        return utils;
    }

    public void setContext(Context c) {
        ShowUtils.c = c;
    }

    private ShowUtils() {
        Process p = null;
        try {
            p = Runtime.getRuntime().exec("su");
        } catch (IOException ignored) {
        }
        dos = new DataOutputStream(p.getOutputStream());
        //线程池
        pool = Executors.newSingleThreadExecutor();

        // ScheduledExecutorService:是从Java SE5的java.util.concurrent里，
        // 做为并发工具类被引进的，这是最理想的定时任务实现方式。
        service = Executors.newSingleThreadScheduledExecutor();


    }

    public synchronized void showLight(int type) {
        if (type == TYPE_CHARGE) {
//            充电 常亮 不管是否息屏
            chargeTodo();
        } else if (type == TYPE_MESSAGE) {
//            通知来了
            Log.d(TAG, "showLight: 通知（总）");

            if (!screenState) {
                if (!isWhileShow) {
                    Log.d(TAG, "showLight:   屏幕关闭 闪！！！");
                    whileLed();
                }
            }

        }
    }

    /**
     * 设置是否在充电
     *
     * @param isCharge 是否
     */
    public void setCharge(boolean isCharge) {
        this.isCharge = isCharge;
//        设置完之后 看要不要亮
        showLight(TYPE_CHARGE);
    }


    /**
     * 充电状态下的操作
     */
    private synchronized void chargeTodo() {

        if (isCharge) {
//            正在充电
            Log.d(TAG, "showLight: 充电");
            setLight(type_Led_max);
        } else {
            Log.d(TAG, "showLight: 这里调用？");
            setLight(type_led_min);
        }
    }


    /**
     * 设置是否息屏
     *
     * @param screenState true为显示 false为息屏
     */
    public synchronized void setScreenState(boolean screenState) {
        Log.d(TAG, "setScreenState: " + screenState);
        this.screenState = screenState;

        if (screenState) {
//            解锁后 清除通知 防止息屏再闪烁
            se.getList().clear();

//            显示屏幕的
            if (isWhileShow) {
                //            任务没结束 表示在闪烁
                Log.d(TAG, "setScreenState:    任务没结束 表示在闪烁::" + !service.isTerminated());
                service.shutdown();
                if (service.isShutdown()) {

                    service = Executors.newSingleThreadScheduledExecutor();

                    isWhileShow = false;
                    chargeTodo();
                }

            }


        } else {
//            关闭屏幕的
            if (se == null) {
                return;
            }
            Log.d(TAG, "setScreenDead: 调用关闭屏幕");

            if (se.getList().size() > 0) {
//                如果没在闪 就闪？
                if (!isWhileShow) {
                    Log.d(TAG, "setScreenDead: size:" + se.getList().size());
                    whileLed();
                }
            }

        }
    }

    /**
     * 是否正在循环
     */
    private static boolean isWhileShow = false;

    /**
     * 闪烁
     */
    private synchronized void whileLed() {
        // 第二个参数为首次执行的延时时间，第三个参数为定时执行的间隔时间
        // 10：秒   5：秒
        // 第一次执行的时间为10秒，然后每隔五秒执行一次
      final int time = SPUtils.getTime() * 1000;
        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                setLight(type_Led_max);
                SystemClock.sleep(time);
                if (isWhileShow) {
                    setLight(type_led_min);
                }
            }
        }, 0, SPUtils.getNum(), TimeUnit.SECONDS);

        isWhileShow = true;

    }


    public void setService(MessageGetService service) {
        this.se = service;
    }

    /**
     * 显示封装
     *
     * @param i type_Led_max 亮  type_led_min 关
     */
    public synchronized void setLight(final int i) {

        Log.i(TAG, "setLight: " + i);
        pool.execute(new Runnable() {
            @Override
            public void run() {
                String cmd = "echo " + i + " > /sys/class/leds/white/brightness";
                try {
                    dos.writeBytes(cmd + "\n");
                    dos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * 初始化是否息屏
     */
    public void initScreenState() {
        KeyguardManager mKeyguardManager = (KeyguardManager) c.getSystemService(Context.KEYGUARD_SERVICE);
//        boolean isScreenOn = mKeyguardManager.inKeyguardRestrictedInputMode();
//        判断当前是否是锁屏状态
        screenState = !mKeyguardManager.inKeyguardRestrictedInputMode();


    }

    /**
     * Toast显示
     */
    private static Toast showT;

    public static void showToast(String content, int length) {

        if (showT == null) {
            showT = Toast.makeText(c, "", length);
        }
        showT.setText(content);
        showT.show();
    }

    public static void showToast(String content) {
        showToast(content, Toast.LENGTH_SHORT);
    }
}














