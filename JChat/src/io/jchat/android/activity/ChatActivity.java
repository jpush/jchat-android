package io.jchat.android.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.content.EventNotificationContent;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.R;
import io.jchat.android.application.JChatDemoApplication;
import io.jchat.android.controller.ChatController;
import io.jchat.android.controller.RecordVoiceBtnController;
import io.jchat.android.tools.BitmapLoader;
import io.jchat.android.tools.FileHelper;
import io.jchat.android.view.ChatView;

/*
 * 对话界面
 */
public class ChatActivity extends BaseActivity {

    private static final String TAG = "ChatActivity";

    private ChatView mChatView;
    private ChatController mChatController;
    private MyReceiver mReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        JMessageClient.registerEventReceiver(this);
        setContentView(R.layout.activity_chat);
        mChatView = (ChatView) findViewById(R.id.chat_view);
        mChatView.initModule(mDensity, mDensityDpi);
        mChatController = new ChatController(mChatView, this);
        mChatView.setListeners(mChatController);
        mChatView.setOnTouchListener(mChatController);
        mChatView.setOnSizeChangedListener(mChatController);
        mChatView.setOnKbdStateListener(mChatController);
        initReceiver();

    }

    // 监听耳机插入
    private void initReceiver() {
        mReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mReceiver, filter);
    }

    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent data) {
            if (data != null) {
                //插入了耳机
                if (data.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                    mChatController.getAdapter().setAudioPlayByEarPhone(data.getIntExtra("state", 0));
                }
            }
        }

    }

    /*
    重写BaseActivity的handleMsg()方法
     */
    @Override
    public void handleMsg(android.os.Message msg) {
        switch (msg.what) {
            case JChatDemoApplication.REFRESH_GROUP_NAME:
                if (mChatController.getConversation() != null) {
                    int num = msg.getData().getInt("membersCount");
                    String groupName = msg.getData().getString(JChatDemoApplication.GROUP_NAME);
                    mChatView.setChatTitle(groupName, num);
                }
                break;
            case JChatDemoApplication.REFRESH_GROUP_NUM:
                int num = msg.getData().getInt("membersCount");
                mChatView.setChatTitle(ChatActivity.this.getString(R.string.group), num);
                break;
        }
    }

    /**
     * 处理发送图片，刷新界面
     *
     * @param data intent
     */
    private void handleImgRefresh(Intent data) {
        String targetId = data.getStringExtra(JChatDemoApplication.TARGET_ID);
        long groupId = data.getLongExtra(JChatDemoApplication.GROUP_ID, 0);
        Log.i(TAG, "Refresh Image groupId: " + groupId);
        //判断是否在当前会话中发图片
        if (targetId != null) {
            if (targetId.equals(mChatController.getTargetId())) {
                mChatController.getAdapter().setSendImg(targetId, data.getIntArrayExtra(JChatDemoApplication.MsgIDs));
                mChatView.setToBottom();
            }
        } else if (groupId != 0) {
            if (groupId == mChatController.getGroupId()) {
                mChatController.getAdapter().setSendImg(groupId, data.getIntArrayExtra(JChatDemoApplication.MsgIDs));
                mChatView.setToBottom();
            }
        }


    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed!");
        if (RecordVoiceBtnController.mIsPressed) {
            mChatView.dismissRecordDialog();
            mChatView.releaseRecorder();
            RecordVoiceBtnController.mIsPressed = false;
        }
        if (mChatView.getMoreMenu().getVisibility() == View.VISIBLE) {
            mChatView.dismissMoreMenu();
        } else {
            mChatController.resetUnreadMsg();
        }
        super.onBackPressed();
    }

    /**
     * 释放资源
     */
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        JMessageClient.unRegisterEventReceiver(this);
        unregisterReceiver(mReceiver);
        mChatController.releaseMediaPlayer();
        mChatView.releaseRecorder();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        RecordVoiceBtnController.mIsPressed = false;
        JMessageClient.exitConversaion();
        Log.i(TAG, "[Life cycle] - onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        mChatController.getAdapter().stopMediaPlayer();
        if (mChatView.getMoreMenu().getVisibility() == View.VISIBLE) {
            mChatView.dismissMoreMenu();
        }
        if (mChatController.getConversation() != null)
            mChatController.getConversation().resetUnreadCount();
        Log.i(TAG, "[Life cycle] - onStop");
        super.onStop();
    }

    @Override
    protected void onResume() {
        if (!RecordVoiceBtnController.mIsPressed)
            mChatView.dismissRecordDialog();
        String targetID = getIntent().getStringExtra(JChatDemoApplication.TARGET_ID);
        boolean isGroup = getIntent().getBooleanExtra(JChatDemoApplication.IS_GROUP, false);
        if (isGroup) {
            try {
                long groupID = getIntent().getLongExtra(JChatDemoApplication.GROUP_ID, 0);
                if (groupID == 0) {
                    JMessageClient.enterGroupConversation(Long.parseLong(targetID));
                } else JMessageClient.enterGroupConversation(groupID);
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
        } else if (null != targetID) {
            JMessageClient.enterSingleConversaion(targetID);
        }
        mChatController.getAdapter().initMediaPlayer();
        Log.i(TAG, "[Life cycle] - onResume");
        super.onResume();
    }

    /**
     * 用于处理拍照发送图片返回结果以及从其他界面回来后刷新聊天标题
     * 或者聊天消息
     *
     * @param requestCode 请求码
     * @param resultCode  返回码
     * @param data        intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        }
        if (requestCode == JChatDemoApplication.REQUEST_CODE_TAKE_PHOTO) {
            final Conversation conv = mChatController.getConversation();
            try {
                String originPath = mChatController.getPhotoPath();
                Bitmap bitmap = BitmapLoader.getBitmapFromFile(originPath, 720, 1280);
                ImageContent.createImageContentAsync(bitmap, new ImageContent.CreateImageContentCallback() {
                    @Override
                    public void gotResult(int status, String desc, ImageContent imageContent) {
                        if (status == 0) {
                            Message msg = conv.createSendMessage(imageContent);
                            Intent intent = new Intent();
                            intent.putExtra(JChatDemoApplication.MsgIDs, new int[]{msg.getId()});
                            if (conv.getType() == ConversationType.group) {
                                intent.putExtra(JChatDemoApplication.GROUP_ID,
                                        ((GroupInfo) conv.getTargetInfo()).getGroupID());
                            } else {
                                intent.putExtra(JChatDemoApplication.TARGET_ID,
                                        ((UserInfo) conv.getTargetInfo()).getUserName());
                            }
                            handleImgRefresh(intent);
                        }
                    }
                });
            }  catch (NullPointerException e) {
                Log.i(TAG, "onActivityResult unexpected result");
            }
        } else if (resultCode == JChatDemoApplication.RESULT_CODE_SELECT_PICTURE) {
            handleImgRefresh(data);
        } else if (resultCode == JChatDemoApplication.RESULT_CODE_CHAT_DETAIL) {
            if (mChatController.isGroup()) {
                GroupInfo groupInfo = (GroupInfo) mChatController.getConversation().getTargetInfo();
                UserInfo userInfo = groupInfo.getGroupMemberInfo(JMessageClient.getMyInfo().getUserName());
                //如果自己在群聊中，同时显示群人数
                if (userInfo != null) {
                    if (TextUtils.isEmpty(data.getStringExtra(JChatDemoApplication.NAME))) {
                        mChatView.setChatTitle(this.getString(R.string.group),
                                data.getIntExtra("currentCount", 0));
                    } else {
                        mChatView.setChatTitle(data.getStringExtra(JChatDemoApplication.NAME),
                                data.getIntExtra("currentCount", 0));
                    }
                } else {
                    if (TextUtils.isEmpty(data.getStringExtra(JChatDemoApplication.NAME))) {
                        mChatView.setChatTitle(this.getString(R.string.group));
                        mChatView.dismissGroupNum();
                    } else {
                        mChatView.setChatTitle(data.getStringExtra(JChatDemoApplication.NAME));
                        mChatView.dismissGroupNum();
                    }
                }

            } else mChatView.setChatTitle(data.getStringExtra(JChatDemoApplication.NAME));
            if (data.getBooleanExtra("deleteMsg", false)) {
                mChatController.getAdapter().clearMsgList();
            }
        } else if (resultCode == JChatDemoApplication.RESULT_CODE_FRIEND_INFO) {
            if (!mChatController.isGroup()) {
                String nickname = data.getStringExtra(JChatDemoApplication.NICKNAME);
                if (nickname != null) {
                    mChatView.setChatTitle(nickname);
                }
            }
        }
    }

    public void startChatDetailActivity(boolean isGroup, String targetID, long groupID) {
        Intent intent = new Intent();
        intent.putExtra(JChatDemoApplication.IS_GROUP, isGroup);
        intent.putExtra(JChatDemoApplication.TARGET_ID, targetID);
        intent.putExtra(JChatDemoApplication.GROUP_ID, groupID);
        intent.setClass(this, ChatDetailActivity.class);
        startActivityForResult(intent, JChatDemoApplication.REQUEST_CODE_CHAT_DETAIL);
    }

    public void startPickPictureTotalActivity(Intent intent) {
        if (!FileHelper.isSdCardExist()) {
            Toast.makeText(this, this.getString(R.string.sdcard_not_exist_toast), Toast.LENGTH_SHORT).show();
        } else {
            intent.setClass(this, PickPictureTotalActivity.class);
            startActivityForResult(intent, JChatDemoApplication.REQUEST_CODE_SELECT_PICTURE);
        }
    }

    /**
     * 接收消息类事件
     *
     * @param event 消息事件
     */
    public void onEvent(MessageEvent event) {
        final Message msg = event.getMessage();
        //若为群聊相关事件，如添加、删除群成员
        Log.i(TAG, event.getMessage().toString());
        if (msg.getContentType() == ContentType.eventNotification) {
            GroupInfo groupInfo = (GroupInfo) msg.getTargetInfo();
            long groupID = groupInfo.getGroupID();
            UserInfo myInfo = JMessageClient.getMyInfo();
            EventNotificationContent.EventNotificationType type = ((EventNotificationContent) msg
                    .getContent()).getEventNotificationType();
            if (groupID == mChatController.getGroupId()) {
                switch (type) {
                    case group_member_added:
                        //添加群成员事件
                        List<String> userNames = ((EventNotificationContent) msg.getContent()).getUserNames();
                        //群主把当前用户添加到群聊，则显示聊天详情按钮
                        refreshGroupNum();
                        if (userNames.contains(myInfo.getNickname()) || userNames.contains(myInfo.getUserName())) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mChatView.showRightBtn();
                                }
                            });
                        }

                        break;
                    case group_member_removed:
                        //删除群成员事件
                        userNames = ((EventNotificationContent) msg.getContent()).getUserNames();
                        //群主删除了当前用户，则隐藏聊天详情按钮
                        if (userNames.contains(myInfo.getNickname()) || userNames.contains(myInfo.getUserName())) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mChatView.dismissRightBtn();
                                    GroupInfo groupInfo = (GroupInfo) mChatController.getConversation()
                                            .getTargetInfo();
                                    if (TextUtils.isEmpty(groupInfo.getGroupName())) {
                                        mChatView.setChatTitle(ChatActivity.this.getString(R.string.group));
                                    } else {
                                        mChatView.setChatTitle(groupInfo.getGroupName());
                                    }
                                    mChatView.dismissGroupNum();
                                }
                            });
                        } else {
                            refreshGroupNum();
                        }

                        break;
                    case group_member_exit:
                        refreshGroupNum();
                        break;
                }
            }
        }
        //刷新消息
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //收到消息的类型为单聊
                if (msg.getTargetType().equals(ConversationType.single)) {
                    String targetID = ((UserInfo) msg.getTargetInfo()).getUserName();
                    //判断消息是否在当前会话中
                    if (!mChatController.isGroup() && targetID.equals(mChatController.getTargetId())) {
                        Message lastMsg = mChatController.getConversation().getLatestMessage();
                        if (lastMsg == null || msg.getId() != lastMsg.getId()) {
                            mChatController.getAdapter().addMsgToList(msg);
                        }
                    }
                }else {
                    long groupID = ((GroupInfo)msg.getTargetInfo()).getGroupID();
                    if (mChatController.isGroup() && groupID == mChatController.getGroupId()) {
                        Message lastMsg = mChatController.getConversation().getLatestMessage();
                        if (lastMsg == null || msg.getId() != lastMsg.getId()) {
                            mChatController.getAdapter().addMsgToList(msg);
                        }
                    }
                }
            }
        });
    }

    private void refreshGroupNum() {
        Conversation conv = JMessageClient.getGroupConversation(mChatController.getGroupId());
        GroupInfo groupInfo = (GroupInfo) conv.getTargetInfo();
        if (!TextUtils.isEmpty(groupInfo.getGroupName())) {
            android.os.Message handleMessage = mHandler.obtainMessage();
            handleMessage.what = JChatDemoApplication.REFRESH_GROUP_NAME;
            Bundle bundle = new Bundle();
            bundle.putString(JChatDemoApplication.GROUP_NAME, groupInfo.getGroupName());
            bundle.putInt("membersCount", groupInfo.getGroupMembers().size());
            handleMessage.setData(bundle);
            handleMessage.sendToTarget();
        } else {
            android.os.Message handleMessage = mHandler.obtainMessage();
            handleMessage.what = JChatDemoApplication.REFRESH_GROUP_NUM;
            Bundle bundle = new Bundle();
            bundle.putInt("membersCount", groupInfo.getGroupMembers().size());
            handleMessage.setData(bundle);
            handleMessage.sendToTarget();
        }
    }
}
