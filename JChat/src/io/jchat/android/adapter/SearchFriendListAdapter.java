package io.jchat.android.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.model.UserInfo;


public class SearchFriendListAdapter extends BaseAdapter {

    private List<UserInfo> users = new ArrayList<UserInfo>();

    public SearchFriendListAdapter(List<UserInfo> users) {
        this.users = users;
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int i) {
        return users.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        return null;
    }
}
