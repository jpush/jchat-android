package cn.jpush.im.android.demo.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import cn.jpush.im.android.demo.R;

import java.util.List;

import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.demo.activity.ConversationListFragment;
import cn.jpush.im.android.demo.tools.HandleResponseCode;
import cn.jpush.im.android.demo.view.LoadingDialog;
import cn.jpush.im.android.demo.view.MenuItemView;

/**
 * Created by Ken on 2015/1/26.
 */
public class MenuItemController implements View.OnClickListener{

    private MenuItemView mMenuItemView;
    private ConversationListFragment mContext;
    private ConversationListController mController;
    private LoadingDialog mLD;
    private Dialog mLoadingDialog;

    public MenuItemController(MenuItemView view, ConversationListFragment context, ConversationListController controller){
        this.mMenuItemView = view;
        this.mContext = context;
        this.mController = controller;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.create_group_ll:
                mContext.dismissPopWindow();
                mContext.StartCreateGroupActivity();
                break;
            case R.id.add_friend_ll:
                mContext.dismissPopWindow();
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext.getActivity());
                final View view = LayoutInflater.from(mContext.getActivity()).inflate(
                        R.layout.dialog_add_friend_to_conv_list, null);
                builder.setView(view);
                final Dialog dialog = builder.create();
                dialog.show();
                final EditText userNameEt = (EditText)view.findViewById(R.id.user_name_et);
                final Button cancel = (Button) view.findViewById(R.id.cancel_btn);
                final Button commit = (Button) view.findViewById(R.id.commit_btn);
                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (view.getId()){
                            case R.id.cancel_btn:
                                dialog.cancel();
                                break;
                            case R.id.commit_btn:
                                final String targetID = userNameEt.getText().toString().trim();
                                Log.i("MenuItemController", "targetID " + targetID);
                                if(targetID == null || targetID.equals("")){
                                    Toast.makeText(mContext.getActivity(), mContext.getString(R.string.username_not_null_toast), Toast.LENGTH_SHORT).show();
                                    break;
                                }else if(targetID.equals(JMessageClient.getMyInfo().getUserName()) || targetID.equals(JMessageClient.getMyInfo().getNickname())){
                                    Toast.makeText(mContext.getActivity(), mContext.getString(R.string.user_add_self_toast), Toast.LENGTH_SHORT).show();
                                    return;
                                }else {
                                    mLD = new LoadingDialog();
                                    mLoadingDialog = mLD.createLoadingDialog(mContext.getActivity(), mContext.getString(R.string.adding_hint));
                                    mLoadingDialog.show();
                                    dismissSoftInput();
                                    JMessageClient.getUserInfo(targetID, new GetUserInfoCallback(false) {
                                        @Override
                                        public void gotResult(final int status, String desc, final UserInfo userInfo) {
                                            mContext.getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if(mLoadingDialog != null)
                                                        mLoadingDialog.dismiss();
                                                    if(status == 0){
                                                        List<Conversation> list = JMessageClient.getConversationList();
                                                        Conversation conv = Conversation.createConversation(ConversationType.single, targetID);
                                                        list.add(conv);
                                                        if(userInfo.getAvatar() != null){
                                                            mController.loadAvatarAndRefresh(targetID, userInfo.getAvatar().getAbsolutePath());
                                                        }else mController.refreshConvList();
                                                        dialog.cancel();
                                                    }else {
                                                        HandleResponseCode.onHandle(mContext.getActivity(), status);
                                                    }
                                                }
                                            });
                                        }
                                    });

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

    public void dismissSoftInput() {
        InputMethodManager imm = ((InputMethodManager) mContext.getActivity()
                .getSystemService(Activity.INPUT_METHOD_SERVICE));
        if (mContext.getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (mContext.getActivity().getCurrentFocus() != null)
                imm.hideSoftInputFromWindow(mContext.getActivity().getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
