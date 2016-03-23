package io.jchat.android.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.CreateGroupCallback;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.R;
import io.jchat.android.activity.ChatActivity;
import io.jchat.android.activity.ConversationListFragment;
import io.jchat.android.application.JChatDemoApplication;
import io.jchat.android.tools.DialogCreator;
import io.jchat.android.tools.HandleResponseCode;
import io.jchat.android.view.MenuItemView;

/**
 * Created by Ken on 2015/1/26.
 */
public class MenuItemController implements View.OnClickListener {

    private MenuItemView mMenuItemView;
    private ConversationListFragment mContext;
    private ConversationListController mController;
    private Dialog mLoadingDialog;
    private Dialog mAddFriendDialog;
    private int mWidth;

    public MenuItemController(MenuItemView view, ConversationListFragment context,
                              ConversationListController controller, int width) {
        this.mMenuItemView = view;
        this.mContext = context;
        this.mController = controller;
        mWidth = width;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_group_ll:
                mContext.dismissPopWindow();
//                mContext.StartCreateGroupActivity();
                mLoadingDialog = DialogCreator.createLoadingDialog(mContext.getActivity(),
                        mContext.getString(R.string.creating_hint));
                mLoadingDialog.show();
                JMessageClient.createGroup("", "", new CreateGroupCallback() {

                    @Override
                    public void gotResult(final int status, String msg, final long groupId) {
                        mLoadingDialog.dismiss();
                        if (status == 0) {
                            Conversation conv = Conversation.createGroupConversation(groupId);
                            mController.refreshConvList(conv);
                            Intent intent = new Intent();
                            intent.putExtra(JChatDemoApplication.IS_GROUP, true);
                            //设置跳转标志
                            intent.putExtra("fromGroup", true);
                            intent.putExtra(JChatDemoApplication.MEMBERS_COUNT, 1);
                            intent.putExtra(JChatDemoApplication.GROUP_ID, groupId);
                            intent.putExtra(JChatDemoApplication.TARGET_ID, String.valueOf(groupId));
                            intent.setClass(mContext.getActivity(), ChatActivity.class);
                            mContext.startActivity(intent);
                        } else {
                            HandleResponseCode.onHandle(mContext.getActivity(), status, false);
                            Log.i("CreateGroupController", "status : " + status);
                        }
                    }
                });
                break;
            case R.id.add_friend_ll:
                mContext.dismissPopWindow();
                mAddFriendDialog = new Dialog(mContext.getActivity(), R.style.default_dialog_style);
                final View view = LayoutInflater.from(mContext.getActivity())
                        .inflate(R.layout.dialog_add_friend_to_conv_list, null);
                mAddFriendDialog.setContentView(view);
                mAddFriendDialog.getWindow().setLayout((int) (0.8 * mWidth), WindowManager.LayoutParams.WRAP_CONTENT);
                mAddFriendDialog.show();
                final EditText userNameEt = (EditText) view.findViewById(R.id.user_name_et);
                final Button cancel = (Button) view.findViewById(R.id.cancel_btn);
                final Button commit = (Button) view.findViewById(R.id.commit_btn);
                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (view.getId()) {
                            case R.id.cancel_btn:
                                mAddFriendDialog.cancel();
                                break;
                            case R.id.commit_btn:
                                String targetId = userNameEt.getText().toString().trim();
                                Log.i("MenuItemController", "targetID " + targetId);
                                if (TextUtils.isEmpty(targetId)) {
                                    Toast.makeText(mContext.getActivity(),
                                            mContext.getString(R.string.username_not_null_toast),
                                            Toast.LENGTH_SHORT).show();
                                    break;
                                } else if (targetId.equals(JMessageClient.getMyInfo().getUserName())
                                        || targetId.equals(JMessageClient.getMyInfo().getNickname())) {
                                    Toast.makeText(mContext.getActivity(),
                                            mContext.getString(R.string.user_add_self_toast),
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                } else if (isExistConv(targetId)) {
                                    Toast.makeText(mContext.getActivity(),
                                            mContext.getString(R.string.user_already_exist_toast),
                                            Toast.LENGTH_SHORT).show();
                                    userNameEt.setText("");
                                    break;
                                } else {
                                    mLoadingDialog = DialogCreator.createLoadingDialog(mContext.getActivity(),
                                            mContext.getString(R.string.adding_hint));
                                    mLoadingDialog.show();
                                    dismissSoftInput();
                                    getUserInfo(targetId);
                                }
                                break;
                        }
                    }
                };
                cancel.setOnClickListener(listener);
                commit.setOnClickListener(listener);
                break;
        }
    }

    private void getUserInfo(final String targetId){
        JMessageClient.getUserInfo(targetId, new GetUserInfoCallback() {
            @Override
            public void gotResult(final int status, String desc, final UserInfo userInfo) {
                mLoadingDialog.dismiss();
                if (status == 0) {
                    Conversation conv = Conversation.createSingleConversation(targetId);
                    if (!TextUtils.isEmpty(userInfo.getAvatar())) {
                        userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                            @Override
                            public void gotResult(int status, String desc, Bitmap bitmap) {
                                if (status == 0) {
                                    mController.getAdapter().notifyDataSetChanged();
                                } else {
                                    HandleResponseCode.onHandle(mContext.getActivity(), status, false);
                                }
                            }
                        });
                        mController.getAdapter().setToTop(conv);
                    } else {
                        mController.refreshConvList(conv);
                    }
                    mAddFriendDialog.cancel();
                } else {
                    HandleResponseCode.onHandle(mContext.getActivity(), status, true);
                }
            }
        });
    }

    public void dismissSoftInput() {
        InputMethodManager imm = ((InputMethodManager) mContext.getActivity()
                .getSystemService(Activity.INPUT_METHOD_SERVICE));
        if (mContext.getActivity().getWindow().getAttributes().softInputMode
                != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (mContext.getActivity().getCurrentFocus() != null)
                imm.hideSoftInputFromWindow(mContext.getActivity().getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private boolean isExistConv(String targetId) {
        Conversation conv = JMessageClient.getSingleConversation(targetId);
        return conv != null;
    }
}
