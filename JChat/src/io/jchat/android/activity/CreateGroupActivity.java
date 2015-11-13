package io.jchat.android.activity;

import android.content.Intent;
import android.os.Bundle;
import io.jchat.android.R;
import io.jchat.android.application.JChatDemoApplication;
import io.jchat.android.controller.CreateGroupController;
import io.jchat.android.view.CreateGroupView;

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


	public void startChatActivity(long groupId, String groupName) {
		Intent intent = new Intent();
		intent.putExtra(JChatDemoApplication.IS_GROUP, true);
		//设置跳转标志
		intent.putExtra("fromGroup", true);
		intent.putExtra(JChatDemoApplication.GROUP_ID, groupId);
		intent.putExtra(JChatDemoApplication.GROUP_NAME, groupName);
		intent.setClass(this, ChatActivity.class);
		startActivity(intent);
		finish();
	}

}
