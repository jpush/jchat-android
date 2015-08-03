package io.jchat.android.controller;


import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

import io.jchat.android.R;
import io.jchat.android.activity.EditNoteNameActivity;
import io.jchat.android.activity.FriendInfoActivity;
import io.jchat.android.view.FriendInfoView;

public class FriendInfoController implements OnClickListener {

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
                mContext.finish();
                break;
            case R.id.friend_send_msg_btn:
                mContext.StartChatActivity();
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

}
