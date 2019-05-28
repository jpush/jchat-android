package jiguang.chat.view;

import android.view.View;
import android.widget.RelativeLayout;

import jiguang.chat.R;


public class MenuItemView {

    private View mView;
    private RelativeLayout mCreateGroupLl;
    private RelativeLayout mAddFriendLl;
    private RelativeLayout mSendMsgLl;
    private RelativeLayout mLl_saoYiSao;
    private RelativeLayout mAdd_open_group;

    public MenuItemView(View view) {
        this.mView = view;
    }

    public void initModule() {
        mCreateGroupLl = mView.findViewById(R.id.create_group_ll);
        mAddFriendLl = mView.findViewById(R.id.add_friend_with_confirm_ll);
        mSendMsgLl = mView.findViewById(R.id.send_message_ll);
        mLl_saoYiSao = mView.findViewById(R.id.ll_saoYiSao);
        mAdd_open_group = mView.findViewById(R.id.add_open_group);
    }

    public void setListeners(View.OnClickListener listener) {
        mCreateGroupLl.setOnClickListener(listener);
        mAddFriendLl.setOnClickListener(listener);
        mSendMsgLl.setOnClickListener(listener);
        mLl_saoYiSao.setOnClickListener(listener);
        mAdd_open_group.setOnClickListener(listener);
    }

    public void showAddFriendDirect() {
        mAddFriendLl.setVisibility(View.GONE);
    }

    public void showAddFriend() {
        mAddFriendLl.setVisibility(View.VISIBLE);
    }

    public void setColor() {

    }
}
