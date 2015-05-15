package cn.jpush.im.android.demo.application;

import android.app.Application;
import android.util.Log;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.demo.receiver.NotificationClickEventReceiver;

public class JPushDemoApplication extends Application {

    public static final int REQUESTCODE_TAKE_PHOTO = 4;
    public static final int REQUESTCODE_SELECT_PICTURE = 6;
    public static final int RESULTCODE_SELECT_PICTURE = 8;
    public static final int UPDATE_CHAT_LIST_VIEW = 10;
    public static final int REFRESH_GROUP_NAME = 3000;
    public static final int ON_GROUP_EVENT = 3004;

    public static String UPDATE_GROUP_NAME_ACTION = "cn.jpush.im.demo.activity.ACTION_UPDATE_GROUP_NAME";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("JpushDemoApplication", "init");

        JMessageClient.init(getApplicationContext());
        JMessageClient.setNotificationMode(JMessageClient.NOTI_MODE_DEFAULT);
        new NotificationClickEventReceiver(getApplicationContext());
    }

}
