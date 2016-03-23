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
import android.view.WindowManager;

import java.io.File;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.event.LoginStateChangeEvent;
import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.R;
import io.jchat.android.tools.DialogCreator;
import io.jchat.android.tools.FileHelper;
import io.jchat.android.tools.SharePreferenceManager;

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
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mHandler = new BaseHandler();
        //订阅接收消息,子类只要重写onEvent就能收到
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
                String path;
                File avatar = myInfo.getAvatarFile();
                if (avatar != null && avatar.exists()) {
                    path = avatar.getAbsolutePath();
                } else {
                    path = FileHelper.getUserAvatarPath(myInfo.getUserName());
                }
                Log.i(TAG, "userName " + myInfo.getUserName());
                SharePreferenceManager.setCachedUsername(myInfo.getUserName());
                SharePreferenceManager.setCachedAvatarPath(path);
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

    /**
     * 接收登录状态相关事件:登出事件,修改密码事件及被删除事件
     * @param event 登录状态相关事件
     */
    public void onEventMainThread(LoginStateChangeEvent event) {
        LoginStateChangeEvent.Reason reason = event.getReason();
        myInfo = event.getMyInfo();
        switch (reason) {
            case user_password_change:
                String title = mContext.getString(R.string.change_password);
                String msg = mContext.getString(R.string.change_password_message);
                dialog = DialogCreator.createBaseCustomDialog(mContext, title, msg, onClickListener);
                break;
            case user_logout:
                title = mContext.getString(R.string.user_logout_dialog_title);
                msg = mContext.getString(R.string.user_logout_dialog_message);
                dialog = DialogCreator.createBaseCustomDialog(mContext, title, msg, onClickListener);
                break;
            case user_deleted:
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
                title = mContext.getString(R.string.user_logout_dialog_title);
                msg = mContext.getString(R.string.user_delete_hint_message);
                dialog = DialogCreator.createBaseCustomDialog(mContext, title, msg, listener);
                break;
        }
        dialog.getWindow().setLayout((int) (0.8 * mWidth), WindowManager.LayoutParams.WRAP_CONTENT);
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
