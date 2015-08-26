package io.jchat.android.controller;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.R;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetGroupMembersCallback;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.enums.ConversationType;
import io.jchat.android.activity.ChatActivity;
import io.jchat.android.adapter.MsgListAdapter;
import io.jchat.android.application.JPushDemoApplication;
import io.jchat.android.tools.HandleResponseCode;
import io.jchat.android.view.ChatView;
import cn.jpush.im.api.BasicCallback;
import io.jchat.android.view.DropDownListView;

public class ChatController implements OnClickListener, View.OnTouchListener {

    private ChatView mChatView;
    private ChatActivity mContext;
    private MsgListAdapter mChatAdapter;
    Conversation mConv;
    private boolean isInputByKeyBoard = true;
    public boolean mMoreMenuVisible = false;
    public static boolean mIsShowMoreMenu = false;
    private static final int REFRESH_LAST_PAGE = 1023;
    private static final int UPDATE_GROUP_INFO = 1024;
    private static final int UPDATE_CHAT_LISTVIEW = 1026;
    private String mTargetID;
    private long mGroupID;
    private boolean mIsGroup;
    private String mPhotoPath = null;
    private final MyHandler myHandler = new MyHandler(this);
    private GroupInfo mGroupInfo;
    private String mGroupName;

    public ChatController(ChatView mChatView, ChatActivity context) {
        this.mChatView = mChatView;
        this.mContext = context;
        // 得到消息列表
        initData();

    }

