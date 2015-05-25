package cn.jpush.im.android.demo.controller;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Toast;

import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.demo.R;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetGroupMembersCallback;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.demo.activity.ChatActivity;
import cn.jpush.im.android.demo.adapter.MsgListAdapter;
import cn.jpush.im.android.demo.application.JPushDemoApplication;
import cn.jpush.im.android.demo.tools.HandleResponseCode;
import cn.jpush.im.android.demo.view.ChatView;
import cn.jpush.im.api.BasicCallback;

public class ChatController implements OnClickListener, OnScrollListener, View.OnTouchListener {

    private ChatView mChatView;
    private ChatActivity mContext;
    private MsgListAdapter mChatAdapter;
    Conversation mConv;
    private boolean isInputByKeyBoard = true;
    public boolean mMoreMenuVisible = false;
    public static boolean mIsShowMoreMenu = false;
    public static final int UPDATE_LAST_PAGE_LISTVIEW = 1025;
    public static final int UPDATE_CHAT_LISTVIEW = 1026;
    private String mTargetID;
    private long mGroupID;
    private boolean mIsGroup;
    private String mPhotoPath = null;

    public ChatController(ChatView mChatView, ChatActivity context) {
        this.mChatView = mChatView;
        this.mContext = context;
        // 得到消息列表
        initData();

    }

    private void initData() {
        Intent intent = mContext.getIntent();
        mTargetID = intent.getStringExtra("targetID");
        Log.i("ChatController", "mTargetID " + mTargetID);
        mGroupID = intent.getLongExtra("groupID", 0);
        mIsGroup = intent.getBooleanExtra("isGroup", false);
        boolean fromGroup = intent.getBooleanExtra("fromGroup", false);
        // 如果是群组，特别处理
        if (mIsGroup) {
            Log.i("Tag", "mGroupID is " + mGroupID);
            //判断是否从创建群组跳转过来
            if (fromGroup) {
                String groupName = intent.getStringExtra("groupName");
                mChatView.setChatTitle(groupName);
                mConv = JMessageClient.getGroupConversation(mGroupID);
            } else {
                if(mTargetID != null)
                    mGroupID = Long.parseLong(mTargetID);
                mConv = JMessageClient.getGroupConversation(mGroupID);
                //判断自己如果不在群聊中，隐藏群聊详情按钮
                JMessageClient.getGroupMembers(mGroupID, new GetGroupMembersCallback(false) {
                    @Override
                    public void gotResult(final int status, final String desc, final List<String> memberList) {
                        mContext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (status == 0) {
                                    //群主解散后，返回memberList为空
                                    if (memberList.isEmpty()) {
                                        mChatView.dismissRightBtn();
                                        //判断自己如果不在memberList中，则隐藏聊天详情按钮
                                    } else if (!memberList.contains(JMessageClient.getMyInfo().getUserName()))
                                        mChatView.dismissRightBtn();
                                    else mChatView.showRightBtn();
                                } else {
                                    if (memberList.isEmpty()) {
                                        mChatView.dismissRightBtn();
                                    }
                                    HandleResponseCode.onHandle(mContext, status);
                                }
                            }
                        });
                    }
                });
            }
            //聊天信息标志改变
            mChatView.setGroupIcon();
        } else {
            // 用targetID得到会话
            Log.i("Tag", "targetID is " + mTargetID);
            mConv = JMessageClient.getSingleConversation(mTargetID);
        }

        // 如果之前沒有会话记录并且是群聊
        if (mConv == null && mIsGroup) {
            mConv = Conversation.createConversation(ConversationType.group, mGroupID);
            Log.i("ChatController", "create group success");
            // 是单聊
        } else if (mConv == null && !mIsGroup) {
            mConv = Conversation.createConversation(ConversationType.single, mTargetID);
        }
        if(mConv != null){
            mChatView.setChatTitle(mConv.getDisplayName());
            mConv.resetUnreadCount();
        }
        mChatAdapter = new MsgListAdapter(mContext, mIsGroup, mTargetID, mGroupID);
        mChatView.setChatListAdapter(mChatAdapter);
        // 滑动到底部
        mChatView.setToBottom();
    }


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            // 返回按钮
            case R.id.return_btn:
                mConv.resetUnreadCount();
                JMessageClient.exitConversaion();
                mContext.finish();
                break;
            // 聊天详细信息
            case R.id.right_btn:
                if (mIsShowMoreMenu) {
                    mChatView.dismissMoreMenu();
                    dismissSoftInput();
                    mIsShowMoreMenu = false;
                }
                mContext.StartChatDetailActivity(mIsGroup, mTargetID, mGroupID);
                break;
            // 切换输入
            case R.id.switch_voice_ib:
                mChatView.dismissMoreMenu();
                isInputByKeyBoard = !isInputByKeyBoard;
                if (isInputByKeyBoard) {
                    mChatView.isKeyBoard();
                    mChatView.mChatInputEt.requestFocus();
                    mIsShowMoreMenu = true;
                    mChatView.focusToInput(true);
                } else {
                    mChatView.notKeyBoard(mConv, mChatAdapter);
                    mIsShowMoreMenu = false;
                    Log.i("ChatController", "setConversation success");
                    //关闭软键盘
                    dismissSoftInput();
                }
                break;
            case R.id.chat_input_et:
                mChatView.showMoreMenu();
