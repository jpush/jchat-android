package cn.jpush.im.android.demo.activity;

import android.content.Intent;
import android.os.Bundle;

import cn.jpush.im.android.demo.R;

import cn.jpush.im.android.demo.controller.CreateGroupController;
import cn.jpush.im.android.demo.view.CreateGroupView;

/*
创建群聊
 */
public class CreateGroupActivity extends BaseActivity{
	
	private CreateGroupView mCreateGroupView;
	private CreateGroupController mCreateGroupController;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_group);
		mCreateGroupView = (CreateGroupView) findViewById(R.id.create_group_view);
		mCreateGroupView.initModule();
		mCreateGroupController = new CreateGroupController(mCreateGroupView, this);
		mCreateGroupView.setListeners(mCreateGroupController);
	}


	public void StartChatActivity(long groupID, String groupName) {
		Intent intent = new Intent();
		intent.putExtra("isGroup", true);
		//设置跳转标志
		intent.putExtra("fromGroup", true);
		intent.putExtra("groupID", groupID);
		intent.putExtra("groupName", groupName);
		intent.setClass(this, ChatActivity.class);
		startActivity(intent);
		finish();
	}

}
