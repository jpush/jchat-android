package io.jchat.android.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import cn.jpush.im.android.api.ContactManager;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoListCallback;
import cn.jpush.im.android.api.event.ContactNotifyEvent;
import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.R;
import io.jchat.android.controller.ContactsController;
import io.jchat.android.view.ContactsView;

public class ContactsFragment extends BaseFragment{
	private View mRootView;
	private ContactsView mContactsView;
	private ContactsController mContactsController;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		LayoutInflater layoutInflater = getActivity().getLayoutInflater();
		mRootView = layoutInflater.inflate(R.layout.fragment_contacts,
				(ViewGroup) getActivity().findViewById(R.id.main_view), false);
		mContactsView = (ContactsView) mRootView.findViewById(R.id.contacts_view);
		mContactsView.initModule();
		mContactsController = new ContactsController(mContactsView, this.getActivity());
		mContactsView.setOnClickListener(mContactsController);
        mContactsView.setItemListeners(mContactsController);
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		ViewGroup p = (ViewGroup) mRootView.getParent();
		if (p != null) {
			p.removeAllViewsInLayout();
		}
		return mRootView;
	}

	public void onEvent(ContactNotifyEvent event) {
        if (event.getType() == ContactNotifyEvent.Type.invite_accepted) {

        } else if (event.getType() == ContactNotifyEvent.Type.invite_declined) {

        } else {

        }
    }
}