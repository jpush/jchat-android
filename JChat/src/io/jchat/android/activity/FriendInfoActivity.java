package io.jchat.android.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.eventbus.EventBus;
import io.jchat.android.R;
import io.jchat.android.application.JChatDemoApplication;
import io.jchat.android.controller.FriendInfoController;
import io.jchat.android.entity.Event;
import io.jchat.android.tools.DialogCreator;
import io.jchat.android.tools.HandleResponseCode;
import io.jchat.android.tools.NativeImageLoader;
import io.jchat.android.view.FriendInfoView;

public class FriendInfoActivity extends BaseActivity {

    private FriendInfoView mFriendInfoView;
    private FriendInfoController mFriendInfoController;
    private String mTargetId;
    private long mGroupId;
    private UserInfo mUserInfo;
    private String mNickname;
    private boolean mIsGetAvatar = false;
    private String mTargetAppKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_info);
        mFriendInfoView = (FriendInfoView) findViewById(R.id.friend_info_view);
        mTargetId = getIntent().getStringExtra(JChatDemoApplication.TARGET_ID);
        mTargetAppKey = getIntent().getStringExtra(JChatDemoApplication.TARGET_APP_KEY);
        if (mTargetAppKey == null) {
            mTargetAppKey = JMessageClient.getMyInfo().getAppKey();
        }
        mGroupId = getIntent().getLongExtra(JChatDemoApplication.GROUP_ID, 0);
        Conversation conv;
        if (mGroupId == 0) {
            conv = JMessageClient.getSingleConversation(mTargetId, mTargetAppKey);
            mUserInfo = (UserInfo) conv.getTargetInfo();
        } else {
            conv = JMessageClient.getGroupConversation(mGroupId);
            GroupInfo groupInfo = (GroupInfo) conv.getTargetInfo();
            mUserInfo = groupInfo.getGroupMemberInfo(mTargetId);
        }

        mFriendInfoView.initModule();
        //先从Conversation里获得UserInfo展示出来
        mFriendInfoView.initInfo(mUserInfo);
        mFriendInfoController = new FriendInfoController(mFriendInfoView, this);
        mFriendInfoView.setListeners(mFriendInfoController);
        mFriendInfoView.setOnChangeListener(mFriendInfoController);
        //更新一次UserInfo
        final Dialog dialog = DialogCreator.createLoadingDialog(FriendInfoActivity.this,
                FriendInfoActivity.this.getString(R.string.loading));
        dialog.show();
        JMessageClient.getUserInfo(mTargetId, mTargetAppKey, new GetUserInfoCallback() {
            @Override
            public void gotResult(int status, String desc, final UserInfo userInfo) {
                dialog.dismiss();
                if (status == 0) {
                    mUserInfo = userInfo;
                    mNickname = userInfo.getNickname();
                    mFriendInfoView.initInfo(userInfo);
                } else {
                    HandleResponseCode.onHandle(FriendInfoActivity.this, status, false);
                }
            }
        });

    }

    /**
     * 如果是从群聊跳转过来，使用startActivity启动聊天界面，如果是单聊跳转过来，setResult然后
     * finish掉此界面
     */
    public void startChatActivity() {
        if (mGroupId != 0) {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(JChatDemoApplication.TARGET_ID, mTargetId);
            intent.putExtra(JChatDemoApplication.TARGET_APP_KEY, mTargetAppKey);
            intent.setClass(this, ChatActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent();
            intent.putExtra("returnChatActivity", true);
            intent.putExtra(JChatDemoApplication.NICKNAME, mNickname);
            setResult(JChatDemoApplication.RESULT_CODE_FRIEND_INFO, intent);
        }
        Conversation conv = JMessageClient.getSingleConversation(mTargetId, mTargetAppKey);
        //如果会话为空，使用EventBus通知会话列表添加新会话
        if (conv == null) {
            conv = Conversation.createSingleConversation(mTargetId, mTargetAppKey);
            EventBus.getDefault().post(new Event.StringEvent(mTargetId, mTargetAppKey));
        }
        finish();
    }

    public String getNickname() {
        return mNickname;
    }

    public String getUserName() {
        return mUserInfo.getUserName();
    }


    //点击头像预览大图
    public void startBrowserAvatar() {
        if (mUserInfo != null && !TextUtils.isEmpty(mUserInfo.getAvatar())) {
            if (mIsGetAvatar) {
                //如果缓存了图片，直接加载
                Bitmap bitmap = NativeImageLoader.getInstance().getBitmapFromMemCache(mUserInfo.getUserName());
                if (bitmap != null) {
                    Intent intent = new Intent();
                    intent.putExtra("browserAvatar", true);
                    intent.putExtra("avatarPath", mUserInfo.getUserName());
                    intent.setClass(this, BrowserViewPagerActivity.class);
                    startActivity(intent);
                }
            } else {
                final Dialog dialog = DialogCreator.createLoadingDialog(this, this.getString(R.string.loading));
                dialog.show();
                mUserInfo.getBigAvatarBitmap(new GetAvatarBitmapCallback() {
                    @Override
                    public void gotResult(int status, String desc, Bitmap bitmap) {
                        if (status == 0) {
                            mIsGetAvatar = true;
                            //缓存头像
                            NativeImageLoader.getInstance().updateBitmapFromCache(mUserInfo.getUserName(), bitmap);
                            Intent intent = new Intent();
                            intent.putExtra("browserAvatar", true);
                            intent.putExtra("avatarPath", mUserInfo.getUserName());
                            intent.setClass(FriendInfoActivity.this, BrowserViewPagerActivity.class);
                            startActivity(intent);
                        } else {
                            HandleResponseCode.onHandle(FriendInfoActivity.this, status, false);
                        }
                        dialog.dismiss();
                    }
                });
            }
        }
    }


    //将获得的最新的昵称返回到聊天界面
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(JChatDemoApplication.NICKNAME, mNickname);
        setResult(JChatDemoApplication.RESULT_CODE_FRIEND_INFO, intent);
        finish();
        super.onBackPressed();
    }
}
