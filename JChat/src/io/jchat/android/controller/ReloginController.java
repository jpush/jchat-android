package io.jchat.android.controller;

import android.app.Activity;
import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import io.jchat.android.R;

import cn.jpush.im.android.api.JMessageClient;
import io.jchat.android.activity.ReloginActivity;
import io.jchat.android.tools.HandleResponseCode;
import io.jchat.android.view.DialogCreator;
import io.jchat.android.view.ReloginView;
import cn.jpush.im.api.BasicCallback;

public class ReloginController implements ReloginView.Listener, OnClickListener {

    private ReloginView mReloginView;
    private ReloginActivity mContext;
    private Dialog mLoadingDialog = null;
    private String mUserName;

    public ReloginController(ReloginView reloginView, ReloginActivity context, String userName) {
        this.mReloginView = reloginView;
        this.mContext = context;
        this.mUserName = userName;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.relogin_btn:
                //隐藏软键盘
                InputMethodManager manager = ((InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE));
                if (mContext.getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
                    if (mContext.getCurrentFocus() != null)
                        manager.hideSoftInputFromWindow(mContext.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                final String password = mReloginView.getPassword();

                if (password.equals("")) {
                    mReloginView.passwordError(mContext);
                    break;
                }
                DialogCreator ld = new DialogCreator();
                mLoadingDialog = ld.createLoadingDialog(mContext, mContext.getString(R.string.login_hint));
                mLoadingDialog.show();
                Log.i("ReloginController", "mUserName: " + mUserName);
                JMessageClient.login(mUserName, password, new BasicCallback() {

                    @Override
                    public void gotResult(final int status, final String desc) {
                        mContext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mLoadingDialog.dismiss();
                                if (status == 0) {
                                    mContext.StartRelogin();
                                } else {
                                    HandleResponseCode.onHandle(mContext, status, false);
                                }
                            }
                        });
                    }
                });

                break;

            case R.id.relogin_switch_user_btn:
                mContext.StartSwitchUser();
                break;
            case R.id.register_btn:
                mContext.StartRegisterActivity();
                break;
        }

    }

    @Override
    public void onSoftKeyboardShown(int w, int h, int oldw, int oldh) {
        int softKeyboardHeight = oldh - h;
        if(softKeyboardHeight > 300){
            mReloginView.setRegisterBtnVisible(View.INVISIBLE);
            mReloginView.setToBottom();
        }else {
            mReloginView.setRegisterBtnVisible(View.VISIBLE);
        }
    }

}
