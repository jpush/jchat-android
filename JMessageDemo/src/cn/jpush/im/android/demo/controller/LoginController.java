package cn.jpush.im.android.demo.controller;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import cn.jpush.im.android.demo.R;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.demo.activity.LoginActivity;
import cn.jpush.im.android.demo.tools.HandleResponseCode;
import cn.jpush.im.android.demo.view.LoginDialog;
import cn.jpush.im.android.demo.view.LoginView;
import cn.jpush.im.api.BasicCallback;

public class LoginController implements LoginView.Listener, OnClickListener {

    private LoginView mLoginView;
    private LoginActivity mContext;
    private Dialog mDialog;

    public LoginController(LoginView mLoginView, LoginActivity context) {
        this.mLoginView = mLoginView;
        this.mContext = context;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_btn:
                //隐藏软键盘
                InputMethodManager manager = ((InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE));
                if (mContext.getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
                    if (mContext.getCurrentFocus() != null)
                        manager.hideSoftInputFromWindow(mContext.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }

                Log.d("Tag", "[login]login event execute!");
                final String userId = mLoginView.getUserId();
                final String password = mLoginView.getPassword();

                if (userId.equals("")) {
                    mLoginView.userNameError(mContext);
                    break;
                } else if (password.equals("")) {
                    mLoginView.passwordError(mContext);
                    break;
                }
                LoginDialog loginDialog = new LoginDialog();
                mDialog = loginDialog.createLoadingDialog(mContext);
                mDialog.show();
                JMessageClient.login(userId, password,
                        new BasicCallback() {
                            @Override
                            public void gotResult(final int status, final String desc) {
                                mContext.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (status == 0) {
                                            //后台拿UserInfo
                                            JMessageClient.getUserInfo(userId, null);
                                            mContext.StartMainActivity();
                                        } else {
                                            dismissDialog();
                                            Log.i("LoginController", "status = " + status);
                                            HandleResponseCode.onHandle(mContext, status);
                                        }
                                    }
                                });
                            }
                        });
                break;

            case R.id.register_btn:
                mContext.StartRegisterActivity();
        }
    }

    @Override
    public void onSoftKeyboardShown(int softKeyboardHeight) {
        if (softKeyboardHeight > 300) {
            Log.i("LoginController", "softKeyboardHeight h: " + softKeyboardHeight);
            SharedPreferences sp = mContext.getSharedPreferences("JPushDemo", Context.MODE_PRIVATE);
            boolean writable = sp.getBoolean("writable", true);
            if (writable) {
                Log.i("LoginController", "commit h: " + softKeyboardHeight);
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt("SoftKeyboardHeight", softKeyboardHeight);
                editor.putBoolean("writable", false);
                editor.commit();
            }
        }
    }

    public void dismissDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }
}
