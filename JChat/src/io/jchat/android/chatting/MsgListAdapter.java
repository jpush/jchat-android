package io.jchat.android.chatting;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.content.CustomContent;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.enums.MessageStatus;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.callback.ProgressUpdateCallback;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.enums.MessageDirect;
import io.jchat.android.activity.FriendInfoActivity;
import io.jchat.android.activity.MeInfoActivity;
import io.jchat.android.activity.SearchFriendDetailActivity;
import io.jchat.android.application.JChatDemoApplication;
import io.jchat.android.chatting.utils.DialogCreator;
import io.jchat.android.chatting.utils.HandleResponseCode;
import io.jchat.android.chatting.utils.IdHelper;
import io.jchat.android.chatting.utils.TimeFormat;
import cn.jpush.im.api.BasicCallback;

@SuppressLint("NewApi")
public class MsgListAdapter extends BaseAdapter {

    private static final String TAG = "MsgListAdapter";
    public static final int PAGE_MESSAGE_COUNT = 18;
    private Context mContext;
    private Conversation mConv;
    private List<Message> mMsgList = new ArrayList<Message>();//所有消息列表
    private LayoutInflater mInflater;
    private long mGroupId;

    // 14种Item的类型
    // 文本
    private final int TYPE_RECEIVE_TXT = 0;
    private final int TYPE_SEND_TXT = 1;
    // 图片
    private final int TYPE_SEND_IMAGE = 2;
    private final int TYPE_RECEIVER_IMAGE = 3;
    // 位置
    private final int TYPE_SEND_LOCATION = 4;
    private final int TYPE_RECEIVER_LOCATION = 5;
    // 语音
    private final int TYPE_SEND_VOICE = 6;
    private final int TYPE_RECEIVER_VOICE = 7;
    //群成员变动
    private final int TYPE_GROUP_CHANGE = 8;
    //自定义消息
    private final int TYPE_CUSTOM_TXT = 9;
    //视频
    private final int TYPE_SEND_VIDEO = 10;
    private final int TYPE_RECEIVE_VIDEO = 11;
    //文件
    private final int TYPE_SEND_FILE = 12;
    private final int TYPE_RECEIVE_FILE = 13;
    //当前第0项消息的位置
    private int mStart;
    //上一页的消息数
    private int mOffset = PAGE_MESSAGE_COUNT;
    private boolean mHasLastPage = false;
    private Dialog mDialog;
    //发送图片消息的队列
    private Queue<Message> mMsgQueue = new LinkedList<Message>();
    private ChatItemController mController;
    private Activity mActivity;
    private int mWidth;
    private ContentLongClickListener mLongClickListener;

