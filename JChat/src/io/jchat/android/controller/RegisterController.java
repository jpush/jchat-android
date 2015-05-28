package io.jchat.android.controller;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import io.jchat.android.R;
import cn.jpush.im.android.api.JMessageClient;
import io.jchat.android.activity.RegisterActivity;
import io.jchat.android.tools.HandleResponseCode;
import io.jchat.android.view.LoadingDialog;
import io.jchat.android.view.LoginDialog;
import io.jchat.android.view.RegisterView;
import cn.jpush.im.api.BasicCallback;

public class RegisterController implements RegisterView.Listener, OnClickListener {

    private RegisterView mRegisterView;
    private RegisterActivity mContext;
    private Dialog mLoginDialog;
    private int mAfterMeasureSize;
    private int mPreMeasureSize;

    public RegisterController(RegisterView registerView, RegisterActivity context) {
        this.mRegisterView = registerView;
        this.mContext = context;

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.regist_btn:
                Log.i("Tag", "[register]register event execute!");
                final String userId = mRegisterView.getUserId();
                final String password = mRegisterView.getPassword();

                if (userId.equals("")) {
                    mRegisterView.userNameError(mContext);
                    break;
                } else if (password.equals("")) {
                    mRegisterView.passwordError(mContext);
                    break;
                } else if (password.length() > 128 || password.length() < 4) {
                    mRegisterView.passwordLengthError(mContext);
                    break;
                }

                LoadingDialog ld = new LoadingDialog();
                final Dialog dialog = ld.createLoadingDialog(mContext, mContext.getString(R.string.registering_hint));
                dialog.show();
                JMessageClient.register(userId, password, new BasicCallback() {

                    @Override
                    public void gotResult(final int status, final String desc) {
                        mContext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                if (status == 0) {
                                    LoginDialog loginDialog = new LoginDialog();
                                    mLoginDialog = loginDialog.createLoadingDialog(mContext);
                                    mLoginDialog.show();
                                    JMessageClient.login(userId, password, new BasicCallback() {
                                        @Override
                                        public void gotResult(final int status, String desc) {
                                            if(status == 0){
                                                mContext.OnRegistSuccess();
                                            }else {
                                                mContext.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mLoginDialog.dismiss();
                                                        HandleResponseCode.onHandle(mContext, status);
                                                    }
                                                });
                                            }
                                        }
                                    });
                                } else {
                                    HandleResponseCode.onHandle(mContext, status);
                                }
                            }
                        });
                    }
                });
                break;
        }
    }

    public void dismissDialog() {
        if(mLoginDialog != null)
            mLoginDialog.dismiss();
    }

    @Override
    public void onSoftKeyboardShown(int softKeyboardHeight) {
        if (softKeyboardHeight > 300) {
            mAfterMeasureSize = softKeyboardHeight;
        } else {
            mPreMeasureSize = softKeyboardHeight;
        }
        int h = mAfterMeasureSize - mPreMeasureSize;
        if (h > 300) {
            SharedPreferences sp = mContext.getSharedPreferences("JPushDemo", Context.MODE_PRIVATE);
            boolean writable = sp.getBoolean("writable", true);
            if (writable) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt("SoftKeyboardHeight", h);
                editor.putBoolean("writable", false);
                editor.commit();
            }
        }
    }
}
