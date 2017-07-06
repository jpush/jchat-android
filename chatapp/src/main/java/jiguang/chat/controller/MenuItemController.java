package jiguang.chat.controller;

import android.app.Dialog;
import android.content.Intent;
import android.view.View;

import jiguang.chat.R;
import jiguang.chat.activity.SearchForAddFriendActivity;
import jiguang.chat.activity.CreateGroupActivity;
import jiguang.chat.activity.fragment.ConversationListFragment;
import jiguang.chat.view.MenuItemView;

/**
 * Created by ${chenyn} on 2017/4/9.
 */

public class MenuItemController implements View.OnClickListener {
    private MenuItemView mMenuItemView;
    private ConversationListFragment mFragment;
    private ConversationListController mController;
    private Dialog mLoadingDialog;
    private Dialog mAddFriendDialog;
    private int mWidth;

    public MenuItemController(MenuItemView view, ConversationListFragment fragment, ConversationListController controller, int width) {
        this.mMenuItemView = view;
        this.mFragment = fragment;
        this.mController = controller;
        mWidth = width;
    }

    //会话界面的加号
    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.create_group_ll:
                mFragment.dismissPopWindow();
                intent = new Intent(mFragment.getContext(), CreateGroupActivity.class);
                mFragment.getContext().startActivity(intent);
                break;
            case R.id.add_friend_with_confirm_ll:
                mFragment.dismissPopWindow();
                intent = new Intent(mFragment.getContext(), SearchForAddFriendActivity.class);
                intent.setFlags(1);
                mFragment.startActivity(intent);
                break;
            case R.id.send_message_ll:
                mFragment.dismissPopWindow();
                intent = new Intent(mFragment.getContext(), SearchForAddFriendActivity.class);
                intent.setFlags(2);
                mFragment.startActivity(intent);
                break;
            default:
                break;
        }

    }
}