    public MsgListAdapter(Context context, Conversation conv, ContentLongClickListener longClickListener) {
        this.mContext = context;
        mActivity = (Activity) context;
        DisplayMetrics dm = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        mWidth = dm.widthPixels;

        mInflater = LayoutInflater.from(mContext);
        this.mConv = conv;
        this.mMsgList = mConv.getMessagesFromNewest(0, mOffset);
        reverse(mMsgList);
        mLongClickListener = longClickListener;
        this.mController = new ChatItemController(this, context, conv, mMsgList, dm.density,
                longClickListener);
        mStart = mOffset;
        if (mConv.getType() == ConversationType.single) {
            UserInfo userInfo = (UserInfo) mConv.getTargetInfo();
            if (!TextUtils.isEmpty(userInfo.getAvatar())) {
                userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                    @Override
                    public void gotResult(int status, String desc, Bitmap bitmap) {
                        if (status == 0) {
                            notifyDataSetChanged();
                        } else {
                            HandleResponseCode.onHandle(mContext, status, false);
                        }
                    }
                });
            }
        } else {
            GroupInfo groupInfo = (GroupInfo) mConv.getTargetInfo();
            mGroupId = groupInfo.getGroupID();
        }
        checkSendingImgMsg();
    }

    public MsgListAdapter(Context context, Conversation conv, ContentLongClickListener longClickListener,
                          int msgId) {
        this.mContext = context;
        mActivity = (Activity) context;
        DisplayMetrics dm = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        mWidth = dm.widthPixels;

        mInflater = LayoutInflater.from(mContext);
        this.mConv = conv;
        if (mConv.getUnReadMsgCnt() > PAGE_MESSAGE_COUNT) {
            this.mMsgList = mConv.getMessagesFromNewest(0, mConv.getUnReadMsgCnt());
            mStart = mConv.getUnReadMsgCnt();
        } else {
            this.mMsgList = mConv.getMessagesFromNewest(0, mOffset);
            mStart = mOffset;
        }
        reverse(mMsgList);
        mLongClickListener = longClickListener;
        this.mController = new ChatItemController(this, context, conv, mMsgList, dm.density,
                longClickListener);
        GroupInfo groupInfo = (GroupInfo) mConv.getTargetInfo();
        mGroupId = groupInfo.getGroupID();
        checkSendingImgMsg();

    }

    private void reverse(List<Message> list) {
        if (list.size() > 0) {
            Collections.reverse(list);
        }
    }

    public void dropDownToRefresh() {
        if (mConv != null) {
            List<Message> msgList = mConv.getMessagesFromNewest(mStart, PAGE_MESSAGE_COUNT);
            if (msgList != null) {
                for (Message msg : msgList) {
                    mMsgList.add(0, msg);
                }
                if (msgList.size() > 0) {
                    checkSendingImgMsg();
                    mOffset = msgList.size();
                    mHasLastPage = true;
                } else {
                    mOffset = 0;
                    mHasLastPage = false;
                }
                notifyDataSetChanged();
            }
        }
    }

    public int getOffset() {
        return mOffset;
    }

    public boolean isHasLastPage() {
        return mHasLastPage;
    }

    public void refreshStartPosition() {
        mStart += mOffset;
    }

    //当有新消息加到MsgList，自增mStart
    private void incrementStartPosition() {
        ++mStart;
    }


    /**
     * 检查图片是否处于创建状态，如果是，则加入发送队列
     */
    private void checkSendingImgMsg() {
        for (Message msg : mMsgList) {
            if (msg.getStatus() == MessageStatus.created
                    && msg.getContentType() == ContentType.image) {
                mMsgQueue.offer(msg);
            }
        }

        if (mMsgQueue.size() > 0) {
            Message message = mMsgQueue.element();
            if (mConv.getType() == ConversationType.single) {
                UserInfo userInfo = (UserInfo) message.getTargetInfo();
                if (userInfo.isFriend()) {
                    sendNextImgMsg(message);
                } else {
                    CustomContent customContent = new CustomContent();
                    customContent.setBooleanValue("notFriend", true);
                    Message customMsg = mConv.createSendMessage(customContent);
                    addMsgToList(customMsg);
                }
            } else {
                sendNextImgMsg(message);
            }

            notifyDataSetChanged();
        }
    }

    public void setSendMsgs(int[] msgIds) {
        Message msg;
        for (int msgId : msgIds) {
            msg = mConv.getMessage(msgId);
            if (msg != null) {
                mMsgList.add(msg);
                incrementStartPosition();
                mMsgQueue.offer(msg);
            }
        }

        if (mMsgQueue.size() > 0) {
            Message message = mMsgQueue.element();
            if (mConv.getType() == ConversationType.single) {
                UserInfo userInfo = (UserInfo) message.getTargetInfo();
                if (userInfo.isFriend()) {
                    sendNextImgMsg(message);
                } else {
                    CustomContent customContent = new CustomContent();
                    customContent.setBooleanValue("notFriend", true);
                    Message customMsg = mConv.createSendMessage(customContent);
                    addMsgToList(customMsg);
                }
            } else {
                sendNextImgMsg(message);
            }

            notifyDataSetChanged();
        }
    }

    /**
     * 从发送队列中出列，并发送图片
     *
     * @param msg 图片消息
     */
    private void sendNextImgMsg(Message msg) {
        JMessageClient.sendMessage(msg);
        msg.setOnSendCompleteCallback(new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                //出列
                mMsgQueue.poll();
                //如果队列不为空，则继续发送下一张
                if (!mMsgQueue.isEmpty()) {
                    sendNextImgMsg(mMsgQueue.element());
                }
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addMsgToList(Message msg) {
        mMsgList.add(msg);
        incrementStartPosition();
        notifyDataSetChanged();
    }

    public void addMsgList(int[] msgIds) {
        for (int msgId : msgIds) {
            Message msg = mConv.getMessage(msgId);
            if (msg != null) {
                mMsgList.add(msg);
                incrementStartPosition();
            }
        }
        notifyDataSetChanged();
    }

    public Message getLastMsg() {
        if (mMsgList.size() > 0) {
            return mMsgList.get(mMsgList.size() - 1);
        } else {
            return null;
        }
    }

    public Message getMessage(int position) {
        return mMsgList.get(position);
    }

    public void removeMessage(int position) {
        mMsgList.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mMsgList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = mMsgList.get(position);
        //是文字类型或者自定义类型（用来显示群成员变化消息）
        switch (msg.getContentType()) {
            case text:
                return msg.getDirect() == MessageDirect.send ? TYPE_SEND_TXT
                        : TYPE_RECEIVE_TXT;
            case image:
                return msg.getDirect() == MessageDirect.send ? TYPE_SEND_IMAGE
                        : TYPE_RECEIVER_IMAGE;
            case voice:
                return msg.getDirect() == MessageDirect.send ? TYPE_SEND_VOICE
                        : TYPE_RECEIVER_VOICE;
            case location:
                return msg.getDirect() == MessageDirect.send ? TYPE_SEND_LOCATION
                        : TYPE_RECEIVER_LOCATION;
            case video:
                return msg.getDirect() == MessageDirect.send ? TYPE_SEND_VIDEO
                        : TYPE_RECEIVE_VIDEO;
            case file:
                return msg.getDirect() == MessageDirect.send ? TYPE_SEND_FILE
                        : TYPE_RECEIVE_FILE;
            case eventNotification:
                return TYPE_GROUP_CHANGE;
            default:
                return TYPE_CUSTOM_TXT;
        }
    }

    public int getViewTypeCount() {
        return 14;
    }

    private View createViewByType(Message msg, int position) {
        // 会话类型
        switch (msg.getContentType()) {
            case image:
                return getItemViewType(position) == TYPE_SEND_IMAGE ? mInflater
                        .inflate(IdHelper.getLayout(mContext, "jmui_chat_item_send_image"), null) : mInflater
                        .inflate(IdHelper.getLayout(mContext, "jmui_chat_item_receive_image"), null);
            case voice:
                return getItemViewType(position) == TYPE_SEND_VOICE ? mInflater
                        .inflate(IdHelper.getLayout(mContext, "jmui_chat_item_send_voice"), null) : mInflater
                        .inflate(IdHelper.getLayout(mContext, "jmui_chat_item_receive_voice"), null);
            case location:
                return getItemViewType(position) == TYPE_SEND_LOCATION ? mInflater
                        .inflate(IdHelper.getLayout(mContext, "jmui_chat_item_send_location"), null) : mInflater
                        .inflate(IdHelper.getLayout(mContext, "jmui_chat_item_receive_location"), null);
            case video:
                return getItemViewType(position) == TYPE_SEND_VIDEO ? mInflater
                        .inflate(IdHelper.getLayout(mContext, "jmui_chat_item_send_video"), null) : mInflater
                        .inflate(IdHelper.getLayout(mContext, "jmui_chat_item_receive_video"), null);
            case file:
                return getItemViewType(position) == TYPE_SEND_FILE ? mInflater
                        .inflate(IdHelper.getLayout(mContext, "jmui_chat_item_send_file"), null) : mInflater
                        .inflate(IdHelper.getLayout(mContext, "jmui_chat_item_receive_file"), null);
            case eventNotification:
                if (getItemViewType(position) == TYPE_GROUP_CHANGE)
                    return mInflater.inflate(IdHelper.getLayout(mContext, "jmui_chat_item_group_change"), null);
            case text:
                return getItemViewType(position) == TYPE_SEND_TXT ? mInflater
                        .inflate(IdHelper.getLayout(mContext, "jmui_chat_item_send_text"), null) : mInflater
                        .inflate(IdHelper.getLayout(mContext, "jmui_chat_item_receive_text"), null);
            default:
                return mInflater.inflate(IdHelper.getLayout(mContext, "jmui_chat_item_group_change"), null);
        }
    }

    @Override
    public Message getItem(int position) {
        return mMsgList.get(position);
    }

    public void clearMsgList() {
        mMsgList.clear();
        mStart = 0;
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Message msg = mMsgList.get(position);
        final UserInfo userInfo = msg.getFromUser();
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = createViewByType(msg, position);
            holder.msgTime = (TextView) convertView
                    .findViewById(IdHelper.getViewID(mContext, "jmui_send_time_txt"));
            holder.headIcon = (CircleImageView) convertView
                    .findViewById(IdHelper.getViewID(mContext, "jmui_avatar_iv"));
            holder.displayName = (TextView) convertView
                    .findViewById(IdHelper.getViewID(mContext, "jmui_display_name_tv"));
            holder.txtContent = (TextView) convertView
                    .findViewById(IdHelper.getViewID(mContext, "jmui_msg_content"));
            holder.sendingIv = (ImageView) convertView
                    .findViewById(IdHelper.getViewID(mContext, "jmui_sending_iv"));
            holder.resend = (ImageButton) convertView
                    .findViewById(IdHelper.getViewID(mContext, "jmui_fail_resend_ib"));
            switch (msg.getContentType()) {
                case file:
                    holder.picture = (ImageView) convertView
                            .findViewById(IdHelper.getViewID(mContext, "jmui_picture_iv"));
                    holder.progressTv = (TextView) convertView
                            .findViewById(IdHelper.getViewID(mContext, "jmui_progress_tv"));
                    holder.contentLl = (LinearLayout) convertView
                            .findViewById(IdHelper.getViewID(mContext, "jmui_send_file_ll"));
                    holder.sizeTv = (TextView) convertView
                            .findViewById(IdHelper.getViewID(mContext, "jmui_send_file_size"));
                    break;
                case image:
                    holder.picture = (ImageView) convertView
                            .findViewById(IdHelper.getViewID(mContext, "jmui_picture_iv"));
                    holder.progressTv = (TextView) convertView
                            .findViewById(IdHelper.getViewID(mContext, "jmui_progress_tv"));
                    break;
                case voice:
                    holder.voice = (ImageView) convertView
                            .findViewById(IdHelper.getViewID(mContext, "jmui_voice_iv"));
                    holder.voiceLength = (TextView) convertView
                            .findViewById(IdHelper.getViewID(mContext, "jmui_voice_length_tv"));
                    holder.readStatus = (ImageView) convertView
                            .findViewById(IdHelper.getViewID(mContext, "jmui_read_status_iv"));
                    break;
                case location:
                    holder.location = (TextView) convertView
                            .findViewById(IdHelper.getViewID(mContext, "jmui_loc_desc"));
                    holder.picture = (ImageView) convertView
                            .findViewById(IdHelper.getViewID(mContext, "jmui_picture_iv"));
                    break;
                case custom:
                case eventNotification:
                    holder.groupChange = (TextView) convertView
                            .findViewById(IdHelper.getViewID(mContext, "jmui_group_content"));
                    break;
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        long nowDate = msg.getCreateTime();
        if (mOffset == 18) {
            if (position == 0 || position % 18 == 0) {
                TimeFormat timeFormat = new TimeFormat(mContext, nowDate);
                holder.msgTime.setText(timeFormat.getDetailTime());
                holder.msgTime.setVisibility(View.VISIBLE);
            } else {
                long lastDate = mMsgList.get(position - 1).getCreateTime();
                // 如果两条消息之间的间隔超过十分钟则显示时间
                if (nowDate - lastDate > 600000) {
                    TimeFormat timeFormat = new TimeFormat(mContext, nowDate);
                    holder.msgTime.setText(timeFormat.getDetailTime());
                    holder.msgTime.setVisibility(View.VISIBLE);
                } else {
                    holder.msgTime.setVisibility(View.GONE);
                }
            }
        } else {
            if (position == 0 || position == mOffset
                    || (position - mOffset) % 18 == 0) {
                TimeFormat timeFormat = new TimeFormat(mContext, nowDate);

                holder.msgTime.setText(timeFormat.getDetailTime());
                holder.msgTime.setVisibility(View.VISIBLE);
            } else {
                long lastDate = mMsgList.get(position - 1).getCreateTime();
                // 如果两条消息之间的间隔超过十分钟则显示时间
                if (nowDate - lastDate > 600000) {
                    TimeFormat timeFormat = new TimeFormat(mContext, nowDate);
                    holder.msgTime.setText(timeFormat.getDetailTime());
                    holder.msgTime.setVisibility(View.VISIBLE);
                } else {
                    holder.msgTime.setVisibility(View.GONE);
                }
            }
        }

        //显示头像
        if (holder.headIcon != null) {
            if (userInfo != null && !TextUtils.isEmpty(userInfo.getAvatar())) {
                userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                    @Override
                    public void gotResult(int status, String desc, Bitmap bitmap) {
                        if (status == 0) {
                            holder.headIcon.setImageBitmap(bitmap);
                        } else {
                            holder.headIcon.setImageResource(IdHelper.getDrawable(mContext,
                                    "jmui_head_icon"));
                            HandleResponseCode.onHandle(mContext, status, false);
                        }
                    }
                });
            } else {
                holder.headIcon.setImageResource(IdHelper.getDrawable(mContext, "jmui_head_icon"));
            }

            // 点击头像跳转到个人信息界面
            holder.headIcon.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Intent intent = new Intent();
                    if (msg.getDirect() == MessageDirect.send) {
                        intent.putExtra(JChatDemoApplication.TARGET_ID, JMessageClient.getMyInfo().getUserName());
                        intent.setClass(mContext, MeInfoActivity.class);
                        mContext.startActivity(intent);
                    } else {
                        String targetID = userInfo.getUserName();
                        intent.putExtra(JChatDemoApplication.TARGET_ID, targetID);
                        intent.putExtra(JChatDemoApplication.TARGET_APP_KEY, userInfo.getAppKey());
                        intent.putExtra(JChatDemoApplication.GROUP_ID, mGroupId);
                        if (userInfo.isFriend()) {
                            intent.setClass(mContext, FriendInfoActivity.class);
                        } else {
                            intent.setClass(mContext, SearchFriendDetailActivity.class);
                        }
                        ((Activity) mContext).startActivityForResult(intent,
                                JChatDemoApplication.REQUEST_CODE_FRIEND_INFO);
                    }
                }
            });

            holder.headIcon.setTag(position);
            holder.headIcon.setOnLongClickListener(mLongClickListener);
        }

        switch (msg.getContentType()) {
            case text:
                mController.handleTextMsg(msg, holder, position);
                break;
            case image:
                mController.handleImgMsg(msg, holder, position);
                break;
            case voice:
                mController.handleVoiceMsg(msg, holder, position);
                break;
            case location:
                mController.handleLocationMsg(msg, holder, position);
                break;
            case file:
                mController.handleFileMsg(msg, holder, position);
                break;
            case video:
                break;
            case eventNotification:
                mController.handleGroupChangeMsg(msg, holder);
                break;
            default:
                mController.handleCustomMsg(msg, holder);
        }

        return convertView;
    }

    //重发对话框
    public void showResendDialog(final ViewHolder holder, final Message msg) {
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == IdHelper.getViewID(mContext, "jmui_cancel_btn")) {
                    mDialog.dismiss();
                } else {
                    mDialog.dismiss();
                    switch (msg.getContentType()) {
                        case text:
                        case voice:
                            resendTextOrVoice(holder, msg);
                            break;
                        case image:
                            resendImage(holder, msg);
                            break;
                        case file:
                            resendFile(holder, msg);
                            break;
                    }
                }
            }
        };
        if (mConv.getType() == ConversationType.single) {
            UserInfo userInfo = (UserInfo) msg.getTargetInfo();
            if (!userInfo.isFriend()) {
                Toast.makeText(mContext, IdHelper.getString(mContext, "send_target_is_not_friend"),
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }
        mDialog = DialogCreator.createResendDialog(mContext, listener);
        mDialog.getWindow().setLayout((int) (0.8 * mWidth), WindowManager.LayoutParams.WRAP_CONTENT);
        mDialog.show();
    }

    private void resendTextOrVoice(final ViewHolder holder, Message msg) {
        holder.resend.setVisibility(View.GONE);
        holder.sendingIv.setVisibility(View.VISIBLE);
        holder.sendingIv.startAnimation(mController.mSendingAnim);

        if (!msg.isSendCompleteCallbackExists()) {
            msg.setOnSendCompleteCallback(new BasicCallback() {
                @Override
                public void gotResult(final int status, String desc) {
                    holder.sendingIv.clearAnimation();
                    holder.sendingIv.setVisibility(View.GONE);
                    if (status != 0) {
                        HandleResponseCode.onHandle(mContext, status, false);
                        holder.resend.setVisibility(View.VISIBLE);
                        Log.i(TAG, "Resend message failed!");
                    }
                }
            });
        }

        JMessageClient.sendMessage(msg);
    }

    private void resendImage(final ViewHolder holder, Message msg) {
        holder.sendingIv.setVisibility(View.VISIBLE);
        holder.sendingIv.startAnimation(mController.mSendingAnim);
        holder.picture.setAlpha(0.75f);
        holder.resend.setVisibility(View.GONE);
        holder.progressTv.setVisibility(View.VISIBLE);
        try {
            // 显示上传进度
            msg.setOnContentUploadProgressCallback(new ProgressUpdateCallback() {
                @Override
                public void onProgressUpdate(final double progress) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String progressStr = (int) (progress * 100) + "%";
                            holder.progressTv.setText(progressStr);
                        }
                    });
                }
            });
            if (!msg.isSendCompleteCallbackExists()) {
                msg.setOnSendCompleteCallback(new BasicCallback() {
                    @Override
                    public void gotResult(final int status, String desc) {
                        holder.sendingIv.clearAnimation();
                        holder.sendingIv.setVisibility(View.GONE);
                        holder.progressTv.setVisibility(View.GONE);
                        holder.picture.setAlpha(1.0f);
                        if (status != 0) {
                            HandleResponseCode.onHandle(mContext, status, false);
                            holder.resend.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
            JMessageClient.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resendFile(final ViewHolder holder, final Message msg) {
        holder.contentLl.setBackgroundColor(Color.parseColor("#86222222"));
        holder.resend.setVisibility(View.GONE);
        holder.progressTv.setVisibility(View.VISIBLE);
        try {
            msg.setOnContentUploadProgressCallback(new ProgressUpdateCallback() {
                @Override
                public void onProgressUpdate(final double progress) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String progressStr = (int) (progress * 100) + "%";
                            holder.progressTv.setText(progressStr);
                        }
                    });
                }
            });
            if (!msg.isSendCompleteCallbackExists()) {
                msg.setOnSendCompleteCallback(new BasicCallback() {
                    @Override
                    public void gotResult(final int status, String desc) {
                        holder.progressTv.setVisibility(View.GONE);
                        holder.contentLl.setBackground(mContext.getDrawable(IdHelper.getDrawable(mContext,
                                "jmui_msg_send_bg")));
                        if (status != 0) {
                            HandleResponseCode.onHandle(mContext, status, false);
                            holder.resend.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
            JMessageClient.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAudioPlayByEarPhone(int state) {
        mController.setAudioPlayByEarPhone(state);
    }

    public void initMediaPlayer() {
        mController.initMediaPlayer();
    }

    public void releaseMediaPlayer() {
        mController.releaseMediaPlayer();
    }

    public void stopMediaPlayer() {
        mController.stopMediaPlayer();
    }


    public static abstract class ContentLongClickListener implements OnLongClickListener {

        @Override
        public boolean onLongClick(View v) {
            onContentLongClick((Integer) v.getTag(), v);
            return true;
        }

        public abstract void onContentLongClick(int position, View view);
    }

    public static class ViewHolder {
        TextView msgTime;
        CircleImageView headIcon;
        TextView displayName;
        TextView txtContent;
        ImageView picture;
        TextView progressTv;
        ImageButton resend;
        TextView voiceLength;
        ImageView voice;
        // 录音是否播放过的标志
        ImageView readStatus;
        TextView location;
        TextView groupChange;
        ImageView sendingIv;
        LinearLayout contentLl;
        TextView sizeTv;
    }
}
