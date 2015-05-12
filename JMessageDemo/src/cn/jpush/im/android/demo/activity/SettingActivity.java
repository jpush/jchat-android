package cn.jpush.im.android.demo.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.jpush.im.android.demo.R;

import cn.jpush.im.android.api.JMessageClient;

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
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                final LayoutInflater inflater = LayoutInflater.from(this);
                View view = inflater.inflate(R.layout.dialog_reset_password, null);
                builder.setView(view);
                final Dialog dialog = builder.create();
                dialog.show();
                final EditText pwdEt = (EditText) view.findViewById(R.id.password_et);
                final Button cancel = (Button) view.findViewById(R.id.cancel_btn);
                final Button commit = (Button) view.findViewById(R.id.commit_btn);
                OnClickListener listener = new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (view.getId()){
                            case R.id.cancel_btn:
                                dialog.cancel();
                                break;
                            case R.id.commit_btn:
                                String input = pwdEt.getText().toString().trim();
                                if(JMessageClient.isCurrentUserPasswordValid(input)){
                                    Intent intent = new Intent();
                                    intent.putExtra("oldPassword", input);
                                    intent.setClass(mContext, ResetPasswordActivity.class);
                                    startActivity(intent);
                                    dialog.cancel();
                                }else {
                                    Toast.makeText(mContext, mContext.getString(R.string.input_password_error_toast), Toast.LENGTH_SHORT).show();
                                }
                                break;
                        }
                    }
                };
                cancel.setOnClickListener(listener);
                commit.setOnClickListener(listener);
                break;
		}
	} 

}
