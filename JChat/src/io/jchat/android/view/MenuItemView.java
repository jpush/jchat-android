package io.jchat.android.view;

import android.view.View;
import android.widget.LinearLayout;
import io.jchat.android.R;
import io.jchat.android.chatting.utils.SharePreferenceManager;


/**
 * Created by Ken on 2015/1/26.
 */
public class MenuItemView {

    private View mView;
    private LinearLayout mCreateGroupLl;
    private LinearLayout mAddFriendDirectLl;
    private LinearLayout mAddFriendLl;

    public MenuItemView(View view) {
        this.mView = view;
    }

    public void initModule() {
        mCreateGroupLl = (LinearLayout) mView.findViewById(R.id.create_group_ll);
        mAddFriendDirectLl = (LinearLayout) mView.findViewById(R.id.add_friend_direct_ll);
        mAddFriendLl = (LinearLayout) mView.findViewById(R.id.add_friend_with_confirm_ll);
    }

    public void setListeners(View.OnClickListener listener) {
        mCreateGroupLl.setOnClickListener(listener);
        mAddFriendDirectLl.setOnClickListener(listener);
        mAddFriendLl.setOnClickListener(listener);
    }

    public void showAddFriendDirect() {
        mAddFriendDirectLl.setVisibility(View.VISIBLE);
        mAddFriendLl.setVisibility(View.GONE);
    }

    public void showAddFriend() {
        mAddFriendDirectLl.setVisibility(View.GONE);
        mAddFriendLl.setVisibility(View.VISIBLE);
    }
}
