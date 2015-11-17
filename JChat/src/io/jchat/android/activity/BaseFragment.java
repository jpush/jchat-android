package io.jchat.android.activity;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import io.jchat.android.tools.FileHelper;

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
    protected int mAvatarSize;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JMessageClient.registerEventReceiver(this);
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        mDensity = dm.density;
        mDensityDpi = dm.densityDpi;
        mWidth = dm.widthPixels;
        mAvatarSize = (int) (50 * mDensity);
    }

    @Override
    public void onDestroy() {
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
                intent.putExtra("userName", myInfo.getUserName());
                File avatar = myInfo.getAvatarFile();
                if (null != avatar && avatar.exists()) {
                    intent.putExtra("avatarFilePath", avatar.getAbsolutePath());
                }else {
                    String path = FileHelper.getUserAvatarPath(myInfo.getUserName());
                    avatar = new File(path);
                    if (avatar.exists()) {
                        intent.putExtra("avatarFilePath", avatar.getAbsolutePath());
                    }
                }
                Log.i(TAG, "userName " + myInfo.getUserName());
                JMessageClient.logout();
                intent.setClass(BaseFragment.this.getActivity(), ReloginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                BaseFragment.this.getActivity().finish();
            } else {
                Log.d(TAG, "user info is null!");
            }
        }
    };

    public void onEventMainThread(UserLogoutEvent event) {
        Context context = BaseFragment.this.getActivity();
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
                intent.setClass(BaseFragment.this.getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                BaseFragment.this.getActivity().finish();
            }
        };
        Context context = BaseFragment.this.getActivity();
        String title = context.getString(R.string.user_logout_dialog_title);
        String msg = context.getString(R.string.user_delete_hint_message);
        dialog = DialogCreator.createBaseCustomDialog(context, title, msg, listener);
        dialog.show();
    }


}
