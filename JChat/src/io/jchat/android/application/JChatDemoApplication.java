package io.jchat.android.application;

import android.app.Application;
import android.util.Log;
import cn.jpush.im.android.api.JMessageClient;
import io.jchat.android.receiver.NotificationClickEventReceiver;
import io.jchat.android.chatting.utils.SharePreferenceManager;

public class JChatDemoApplication extends Application {

    public static final int REQUEST_CODE_TAKE_PHOTO = 4;
    public static final int REQUEST_CODE_SELECT_PICTURE = 6;
    public static final int RESULT_CODE_SELECT_PICTURE = 8;
    public static final int REQUEST_CODE_SELECT_ALBUM = 10;
    public static final int RESULT_CODE_SELECT_ALBUM = 11;
    public static final int REQUEST_CODE_BROWSER_PICTURE = 12;
    public static final int RESULT_CODE_BROWSER_PICTURE = 13;
    public static final int RESULT_CODE_CHAT_DETAIL = 15;
    public static final int REQUEST_CODE_FRIEND_INFO = 16;
    public static final int RESULT_CODE_FRIEND_INFO = 17;
    public static final int REQUEST_CODE_CROP_PICTURE = 18;
    public static final int REQUEST_CODE_ME_INFO = 19;
    public static final int RESULT_CODE_ME_INFO = 20;
    public static final int REQUEST_CODE_ALL_MEMBER = 21;
    public static final int RESULT_CODE_ALL_MEMBER = 22;
<<<<<<< HEAD
    public static final int REFRESH_GROUP_NAME = 3000;
    public static final int REFRESH_GROUP_NUM = 3001;
=======
>>>>>>> master
    public static final int ON_GROUP_EVENT = 3004;

    private static final String JCHAT_CONFIGS = "JChat_configs";
<<<<<<< HEAD

=======
    public static final String TARGET_APP_KEY = "targetAppKey";
>>>>>>> master
    public static final String TARGET_ID = "targetId";
    public static final String NAME = "name";
    public static final String NICKNAME = "nickname";
    public static final String GROUP_ID = "groupId";
<<<<<<< HEAD
    public static final String IS_GROUP = "isGroup";
=======
>>>>>>> master
    public static final String GROUP_NAME = "groupName";
    public static final String STATUS = "status";
    public static final String POSITION = "position";
    public static final String MsgIDs = "msgIDs";
    public static final String DRAFT = "draft";
    public static final String DELETE_MODE = "deleteMode";
    public static final String MEMBERS_COUNT = "membersCount";
<<<<<<< HEAD
    public static final String PICTURE_DIR = "sdcard/JChatDemo/pictures/";
=======
    public static String PICTURE_DIR = "sdcard/JChatDemo/pictures/";
>>>>>>> master


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("JpushDemoApplication", "init");
        //初始化JMessage-sdk
        JMessageClient.init(getApplicationContext());
        SharePreferenceManager.init(getApplicationContext(), JCHAT_CONFIGS);
        //设置Notification的模式
        JMessageClient.setNotificationMode(JMessageClient.NOTI_MODE_DEFAULT);
        //注册Notification点击的接收器
        new NotificationClickEventReceiver(getApplicationContext());
    }

    public static void setPicturePath(String appKey) {
        if (!SharePreferenceManager.getCachedAppKey().equals(appKey)) {
            SharePreferenceManager.setCachedAppKey(appKey);
            PICTURE_DIR = "sdcard/JChatDemo/pictures/" + appKey + "/";
        }
    }

}
