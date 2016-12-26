package io.jchat.android.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.IntegerCallback;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import io.jchat.android.R;
import io.jchat.android.chatting.utils.DialogCreator;
import io.jchat.android.chatting.utils.HandleResponseCode;
import io.jchat.android.chatting.utils.SharePreferenceManager;
import io.jchat.android.view.SlipButton;

public class SettingActivity extends BaseActivity implements OnClickListener, SlipButton.OnChangedListener {

    private ImageButton mReturnBtn;
    private ImageButton mMenuBtn;
    private TextView mTitle;
    private RelativeLayout mNotificationLl;
    private RelativeLayout mResetPwdRl;
    private RelativeLayout mAboutRl;
    private SlipButton mNoDisturbBtn;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mContext = this;
        mReturnBtn = (ImageButton) findViewById(R.id.return_btn);
        mMenuBtn = (ImageButton) findViewById(R.id.right_btn);
        mTitle = (TextView) findViewById(R.id.title);
        mNotificationLl = (RelativeLayout) findViewById(R.id.notification_rl);
        mResetPwdRl = (RelativeLayout) findViewById(R.id.change_password_rl);
        mNoDisturbBtn = (SlipButton) findViewById(R.id.global_no_disturb_setting);
        mAboutRl = (RelativeLayout) findViewById(R.id.about_rl);

        mMenuBtn.setVisibility(View.GONE);
        mTitle.setText(this.getString(R.string.setting));
        mReturnBtn.setOnClickListener(this);
        mNotificationLl.setOnClickListener(this);
        mResetPwdRl.setOnClickListener(this);
        mAboutRl.setOnClickListener(this);
        mNoDisturbBtn.setOnChangedListener(R.id.global_no_disturb_setting, this);

        final Dialog dialog = DialogCreator.createLoadingDialog(this, this.getString(R.string.jmui_loading));
        dialog.show();
        JMessageClient.getNoDisturbGlobal(new IntegerCallback() {
            @Override
            public void gotResult(int status, String desc, Integer integer) {
                dialog.dismiss();
                if (status == 0) {
                    mNoDisturbBtn.setChecked(1 == integer);
                } else {
                    HandleResponseCode.onHandle(mContext, status, false);
                }
            }
        });
    }



    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.return_btn:
                finish();
                break;
            case R.id.notification_rl:
                intent = new Intent();
                intent.setClass(mContext, NotificationSettingActivity.class);
                startActivity(intent);
                break;
            case R.id.change_password_rl:
                Dialog dialog = DialogCreator.createResetPwdDialog(this);
                dialog.getWindow().setLayout((int) (0.8 * mWidth), WindowManager.LayoutParams.WRAP_CONTENT);
                dialog.show();
                break;
            case R.id.about_rl:
                intent = new Intent();
                intent.setClass(mContext, AboutActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onChanged(int id, final boolean checkState) {
        switch (id) {
            case R.id.global_no_disturb_setting:
                final Dialog loadingDialog = DialogCreator.createLoadingDialog(mContext,
                        mContext.getString(R.string.jmui_loading));
                loadingDialog.show();
                JMessageClient.setNoDisturbGlobal(checkState ? 1 : 0, new BasicCallback() {
                    @Override
                    public void gotResult(int status, String desc) {
                        loadingDialog.dismiss();
                        if (status == 0) {
                            if (checkState) {
                                Toast.makeText(mContext, mContext.getString(R.string.set_no_disturb_global_succeed),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(mContext, mContext.getString(R.string.remove_no_disturb_global_succeed),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if (checkState) {
                                mNoDisturbBtn.setChecked(false);
                            } else {
                                mNoDisturbBtn.setChecked(true);
                            }
                            HandleResponseCode.onHandle(mContext, status, false);
                        }
                    }
                });
                break;
        }
    }
}
