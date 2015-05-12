package cn.jpush.im.android.demo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.util.List;

import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.event.ConversationRefreshEvent;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.demo.R;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.demo.application.JPushDemoApplication;
import cn.jpush.im.android.demo.controller.ChatDetailController;
import cn.jpush.im.android.demo.tools.HandleResponseCode;
import cn.jpush.im.android.demo.view.ChatDetailView;
import cn.jpush.im.api.BasicCallback;

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
		mChatDetailController = new ChatDetailController(mChatDetailView, this);
		mChatDetailView.setListeners(mChatDetailController);
		mChatDetailView.setItemListener(mChatDetailController);
		mChatDetailView.setLongClickListener(mChatDetailController);

	}

    //设置群聊名称
	public void showGroupNameSettingDialog(int which, final long groupID, String groupName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_reset_password, null);
        builder.setView(view);
		if(which == 1){
            TextView title = (TextView) view.findViewById(R.id.title_tv);
            title.setText(mContext.getString(R.string.group_name_hit));
            title.setTextColor(Color.parseColor("#000000"));
            final EditText pwdEt = (EditText) view.findViewById(R.id.password_et);
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
                            if(newName.equals("")){
                                Toast.makeText(mContext, mContext.getString(R.string.group_name_not_null_toast), Toast.LENGTH_SHORT).show();
                            }else {
                                dismissSoftInput();
                                dialog.dismiss();
                                mDialog = new ProgressDialog(mContext);
                                mDialog.setMessage(mContext.getString(R.string.modifying_hint));
                                mDialog.show();
                                JMessageClient.updateGroupName(groupID, newName, new BasicCallback(false) {
                                    @Override
                                    public void gotResult(final int status, final String desc) {
                                        ChatDetailActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mDialog.dismiss();
                                                if(status == 0){
                                                    mChatDetailView.updateGroupName(newName);
                                                    Intent intent = new Intent(JPushDemoApplication.UPDATE_GROUP_NAME_ACTION);
                                                    intent.putExtra("newGroupName", newName);
                                                    sendBroadcast(intent);
                                                    Toast.makeText(mContext, mContext.getString(R.string.modify_success_toast), Toast.LENGTH_SHORT).show();
                                                }else {
                                                    Log.i(TAG, "desc :" + desc);
                                                    HandleResponseCode.onHandle(mContext, status);
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
		}
		if(which == 2){
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
        finish();
        super.onBackPressed();
    }

    private void dismissSoftInput() {
        //隐藏软键盘
        InputMethodManager imm = ((InputMethodManager) mContext
                .getSystemService(Activity.INPUT_METHOD_SERVICE));
        if (this.getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (this.getCurrentFocus() != null)
                imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void handleMsg(Message msg){
        switch (msg.what){
            case JPushDemoApplication.ON_GROUP_EVENT:
                mChatDetailController.refresh(msg.getData().getLong("groupID", 0));
                break;
            case JPushDemoApplication.REFRESH_GROUP_NAME:
                Log.i(TAG, "Refresh GroupName Or user name");
                mChatDetailController.NotifyNameChange();
                break;
        }
    }


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(data == null){
			return;
		}
		else if(requestCode == GROUP_NAME_REQUEST_CODE){
			Log.i(TAG, "resultName = "+ data.getStringExtra("resultName"));
			mChatDetailView.setGroupName(data.getStringExtra("resultName"));
		}
		else if(requestCode == MY_NAME_REQUEST_CODE){
			Log.i(TAG, "myName = "+ data.getStringExtra("resultName"));
			mChatDetailView.setMyName(data.getStringExtra("resultName"));
		}
	}

    @Override
    protected void onResume() {
        mChatDetailController.NotifyNameChange();
        super.onResume();
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
		intent.setClass(this, ContactsFragment.class);
		startActivityForResult(intent, ADD_FRIEND_REQUEST_CODE);
	}


    public void StartMainActivity() {
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        startActivity(intent);
    }

    public void StartChatActivity(long groupID, String groupName) {
        Intent intent = new Intent();
        intent.putExtra("isGroup", true);
        //设置跳转标志
        intent.putExtra("fromGroup", true);
        intent.putExtra("groupID", groupID);
        intent.putExtra("groupName", groupName);
        intent.setClass(this, ChatActivity.class);
        startActivity(intent);
        finish();
    }

    public void onEvent(ConversationRefreshEvent conversationRefreshEvent){
        mHandler.sendEmptyMessage(JPushDemoApplication.REFRESH_GROUP_NAME);
    }

    /**
     * 接收群成员变化事件
     * @param event
     */
    public void onEvent(MessageEvent event){
        cn.jpush.im.android.api.model.Message msg = event.getMessage();
        if(msg.getContentType() == ContentType.eventNotification){
            android.os.Message handleMsg = mHandler.obtainMessage();
            handleMsg.what = JPushDemoApplication.ON_GROUP_EVENT;
            Bundle bundle = new Bundle();
            bundle.putLong("groupID", Long.parseLong(msg.getTargetID()));
            handleMsg.setData(bundle);
            handleMsg.sendToTarget();
        }
    }
}
