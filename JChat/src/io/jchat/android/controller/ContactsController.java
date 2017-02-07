package io.jchat.android.controller;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.activeandroid.ActiveAndroid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.jpush.im.android.api.ContactManager;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoListCallback;
import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.R;
import io.jchat.android.activity.FriendRecommendActivity;
import io.jchat.android.adapter.StickyListAdapter;
import io.jchat.android.chatting.utils.DialogCreator;
import io.jchat.android.chatting.utils.HandleResponseCode;
import io.jchat.android.database.FriendEntry;
import io.jchat.android.database.UserEntry;
import io.jchat.android.tools.HanziToPinyin;
import io.jchat.android.tools.PinyinComparator;
import io.jchat.android.view.ContactsView;
import io.jchat.android.view.SideBar;

public class ContactsController implements OnClickListener, SideBar.OnTouchingLetterChangedListener,
        TextWatcher{

	private ContactsView mContactsView;
	private Activity mContext;
    private List<FriendEntry> mList = new ArrayList<FriendEntry>();
    private StickyListAdapter mAdapter;

	public ContactsController(ContactsView mContactsView, Activity context) {
		this.mContactsView = mContactsView;
		this.mContext = context;
	}

    public void initContacts() {
        final UserEntry user = UserEntry.getUser(JMessageClient.getMyInfo().getUserName(),
                JMessageClient.getMyInfo().getAppKey());
        List<FriendEntry> friends = user.getFriends();
        if (friends.size() == 0 ) {
            mContactsView.showLoadingHeader();
            ContactManager.getFriendList(new GetUserInfoListCallback() {
                @Override
                public void gotResult(int status, String desc, List<UserInfo> list) {
                    if (status == 0) {
                        if (list.size() != 0) {
                            ActiveAndroid.beginTransaction();
                            try {
                                for (UserInfo userInfo : list) {
                                    String displayName = userInfo.getNotename();
                                    if (TextUtils.isEmpty(displayName)) {
                                        displayName = userInfo.getNickname();
                                        if (TextUtils.isEmpty(displayName)) {
                                            displayName = userInfo.getUserName();
                                        }
                                    }
                                    String letter;
                                    ArrayList<HanziToPinyin.Token> tokens = HanziToPinyin.getInstance()
                                            .get(displayName);
                                    StringBuilder sb = new StringBuilder();
                                    if (tokens != null && tokens.size() > 0) {
                                        for (HanziToPinyin.Token token : tokens) {
                                            if (token.type == HanziToPinyin.Token.PINYIN) {
                                                sb.append(token.target);
                                            } else {
                                                sb.append(token.source);
                                            }
                                        }
                                    }
                                    String sortString = sb.toString().substring(0, 1).toUpperCase();
                                    if (sortString.matches("[A-Z]")) {
                                        letter = sortString.toUpperCase();
                                    } else {
                                        letter = "#";
                                    }
                                    //避免重复请求时导致数据重复
                                    FriendEntry friend = FriendEntry.getFriend(user,
                                            userInfo.getUserName(), userInfo.getAppKey());
                                    if (null == friend) {
                                        if (TextUtils.isEmpty(userInfo.getAvatar())) {
                                            friend = new FriendEntry(userInfo.getUserName(), userInfo.getAppKey(),
                                                    null, displayName, letter, user);
                                        } else {
                                            friend = new FriendEntry(userInfo.getUserName(), userInfo.getAppKey(),
                                                    userInfo.getAvatarFile().getAbsolutePath(), displayName, letter, user);
                                        }
                                        friend.save();
                                        mList.add(friend);
                                    }
                                }
                                ActiveAndroid.setTransactionSuccessful();
                            } finally {
                                ActiveAndroid.endTransaction();
                            }
                        }
                        mContactsView.dismissLoadingHeader();
                        Collections.sort(mList, new PinyinComparator());
                        mAdapter = new StickyListAdapter(mContext, mList, false);
                        mContactsView.setAdapter(mAdapter);
                    } else {
                        mContactsView.dismissLoadingHeader();
                        HandleResponseCode.onHandle(mContext, status, false);
                    }
                }
            });
        } else {
            mList = friends;
            Collections.sort(mList, new PinyinComparator());
            mAdapter = new StickyListAdapter(mContext, mList, false);
            mContactsView.setAdapter(mAdapter);
        }
    }

	@Override
	public void onClick(View v) {
        Intent intent = new Intent();
		switch(v.getId()){
            case R.id.delete_ib:
                mContactsView.clearSearchText();
                break;
            case R.id.verify_rl:
                intent.setClass(mContext, FriendRecommendActivity.class);
                mContext.startActivity(intent);
                mContactsView.dismissNewFriends();
                break;
            case R.id.group_chat_rl:
                break;
		}
		
	}

    public void refresh(FriendEntry entry) {
        mList.add(entry);
        if (null == mAdapter) {
            mAdapter = new StickyListAdapter(mContext, mList, false);
        } else {
            Collections.sort(mList, new PinyinComparator());
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTouchingLetterChanged(String s) {
        //该字母首次出现的位置
        if (null != mAdapter) {
            int position = mAdapter.getSectionForLetter(s);
            Log.d("SelectFriendController", "Section position: " + position);
            if (position != -1 && position < mAdapter.getCount()) {
                mContactsView.setSelection(position);
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        filterData(s.toString());
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    /**
     * 根据输入框中的值来过滤数据并更新ListView
     * @param filterStr
     */
    private void filterData(String filterStr) {
        List<FriendEntry> filterDateList = new ArrayList<FriendEntry>();

        if (TextUtils.isEmpty(filterStr)) {
            filterDateList = mList;
        } else {
            filterDateList.clear();
            for(FriendEntry entry : mList) {
                String name = entry.displayName;
                if (name.contains(filterStr) || name.startsWith(filterStr)
                        || entry.letter.equals(filterStr.substring(0, 1).toUpperCase())) {
                    filterDateList.add(entry);
                }
            }
        }

        // 根据a-z进行排序
        if (null != filterDateList && null != mAdapter) {
            Collections.sort(filterDateList, new PinyinComparator());
            mAdapter.updateListView(filterDateList);
        }
    }
}
