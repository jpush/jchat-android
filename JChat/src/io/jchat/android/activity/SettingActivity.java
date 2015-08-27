package io.jchat.android.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import io.jchat.android.R;

import cn.jpush.im.android.api.JMessageClient;
import io.jchat.android.tools.DialogCreator;

public class SettingActivity extends BaseActivity implements OnClickListener{
	
	private ImageButton mReturnBtn;
	private ImageButton mMenuBtn;
	private TextView mTitle;
	private RelativeLayout mNotificationLl;
	private RelativeLayout mDisturbModeLl;
    private RelativeLayout mResetPwdRl;
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
		mDisturbModeLl = (RelativeLayout) findViewById(R.id.disturb_mode_rl);
        mResetPwdRl = (RelativeLayout) findViewById(R.id.change_password_rl);

		mMenuBtn.setVisibility(View.GONE);
		mTitle.setText(this.getString(R.string.setting));
		mReturnBtn.setOnClickListener(this);
		mNotificationLl.setOnClickListener(this);
		mDisturbModeLl.setOnClickListener(this);
        mResetPwdRl.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.return_btn:
			finish();
			break;
		case R.id.notification_rl:
			Intent intent = new Intent();
			intent.setClass(mContext, NotificationSettingActivity.class);
			startActivity(intent);
			break;
		case R.id.disturb_mode_rl:
            intent = new Intent();
            intent.setClass(mContext, DisturbSettingActivity.class);
            startActivity(intent);
			break;
            case R.id.change_password_rl:
                Dialog dialog = DialogCreator.createResetPwdDialog(this);
                dialog.show();
                break;
		}
	} 

}
