package io.jchat.android.controller;


import android.app.Dialog;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.api.BasicCallback;
import io.jchat.android.R;
import io.jchat.android.activity.FriendInfoActivity;
import io.jchat.android.application.JChatDemoApplication;
import io.jchat.android.tools.DialogCreator;
import io.jchat.android.tools.HandleResponseCode;
import io.jchat.android.view.FriendInfoView;
import io.jchat.android.view.SlipButton;

public class FriendInfoController implements OnClickListener, SlipButton.OnChangedListener {

    private FriendInfoView mFriendInfoView;
    private FriendInfoActivity mContext;


    public FriendInfoController(FriendInfoView view, FriendInfoActivity context) {
        this.mFriendInfoView = view;
        this.mContext = context;
        initData();
    }

    private void initData() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.friend_info_return_btn:
                Intent intent = new Intent();
                String nickname = mContext.getNickname();
                intent.putExtra(JChatDemoApplication.NICKNAME, nickname);
                mContext.setResult(JChatDemoApplication.RESULT_CODE_FRIEND_INFO, intent);
                mContext.finish();
                break;
            case R.id.friend_send_msg_btn:
                mContext.startChatActivity();
                break;
            case R.id.friend_detail_avatar:
                mContext.startBrowserAvatar();
                break;
            case R.id.name_rl:
//                Intent intent = new Intent();
//                intent.setClass(mContext, EditNoteNameActivity.class);
//                intent.putExtra("noteName", "ddsklf");
//                intent.putExtra("friendDescription", "kjdkjdlkj");
//                mContext.startActivity(intent);
                break;
        }
    }

    @Override
    public void onChanged(int id, final boolean flag) {
        List<String> list = new ArrayList<String>();
        list.add(mContext.getUserName());
        final Dialog dialog = DialogCreator.createLoadingDialog(mContext, mContext.getString(R.string.loading));
        dialog.show();
        if (flag) {
            JMessageClient.addUsersToBlacklist(list, new BasicCallback() {
                @Override
                public void gotResult(int status, String desc) {
                    dialog.dismiss();
                    if (status == 0) {
                        Log.d("FriendInfoController", "add user to black list success!");
                        Toast.makeText(mContext,
                                mContext.getString(R.string.add_to_blacklist_success_hint),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        mFriendInfoView.setCheck(false);
                        HandleResponseCode.onHandle(mContext, status, false);
                    }
                }
            });
        } else {
            JMessageClient.delUsersFromBlacklist(list, new BasicCallback() {
                @Override
                public void gotResult(int status, String desc) {
                    dialog.dismiss();
                    if (status == 0) {
                        Toast.makeText(mContext,
                                mContext.getString(R.string.remove_from_blacklist_hint),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        mFriendInfoView.setCheck(true);
                        HandleResponseCode.onHandle(mContext, status, false);
                    }
                }
            });
        }
    }
}
