package io.jchat.android.activity;

import android.content.Intent;
import android.os.Bundle;


import cn.jpush.im.android.api.JMessageClient;
import io.jchat.android.R;

import io.jchat.android.controller.RegisterController;
import io.jchat.android.view.RegisterView;

public class RegisterActivity extends BaseActivity {

	private RegisterView mRegisterView = null;
    private RegisterController mRegisterController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_regist);
		
		mRegisterView = (RegisterView) findViewById(R.id.regist_view);
		mRegisterView.initModule();
		mRegisterController = new RegisterController(mRegisterView,this);
        mRegisterView.setListener(mRegisterController);
		mRegisterView.setListeners(mRegisterController);
	}

	//注册成功
	public void OnRegistSuccess(){
        Intent intent = new Intent();
        intent.setClass(this, FixProfileActivity.class);
        startActivity(intent);
        finish();
	}

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setClass(this, LoginActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        mRegisterController.dismissDialog();
        super.onDestroy();
    }
}
