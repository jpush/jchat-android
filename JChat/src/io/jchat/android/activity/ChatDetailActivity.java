package io.jchat.android.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.content.EventNotificationContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import io.jchat.android.R;
import io.jchat.android.application.JChatDemoApplication;
import io.jchat.android.controller.ChatDetailController;
import io.jchat.android.tools.HandleResponseCode;
import io.jchat.android.view.ChatDetailView;

/*
 * 在对话界面中点击聊天信息按钮进来的聊天信息界面
 */
public class ChatDetailActivity extends BaseActivity {

    private static final String TAG = "ChatDetailActivity";

    private ChatDetailView mChatDetailView;
    private ChatDetailController mChatDetailController;
    public final static String START_FOR_WHICH = "which";
    private final static int GROUP_NAME_REQUEST_CODE = 1;
    private final static int MY_NAME_REQUEST_CODE = 2;
    private static final int ADD_FRIEND_REQUEST_CODE = 3;
    private Context mContext;
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        JMessageClient.registerEventReceiver(this);
        setContentView(R.layout.activity_chat_detail);
        mContext = this;
        mChatDetailView = (ChatDetailView) findViewById(R.id.chat_detail_view);
        mChatDetailView.initModule();
        mChatDetailController = new ChatDetailController(mChatDetailView, this, mAvatarSize);
        mChatDetailView.setListeners(mChatDetailController);
        mChatDetailView.setItemListener(mChatDetailController);
        mChatDetailView.setLongClickListener(mChatDetailController);
    }

    //设置群聊名称
    public void showGroupNameSettingDialog(int which, final long groupID, String groupName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_reset_password, null);
        builder.setView(view);
        if (which == 1) {
            TextView title = (TextView) view.findViewById(R.id.title_tv);
            title.setText(mContext.getString(R.string.group_name_hit));
            final EditText pwdEt = (EditText) view.findViewById(R.id.password_et);
            pwdEt.addTextChangedListener(new TextWatcher() {
                private CharSequence temp = "";
                private int editStart;
                private int editEnd;
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    temp = s;
                }

                @Override
                public void afterTextChanged(Editable s) {
                    editStart = pwdEt.getSelectionStart();
                    editEnd = pwdEt.getSelectionEnd();
                    byte[] data = temp.toString().getBytes();
                    if (data.length > 64) {
                        s.delete(editStart - 1, editEnd);
                        int tempSelection = editStart;
                        pwdEt.setText(s);
                        pwdEt.setSelection(tempSelection);
                    }
                }
            });
            pwdEt.setInputType(InputType.TYPE_CLASS_TEXT);
            pwdEt.setHint(groupName);
            pwdEt.setHintTextColor(getResources().getColor(R.color.chat_detail_item_content_color));
            final Button cancel = (Button) view.findViewById(R.id.cancel_btn);
            final Button commit = (Button) view.findViewById(R.id.commit_btn);
            final Dialog dialog = builder.create();
            dialog.show();
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (view.getId()) {
                        case R.id.cancel_btn:
                            dialog.cancel();
                            break;
                        case R.id.commit_btn:
                            final String newName = pwdEt.getText().toString().trim();
                            if (newName.equals("")) {
                                Toast.makeText(mContext, mContext.getString(R.string.group_name_not_null_toast), Toast.LENGTH_SHORT).show();
                            } else {
                                dismissSoftInput();
                                dialog.dismiss();
                                mDialog = new ProgressDialog(mContext);
                                mDialog.setMessage(mContext.getString(R.string.modifying_hint));
                                mDialog.show();
                                JMessageClient.updateGroupName(groupID, newName, new BasicCallback() {
                                    @Override
                                    public void gotResult(final int status, final String desc) {
                                        mDialog.dismiss();
                                        if (status == 0) {
                                            mChatDetailView.updateGroupName(newName);
                                            mChatDetailController.refreshGroupName(newName);
                                            Toast.makeText(mContext, mContext.getString(R.string.modify_success_toast), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Log.i(TAG, "desc :" + desc);
                                            HandleResponseCode.onHandle(mContext, status, false);
                                        }
                                    }
                                });
                            }
                            break;
                    }
                }
            };
            cancel.setOnClickListener(listener);
            commit.setOnClickListener(listener);
        }
        if (which == 2) {
            TextView title = (TextView) view.findViewById(R.id.title_tv);
            title.setText(mContext.getString(R.string.group_my_name_hit));
            title.setTextColor(Color.parseColor("#000000"));
            final EditText pwdEt = (EditText) view.findViewById(R.id.password_et);
            pwdEt.setHint(mContext.getString(R.string.change_nickname_hint));
            final Button cancel = (Button) view.findViewById(R.id.cancel_btn);
            final Button commit = (Button) view.findViewById(R.id.commit_btn);
            final Dialog dialog = builder.create();
            dialog.show();
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (view.getId()) {
                        case R.id.cancel_btn:
                            dialog.cancel();
                            break;
                        case R.id.commit_btn:
                            dialog.cancel();
                            break;
                    }
                }
            };
            cancel.setOnClickListener(listener);
            commit.setOnClickListener(listener);
        }
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed");
        Intent intent = new Intent();
        intent.putExtra(JChatDemoApplication.NAME, mChatDetailController.getName());
        intent.putExtra("currentCount", mChatDetailController.getCurrentCount());
        intent.putExtra("deleteMsg", mChatDetailController.getDeleteFlag());
        setResult(JChatDemoApplication.RESULT_CODE_CHAT_DETAIL, intent);
        finish();
        super.onBackPressed();
    }

    private void dismissSoftInput() {
        //隐藏软键盘
        InputMethodManager imm = ((InputMethodManager) mContext
                .getSystemService(Activity.INPUT_METHOD_SERVICE));
        if (this.getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (this.getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    @Override
    public void handleMsg(Message msg) {
        switch (msg.what) {
            case JChatDemoApplication.ON_GROUP_EVENT:
                mChatDetailController.refresh(msg.getData().getLong(JChatDemoApplication.GROUP_ID, 0));
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GROUP_NAME_REQUEST_CODE) {
            Log.i(TAG, "resultName = " + data.getStringExtra("resultName"));
            mChatDetailView.setGroupName(data.getStringExtra("resultName"));
        } else if (requestCode == MY_NAME_REQUEST_CODE) {
            Log.i(TAG, "myName = " + data.getStringExtra("resultName"));
            mChatDetailView.setMyName(data.getStringExtra("resultName"));
        }else if (resultCode == JChatDemoApplication.RESULT_CODE_FRIEND_INFO){
            if (data.getBooleanExtra("returnChatActivity", false)){
                data.putExtra("deleteMsg", mChatDetailController.getDeleteFlag());
                data.putExtra(JChatDemoApplication.NAME, mChatDetailController.getName());
                setResult(JChatDemoApplication.RESULT_CODE_CHAT_DETAIL, data);
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mChatDetailController.getAdapter() != null) {
            mChatDetailController.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        JMessageClient.unRegisterEventReceiver(this);
        super.onDestroy();
    }


    /**
     * 从ContactsActivity中选择朋友加入到群组中
     */
    public void showContacts() {
        Intent intent = new Intent();
        intent.putExtra(TAG, 1);
        intent.setClass(this, SelectFriendActivity.class);
        startActivityForResult(intent, ADD_FRIEND_REQUEST_CODE);
    }


    public void StartMainActivity() {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setClass(this, MainActivity.class);
        startActivity(intent);
    }

    public void startChatActivity(long groupID, String groupName) {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("isGroup", true);
        //设置跳转标志
        intent.putExtra("fromGroup", true);
        intent.putExtra("memberCount", 3);
        intent.putExtra("groupID", groupID);
        intent.putExtra("groupName", groupName);
        intent.setClass(this, ChatActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * 接收群成员变化事件
     *
     * @param event 消息事件
     */
    public void onEvent(MessageEvent event) {
        final cn.jpush.im.android.api.model.Message msg = event.getMessage();
        if (msg.getContentType() == ContentType.eventNotification) {
            EventNotificationContent.EventNotificationType msgType = ((EventNotificationContent) msg
                    .getContent()).getEventNotificationType();
            switch (msgType) {
                //添加群成员事件特殊处理
                case group_member_added:
                    List<String> userNames = ((EventNotificationContent) msg.getContent()).getUserNames();
                    for (final String userName : userNames) {
                        JMessageClient.getUserInfo(userName, new GetUserInfoCallback() {
                            @Override
                            public void gotResult(int status, String desc, UserInfo userInfo) {
                                if (status == 0) {
                                    mChatDetailController.getAdapter().notifyDataSetChanged();
                                } else {
                                    HandleResponseCode.onHandle(mContext, status, false);
                                }
                            }
                        });
                    }
                    break;
                case group_member_removed:
                    break;
                case group_member_exit:
                    break;
            }
            //无论是否添加群成员，刷新界面
            android.os.Message handleMsg = mHandler.obtainMessage();
            handleMsg.what = JChatDemoApplication.ON_GROUP_EVENT;
            Bundle bundle = new Bundle();
            bundle.putLong("groupID", ((GroupInfo)msg.getTargetInfo()).getGroupID());
            handleMsg.setData(bundle);
            handleMsg.sendToTarget();
        }
    }

}
