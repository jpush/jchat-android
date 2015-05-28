package io.jchat.android.activity;

import android.content.Intent;
import android.os.Bundle;


import io.jchat.android.R;

import io.jchat.android.controller.ReloginController;
import io.jchat.android.view.ReloginView;

public class ReloginActivity extends BaseActivity {

	private ReloginView mReloginView;
	private ReloginController mReloginController;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_re_login);
		mReloginView = (ReloginView) findViewById(R.id.relogin_view);
		mReloginView.initModule();
        String userName = getIntent().getStringExtra("userName");
        mReloginView.setUserName(userName);
		mReloginController = new ReloginController(mReloginView, this, userName);
		mReloginView.setListeners(mReloginController);
	}


	public void StartRelogin() {
		// TODO Auto-generated method stub
		Intent intent  = new Intent();
		intent.setClass(this, MainActivity.class);
		startActivity(intent);
		finish();
	}


	public void StartSwitchUser() {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		intent.setClass(this, LoginActivity.class);
		startActivity(intent);
		finish();
	}


    public void StartRegisterActivity() {
        Intent intent = new Intent();
        intent.setClass(this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }
}
