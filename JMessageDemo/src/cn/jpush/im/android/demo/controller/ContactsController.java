package cn.jpush.im.android.demo.controller;

import android.view.View;
import android.view.View.OnClickListener;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.demo.activity.ContactsFragment;
import cn.jpush.im.android.demo.view.ContactsView;

public class ContactsController implements OnClickListener {

	private ContactsView mContactsView;
	private ContactsFragment mContactsActivity;
	
	public ContactsController(ContactsView mContactsView, ContactsFragment context){
		this.mContactsView = mContactsView;
		this.mContactsActivity = context;
		initContacts();
	}

    public void initContacts(){
        //初始化用户名列表
        List<String> userNameList = new ArrayList<String>();
    }
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		
		}
		
	}

}
