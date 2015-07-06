package io.jchat.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import io.jchat.android.R;
import io.jchat.android.controller.LoginController;
import io.jchat.android.tools.ActivityManager;
import io.jchat.android.view.LoginView;

public class LoginActivity extends BaseActivity {

    private LoginView mLoginView = null;
    private LoginController mLoginController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActivityManager.addActivity(this);
        mLoginView = (LoginView) findViewById(R.id.login_view);
        mLoginView.initModule();
        mLoginController = new LoginController(mLoginView, this);
        mLoginView.setListener(mLoginController);
        mLoginView.setListeners(mLoginController);
        mLoginView.setOnCheckedChangeListener(mLoginController);
        Intent intent = this.getIntent();
        mLoginView.isShowReturnBtn(intent.getBooleanExtra("fromSwitch", false));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    public Context getContext() {
        return this;
    }

    public void StartMainActivity() {
        Intent intent = new Intent();
        intent.setClass(getContext(), MainActivity.class);
        startActivity(intent);
        ActivityManager.clearList();
    }

    public void StartRegisterActivity() {
        Intent intent = new Intent();
        intent.setClass(this, RegisterActivity.class);
        startActivity(intent);
    }

}
