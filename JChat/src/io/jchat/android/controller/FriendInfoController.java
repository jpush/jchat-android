package io.jchat.android.controller;


import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.R;
import io.jchat.android.activity.FriendInfoActivity;
import io.jchat.android.activity.FriendSettingActivity;
import io.jchat.android.view.FriendInfoView;

public class FriendInfoController implements OnClickListener{

    private FriendInfoActivity mContext;
    private UserInfo friendInfo;

    public FriendInfoController(FriendInfoView friendInfoView, FriendInfoActivity context) {
        this.mContext = context;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_goToChat:
                mContext.startChatActivity();
                break;
            case R.id.iv_friendPhoto:
                mContext.startBrowserAvatar();
                break;
            case R.id.jmui_commit_btn:
                Intent intent = new Intent(mContext, FriendSettingActivity.class);
                intent.putExtra("userName", friendInfo.getUserName());
                intent.putExtra("noteName", friendInfo.getNotename());
                mContext.startActivity(intent);
                break;
            case R.id.return_btn:
                mContext.finish();
                break;
            default:
                break;
        }
    }

    public void setFriendInfo(UserInfo info) {
        friendInfo = info;
    }

}
