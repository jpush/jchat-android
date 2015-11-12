package io.jchat.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
	public void onRegistSuccess(){
        Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setClass(this, FixProfileActivity.class);
        startActivity(intent);
	}

    @Override
    protected void onDestroy() {
        mRegisterController.dismissDialog();
		Log.i("RegisterActivity", "onDestroy!");
		super.onDestroy();
    }
}
