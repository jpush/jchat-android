package io.jchat.android.application;

import android.app.Application;
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
    public static final int REQUEST_CODE_FRIEND_INFO = 16;
    public static final int RESULT_CODE_FRIEND_INFO = 17;
    public static final int REQUEST_CODE_CROP_PICTURE = 18;
    public static final int REFRESH_GROUP_NAME = 3000;
    public static final int REFRESH_GROUP_NUM = 3001;
    public static final int ON_GROUP_EVENT = 3004;
    private static final String JCHAT_CONFIGS = "JChat_configs";

    public static final String TARGET_ID = "targetID";
    public static final String NAME = "name";
    public static final String NICKNAME = "nickname";
    public static final String GROUP_ID = "groupID";
    public static final String IS_GROUP = "isGroup";
    public static final String GROUP_NAME = "groupName";
    public static final String STATUS = "status";
    public static final String POSITION = "position";
    public static final String MsgIDs = "msgIDs";

    public static final String PICTURE_DIR = "sdcard/JPushDemo/pictures/";


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
