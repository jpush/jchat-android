package jiguang.chat.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.UserInfo;
import jiguang.chat.R;
import jiguang.chat.application.JGApplication;
import jiguang.chat.entity.Event;
import jiguang.chat.entity.EventType;
import jiguang.chat.utils.DialogCreator;

/**
 * Created by ${chenyn} on 2017/11/27.
 */

public class GroupUserInfoActivity extends BaseActivity {

    private ImageView mIv_more;
    private ImageView mIv_userAvatar;
    private TextView mTv_displayName;
    private TextView mTv_sign;
    private TextView mTv_userName;
    private TextView mTv_nick;
    private TextView mTv_gender;
    private TextView mTv_birthday;
    private TextView mTv_address;
    private Button mBtn_send_message;
    private String mUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_user_info);

        initView();
        initData();
    }

    private void initData() {
        Dialog dialog = DialogCreator.createLoadingDialog(GroupUserInfoActivity.this, "正在加载...");
        dialog.show();
        JMessageClient.getUserInfo(mUserName, new GetUserInfoCallback() {
            @Override
            public void gotResult(int i, String s, UserInfo userInfo) {
                dialog.dismiss();
                if (i == 0) {
                    userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                        @Override
                        public void gotResult(int i, String s, Bitmap bitmap) {
                            if (i == 0) {
                                mIv_userAvatar.setImageBitmap(bitmap);
                            } else {
                                mIv_userAvatar.setImageResource(R.drawable.jmui_head_icon);
                            }
                        }
                    });

                    mTv_displayName.setText(userInfo.getDisplayName());
                    mTv_sign.setText(userInfo.getSignature());
                    mTv_userName.setText(userInfo.getUserName());
                    mTv_nick.setText(userInfo.getNickname() == null ? "" : userInfo.getNickname());
                    UserInfo.Gender gender = userInfo.getGender();
                    if (gender.equals(UserInfo.Gender.male)) {
                        mTv_gender.setText("男");
                    } else if (gender.equals(UserInfo.Gender.female)) {
                        mTv_gender.setText("女");
                    } else {
                        mTv_gender.setText("保密");
                    }
                    mTv_birthday.setText(getBirthday(userInfo));
                    mTv_address.setText(userInfo.getRegion());
                    mBtn_send_message.setOnClickListener(v -> {
                        Intent intent = new Intent();
                        intent.setClass(GroupUserInfoActivity.this, ChatActivity.class);
                        //创建会话
                        intent.putExtra(JGApplication.TARGET_ID, userInfo.getUserName());
                        intent.putExtra(JGApplication.TARGET_APP_KEY, userInfo.getAppKey());
                        String notename = userInfo.getNotename();
                        if (TextUtils.isEmpty(notename)) {
                            notename = userInfo.getNickname();
                            if (TextUtils.isEmpty(notename)) {
                                notename = userInfo.getUserName();
                            }
                        }
                        intent.putExtra(JGApplication.CONV_TITLE, notename);
                        Conversation conv = JMessageClient.getSingleConversation(userInfo.getUserName(), userInfo.getAppKey());
                        //如果会话为空，使用EventBus通知会话列表添加新会话
                        if (conv == null) {
                            conv = Conversation.createSingleConversation(userInfo.getUserName(), userInfo.getAppKey());
                            EventBus.getDefault().post(new Event.Builder()
                                    .setType(EventType.createConversation)
                                    .setConversation(conv)
                                    .build());
                        }
                        startActivity(intent);
                    });
                }
            }
        });

        mIv_more.setOnClickListener(v -> {
            Intent intent = new Intent(GroupUserInfoActivity.this, SetGroupSilenceActivity.class);
            long groupID = getIntent().getLongExtra("groupID", 0);
            intent.putExtra("groupID", groupID);
            intent.putExtra("userName", mUserName);
            startActivity(intent);
        });

    }

    private void initView() {
        mUserName = getIntent().getStringExtra("groupUserName");
        findViewById(R.id.return_btn).setOnClickListener(v -> finish());
        String groupOwner = getIntent().getStringExtra("groupOwner");
        mIv_more = (ImageView) findViewById(R.id.iv_more);
        if (groupOwner.equals(JMessageClient.getMyInfo().getUserName())) {
            mIv_more.setVisibility(View.VISIBLE);
        } else {
            mIv_more.setVisibility(View.GONE);
        }

        mIv_userAvatar = (ImageView) findViewById(R.id.iv_userAvatar);
        mTv_displayName = (TextView) findViewById(R.id.tv_displayName);
        mTv_sign = (TextView) findViewById(R.id.tv_sign);
        mTv_userName = (TextView) findViewById(R.id.tv_userName);
        mTv_nick = (TextView) findViewById(R.id.tv_nick);
        mTv_gender = (TextView) findViewById(R.id.tv_gender);
        mTv_birthday = (TextView) findViewById(R.id.tv_birthday);
        mTv_address = (TextView) findViewById(R.id.tv_address);
        mBtn_send_message = (Button) findViewById(R.id.btn_send_message);
    }

    public String getBirthday(UserInfo info) {
        long birthday = info.getBirthday();
        if (birthday == 0) {
            return "";
        } else {
            Date date = new Date(birthday);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return dateFormat.format(date);
        }
    }
}
