package io.jchat.android.activity;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import java.io.File;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.event.LoginStateChangeEvent;
import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.R;
import io.jchat.android.chatting.utils.DialogCreator;
import io.jchat.android.chatting.utils.FileHelper;
import io.jchat.android.chatting.utils.SharePreferenceManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class BaseFragment extends Fragment {

    private static final String TAG = "BaseFragment";

    private Dialog dialog;

    private UserInfo myInfo;
    protected float mDensity;
    protected int mDensityDpi;
    protected int mWidth;
    protected int mHeight;
    protected float mRatio;
    protected int mAvatarSize;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getActivity();
        //订阅接收消息,子类只要重写onEvent就能收到消息
        JMessageClient.registerEventReceiver(this);
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        mDensity = dm.density;
        mDensityDpi = dm.densityDpi;
        mWidth = dm.widthPixels;
        mHeight = dm.heightPixels;
        mRatio = Math.min((float) mWidth / 720, (float) mHeight / 1280);
        mAvatarSize = (int) (50 * mDensity);
    }

    @Override
    public void onDestroy() {
        //注销消息接收
        JMessageClient.unRegisterEventReceiver(this);
        if (dialog != null) {
            dialog.dismiss();
        }
        super.onDestroy();
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dialog.dismiss();
            Intent intent = new Intent();
            if (null != myInfo) {
                intent.setClass(getActivity(), ReloginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().finish();
            } else {
                Log.d(TAG, "user info is null! Jump to Login activity");
                intent.setClass(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
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
        }
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
                        intent.setClass(mContext, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        BaseFragment.this.getActivity().finish();
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

}
