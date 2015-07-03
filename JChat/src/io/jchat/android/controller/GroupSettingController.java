package io.jchat.android.controller;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

import io.jchat.android.R;

import cn.jpush.im.android.api.JMessageClient;
import io.jchat.android.activity.GroupSettingActivity;
import io.jchat.android.view.GroupSettingView;

public class GroupSettingController implements OnClickListener{

	private GroupSettingView mGroupSettingView;
	private GroupSettingActivity mContext;
	int mWhich;
	
	public GroupSettingController(GroupSettingView view,
			GroupSettingActivity context, int which){
		this.mGroupSettingView = view;
		this.mContext = context;
		this.mWhich = which;
		setTitle(mWhich);
	}
	
	private void setTitle(int which) {
		if(which == 1){
			mGroupSettingView.setTitleText(mContext.getString(R.string.group_name_hit));
//			String groupName = JMessageClient.getGroupInfo().getGroupName();
			//初始化EditText
//			mGroupSettingView.setHintText();
		}
		if(which == 2){
			mGroupSettingView.setTitleText(mContext.getString(R.string.group_my_name_hit));
			//初始化EditText
			mGroupSettingView.setEditText(JMessageClient.getMyInfo().getNickname());
			mGroupSettingView.setVisible();
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.commit_btn:
			Intent intent = new Intent();
			String resultName = mGroupSettingView.getResultName();
			if(!resultName.equals("")){
			//TODO 更新至服务器并保存到数据库中
				
			intent.putExtra("resultName", resultName);
			mContext.setResult(2,intent);
			}
			mContext.finish();
			break;
		case R.id.cancel_btn:
			mContext.finish();
			break;
		}
		
	}

}
