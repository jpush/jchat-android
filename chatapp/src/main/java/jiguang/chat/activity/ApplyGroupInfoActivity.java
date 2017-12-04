package jiguang.chat.activity;

import android.app.Dialog;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.UserInfo;
import jiguang.chat.R;
import jiguang.chat.application.JGApplication;
import jiguang.chat.database.GroupApplyEntry;
import jiguang.chat.database.UserEntry;
import jiguang.chat.utils.DialogCreator;

/**
 * Created by ${chenyn} on 2017/11/22.
 */

public class ApplyGroupInfoActivity extends BaseActivity {

    private ImageView mIv_avatar;
    private TextView mTv_nickName;
    private TextView mTv_sign;
    private TextView mTv_additionalMsg;
    private TextView mTv_userName;
    private TextView mTv_gender;
    private TextView mTv_birthday;
    private TextView mTv_address;
    private Button mBtn_refusal;
    private Button mBtn_agree;
    private String name;
    private LinearLayout mBtn_refuseAgree;
    private LinearLayout mLl_state;
    private TextView mTv_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_apply_group_info);

        initView();
        initData();

    }

    private void initView() {
        initTitle(true, true, "返回", "详细资料", false, "");

        mIv_avatar = (ImageView) findViewById(R.id.iv_avatar);
        mTv_nickName = (TextView) findViewById(R.id.tv_nickName);
        mTv_sign = (TextView) findViewById(R.id.tv_sign);
        mTv_additionalMsg = (TextView) findViewById(R.id.tv_additionalMsg);
        mTv_userName = (TextView) findViewById(R.id.tv_userName);
        mTv_gender = (TextView) findViewById(R.id.tv_gender);
        mTv_birthday = (TextView) findViewById(R.id.tv_birthday);
        mTv_address = (TextView) findViewById(R.id.tv_address);

        mBtn_refuseAgree = (LinearLayout) findViewById(R.id.btn_refuseAgree);
        mBtn_refusal = (Button) findViewById(R.id.btn_refusal);
        mBtn_agree = (Button) findViewById(R.id.btn_agree);

        mLl_state = (LinearLayout) findViewById(R.id.ll_state);
        mTv_state = (TextView) findViewById(R.id.tv_state);
    }

    private void initData() {

        UserEntry user = JGApplication.getUserEntry();
        String toName = getIntent().getStringExtra("toName");
        String reason = getIntent().getStringExtra("reason");
        GroupApplyEntry entry = GroupApplyEntry.getEntry(user, toName, JMessageClient.getMyInfo().getAppKey());

        if (entry.btnState == 0) {
            mBtn_refuseAgree.setVisibility(View.VISIBLE);
            mLl_state.setVisibility(View.GONE);
        } else if (entry.btnState == 1) {
            mBtn_refuseAgree.setVisibility(View.GONE);
            mLl_state.setVisibility(View.VISIBLE);
            mTv_state.setText("已同意");
        } else {
            mBtn_refuseAgree.setVisibility(View.GONE);
            mLl_state.setVisibility(View.VISIBLE);
            mTv_state.setText("已拒绝");
        }

        mTv_additionalMsg.setText(reason);
        Dialog dialog = DialogCreator.createLoadingDialog(ApplyGroupInfoActivity.this, "正在加载...");
        dialog.show();
        JMessageClient.getUserInfo(toName, new GetUserInfoCallback() {
            @Override
            public void gotResult(int i, String s, UserInfo userInfo) {
                dialog.dismiss();
                if (i == 0) {
                    if (userInfo.getAvatar() != null) {
                        mIv_avatar.setImageBitmap(BitmapFactory.decodeFile(userInfo.getAvatarFile().getPath()));
                    }

                    mTv_nickName.setText(userInfo.getNickname());
                    mTv_sign.setText(userInfo.getSignature());
                    mTv_userName.setText(userInfo.getUserName());
                    UserInfo.Gender gender = userInfo.getGender();
                    if (gender.equals(UserInfo.Gender.male)) {
                        name = "男";
                    } else if (gender.equals(UserInfo.Gender.female)) {
                        name = "女";
                    } else {
                        name = "保密";
                    }
                    mTv_gender.setText(name);

                    mTv_address.setText(userInfo.getRegion());
                    mTv_birthday.setText(getBirthday(userInfo));
                }
            }
        });

        mBtn_refusal.setOnClickListener(v -> {
            entry.btnState = 2;
            entry.save();
            finish();
        });

        mBtn_agree.setOnClickListener(v -> {
            entry.btnState = 1;
            entry.save();
            finish();
        });

    }

    public String getBirthday(UserInfo info) {
        long birthday = info.getBirthday();
        Date date = new Date(birthday);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        return dateFormat.format(date);
    }
}
