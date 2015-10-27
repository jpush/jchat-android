package io.jchat.android.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import java.io.File;
import java.lang.ref.WeakReference;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.DownloadAvatarCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.eventbus.EventBus;
import io.jchat.android.R;
import io.jchat.android.application.JPushDemoApplication;
import io.jchat.android.controller.FriendInfoController;
import io.jchat.android.entity.Event;
import io.jchat.android.tools.BitmapLoader;
import io.jchat.android.tools.DialogCreator;
import io.jchat.android.tools.HandleResponseCode;
import io.jchat.android.tools.NativeImageLoader;
import io.jchat.android.view.FriendInfoView;

public class FriendInfoActivity extends BaseActivity {

    private FriendInfoView mFriendInfoView;
    private FriendInfoController mFriendInfoController;
    private String mTargetID;
    private long mGroupID;
    private UserInfo mUserInfo;
    private final MyHandler myHandler = new MyHandler(this);
    private String mNickname;
    private final static int GET_INFO_SUCCEED = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_info);
        mFriendInfoView = (FriendInfoView) findViewById(R.id.friend_info_view);
        mTargetID = getIntent().getStringExtra(JPushDemoApplication.TARGET_ID);
        mGroupID = getIntent().getLongExtra(JPushDemoApplication.GROUP_ID, 0);
        Conversation conv;
        conv = JMessageClient.getSingleConversation(mTargetID);
        if (conv == null) {
            conv = JMessageClient.getGroupConversation(mGroupID);
            GroupInfo groupInfo = (GroupInfo) conv.getTargetInfo();
            mUserInfo = groupInfo.getGroupMemberInfo(mTargetID);
        } else {
            mUserInfo = (UserInfo) conv.getTargetInfo();
        }
        mFriendInfoView.initModule(mTargetID);
        //先从Conversation里获得UserInfo展示出来
        mFriendInfoView.initInfo(mUserInfo, mDensity);
        mFriendInfoController = new FriendInfoController(mFriendInfoView, this);
        mFriendInfoView.setListeners(mFriendInfoController);
        //更新一次UserInfo
        final Dialog dialog = DialogCreator.createLoadingDialog(FriendInfoActivity.this,
                FriendInfoActivity.this.getString(R.string.loading));
        dialog.show();
        JMessageClient.getUserInfo(mTargetID, new GetUserInfoCallback() {
            @Override
            public void gotResult(int status, String desc, final UserInfo userInfo) {
                dialog.dismiss();
                if (status == 0) {
                    File file = userInfo.getSmallAvatarFile();
                    if (file != null && file.isFile()) {
                        Bitmap bitmap = BitmapLoader.getBitmapFromFile(file.getAbsolutePath(),
                                mAvatarSize, mAvatarSize);
                        //更新头像缓存
                        NativeImageLoader.getInstance().updateBitmapFromCache(mTargetID, bitmap);
                        android.os.Message msg = myHandler.obtainMessage();
                        msg.what = GET_INFO_SUCCEED;
                        msg.obj = userInfo;
                        msg.sendToTarget();
                    } else {
                        userInfo.getSmallAvatarAsync(new DownloadAvatarCallback() {
                            @Override
                            public void gotResult(int status, String desc, File file) {
                                if (status == 0) {
                                    Bitmap bitmap = BitmapLoader.getBitmapFromFile(file.getAbsolutePath(),
                                            mAvatarSize, mAvatarSize);
                                    //更新头像缓存
                                    NativeImageLoader.getInstance().updateBitmapFromCache(mTargetID, bitmap);
                                    android.os.Message msg = myHandler.obtainMessage();
                                    msg.what = GET_INFO_SUCCEED;
                                    msg.obj = userInfo;
                                    msg.sendToTarget();
                                }
                            }
                        });
                    }
                } else {
                    HandleResponseCode.onHandle(FriendInfoActivity.this, status, false);
                }
            }
        });

    }

    /**
     * 如果是群聊，使用startActivity启动聊天界面，如果是单聊，setResult然后
     * finish掉此界面
     */
    public void startChatActivity() {
        if (mGroupID != 0) {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(JPushDemoApplication.TARGET_ID, mTargetID);
            intent.setClass(this, ChatActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent();
            intent.putExtra("returnChatActivity", true);
            setResult(JPushDemoApplication.RESULT_CODE_FRIEND_INFO, intent);
        }
        Conversation conv = JMessageClient.getSingleConversation(mTargetID);
        //如果会话为空，使用EventBus通知会话列表添加新会话
        if (conv == null) {
            conv = Conversation.createSingleConversation(mTargetID);
            EventBus.getDefault().post(new Event.StringEvent(mTargetID));
        }
        finish();
    }

    private static class MyHandler extends Handler {
        private final WeakReference<FriendInfoActivity> mActivity;

        public MyHandler(FriendInfoActivity activity) {
            mActivity = new WeakReference<FriendInfoActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            FriendInfoActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case GET_INFO_SUCCEED:
                        activity.mUserInfo = (UserInfo) msg.obj;
                        activity.mFriendInfoView.initInfo(activity.mUserInfo, activity.mDensity);
                        activity.mNickname = activity.mUserInfo.getNickname();
                        break;
                }
            }
        }
    }

    public String getNickname() {
        return mNickname;
    }


    //点击头像预览大图，若此时UserInfo还是空，则再取一次
    public void startBrowserAvatar() {
        if (mUserInfo != null && !TextUtils.isEmpty(mUserInfo.getAvatar())) {
            File file = mUserInfo.getAvatarFile();
            if (file != null && file.exists()) {
                Intent intent = new Intent();
                intent.putExtra("browserAvatar", true);
                intent.putExtra("avatarPath", mUserInfo.getAvatarFile().getAbsolutePath());
                intent.setClass(this, BrowserViewPagerActivity.class);
                startActivity(intent);
            } else {
                final Dialog dialog = DialogCreator.createLoadingDialog(this, this.getString(R.string.loading));
                dialog.show();
                mUserInfo.getAvatarFileAsync(new DownloadAvatarCallback() {
                    @Override
                    public void gotResult(int status, String desc, File file) {
                        dialog.dismiss();
                        if (status == 0) {
                            Intent intent = new Intent();
                            intent.putExtra("browserAvatar", true);
                            intent.putExtra("avatarPath", file.getAbsolutePath());
                            intent.setClass(FriendInfoActivity.this, BrowserViewPagerActivity.class);
                            startActivity(intent);
                        } else {
                            HandleResponseCode.onHandle(FriendInfoActivity.this, status, false);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(JPushDemoApplication.NICKNAME, mNickname);
        setResult(JPushDemoApplication.RESULT_CODE_FRIEND_INFO, intent);
        finish();
        super.onBackPressed();
    }
}
