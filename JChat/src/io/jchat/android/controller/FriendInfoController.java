package io.jchat.android.controller;


import android.app.Dialog;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import io.jchat.android.R;
import io.jchat.android.activity.EditNoteNameActivity;
import io.jchat.android.activity.FriendInfoActivity;
import io.jchat.android.application.JChatDemoApplication;
import io.jchat.android.chatting.utils.DialogCreator;
import io.jchat.android.chatting.utils.HandleResponseCode;
import io.jchat.android.database.FriendEntry;
import io.jchat.android.database.FriendRecommendEntry;
import io.jchat.android.view.FriendInfoView;
import io.jchat.android.view.SlipButton;

public class FriendInfoController implements OnClickListener, SlipButton.OnChangedListener {

    private FriendInfoView mFriendInfoView;
    private FriendInfoActivity mContext;
    private Dialog mDialog;
    private List<FriendEntry> mList = new ArrayList<FriendEntry>();



    public FriendInfoController(FriendInfoView view, FriendInfoActivity context) {
        this.mFriendInfoView = view;
        this.mContext = context;
        initData();
    }

    private void initData() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onClick(final View v) {
        Intent intent = new Intent();
        final UserInfo userInfo = mContext.getUserInfo();
        switch (v.getId()) {
            case R.id.friend_info_return_btn:
                String nickname = userInfo.getNickname();
                intent.putExtra(JChatDemoApplication.NICKNAME, nickname);
                mContext.setResult(JChatDemoApplication.RESULT_CODE_FRIEND_INFO, intent);
                mContext.finish();
                break;
            case R.id.friend_send_msg_btn:
                mContext.startChatActivity();
                break;
            case R.id.friend_detail_avatar:
                mContext.startBrowserAvatar();
                break;
            case R.id.name_ll:
                intent.setClass(mContext, EditNoteNameActivity.class);
                intent.putExtra(JChatDemoApplication.TARGET_ID, userInfo.getUserName());
                intent.putExtra(JChatDemoApplication.TARGET_APP_KEY, userInfo.getAppKey());
                intent.putExtra("noteName", userInfo.getNotename());
                intent.putExtra("friendDescription", userInfo.getNoteText());
                mContext.startActivityForResult(intent, JChatDemoApplication.REQUEST_CODE_EDIT_NOTENAME);
                break;
            case R.id.delete_friend_btn:
                OnClickListener listener = new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (view.getId()) {
                            case R.id.jmui_cancel_btn:
                                mDialog.dismiss();
                                break;
                            case R.id.jmui_commit_btn:
                                mDialog.dismiss();
                                final Dialog dialog = DialogCreator.createLoadingDialog(mContext, mContext
                                        .getString(R.string.processing));
                                dialog.show();
                                userInfo.removeFromFriendList(new BasicCallback() {
                                    @Override
                                    public void gotResult(int status, String desc) {
                                        dialog.dismiss();
                                        if (status == 0) {
                                            //将好友删除时候还原黑名单设置
                                            List<String> name = new ArrayList<>();
                                            name.add(userInfo.getUserName());
                                            JMessageClient.delUsersFromBlacklist(name, null);

                                            FriendEntry friend = FriendEntry.getFriend(JChatDemoApplication.getUserEntry(),
                                                    userInfo.getUserName(), userInfo.getAppKey());
                                            if (friend != null) {
                                                friend.delete();
                                            }
                                            FriendRecommendEntry entry = FriendRecommendEntry
                                                    .getEntry(JChatDemoApplication.getUserEntry(),
                                                            userInfo.getUserName(), userInfo.getAppKey());
                                            if (entry != null) {
                                                entry.delete();
                                            }
                                            Toast.makeText(mContext, mContext.getString(R.string
                                                    .friend_already_deleted_hint), Toast.LENGTH_SHORT).show();
                                            mContext.delConvAndReturnMainActivity();
                                            //删除好友后
                                        } else {
                                            HandleResponseCode.onHandle(mContext, status, false);
                                        }
                                    }
                                });
                                break;
                        }
                    }
                };
                mDialog = DialogCreator.createBaseDialogWithTitle(mContext,
                        mContext.getString(R.string.delete_friend_dialog_title), listener);
                mDialog.getWindow().setLayout((int) (0.8 * mContext.getWidth()), WindowManager.LayoutParams.WRAP_CONTENT);
                mDialog.show();
                break;
        }
    }

    @Override
    public void onChanged(int id, final boolean flag) {
        final Dialog dialog = DialogCreator.createLoadingDialog(mContext, mContext.getString(R.string.jmui_loading));
        switch (id) {
            case R.id.black_list_slip_btn:
                List<String> list = new ArrayList<String>();
                list.add(mContext.getUserName());
                dialog.show();
                if (flag) {
                    JMessageClient.addUsersToBlacklist(list, mContext.getTargetAppKey(), new BasicCallback() {
                        @Override
                        public void gotResult(int status, String desc) {
                            dialog.dismiss();
                            if (status == 0) {
                                Log.d("FriendInfoController", "add user to black list success!");
                                Toast.makeText(mContext,
                                        mContext.getString(R.string.add_to_blacklist_success_hint),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                mFriendInfoView.setBlackBtnChecked(false);
                                HandleResponseCode.onHandle(mContext, status, false);
                            }
                        }
                    });
                } else {
                    JMessageClient.delUsersFromBlacklist(list, mContext.getTargetAppKey(), new BasicCallback() {
                        @Override
                        public void gotResult(int status, String desc) {
                            dialog.dismiss();
                            if (status == 0) {
                                Toast.makeText(mContext,
                                        mContext.getString(R.string.remove_from_blacklist_hint),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                mFriendInfoView.setBlackBtnChecked(true);
                                HandleResponseCode.onHandle(mContext, status, false);
                            }
                        }
                    });
                }
                break;
            //滑动免打扰按钮是否加入免打扰,userInfo.setNoDisturb(int flag, BasicCallback callback)
            //flag为1表示加入免打扰,否则为0
            case R.id.no_disturb_slip_btn:
                dialog.show();
                mContext.getUserInfo().setNoDisturb(flag ? 1 : 0, new BasicCallback() {
                    @Override
                    public void gotResult(int status, String desc) {
                        dialog.dismiss();
                        if (status == 0) {
                            if (flag) {
                                Toast.makeText(mContext, mContext
                                                .getString(R.string.set_do_not_disturb_success_hint),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(mContext, mContext
                                                .getString(R.string.remove_from_no_disturb_list_hint),
                                        Toast.LENGTH_SHORT).show();
                            }
                        //设置失败恢复原来的状态
                        } else {
                            if (flag) {
                                mFriendInfoView.setNoDisturbChecked(false);
                            } else {
                                mFriendInfoView.setNoDisturbChecked(true);
                            }
                            HandleResponseCode.onHandle(mContext, status, false);
                        }
                    }
                });
                break;
        }
    }
}
