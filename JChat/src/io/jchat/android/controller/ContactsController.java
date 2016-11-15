package io.jchat.android.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.ContactManager;
import cn.jpush.im.android.api.callback.GetUserInfoListCallback;
import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.R;
import io.jchat.android.activity.ContactsFragment;
import io.jchat.android.activity.FriendInfoActivity;
import io.jchat.android.adapter.ContactsListAdapter;
import io.jchat.android.application.JChatDemoApplication;
import io.jchat.android.chatting.utils.HandleResponseCode;
import io.jchat.android.view.ContactsView;

public class ContactsController implements OnClickListener, AdapterView.OnItemClickListener {

	private ContactsView mContactsView;
	private Activity mContext;
    private List<UserInfo> mList;
    private ContactsListAdapter mAdapter;
	
	public ContactsController(ContactsView mContactsView, Activity context) {
		this.mContactsView = mContactsView;
		this.mContext = context;
		initContacts();
	}

    public void initContacts() {
        ContactManager.getFriendList(new GetUserInfoListCallback() {
            @Override
            public void gotResult(int status, String desc, List<UserInfo> list) {
                if (status == 0) {
                    mList = list;
                    mAdapter = new ContactsListAdapter(mContext, list);
                    mContactsView.setAdapter(mAdapter);
                } else {
                    HandleResponseCode.onHandle(mContext, status, false);
                }
            }
        });
    }
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
            case R.id.search_btn:
                break;
            case R.id.recommend_rl:
                break;
            case R.id.group_chat_rl:
                break;
		}
		
	}

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        UserInfo userInfo = mList.get(position);
        Intent intent = new Intent();
        intent.putExtra(JChatDemoApplication.TARGET_ID, userInfo.getUserName());
        intent.putExtra(JChatDemoApplication.TARGET_APP_KEY, userInfo.getAppKey());
        intent.setClass(mContext, FriendInfoActivity.class);
    }
}
