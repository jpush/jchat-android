package cn.jpush.im.android.demo.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.jpush.im.android.demo.R;

import cn.jpush.im.android.demo.controller.ContactsController;
import cn.jpush.im.android.demo.view.ContactsView;

public class ContactsFragment extends Fragment{
	private View mRootView;
	private ContactsView mContactsView;
	private ContactsController mContactsController;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		LayoutInflater layoutInflater = getActivity().getLayoutInflater();
		mRootView = layoutInflater.inflate(R.layout.fragment_contacts,
				(ViewGroup) getActivity().findViewById(R.id.main_view),
				false);
		mContactsView = (ContactsView) mRootView.findViewById(R.id.contacts_view);
		mContactsView.initModule();
		mContactsController = new ContactsController(mContactsView, this);
		mContactsView.setOnClickListener(mContactsController);
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
}
