package cn.jpush.im.android.demo.controller;


import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;

import cn.jpush.im.android.demo.R;

import cn.jpush.im.android.demo.activity.ChatActivity;
import cn.jpush.im.android.demo.activity.FriendInfoActivity;
import cn.jpush.im.android.demo.view.FriendInfoView;

public class FriendInfoController implements OnClickListener{

	private FriendInfoView mFriendInfoView;
	private FriendInfoActivity mContext;
	
	
	public FriendInfoController(FriendInfoView view, FriendInfoActivity context){
		this.mFriendInfoView = view;
		this.mContext = context;
		initData();
	}
	
	private void initData() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.friend_info_return_btn:
			mContext.finish();
			break;
		case R.id.friend_more_btn:
			break;
		case R.id.friend_send_msg_btn:
            ((Activity) ChatActivity.mChatActivity).finish();
			mContext.StartChatActivity();
            break;
            case R.id.friend_detail_avatar:
                mContext.startBrowserAvatar();
                break;
		}
	}

}
