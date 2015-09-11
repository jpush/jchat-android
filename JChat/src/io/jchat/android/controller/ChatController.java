package io.jchat.android.controller;

import android.content.ActivityNotFoundException;
import android.content.Context;
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
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.eventbus.EventBus;
import io.jchat.android.R;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Locale;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.content.TextContent;
import io.jchat.android.activity.ChatActivity;
import io.jchat.android.adapter.MsgListAdapter;
import io.jchat.android.application.JPushDemoApplication;
import io.jchat.android.entity.Event;
import io.jchat.android.tools.HandleResponseCode;
import io.jchat.android.view.ChatView;
import cn.jpush.im.api.BasicCallback;
import io.jchat.android.view.DropDownListView;

public class ChatController implements OnClickListener, View.OnTouchListener,
        ChatView.OnSizeChangedListener, ChatView.OnKeyBoardChangeListener {

    private ChatView mChatView;
    private ChatActivity mContext;
    private MsgListAdapter mChatAdapter;
    Conversation mConv;
    private boolean isInputByKeyBoard = true;
    private boolean mShowSoftInput = false;
    private static final int REFRESH_LAST_PAGE = 1023;
    private static final int UPDATE_CHAT_LISTVIEW = 1026;
    private String mTargetID;
    private long mGroupID;
    private boolean mIsGroup;
    private String mPhotoPath = null;
    private final MyHandler myHandler = new MyHandler(this);
    private GroupInfo mGroupInfo;
    private String mGroupName;
    Window mWindow;
    InputMethodManager mImm;
    private int mDensityDpi;

    public ChatController(ChatView mChatView, ChatActivity context, int densityDpi) {
        this.mChatView = mChatView;
        this.mContext = context;
        this.mDensityDpi = densityDpi;
        this.mWindow = mContext.getWindow();
        this.mImm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
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
                mChatView.setChatTitle(mContext.getString(R.string.group),
                        intent.getIntExtra("memberCount", 0), mDensityDpi);
                mConv = JMessageClient.getGroupConversation(mGroupID);
            } else {
                if (mTargetID != null){
                    mGroupID = Long.parseLong(mTargetID);
                }
                mConv = JMessageClient.getGroupConversation(mGroupID);
                mGroupInfo = (GroupInfo)mConv.getTargetInfo();
                Log.d("ChatController", "GroupInfo: " + mGroupInfo.toString());
                UserInfo userInfo = mGroupInfo.getGroupMemberInfo(JMessageClient.getMyInfo().getUserName());
                //如果自己在群聊中，聊天标题显示群人数
                if (userInfo != null) {
                    if (!TextUtils.isEmpty(mGroupInfo.getGroupName())) {
                        mGroupName = mGroupInfo.getGroupName();
                        mChatView.setChatTitle(mGroupName, mGroupInfo.getGroupMembers().size(), mDensityDpi);
                    } else {
                        mChatView.setChatTitle(mContext.getString(R.string.group),
                                mGroupInfo.getGroupMembers().size(), mDensityDpi);
                    }
                    mChatView.showRightBtn();
                } else {
                    if (!TextUtils.isEmpty(mGroupInfo.getGroupName())) {
                        mGroupName = mGroupInfo.getGroupName();
                        mChatView.setChatTitle(mGroupName, mDensityDpi);
                    } else {
                        mChatView.setChatTitle(mContext.getString(R.string.group), mDensityDpi);
                    }
                    mChatView.dismissRightBtn();
                }
                //更新群名
                JMessageClient.getGroupInfo(mGroupID, new GetGroupInfoCallback() {
                    @Override
                    public void gotResult(int status, String desc, GroupInfo groupInfo) {
                        if (status == 0){
                            if (!TextUtils.isEmpty(groupInfo.getGroupName())){
                                mGroupName = groupInfo.getGroupName();
                                mChatView.setChatTitle(mGroupName,
                                        groupInfo.getGroupMembers().size(), mDensityDpi);
                            }
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
            if (mConv != null) {
                mChatView.setChatTitle(((UserInfo)mConv.getTargetInfo()).getNickname(), mDensityDpi);
            }
        }

        // 如果之前沒有会话记录并且是群聊
        if (mConv == null && mIsGroup) {
            mConv = Conversation.createGroupConversation(mGroupID);
            Log.i("ChatController", "create group success");
            // 是单聊
        } else if (mConv == null && !mIsGroup) {
            mConv = Conversation.createSingleConversation(mTargetID);
            mChatView.setChatTitle(((UserInfo)mConv.getTargetInfo()).getNickname(), mDensityDpi);
        }
        if (mConv != null) {
            if (mIsGroup) {
                mChatAdapter = new MsgListAdapter(mContext, mGroupID, mGroupInfo);
            } else {
                mChatAdapter = new MsgListAdapter(mContext, mTargetID);
            }
            mChatView.setChatListAdapter(mChatAdapter);
            //监听下拉刷新
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
                mConv.resetUnreadCount();
                dismissSoftInput();
                JMessageClient.exitConversaion();
                mContext.finish();
                break;
            // 聊天详细信息
            case R.id.right_btn:
                if (mChatView.getMoreMenu().getVisibility() == View.VISIBLE) {
                    mChatView.dismissMoreMenu();
                }
                dismissSoftInput();
                mContext.startChatDetailActivity(mIsGroup, mTargetID, mGroupID);
                break;
            // 切换输入
            case R.id.switch_voice_ib:
                mChatView.dismissMoreMenu();
                isInputByKeyBoard = !isInputByKeyBoard;
                //当前为语音输入，点击后切换为文字输入，弹出软键盘
                if (isInputByKeyBoard) {
                    mChatView.isKeyBoard();
                    showSoftInputAndDismissMenu();
                } else {
                    //否则切换到语音输入
                    mChatView.notKeyBoard(mConv, mChatAdapter);
                    if (mShowSoftInput) {
                        if (mImm != null) {
                            mImm.hideSoftInputFromWindow(mChatView.getInputView().getWindowToken(), 0); //强制隐藏键盘
                            mShowSoftInput = false;
                        }
                    } else if (mChatView.getMoreMenu().getVisibility() == View.VISIBLE) {
                        mChatView.dismissMoreMenu();
                    }
                    Log.i("ChatController", "setConversation success");
                }
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
                            HandleResponseCode.onHandle(mContext, status, false);
                        }
                        // 发送成功或失败都要刷新一次
                        myHandler.sendEmptyMessage(UPDATE_CHAT_LISTVIEW);
                    }
                });
                mChatAdapter.addMsgToList(msg);
                JMessageClient.sendMessage(msg);
                //暂时使用EventBus更新会话列表，以后sdk会同步更新Conversation
                EventBus.getDefault().post(new Event.MessageEvent(msg));
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
                } else {
                    //如果弹出软键盘 则隐藏软键盘
                    if (mChatView.getMoreMenu().getVisibility() != View.VISIBLE) {
                        dismissSoftInputAndShowMenu();
                        mChatView.focusToInput(false);
                        //如果弹出了更多选项菜单，则隐藏菜单并显示软键盘
                    } else {
                        showSoftInputAndDismissMenu();
                    }
                }
                break;
            // 拍照
            case R.id.pick_from_camera_btn:
                takePhoto();
                if (mChatView.getMoreMenu().getVisibility() == View.VISIBLE) {
                    mChatView.dismissMoreMenu();
                }
                break;
            case R.id.pick_from_local_btn:
                if (mChatView.getMoreMenu().getVisibility() == View.VISIBLE) {
                    mChatView.dismissMoreMenu();
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
                switch (view.getId()){
                    case R.id.chat_input_et:
                        if (mChatView.getMoreMenu().getVisibility() == View.VISIBLE && !mShowSoftInput){
                            showSoftInputAndDismissMenu();
                            return false;
                        }else return false;
                }
                if (mChatView.getMoreMenu().getVisibility() == View.VISIBLE){
                    mChatView.dismissMoreMenu();
                }else if (mShowSoftInput){
                    View v = mContext.getCurrentFocus();
                    if (mImm != null && v != null) {
                        mImm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        mWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                        mShowSoftInput = false;
                    }
                }
                break;
        }
        return false;
    }

    private void dismissSoftInput(){
        if (mShowSoftInput){
            if (mImm != null) {
                mImm.hideSoftInputFromWindow(mChatView.getInputView().getWindowToken(), 0);
                mShowSoftInput = false;
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void showSoftInputAndDismissMenu() {
        mWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN); // 隐藏软键盘
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mChatView.invisibleMoreMenu();
        mChatView.getInputView().requestFocus();
        if (mImm != null) {
            mImm.showSoftInput(mChatView.getInputView(),
                    InputMethodManager.SHOW_FORCED);//强制显示键盘
        }
        mShowSoftInput = true;
        mChatView.setMoreMenuHeight();
    }

    public void dismissSoftInputAndShowMenu() {
        //隐藏软键盘
        mWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN); // 隐藏软键盘
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mChatView.showMoreMenu();
        if (mImm != null) {
            mImm.hideSoftInputFromWindow(mChatView.getInputView().getWindowToken(), 0); //强制隐藏键盘
        }
        mChatView.setMoreMenuHeight();
        mShowSoftInput = false;
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
                Toast.makeText(mContext, mContext.getString(R.string.camera_not_prepared),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.sdcard_not_exist_toast),
                    Toast.LENGTH_SHORT).show();
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

    public void resetUnreadMsg() {
        if (mConv != null){
            mConv.resetUnreadCount();
        }
    }

    private static class MyHandler extends Handler {
        private final WeakReference<ChatController> mController;

        public MyHandler(ChatController controller) {
            mController = new WeakReference<ChatController>(controller);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            ChatController controller = mController.get();
            if (controller != null) {
                switch (msg.what) {
                    case REFRESH_LAST_PAGE:
                        controller.mChatAdapter.dropDownToRefresh();
                        controller.mChatView.getListView().onDropDownComplete();
                        if (controller.mChatAdapter.isHasLastPage()) {
                            controller.mChatView.getListView()
                                    .setSelection(controller.mChatAdapter.getOffset());
                            controller.mChatAdapter.refreshStartPosition();
                        } else {
                            controller.mChatView.getListView().setSelection(0);
                        }
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

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (oldh - h > 300) {
            Log.i("ChatController", "onSizeChanged, soft input is open");
            mShowSoftInput = true;
            mChatView.setMoreMenuHeight();
        } else {
            mShowSoftInput = false;
            Log.i("ChatController", "onSizeChanged, soft input is close");
        }
    }

    @Override
    public void onKeyBoardStateChange(int state) {
        switch (state) {
            case ChatView.KEYBOARD_STATE_INIT:
                if (mImm != null) {
                    mImm.isActive();
                }
                if (mChatView.getMoreMenu().getVisibility() == View.INVISIBLE
                        || (!mShowSoftInput && mChatView.getMoreMenu().getVisibility() == View.GONE)) {

                    mWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                            | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mChatView.getMoreMenu().setVisibility(View.GONE);
                }
                break;
            default:
                break;
        }
    }

}
