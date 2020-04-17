package duckjing.led;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * sp工具类
 */
public class SPUtils {
    private static Context context;

    // 默认显示时长
    private static int time4show = 1;
    // 默认隔几秒
    private static int timeNum   =2;

    public static void setContext(Context c) {
        context = c;
    }

    /**
     * 设置是否打开呼吸灯
     * @param flag
     */
    public static void setIsOpen(boolean flag) {

        SharedPreferences.Editor edit = get();
        edit.putBoolean("led", flag);
        edit.commit();
    }

    /**
     * 获取是否开启呼吸灯
     * @return
     */
    public static boolean getIsOpen(){
        return context.getSharedPreferences("settings", Context.MODE_PRIVATE).getBoolean("led",false);
    }

    private static SharedPreferences.Editor get() {
        SharedPreferences sp = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        return sp.edit();

    }

   public static int getNum(){
       return context.getSharedPreferences("settings", Context.MODE_PRIVATE).getInt("num",timeNum);

   }
    public static int getTime(){
        return context.getSharedPreferences("settings", Context.MODE_PRIVATE).getInt("time",time4show);

    }

    public static void writeDate(String key,int data) {
        SharedPreferences.Editor edit = get();
        edit.putInt(key,data);
        edit.commit();
    }
}
