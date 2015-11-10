package io.jchat.android.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import java.io.File;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.event.UserDeletedEvent;
import cn.jpush.im.android.api.event.UserLogoutEvent;
import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.R;
import io.jchat.android.tools.DialogCreator;

/**
 * Created by Ken on 2015/3/13.
 */
public class BaseActivity extends Activity {
    private static final String TAG = "BaseActivity";

    protected BaseHandler mHandler;
    protected float mDensity;
    protected int mDensityDpi;
    protected int mAvatarSize;
    protected int mWidth;
    protected int mHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mHandler = new BaseHandler();
        JMessageClient.registerEventReceiver(this);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mDensity = dm.density;
        mDensityDpi = dm.densityDpi;
        mWidth = dm.widthPixels;
        mHeight = dm.heightPixels;
        mAvatarSize = (int) (50 * mDensity);
    }

    private Dialog dialog;

    private UserInfo myInfo;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dialog.dismiss();
            Intent intent = new Intent();
            if (null != myInfo) {
                intent.putExtra("userName", myInfo.getUserName());
                File avatar = myInfo.getAvatarFile();
                if (null != avatar && avatar.exists()) {
                    intent.putExtra("avatarFilePath", avatar.getAbsolutePath());
                }
                Log.i(TAG, "userName " + myInfo.getUserName());
                JMessageClient.logout();
                intent.setClass(BaseActivity.this, ReloginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                BaseActivity.this.finish();
            } else {
                Log.d(TAG, "user info is null!");
            }
        }
    };

    public void onEventMainThread(UserLogoutEvent event) {
        Context context = BaseActivity.this;
        String title = context.getString(R.string.user_logout_dialog_title);
        String msg = context.getString(R.string.user_logout_dialog_message);
        dialog = DialogCreator.createBaseCustomDialog(context, title, msg, onClickListener);
        myInfo = event.getMyInfo();
        dialog.show();
    }

    public void onEventMainThread(UserDeletedEvent event){
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent();
                intent.setClass(BaseActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                BaseActivity.this.finish();
            }
        };
        Context context = BaseActivity.this;
        String title = context.getString(R.string.user_logout_dialog_title);
        String msg = context.getString(R.string.user_delete_hint_message);
        dialog = DialogCreator.createBaseCustomDialog(context, title, msg, listener);
        dialog.show();
    }


    @Override
    protected void onDestroy() {
        JMessageClient.unRegisterEventReceiver(this);
        if (dialog != null){
            dialog.dismiss();
        }
        super.onDestroy();
    }

    public class BaseHandler extends Handler {

        @Override
        public void handleMessage(android.os.Message msg) {
            handleMsg(msg);
        }
    }

    public void handleMsg(Message message) {
    }

}
