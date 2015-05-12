package cn.jpush.im.android.demo.receiver;


import android.content.Context;
import android.content.Intent;
import android.util.Log;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.event.NotificationClickEvent;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.demo.activity.ChatActivity;

public class NotificationClickEventReceiver {
    private static final String TAG = NotificationClickEventReceiver.class.getSimpleName();

    private Context mContext;

    public NotificationClickEventReceiver(Context context) {
        mContext = context;
        JMessageClient.registerEventReceiver(this);
    }

    public void onEvent(NotificationClickEvent notificationClickEvent) {
        Log.d(TAG, "[onEvent] NotificationClickEvent !!!!");
        if (null == notificationClickEvent) {
            Log.w(TAG, "[onNotificationClick] message is null");
            return;
        }
        Message msg = notificationClickEvent.getMessage();
        String targetID = msg.getTargetID();
        ConversationType type = msg.getTargetType();
        Intent notificationIntent = new Intent(mContext, ChatActivity.class);
//        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.putExtra("targetID", targetID);
        if (ConversationType.group == type) {
            notificationIntent.putExtra("isGroup", true);
        } else {
            notificationIntent.putExtra("isGroup", false);
        }
        notificationIntent.putExtra("fromGroup", false);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mContext.startActivity(notificationIntent);
    }

}
