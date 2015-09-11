package io.jchat.android.activity;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
 * A simple {@link Fragment} subclass.
 */
public class BaseFragment extends Fragment {

    private static final String TAG = "BaseFragment";

    private Dialog dialog;

    private UserInfo myInfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JMessageClient.registerEventReceiver(this);
    }

    @Override
    public void onDestroy() {
        JMessageClient.unRegisterEventReceiver(this);
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
                    intent.putExtra("userAvatar", avatar.getAbsolutePath());
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
