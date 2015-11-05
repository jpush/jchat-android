package io.jchat.android.controller;

import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import io.jchat.android.R;
import cn.jpush.im.android.api.JMessageClient;
import io.jchat.android.activity.RegisterActivity;
import io.jchat.android.tools.HandleResponseCode;
import io.jchat.android.tools.SharePreferenceManager;
import io.jchat.android.tools.DialogCreator;
import io.jchat.android.view.LoginDialog;
import io.jchat.android.view.RegisterView;
import cn.jpush.im.api.BasicCallback;

public class RegisterController implements RegisterView.Listener, OnClickListener {

    private RegisterView mRegisterView;
    private RegisterActivity mContext;
    private Dialog mLoginDialog;

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

                final Dialog dialog = DialogCreator.createLoadingDialog(mContext, mContext.getString(R.string.registering_hint));
                dialog.show();
                JMessageClient.register(userId, password, new BasicCallback() {

                    @Override
                    public void gotResult(final int status, final String desc) {
                        dialog.dismiss();
                        if (status == 0) {
                            LoginDialog loginDialog = new LoginDialog();
                            mLoginDialog = loginDialog.createLoadingDialog(mContext);
                            mLoginDialog.show();
                            JMessageClient.login(userId, password, new BasicCallback() {
                                @Override
                                public void gotResult(final int status, String desc) {
                                    if (status == 0) {
                                        mContext.onRegistSuccess();
                                    } else {
                                        mLoginDialog.dismiss();
                                        HandleResponseCode.onHandle(mContext, status, false);
                                    }
                                }
                            });
                        } else {
                            HandleResponseCode.onHandle(mContext, status, false);
                        }
                    }
                });
                break;
            case R.id.return_btn:
                mContext.finish();
                break;
        }
    }

    public void dismissDialog() {
        if(mLoginDialog != null)
            mLoginDialog.dismiss();
    }

    @Override
    public void onSoftKeyboardShown(int w, int h, int oldw, int oldh) {
        int softKeyboardHeight = oldh - h;
        if(softKeyboardHeight > 300){
            boolean writable = SharePreferenceManager.getCachedWritableFlag();
            if (writable) {
                SharePreferenceManager.setCachedKeyboardHeight(softKeyboardHeight);
                SharePreferenceManager.setCachedWritableFlag(false);
            }
        }
    }
}
