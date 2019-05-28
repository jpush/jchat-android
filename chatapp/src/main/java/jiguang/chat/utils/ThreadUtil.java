package jiguang.chat.utils;


import android.os.Handler;
import android.os.Looper;

/**
 * Created by ${chenyn} on 2017/3/10.
 */

public class ThreadUtil {
    static Handler mHandler = new Handler(Looper.getMainLooper());

    public static void runInThread(Runnable task) {
        new Thread(task).start();
    }

    public static void runInUiThread(Runnable task) {
        mHandler.post(task);
    }
}
