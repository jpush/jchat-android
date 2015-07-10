package io.jchat.android.controller;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.jchat.android.R;
import cn.jpush.im.android.api.JMessageClient;
import io.jchat.android.activity.LoginActivity;
import io.jchat.android.tools.ActivityManager;
import io.jchat.android.tools.HandleResponseCode;
import io.jchat.android.view.DialogCreator;
import io.jchat.android.view.LoginView;
import cn.jpush.im.api.BasicCallback;

public class LoginController implements LoginView.Listener, OnClickListener, CompoundButton.OnCheckedChangeListener {

    private LoginView mLoginView;
    private LoginActivity mContext;

    public LoginController(LoginView mLoginView, LoginActivity context) {
        this.mLoginView = mLoginView;
        this.mContext = context;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.return_btn:
                mContext.finish();
                ActivityManager.removeActivity(mContext);
                break;
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
                DialogCreator ld = new DialogCreator();
                final Dialog dialog = ld.createLoadingDialog(mContext, mContext.getString(R.string.login_hint));
                dialog.show();
                JMessageClient.login(userId, password,
                        new BasicCallback() {
                            @Override
                            public void gotResult(final int status, final String desc) {
                                mContext.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.dismiss();
                                        if (status == 0) {
                                            mContext.StartMainActivity();
                                        } else {
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
            mLoginView.setRegistBtnVisable(View.INVISIBLE);
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
        } else {
            mLoginView.setRegistBtnVisable(View.VISIBLE);
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d("sdfs", "onCheckedChanged !!!! isChecked = " + isChecked);
        if (isChecked) {
            swapEnvironment(true);
        } else {
            swapEnvironment(false);
        }
    }

    private void swapEnvironment(boolean isTest) {
        try {
            Method method = JMessageClient.class.getDeclaredMethod("swapEnvironment", Context.class, Boolean.class);
            method.invoke(null, mContext, isTest);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
