package io.jchat.android.controller;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import java.io.File;
import java.lang.ref.WeakReference;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import de.greenrobot.event.EventBus;
import io.jchat.android.R;
import io.jchat.android.activity.ChatActivity;
import io.jchat.android.adapter.MsgListAdapter;
import io.jchat.android.application.JChatDemoApplication;
import io.jchat.android.entity.Event;
import io.jchat.android.tools.DialogCreator;
import io.jchat.android.tools.FileHelper;
import io.jchat.android.view.ChatView;
import io.jchat.android.view.DropDownListView;
import io.jchat.android.adapter.MsgListAdapter.ContentLongClickListener;

public class ChatController implements OnClickListener, View.OnTouchListener,
        ChatView.OnSizeChangedListener, ChatView.OnKeyBoardChangeListener {

    private ChatView mChatView;
    private ChatActivity mContext;
    private MsgListAdapter mChatAdapter;
    Conversation mConv;
    private boolean isInputByKeyBoard = true;
    private boolean mShowSoftInput = false;
    private static final int REFRESH_LAST_PAGE = 1023;
    private static final int REFRESH_CHAT_TITLE = 1024;
    private String mTargetId;
    private long mGroupId;
    private boolean mIsGroup;
    private String mPhotoPath = null;
    private final MyHandler myHandler = new MyHandler(this);
    private String mGroupName;
    Window mWindow;
    InputMethodManager mImm;
    private Dialog mDialog;
    private int mWidth;
    private String mTargetAppKey;
    GroupInfo mGroupInfo;

    public ChatController(ChatView mChatView, ChatActivity context, int width) {
        this.mChatView = mChatView;
        this.mContext = context;
        this.mWidth = width;
        this.mWindow = mContext.getWindow();
        this.mImm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        // 得到消息列表
        initData();
    }

    private void initData() {
        Intent intent = mContext.getIntent();
        mTargetAppKey = intent.getStringExtra(JChatDemoApplication.TARGET_APP_KEY);
        mTargetId = intent.getStringExtra(JChatDemoApplication.TARGET_ID);
        Log.i("ChatController", "mTargetId " + mTargetId);
        mGroupId = intent.getLongExtra(JChatDemoApplication.GROUP_ID, 0);
        mIsGroup = intent.getBooleanExtra(JChatDemoApplication.IS_GROUP, false);
        final boolean fromGroup = intent.getBooleanExtra("fromGroup", false);
        // 如果是群组，特别处理
        if (mIsGroup) {
            Log.i("Tag", "mGroupId is " + mGroupId);
            //判断是否从创建群组跳转过来
            if (fromGroup) {
                mChatView.setChatTitle(mContext.getString(R.string.group),
                        intent.getIntExtra(JChatDemoApplication.MEMBERS_COUNT, 0));
                mConv = JMessageClient.getGroupConversation(mGroupId);
            } else {
                if (mTargetId != null) {
                    mGroupId = Long.parseLong(mTargetId);
                }
                mConv = JMessageClient.getGroupConversation(mGroupId);
                if (mConv != null) {
                    GroupInfo groupInfo = (GroupInfo)mConv.getTargetInfo();
                    Log.d("ChatController", "GroupInfo: " + groupInfo.toString());
                    UserInfo userInfo = groupInfo.getGroupMemberInfo(JMessageClient.getMyInfo().getUserName());
                    //如果自己在群聊中，聊天标题显示群人数
                    if (userInfo != null) {
                        if (!TextUtils.isEmpty(groupInfo.getGroupName())) {
                            mGroupName = groupInfo.getGroupName();
                            mChatView.setChatTitle(mGroupName, groupInfo.getGroupMembers().size());
                        } else {
                            mChatView.setChatTitle(mContext.getString(R.string.group),
                                    groupInfo.getGroupMembers().size());
                        }
                        mChatView.showRightBtn();
                    } else {
                        if (!TextUtils.isEmpty(groupInfo.getGroupName())) {
                            mGroupName = groupInfo.getGroupName();
                            mChatView.setChatTitle(mGroupName);
                        } else {
                            mChatView.setChatTitle(mContext.getString(R.string.group));
                        }
                        mChatView.dismissRightBtn();
                    }
                }
                //更新群名
                JMessageClient.getGroupInfo(mGroupId, new GetGroupInfoCallback(false) {
                    @Override
                    public void gotResult(int status, String desc, GroupInfo groupInfo) {
                        if (status == 0){
                            mGroupInfo = groupInfo;
                            myHandler.sendEmptyMessage(REFRESH_CHAT_TITLE);
                        }
                    }
                });
            }
            //聊天信息标志改变
            mChatView.setGroupIcon();
        } else {
            // 用targetId得到会话
            Log.i("Tag", "targetId is " + mTargetId);
            mConv = JMessageClient.getSingleConversation(mTargetId, mTargetAppKey);
            if (mConv != null) {
                UserInfo userInfo = (UserInfo)mConv.getTargetInfo();
                if (TextUtils.isEmpty(userInfo.getNickname())) {
                    mChatView.setChatTitle(userInfo.getUserName());
                }else {
                    mChatView.setChatTitle(userInfo.getNickname());
                }
            }
        }

        // 如果之前沒有会话记录并且是群聊
        if (mConv == null && mIsGroup) {
            mConv = Conversation.createGroupConversation(mGroupId);
            Log.i("ChatController", "create group success");
            // 是单聊
        } else if (mConv == null && !mIsGroup) {
            mConv = Conversation.createSingleConversation(mTargetId, mTargetAppKey);
            UserInfo userInfo = (UserInfo)mConv.getTargetInfo();
            if (TextUtils.isEmpty(userInfo.getNickname())) {
                mChatView.setChatTitle(userInfo.getUserName());
            }else {
                mChatView.setChatTitle(userInfo.getNickname());
            }
        }
        if (mConv != null) {
            String draft = intent.getStringExtra(JChatDemoApplication.DRAFT);
            if (draft != null && !TextUtils.isEmpty(draft)) {
                mChatView.setInputText(draft);
            }
            if (mIsGroup) {
                mChatAdapter = new MsgListAdapter(mContext, mGroupId, longClickListener);
            } else {
                mChatAdapter = new MsgListAdapter(mContext, mTargetId, mTargetAppKey, longClickListener);
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
                //发送保存为草稿事件到会话列表
                if (isGroup()) {
                    EventBus.getDefault().post(new Event.DraftEvent(getGroupId(),
                            mChatView.getChatInput()));
                } else {
                    EventBus.getDefault().post(new Event.DraftEvent(getTargetId(), mTargetAppKey,
                            mChatView.getChatInput()));
                }
                mContext.finish();
                break;
            // 聊天详细信息
            case R.id.right_btn:
                if (mChatView.getMoreMenu().getVisibility() == View.VISIBLE) {
                    mChatView.dismissMoreMenu();
                }
                dismissSoftInput();
                mContext.startChatDetailActivity(mIsGroup, mTargetId, mTargetAppKey, mGroupId);
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
                    mChatView.notKeyBoard(mConv, mChatAdapter, mChatView);
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
                    intent.putExtra(JChatDemoApplication.GROUP_ID, mGroupId);
                } else {
                    intent.putExtra(JChatDemoApplication.TARGET_ID, mTargetId);
                    intent.putExtra(JChatDemoApplication.TARGET_APP_KEY, mTargetAppKey);
                }
                intent.putExtra(JChatDemoApplication.IS_GROUP, mIsGroup);
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
                switch (view.getId()) {
                    case R.id.chat_input_et:
                        if (mChatView.getMoreMenu().getVisibility() == View.VISIBLE && !mShowSoftInput) {
                            showSoftInputAndDismissMenu();
                            return false;
                        }else {
                            return false;
                        }
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

    private void dismissSoftInput() {
        if (mShowSoftInput) {
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
        if (FileHelper.isSdCardExist()) {
            mPhotoPath = FileHelper.createAvatarPath(null);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mPhotoPath)));
            try {
                mContext.startActivityForResult(intent, JChatDemoApplication.REQUEST_CODE_TAKE_PHOTO);
            } catch (ActivityNotFoundException anf) {
                Toast.makeText(mContext, mContext.getString(R.string.camera_not_prepared),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.sdcard_not_exist_toast),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public String getPhotoPath() {
        return mPhotoPath;
    }

    public void releaseMediaPlayer() {
        mChatAdapter.releaseMediaPlayer();
    }

    public void resetUnreadMsg() {
        if (mConv != null) {
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
                        controller.mChatView.getListView()
                                .setOffset(controller.mChatAdapter.getOffset());
                        break;
                    case REFRESH_CHAT_TITLE:
                        if (controller.mGroupInfo != null) {
                            //检查自己是否在群组中
                            UserInfo info = controller.mGroupInfo.getGroupMemberInfo(JMessageClient.getMyInfo()
                                    .getUserName());
                            if (!TextUtils.isEmpty(controller.mGroupInfo.getGroupName())){
                                controller.mGroupName = controller.mGroupInfo.getGroupName();
                                if (info != null){
                                    controller.mChatView.setChatTitle(controller.mGroupName,
                                            controller.mGroupInfo.getGroupMembers().size());
                                    controller.mChatView.showRightBtn();
                                }else {
                                    controller.mChatView.setChatTitle(controller.mGroupName);
                                    controller.mChatView.dismissRightBtn();
                                }
                            }
                        }
                        break;
                }
            }
        }
    }


    public MsgListAdapter getAdapter() {
        return mChatAdapter;
    }

    public Conversation getConversation() {
        return mConv;
    }

    public String getTargetId() {
        return mTargetId;
    }

    public String getTargetAppKey() {
        return mTargetAppKey;
    }

    public long getGroupId() {
        return mGroupId;
    }

    public boolean isGroup() {
        return mIsGroup;
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (oldh - h > 300) {
            mShowSoftInput = true;
            mChatView.setMoreMenuHeight();
        } else {
            mShowSoftInput = false;
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

    private ContentLongClickListener longClickListener = new ContentLongClickListener() {
        @Override
        public void onContentLongClick(final int position, View view) {
            Log.i("ChatController", "long click position" + position);
            final Message msg = mChatAdapter.getMessage(position);
            UserInfo userInfo = msg.getFromUser();
            if (msg.getContentType() != ContentType.image) {
                // 长按文本弹出菜单
                String name = userInfo.getNickname();
                OnClickListener listener = new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        switch (v.getId()) {
                            case R.id.copy_msg_btn:
                                if (msg.getContentType() == ContentType.text) {
                                    final String content = ((TextContent) msg.getContent()).getText();
                                    if (Build.VERSION.SDK_INT > 11) {
                                        ClipboardManager clipboard = (ClipboardManager) mContext
                                                .getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData clip = ClipData.newPlainText("Simple text", content);
                                        clipboard.setPrimaryClip(clip);
                                    } else {
                                        ClipboardManager clipboard = (ClipboardManager) mContext
                                                .getSystemService(Context.CLIPBOARD_SERVICE);
                                        clipboard.getText();// 设置Clipboard 的内容
                                        if (clipboard.hasText()) {
                                            clipboard.getText();
                                        }
                                    }

                                    Toast.makeText(mContext, mContext.getString(R.string.copy_toast),
                                            Toast.LENGTH_SHORT).show();
                                    mDialog.dismiss();
                                }
                                break;
                            case R.id.forward_msg_btn:
                                mDialog.dismiss();
                                break;
                            case R.id.delete_msg_btn:
                                mConv.deleteMessage(msg.getId());
                                mChatAdapter.removeMessage(position);
                                mDialog.dismiss();
                                break;
                        }
                    }
                };
                boolean hide = msg.getContentType() == ContentType.voice;
                mDialog = DialogCreator.createLongPressMessageDialog(mContext, name, hide, listener);
                mDialog.show();
                mDialog.getWindow().setLayout((int) (0.8 * mWidth), WindowManager.LayoutParams.WRAP_CONTENT);
            } else {
                String name = msg.getFromUser().getNickname();
                OnClickListener listener = new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        switch (v.getId()) {
                            case R.id.copy_msg_btn:
                                break;
                            case R.id.forward_msg_btn:
                                mDialog.dismiss();
                                break;
                            case R.id.delete_msg_btn:
                                mConv.deleteMessage(msg.getId());
                                mChatAdapter.removeMessage(position);
                                mDialog.dismiss();
                                break;
                        }
                    }
                };
                mDialog = DialogCreator.createLongPressMessageDialog(mContext, name, true, listener);
                mDialog.show();
                mDialog.getWindow().setLayout((int) (0.8 * mWidth), WindowManager.LayoutParams.WRAP_CONTENT);
            }
        }
    };

}