//                mChatView.invisibleMoreMenu();
                mIsShowMoreMenu = true;
                showSoftInput();
                break;

            // 发送文本消息
            case R.id.send_msg_btn:
                String msgContent = mChatView.getChatInput();
                mChatView.clearInput();
                mChatView.setToBottom();
                if (msgContent.equals("")) {
                    return;
                }
                TextContent content = new TextContent(msgContent);
                final Message msg = mConv.createSendMessage(content);
                msg.setOnSendCompleteCallback(new BasicCallback() {

                    @Override
                    public void gotResult(final int status, String desc) {
                        Log.i("ChatController", "send callback " + status + " desc " + desc);
                        if (status != 0) {
                            mContext.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    HandleResponseCode.onHandle(mContext, status);
                                }
                            });
                        }
                        // 发送成功或失败都要刷新一次
                        android.os.Message msg = handler.obtainMessage();
                        msg.what = UPDATE_CHAT_LISTVIEW;
                        Bundle bundle = new Bundle();
                        bundle.putString("desc", desc);
                        msg.setData(bundle);
                        msg.sendToTarget();
                    }
                });
                mChatAdapter.addMsgToList(msg);
                JMessageClient.sendMessage(msg);
                break;

            case R.id.expression_btn:
//                if (mMoreMenuVisible) {
//                    mChatView.invisibleMoreMenu();
//                    mMoreMenuVisible = false;
//
//                }
                break;

            // 点击添加按钮，弹出更多选项菜单
            case R.id.add_file_btn:
                //如果在语音输入时点击了添加按钮，则显示菜单并切换到输入框
                if (!isInputByKeyBoard) {
                    mChatView.isKeyBoard();
                    isInputByKeyBoard = true;
                    mChatView.showMoreMenu();
                    mIsShowMoreMenu = true;
                    mChatView.focusToInput(false);
                } else {
                    if (mIsShowMoreMenu) {
                        if (mMoreMenuVisible) {
                            mChatView.focusToInput(true);
                            showSoftInput();
                            mMoreMenuVisible = false;
                        } else {
                            dismissSoftInput();
                            mChatView.focusToInput(false);
                            mMoreMenuVisible = true;
                        }
                    } else {
                        mChatView.focusToInput(false);
                        mChatView.showMoreMenu();
                        mIsShowMoreMenu = true;
                        mMoreMenuVisible = true;
                    }
                }
                break;
            // 拍照
            case R.id.pick_from_camera_btn:
                takePhoto();
                if (mIsShowMoreMenu) {
                    mChatView.dismissMoreMenu();
                    dismissSoftInput();
                    mIsShowMoreMenu = false;
                }
                break;
            case R.id.pick_from_local_btn:
                if (mIsShowMoreMenu) {
                    mChatView.dismissMoreMenu();
                    dismissSoftInput();
                    mIsShowMoreMenu = false;
                }
                Intent intent = new Intent();
                if (mIsGroup) {
                    intent.putExtra("groupID", mGroupID);
                } else {
                    intent.putExtra("targetID", mTargetID);
                }
                intent.putExtra("isGroup", mIsGroup);
                mContext.StartPickPictureTotalActivity(intent);
                break;
            case R.id.send_location_btn:
                break;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mIsShowMoreMenu) {
                    mChatView.dismissMoreMenu();
                    dismissSoftInput();
                    mIsShowMoreMenu = false;
                    mMoreMenuVisible = false;
                    mChatView.setToBottom();
                }
                break;
        }
        return false;
    }

    private void showSoftInput() {
        if (mContext.getWindow().getAttributes().softInputMode == WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (mContext.getCurrentFocus() != null) {
                InputMethodManager imm = ((InputMethodManager) mContext
                        .getSystemService(Activity.INPUT_METHOD_SERVICE));
                imm.showSoftInputFromInputMethod(mChatView.getWindowToken(), 0);
            }
        }
    }

    public void dismissSoftInput() {
        //隐藏软键盘
        InputMethodManager imm = ((InputMethodManager) mContext
                .getSystemService(Activity.INPUT_METHOD_SERVICE));
        if (mContext.getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (mContext.getCurrentFocus() != null)
                imm.hideSoftInputFromWindow(mContext
                                .getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void takePhoto() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            String dir = "sdcard/JPushDemo/pictures/";
            File destDir = new File(dir);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            File file = new File(dir, new DateFormat().format("yyyy_MMdd_hhmmss",
                    Calendar.getInstance(Locale.CHINA)) + ".jpg");
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            setPhotoPath(file.getAbsolutePath());
            try {
                mContext.startActivityForResult(intent, JPushDemoApplication.REQUESTCODE_TAKE_PHOTO);
            } catch (ActivityNotFoundException anf) {
                Toast.makeText(mContext, mContext.getString(R.string.camera_not_prepared), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.sdcard_not_exist_toast), Toast.LENGTH_SHORT).show();
        }
    }

    private void setPhotoPath(String path) {
        mPhotoPath = path;
    }

    public String getPhotoPath() {
        return mPhotoPath;
    }

    public void releaseMediaPlayer() {
        mChatAdapter.releaseMediaPlayer();
    }

    Handler handler = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_LAST_PAGE_LISTVIEW:
                    Log.i("Tag", "收到更新消息列表的消息");
                    mChatAdapter.refresh();
                    mChatView.removeHeadView();
                    break;
                case UPDATE_CHAT_LISTVIEW:
                    mChatAdapter.refresh();
                    break;
            }
        }
    };


    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
//        int touchPosition = 0;
    }

    public MsgListAdapter getAdapter() {
        return mChatAdapter;
    }

    public void setAdapter(MsgListAdapter adapter) {
        mChatAdapter = adapter;
    }

    public Conversation getConversation() {
        return mConv;
    }

    public String getTargetID() {
        return mTargetID;
    }

    public long getGroupID() {
        return mGroupID;
    }

    public boolean isGroup() {
        return mIsGroup;
    }

    public void refresh() {
        mChatView.setChatTitle(mConv.getDisplayName());
        mChatAdapter.refresh();
    }
}
