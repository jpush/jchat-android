package io.jchat.android.application;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import cn.jpush.im.android.api.JMessageClient;
import io.jchat.android.receiver.NotificationClickEventReceiver;
import io.jchat.android.tools.SharePreferenceManager;

public class JPushDemoApplication extends Application {

    public static final int REQUEST_CODE_TAKE_PHOTO = 4;
    public static final int REQUEST_CODE_SELECT_PICTURE = 6;
    public static final int RESULT_CODE_SELECT_PICTURE = 8;
    public static final int REQUEST_CODE_SELECT_ALBUM = 10;
    public static final int RESULT_CODE_SELECT_ALBUM = 11;
    public static final int REQUEST_CODE_BROWSER_PICTURE = 12;
    public static final int RESULT_CODE_BROWSER_PICTURE = 13;
    public static final int REQUEST_CODE_CHAT_DETAIL = 14;
    public static final int RESULT_CODE_CHAT_DETAIL = 15;
    public static final int REFRESH_GROUP_NAME = 3000;
    public static final int REFRESH_GROUP_NUM = 3001;
    public static final int ON_GROUP_EVENT = 3004;
    private static final String JCHAT_CONFIGS = "JChat_configs";

    public static String UPDATE_GROUP_NAME_ACTION = "cn.jpush.im.demo.activity.ACTION_UPDATE_GROUP_NAME";
    public static String CLEAR_MSG_LIST_ACTION = "cn.jpush.im.demo.activity.ACTION_CLEAR_MSG_LIST";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("JpushDemoApplication", "init");
        JMessageClient.init(getApplicationContext());
        SharePreferenceManager.init(getApplicationContext(), JCHAT_CONFIGS);
        JMessageClient.setNotificationMode(JMessageClient.NOTI_MODE_DEFAULT);
        new NotificationClickEventReceiver(getApplicationContext());
    }

}