    private void initData() {
        Intent intent = mContext.getIntent();
        mTargetID = intent.getStringExtra(JPushDemoApplication.TARGET_ID);
        Log.i("ChatController", "mTargetID " + mTargetID);
        mGroupID = intent.getLongExtra(JPushDemoApplication.GROUP_ID, 0);
        mIsGroup = intent.getBooleanExtra(JPushDemoApplication.IS_GROUP, false);
        final boolean fromGroup = intent.getBooleanExtra("fromGroup", false);
        // 如果是群组，特别处理
        if (mIsGroup) {
            Log.i("Tag", "mGroupID is " + mGroupID);
            //判断是否从创建群组跳转过来
            if (fromGroup) {
                mChatView.setChatTitle(mContext.getString(R.string.group), intent.getIntExtra("memberCount", 0));
                mConv = JMessageClient.getGroupConversation(mGroupID);
            } else {
                if (mTargetID != null)
                    mGroupID = Long.parseLong(mTargetID);
                mConv = JMessageClient.getGroupConversation(mGroupID);
                mChatView.setChatTitle(mContext.getString(R.string.group));
                //设置群聊聊天标题
                JMessageClient.getGroupInfo(mGroupID, new GetGroupInfoCallback() {
                    @Override
                    public void gotResult(int status, String desc, GroupInfo groupInfo) {
                        if(status == 0){
                            android.os.Message msg = myHandler.obtainMessage();
                            msg.obj = groupInfo;
                            msg.what = UPDATE_GROUP_INFO;
                            msg.sendToTarget();
                            if(!TextUtils.isEmpty(groupInfo.getGroupName())){
                                mGroupName = groupInfo.getGroupName();
                                mChatView.setChatTitle(mGroupName, groupInfo.getGroupMembers().size());
                            }else {
                                Log.i("ChatController", "GroupMember size: " + groupInfo.getGroupMembers().size());
                                mChatView.setChatTitle(mContext.getString(R.string.group), groupInfo.getGroupMembers().size());
                            }
                        }
                    }
                });
                //判断自己如果不在群聊中，隐藏群聊详情按钮
                JMessageClient.getGroupMembers(mGroupID, new GetGroupMembersCallback() {
                    @Override

                    public void gotResult(final int status, final String desc, final List<UserInfo> members) {
                        if (status == 0) {
                            List<String> userNames = new ArrayList<String>();
                            for(UserInfo info:members){
                                userNames.add(info.getUserName());
                            }
                            //群主解散后，返回memberList为空
                            if (userNames.isEmpty()) {
                                mChatView.dismissRightBtn();
                                //判断自己如果不在memberList中，则隐藏聊天详情按钮
                            } else if (!userNames.contains(JMessageClient.getMyInfo().getUserName()))
                                mChatView.dismissRightBtn();
                            else mChatView.showRightBtn();
                        } else {
                            if (null == members || members.isEmpty()) {
                                mChatView.dismissRightBtn();
                            }
                            HandleResponseCode.onHandle(mContext, status, false);
                        }
                    }
                });
            }
            //聊天信息标志改变
            mChatView.setGroupIcon();
        } else {
            // 用targetID得到会话
            Log.i("Tag", "targetID is " + mTargetID);
            mConv = JMessageClient.getSingleConversation(mTargetID);
            if(mConv != null){
                mChatView.setChatTitle(mConv.getTitle());
            }
        }

        // 如果之前沒有会话记录并且是群聊
        if (mConv == null && mIsGroup) {
            mConv = Conversation.createConversation(ConversationType.group, mGroupID);
            Log.i("ChatController", "create group success");
            // 是单聊
        } else if (mConv == null && !mIsGroup) {
            mConv = Conversation.createConversation(ConversationType.single, mTargetID);
            mChatView.setChatTitle(mConv.getTitle());
        }
        if (mConv != null) {
            if(mIsGroup){
                mChatAdapter = new MsgListAdapter(mContext, mGroupID, mGroupInfo);
            }else {
                mChatAdapter = new MsgListAdapter(mContext, mTargetID);
            }
            mChatView.setChatListAdapter(mChatAdapter);
            mChatView.getListView().setOnDropDownListener(new DropDownListView.OnDropDownListener() {
                @Override
                public void onDropDown() {
                    myHandler.sendEmptyMessageDelayed(REFRESH_LAST_PAGE, 1000);
                }
            });
        }

        // 滑动到底部
        mChatView.setToBottom();
    }


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            // 返回按钮
            case R.id.return_btn:
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
                mContext.startChatDetailActivity(mIsGroup, mTargetID, mGroupID);
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
                mChatView.setMoreMenuHeight();
                mChatView.showMoreMenu();
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
                                    HandleResponseCode.onHandle(mContext, status, false);
                                }
                            });
                        }
                        // 发送成功或失败都要刷新一次
                        myHandler.sendEmptyMessage(UPDATE_CHAT_LISTVIEW);
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
                    intent.putExtra(JPushDemoApplication.GROUP_ID, mGroupID);
                } else {
                    intent.putExtra(JPushDemoApplication.TARGET_ID, mTargetID);
                }
                intent.putExtra(JPushDemoApplication.IS_GROUP, mIsGroup);
                mContext.startPickPictureTotalActivity(intent);
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
                mContext.startActivityForResult(intent, JPushDemoApplication.REQUEST_CODE_TAKE_PHOTO);
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

    public void refreshGroupInfo(GroupInfo groupInfo) {
        mGroupInfo = groupInfo;
        mChatAdapter.refreshGroupInfo(groupInfo);
    }

    private static class MyHandler extends Handler{
        private final WeakReference<ChatController> mController;

        public MyHandler(ChatController controller){
            mController = new WeakReference<ChatController>(controller);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            ChatController controller = mController.get();
            if(controller != null){
                switch (msg.what) {
                    case REFRESH_LAST_PAGE:
                        controller.mChatAdapter.dropDownToRefresh();
                        controller.mChatView.getListView().onDropDownComplete();
                        if (controller.mChatAdapter.isHasLastPage()){
                            controller.mChatView.getListView().setSelection(controller.mChatAdapter.getOffset());
                            controller.mChatAdapter.refreshStartPosition();
                        }else {
                            controller.mChatView.getListView().setSelection(0);
                        }
                        break;
                    case UPDATE_GROUP_INFO:
                        controller.mGroupInfo = (GroupInfo)msg.obj;
                        controller.mChatAdapter.refreshGroupInfo(controller.mGroupInfo);
                        break;
                    case UPDATE_CHAT_LISTVIEW:
                        controller.mChatAdapter.notifyDataSetChanged();
                        break;
                }
            }
        }
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

}
