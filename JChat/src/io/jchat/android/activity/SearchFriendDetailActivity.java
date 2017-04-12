package io.jchat.android.activity;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.R;
import io.jchat.android.application.JChatDemoApplication;
import io.jchat.android.chatting.ChatActivity;
import io.jchat.android.chatting.CircleImageView;
import io.jchat.android.chatting.utils.DialogCreator;
import io.jchat.android.chatting.utils.HandleResponseCode;
import io.jchat.android.tools.NativeImageLoader;

public class SearchFriendDetailActivity extends BaseActivity {

    private TextView title;
    private TextView mNickNameTv;
    private ImageButton mReturnBtn;
    private CircleImageView mAvatarIv;
    private ImageView mGenderIv;
    private TextView mGenderTv;
    private TextView mAreaTv;
    private TextView mSignatureTv;
    private TextView mAddFriendBtn;
    private Context mContext;
    private boolean mIsGetAvatar = false;
    private String mUsername;
    private String mAppKey;
    private String mAvatarPath;
    private String mDisplayName;
    private Button mBt_chat;
    private UserInfo mToUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result_detail);
        mContext = this;
        title = (TextView) findViewById(R.id.title);
        title.setText(this.getString(R.string.search_friend_title_bar));
        mNickNameTv = (TextView) findViewById(R.id.nick_name_tv);
        mReturnBtn = (ImageButton) findViewById(R.id.friend_info_return_btn);
        mAvatarIv = (CircleImageView) findViewById(R.id.friend_detail_avatar);
        mGenderIv = (ImageView) findViewById(R.id.gender_iv);
        mGenderTv = (TextView) findViewById(R.id.gender_tv);
        mAreaTv = (TextView) findViewById(R.id.region_tv);
        mSignatureTv = (TextView) findViewById(R.id.signature_tv);
        mAddFriendBtn = (Button) findViewById(R.id.add_to_friend);
        mBt_chat = (Button) findViewById(R.id.bt_chat);

        inModule();


        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.friend_info_return_btn:
                        SearchFriendDetailActivity.this.finish();
                        break;
                    case R.id.add_to_friend:
                        Intent intent = new Intent();
                        intent.setClass(mContext, SendInvitationActivity.class);
                        intent.putExtra("targetUsername", mUsername);
                        intent.putExtra(JChatDemoApplication.AVATAR, mAvatarPath);
                        intent.putExtra(JChatDemoApplication.TARGET_APP_KEY, mAppKey);
                        intent.putExtra(JChatDemoApplication.NICKNAME, mDisplayName);
                        startActivity(intent);
                        break;
                    case R.id.friend_detail_avatar:
                        startBrowserAvatar();
                        break;
                    case R.id.bt_chat:
                        Intent intentChat = new Intent(SearchFriendDetailActivity.this, ChatActivity.class);
                        String title = mToUserInfo.getNotename();
                        if (TextUtils.isEmpty(title)) {
                            title = mToUserInfo.getNickname();
                            if (TextUtils.isEmpty(title)) {
                                title = mToUserInfo.getUserName();
                            }
                        }
                        intentChat.putExtra(JChatDemoApplication.CONV_TITLE, title);
                        intentChat.putExtra(JChatDemoApplication.TARGET_ID, mToUserInfo.getUserName());
                        intentChat.putExtra(JChatDemoApplication.TARGET_APP_KEY, mToUserInfo.getAppKey());
                        startActivity(intentChat);
                        break;
                }
            }
        };
        mReturnBtn.setOnClickListener(listener);
        mAddFriendBtn.setOnClickListener(listener);
        mAvatarIv.setOnClickListener(listener);
        mBt_chat.setOnClickListener(listener);
    }

    private void inModule() {
        final Dialog dialog = DialogCreator.createLoadingDialog(this, this.getString(R.string.jmui_loading));
        dialog.show();
        Intent intent = getIntent();
        mUsername = intent.getStringExtra(JChatDemoApplication.TARGET_ID);
        mAppKey = intent.getStringExtra(JChatDemoApplication.TARGET_APP_KEY);
        JMessageClient.getUserInfo(mUsername, mAppKey, new GetUserInfoCallback() {
            @Override
            public void gotResult(int status, String desc, final UserInfo userInfo) {
                dialog.dismiss();
                if (status == 0) {
                    mToUserInfo = userInfo;
                    Bitmap bitmap = NativeImageLoader.getInstance().getBitmapFromMemCache(mUsername);
                    if (null != bitmap) {
                        mAvatarIv.setImageBitmap(bitmap);
                    } else if (!TextUtils.isEmpty(userInfo.getAvatar())) {
                        mAvatarPath = userInfo.getAvatarFile().getPath();
                        userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                            @Override
                            public void gotResult(int status, String desc, Bitmap bitmap) {
                                if (status == 0) {
                                    mAvatarIv.setImageBitmap(bitmap);
                                    NativeImageLoader.getInstance().updateBitmapFromCache(mUsername, bitmap);
                                } else {
                                    HandleResponseCode.onHandle(mContext, status, false);
                                }
                            }
                        });
                    }

                    mDisplayName = userInfo.getNickname();
                    if (TextUtils.isEmpty(mDisplayName)) {
                        mDisplayName = userInfo.getUserName();
                    }
                    mNickNameTv.setText(mDisplayName);
                    if (userInfo.getGender() == UserInfo.Gender.male) {
                        mGenderTv.setText(mContext.getString(R.string.man));
                        mGenderIv.setImageResource(R.drawable.sex_man);
                    } else if (userInfo.getGender() == UserInfo.Gender.female) {
                        mGenderTv.setText(mContext.getString(R.string.woman));
                        mGenderIv.setImageResource(R.drawable.sex_woman);
                    } else {
                        mGenderTv.setText(mContext.getString(R.string.unknown));
                    }
                    mAreaTv.setText(userInfo.getRegion());
                    mSignatureTv.setText(userInfo.getSignature());
                } else {
                    HandleResponseCode.onHandle(mContext, status, false);
                }
            }
        });
    }

    private void startBrowserAvatar() {
        if (null != mAvatarPath) {
            if (mIsGetAvatar) {
                //如果缓存了图片，直接加载
                Bitmap bitmap = NativeImageLoader.getInstance().getBitmapFromMemCache(mUsername);
                if (bitmap != null) {
                    Intent intent = new Intent();
                    intent.putExtra("browserAvatar", true);
                    intent.putExtra("avatarPath", mUsername);
                    intent.setClass(this, BrowserViewPagerActivity.class);
                    startActivity(intent);
                }
            } else {
                final Dialog dialog = DialogCreator.createLoadingDialog(this, this.getString(R.string.jmui_loading));
                dialog.show();
                JMessageClient.getUserInfo(mUsername, new GetUserInfoCallback() {
                    @Override
                    public void gotResult(int status, String desc, UserInfo userInfo) {
                        if (status == 0) {
                            userInfo.getBigAvatarBitmap(new GetAvatarBitmapCallback() {
                                @Override
                                public void gotResult(int status, String desc, Bitmap bitmap) {
                                    if (status == 0) {
                                        mIsGetAvatar = true;
                                        //缓存头像
                                        NativeImageLoader.getInstance().updateBitmapFromCache(mUsername, bitmap);
                                        Intent intent = new Intent();
                                        intent.putExtra("browserAvatar", true);
                                        intent.putExtra("avatarPath", mUsername);
                                        intent.setClass(mContext, BrowserViewPagerActivity.class);
                                        startActivity(intent);
                                    } else {
                                        HandleResponseCode.onHandle(mContext, status, false);
                                    }
                                    dialog.dismiss();
                                }
                            });
                        } else {
                            dialog.dismiss();
                            HandleResponseCode.onHandle(mContext, status, false);
                        }
                    }
                });
            }
        }
    }
}
