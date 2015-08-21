package io.jchat.android.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import cn.jpush.im.android.api.callback.DownloadAvatarCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.content.EventNotificationContent;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.R;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.callback.DownloadCompletionCallback;
import cn.jpush.im.android.api.callback.ProgressUpdateCallback;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.content.VoiceContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.enums.MessageDirect;

import io.jchat.android.activity.BrowserViewPagerActivity;
import io.jchat.android.activity.FriendInfoActivity;
import io.jchat.android.activity.MeInfoActivity;
import io.jchat.android.application.JPushDemoApplication;
import io.jchat.android.tools.BitmapLoader;
import io.jchat.android.tools.HandleResponseCode;
import io.jchat.android.tools.NativeImageLoader;
import io.jchat.android.tools.TimeFormat;
import io.jchat.android.view.CircleImageView;
import cn.jpush.im.api.BasicCallback;

@SuppressLint("NewApi")
public class MsgListAdapter extends BaseAdapter {

    private static final String TAG = "MsgListAdapter";

    private Context mContext;
    private String mTargetID;
    private Conversation mConv;
    private List<Message> mMsgList = new ArrayList<Message>();//所有消息列表
    private List<Integer> mIndexList = new ArrayList<Integer>();//语音索引
    private LayoutInflater mInflater;
    private boolean mSetData = false;
    private boolean mIsGroup = false;
    private long mGroupID;
    private int mPosition = -1;// 和mSetData一起组成判断播放哪条录音的依据
    private static final int UPDATE_IMAGEVIEW = 1999;
    private final int UPDATE_PROGRESS = 1998;
    // 9种Item的类型
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
    private final MediaPlayer mp = new MediaPlayer();
    private AnimationDrawable mVoiceAnimation;
    private FileInputStream mFIS;
    private FileDescriptor mFD;
    private Activity mActivity;
    private final MyHandler myHandler = new MyHandler(this);
    private boolean autoPlay = false;
    private int mWidth;
    private int nextPlayPosition = 0;
    private double mDensity;
    private boolean mIsEarPhoneOn;
    private GroupInfo mGroupInfo;
    //当前第0项消息的位置
    private int mStart;
    //偏移量
    private int mOffset = 18;

    public MsgListAdapter(Context context, String targetID) {
        initData(context);
        this.mTargetID = targetID;
        this.mConv = JMessageClient.getSingleConversation(mTargetID);
        this.mMsgList = mConv.getMessagesFromNewest(0, mOffset);
        reverse(mMsgList);
        mStart = mOffset;
        List<String> userIDList = new ArrayList<String>();
        userIDList.add(targetID);
        userIDList.add(JMessageClient.getMyInfo().getUserName());
        NativeImageLoader.getInstance().setAvatarCache(userIDList, (int) (50 * mDensity), new NativeImageLoader.CacheAvatarCallBack() {
            @Override
            public void onCacheAvatarCallBack(int status) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "init avatar succeed");
                        notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void reverse(List<Message> list) {
        Collections.reverse(list);
    }

    public MsgListAdapter(Context context, long groupID, GroupInfo groupInfo) {
        initData(context);
        this.mGroupID = groupID;
        this.mIsGroup = true;
        this.mConv = JMessageClient.getGroupConversation(groupID);
        this.mMsgList = mConv.getMessagesFromNewest(0, mOffset);
        reverse(mMsgList);
        mStart = mOffset;
        this.mGroupInfo = groupInfo;
    }

    private void initData(Context context) {
        this.mContext = context;
        mActivity = (Activity) context;
        DisplayMetrics dm = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        mDensity = dm.density;
        mWidth = dm.widthPixels;
        mInflater = LayoutInflater.from(mContext);
        AudioManager audioManager = (AudioManager) mContext
                .getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_NORMAL);
        if (audioManager.isSpeakerphoneOn()) {
            audioManager.setSpeakerphoneOn(true);
        } else audioManager.setSpeakerphoneOn(false);
        mp.setAudioStreamType(AudioManager.STREAM_RING);
        mp.setOnErrorListener(new OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });
    }

    public void dropDownToRefresh() {
        if(mConv != null){
            List<Message> msgList = mConv.getMessagesFromNewest(mStart, 18);
            if (msgList != null){
                for (Message msg : msgList){
                    mMsgList.add(0, msg);
                }
                mOffset = msgList.size();
                notifyDataSetChanged();
            }
        }
    }

    public int getOffset(){
        return mOffset;
    }

    public void refreshStartPosition() {
        mStart += mOffset;
    }

    public void initMediaPlayer() {
        mp.reset();
    }

    public void setSendImg(String targetID, int[] msgIDs) {
        Message msg;
        mConv = JMessageClient.getSingleConversation(targetID);
        for (int msgID : msgIDs) {
            msg = mConv.getMessage(msgID);
            JMessageClient.sendMessage(msg);
            mMsgList.add(msg);
        }
        notifyDataSetChanged();
    }

    public void setSendImg(long groupID, int[] msgIDs) {
        Message msg;
        mConv = JMessageClient.getGroupConversation(groupID);
        for (int msgID : msgIDs) {
            msg = mConv.getMessage(msgID);
            JMessageClient.sendMessage(msg);
            mMsgList.add(msg);
        }
        notifyDataSetChanged();
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    public void releaseMediaPlayer() {
        if (mp != null)
            mp.release();
    }

    public void addMsgToList(Message msg) {
        mMsgList.add(msg);
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getCount() {
        return mMsgList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = mMsgList.get(position);
        //是文字类型或者自定义类型（用来显示群成员变化消息）
        if (msg.getContentType().equals(ContentType.text)) {
            return msg.getDirect().equals(MessageDirect.send) ? TYPE_SEND_TXT
                    : TYPE_RECEIVE_TXT;
        } else if (msg.getContentType().equals(ContentType.image)) {
            return msg.getDirect().equals(MessageDirect.send) ? TYPE_SEND_IMAGE
                    : TYPE_RECEIVER_IMAGE;
        } else if (msg.getContentType().equals(ContentType.voice)) {
            return msg.getDirect().equals(MessageDirect.send) ? TYPE_SEND_VOICE
                    : TYPE_RECEIVER_VOICE;
        } else if (msg.getContentType().equals(ContentType.eventNotification)) {
            return TYPE_GROUP_CHANGE;
        } else if (msg.getContentType().equals(ContentType.location)) {
            return msg.getDirect().equals(MessageDirect.send) ? TYPE_SEND_LOCATION
                    : TYPE_RECEIVER_LOCATION;
        } else {
            return TYPE_CUSTOM_TXT;
        }
    }

    public int getViewTypeCount() {
        return 11;
    }

    private View createViewByType(Message msg, int position) {
        // 会话类型
        switch (msg.getContentType()) {
            case image:
                return getItemViewType(position) == TYPE_SEND_IMAGE ? mInflater
                        .inflate(R.layout.chat_item_send_image, null) : mInflater
                        .inflate(R.layout.chat_item_receive_image, null);
            case voice:
                return getItemViewType(position) == TYPE_SEND_VOICE ? mInflater
                        .inflate(R.layout.chat_item_send_voice, null) : mInflater
                        .inflate(R.layout.chat_item_receive_voice, null);
            case location:
                return getItemViewType(position) == TYPE_SEND_LOCATION ? mInflater
                        .inflate(R.layout.chat_item_send_location, null)
                        : mInflater.inflate(R.layout.chat_item_receive_location,
                        null);
            case eventNotification:
                if (getItemViewType(position) == TYPE_GROUP_CHANGE)
                    return mInflater.inflate(R.layout.chat_item_group_change, null);
            case text:
                return getItemViewType(position) == TYPE_SEND_TXT ? mInflater
                        .inflate(R.layout.chat_item_send_text, null) : mInflater
                        .inflate(R.layout.chat_item_receive_text, null);
            default:
                return mInflater.inflate(R.layout.chat_item_group_change, null);
        }
    }

    @Override
    public Message getItem(int position) {
        return mMsgList.get(position);
    }

    public void setAudioPlayByEarPhone(int state) {
        AudioManager audioManager = (AudioManager) mContext
                .getSystemService(Context.AUDIO_SERVICE);
        int currVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        audioManager.setMode(AudioManager.MODE_IN_CALL);
        if (state == 0) {
            mIsEarPhoneOn = false;
            audioManager.setSpeakerphoneOn(true);
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
                    AudioManager.STREAM_VOICE_CALL);
            Log.i(TAG, "set SpeakerphoneOn true!");
        } else {
            mIsEarPhoneOn = true;
            audioManager.setSpeakerphoneOn(false);
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, currVolume,
                    AudioManager.STREAM_VOICE_CALL);
            Log.i(TAG, "set SpeakerphoneOn false!");
        }
    }

    public void refreshGroupInfo(GroupInfo groupInfo) {
        mGroupInfo = groupInfo;
        notifyDataSetChanged();
    }

    public void clearMsgList() {
        mMsgList.clear();
        mStart = 0;
        notifyDataSetChanged();
    }

    private static class MyHandler extends Handler {
        private final WeakReference<MsgListAdapter> mAdapter;

        public MyHandler(MsgListAdapter adapter) {
            mAdapter = new WeakReference<MsgListAdapter>(adapter);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            MsgListAdapter adapter = mAdapter.get();
            if (adapter != null) {
                switch (msg.what) {
                    case UPDATE_IMAGEVIEW:
                        Bundle bundle = msg.getData();
                        ViewHolder holder = (ViewHolder) msg.obj;
                        String path = bundle.getString("path");
                        Picasso.with(adapter.mContext).load(new File(path)).into(holder.picture);
                        adapter.notifyDataSetChanged();
                        Log.i(TAG, "Refresh Received picture");
                        break;
                }
            }
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Message msg = mMsgList.get(position);
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            ContentType contentType = msg.getContentType();
            convertView = createViewByType(msg, position);
            if (contentType.equals(ContentType.text)) {
                try {
                    holder.headIcon = (CircleImageView) convertView
                            .findViewById(R.id.avatar_iv);
                    holder.displayName = (TextView) convertView
                            .findViewById(R.id.display_name_tv);
                    holder.txtContent = (TextView) convertView
                            .findViewById(R.id.msg_content);
                    holder.sendingIv = (ImageView) convertView
                            .findViewById(R.id.sending_iv);
                    holder.resend = (ImageButton) convertView
                            .findViewById(R.id.fail_resend_ib);
                    holder.groupChange = (TextView) convertView
                            .findViewById(R.id.group_content);
                } catch (Exception e) {
                }
            } else if (contentType.equals(ContentType.image)) {
                try {
                    holder.headIcon = (CircleImageView) convertView
                            .findViewById(R.id.avatar_iv);
                    holder.displayName = (TextView) convertView
                            .findViewById(R.id.display_name_tv);
                    holder.picture = (ImageView) convertView
                            .findViewById(R.id.picture_iv);
                    holder.sendingIv = (ImageView) convertView
                            .findViewById(R.id.sending_iv);
                    holder.progressTv = (TextView) convertView
                            .findViewById((R.id.progress_tv));
                    holder.resend = (ImageButton) convertView
                            .findViewById(R.id.fail_resend_ib);
                } catch (Exception e) {
                }
            } else if (contentType.equals(ContentType.voice)) {
                try {
                    holder.headIcon = (CircleImageView) convertView
                            .findViewById(R.id.avatar_iv);
                    holder.displayName = (TextView) convertView
                            .findViewById(R.id.display_name_tv);
                    holder.txtContent = (TextView) convertView
                            .findViewById(R.id.msg_content);
                    holder.voice = ((ImageView) convertView
                            .findViewById(R.id.voice_iv));
                    holder.sendingIv = (ImageView) convertView
                            .findViewById(R.id.sending_iv);
                    holder.voiceLength = (TextView) convertView
                            .findViewById(R.id.voice_length_tv);
                    holder.readStatus = (ImageView) convertView
                            .findViewById(R.id.read_status_iv);
                    holder.resend = (ImageButton) convertView
                            .findViewById(R.id.fail_resend_ib);
                } catch (Exception e) {
                }
            } else if (contentType.equals(ContentType.eventNotification)) {
                try {
                    holder.groupChange = (TextView) convertView
                            .findViewById(R.id.group_content);
                } catch (Exception e) {
                }
            } else if (contentType.equals(ContentType.location)) {
                try {
                    holder.headIcon = (CircleImageView) convertView
                            .findViewById(R.id.avatar_iv);
                    holder.displayName = (TextView) convertView
                            .findViewById(R.id.display_name_tv);
                    holder.txtContent = (TextView) convertView
                            .findViewById(R.id.msg_content);
                    holder.sendingIv = (ImageView) convertView
                            .findViewById(R.id.sending_iv);
                    holder.resend = (ImageButton) convertView
                            .findViewById(R.id.fail_resend_ib);
                } catch (Exception e) {
                }
            } else {
                try {
                    holder.groupChange = (TextView) convertView
                            .findViewById(R.id.group_content);
                } catch (Exception e) {
                }
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        switch (msg.getContentType()) {
            case text:
                handleTextMsg(msg, holder, position);
                break;
            case image:
                handleImgMsg(msg, holder, position);
                break;
            case voice:
                handleVoiceMsg(msg, holder, position);
                break;
            case location:
                handleLocationMsg(msg, holder, position);
                break;
            case eventNotification:
                handleGroupChangeMsg(msg, holder);
                break;
            default:
                handleCustomMsg(holder);
        }
        //显示时间
        TextView msgTime = (TextView) convertView
                .findViewById(R.id.send_time_txt);
        long nowDate = msg.getCreateTime();
        if (position != 0) {
            long lastDate = mMsgList.get(position - 1).getCreateTime();
            // 如果两条消息之间的间隔超过十分钟则显示时间
            if (nowDate - lastDate > 600000) {
                TimeFormat timeFormat = new TimeFormat(mContext, nowDate);
                msgTime.setText(timeFormat.getDetailTime());
                msgTime.setVisibility(View.VISIBLE);
            } else {
                msgTime.setVisibility(View.GONE);
            }
        } else {
            TimeFormat timeFormat = new TimeFormat(mContext, nowDate);
            msgTime.setText(timeFormat.getDetailTime());
        }
        //显示头像
        if (holder.headIcon != null) {
            Bitmap bitmap;
            //群聊
            if (mIsGroup) {
                bitmap = NativeImageLoader.getInstance().getBitmapFromMemCache(msg.getFromID());
                if (bitmap != null)
                    holder.headIcon.setImageBitmap(bitmap);
                else if (mGroupInfo != null) {
                    final UserInfo userInfo = mGroupInfo.getGroupMemberInfo(msg.getFromID());
                    //如果本地存在用户信息
                    if (userInfo != null) {
                        //如果mediaID为空，表明用户没有设置过头像，用默认头像
                        if (TextUtils.isEmpty(userInfo.getAvatar())) {
                            holder.headIcon.setImageResource(R.drawable.head_icon);
                        } else {
                            File file = userInfo.getAvatarFile();
                            if (file != null && file.isFile()) {
                                bitmap = BitmapLoader.getBitmapFromFile(file.getAbsolutePath(), (int) (50 * mDensity), (int) (50 * mDensity));
                                NativeImageLoader.getInstance().updateBitmapFromCache(msg.getFromID(), bitmap);
                                holder.headIcon.setImageBitmap(bitmap);
                                //本地不存在头像，从服务器拿
                            } else {
                                userInfo.getAvatarFileAsync(new DownloadAvatarCallback() {
                                    @Override
                                    public void gotResult(int status, String desc, File file) {
                                        if (status == 0) {
                                            Bitmap bitmap = BitmapLoader.getBitmapFromFile(file.getAbsolutePath(), (int) (50 * mDensity), (int) (50 * mDensity));
                                            NativeImageLoader.getInstance().updateBitmapFromCache(msg.getFromID(), bitmap);
                                            holder.headIcon.setImageBitmap(bitmap);
                                        } else {
                                            holder.headIcon.setImageResource(R.drawable.head_icon);
                                        }
                                    }
                                });
                            }
                        }
                        //本地不存在用户信息，从服务器拿
                    } else {
                        Log.i(TAG, "Get UserInfo from server, UserName: " + msg.getFromName());
                        JMessageClient.getUserInfo(msg.getFromID(), new GetUserInfoCallback() {
                            @Override
                            public void gotResult(int status, String desc, UserInfo userInfo) {
                                if (status == 0) {
                                    File file = userInfo.getAvatarFile();
                                    if (file != null && file.isFile()) {
                                        Bitmap bitmap1 = BitmapLoader.getBitmapFromFile(file.getAbsolutePath(), (int) (50 * mDensity), (int) (50 * mDensity));
                                        NativeImageLoader.getInstance().updateBitmapFromCache(msg.getFromID(), bitmap1);
                                        holder.headIcon.setImageBitmap(bitmap1);
                                    }
                                } else {
                                    holder.headIcon.setImageResource(R.drawable.head_icon);
                                }
                            }
                        });
                    }
                }
                //单聊
            } else {
                bitmap = NativeImageLoader.getInstance().getBitmapFromMemCache(msg.getFromID());
                if (bitmap != null)
                    holder.headIcon.setImageBitmap(bitmap);
                else holder.headIcon.setImageResource(R.drawable.head_icon);
            }


            // 点击头像跳转到个人信息界面
            holder.headIcon.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Intent intent = new Intent();
                    if (msg.getDirect().equals(MessageDirect.send)) {
                        intent.putExtra(JPushDemoApplication.TARGET_ID, msg.getFromName());
                        Log.i(TAG, "msg.getFromName() " + msg.getFromName());
                        intent.setClass(mContext, MeInfoActivity.class);
                        mContext.startActivity(intent);
                    } else {
                        String targetID = msg.getFromID();
                        intent.putExtra(JPushDemoApplication.TARGET_ID, targetID);
                        intent.setClass(mContext, FriendInfoActivity.class);
                        mContext.startActivity(intent);
                    }
                }
            });
        }

        OnLongClickListener longClickListener = new OnLongClickListener() {
            @Override
            public boolean onLongClick(View arg0) {
                // 长按文本弹出菜单
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                View view = LayoutInflater.from(mContext).inflate(
                        R.layout.dialog_msg_alert, null);
                builder.setView(view);
                Button copyBtn = (Button) view
                        .findViewById(R.id.copy_msg_btn);
                Button forwardBtn = (Button) view
                        .findViewById(R.id.forward_msg_btn);
                View line1 = view.findViewById(R.id.forward_split_line);
                View line2 = view.findViewById(R.id.delete_split_line);
                Button deleteBtn = (Button) view.findViewById(R.id.delete_msg_btn);
                final TextView title = (TextView) view
                        .findViewById(R.id.dialog_title);
                if (msg.getContentType().equals(ContentType.voice)) {
                    copyBtn.setVisibility(View.GONE);
                    forwardBtn.setVisibility(View.GONE);
                    line1.setVisibility(View.GONE);
                    line2.setVisibility(View.GONE);
                }
                String name;
                name = msg.getFromName();
                title.setText(name);
                final Dialog dialog = builder.create();
                dialog.show();
                dialog.getWindow().setLayout((int) (0.8 * mWidth), WindowManager.LayoutParams.WRAP_CONTENT);
                OnClickListener listener = new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        switch (msg.getContentType()) {
                            case image:
                                break;
                            case voice:
                                break;
                            case video:
                                break;
                            case location:
                                break;
                            default:
                        }
                        switch (v.getId()) {
                            case R.id.copy_msg_btn:
                                if (msg.getContentType().equals(ContentType.text)) {
                                    final String content = ((TextContent) msg.getContent()).getText();
                                    if (Build.VERSION.SDK_INT > 11) {
                                        ClipboardManager clipboard = (ClipboardManager) mContext
                                                .getSystemService(mContext.CLIPBOARD_SERVICE);
                                        ClipData clip = ClipData.newPlainText(
                                                "Simple text", content);
                                        clipboard.setPrimaryClip(clip);
                                    } else {
                                        ClipboardManager clipboard = (ClipboardManager) mContext
                                                .getSystemService(mContext.CLIPBOARD_SERVICE);
                                        clipboard.setText(content);// 设置Clipboard 的内容
                                        if (clipboard.hasText()) {
                                            clipboard.getText();
                                        }
                                    }

                                    Toast.makeText(mContext, mContext.getString(R.string.copy_toast), Toast.LENGTH_SHORT)
                                            .show();
                                    dialog.dismiss();
                                }
                                break;
                            case R.id.forward_msg_btn:
                                dialog.dismiss();
                                break;
                            case R.id.delete_msg_btn:
                                mConv.deleteMessage(msg.getId());
                                mMsgList.remove(position);
                                notifyDataSetChanged();
                                dialog.dismiss();
                                break;
                        }
                    }
                };
                copyBtn.setOnClickListener(listener);
                forwardBtn.setOnClickListener(listener);
                deleteBtn.setOnClickListener(listener);
                return true;
            }
        };
        try {
            holder.txtContent.setOnLongClickListener(longClickListener);
        } catch (Exception e) {
        }

        return convertView;
    }

    private void handleGroupChangeMsg(Message msg, ViewHolder holder) {
        String content = ((EventNotificationContent) msg.getContent()).getEventText();
        holder.groupChange.setText(content);
        holder.groupChange.setVisibility(View.VISIBLE);
    }

    private void handleCustomMsg(ViewHolder holder) {
        holder.groupChange.setVisibility(View.GONE);
    }

    private void handleTextMsg(final Message msg, final ViewHolder holder,
                               final int position) {
        final String content = ((TextContent) msg.getContent()).getText();
        holder.txtContent.setText(content);

        // 检查发送状态，发送方有重发机制
        if (msg.getDirect().equals(MessageDirect.send)) {
            final Animation sendingAnim = AnimationUtils.loadAnimation(mContext, R.anim.rotate);
            LinearInterpolator lin = new LinearInterpolator();
            sendingAnim.setInterpolator(lin);
            switch (msg.getStatus()) {
                case send_success:
                    if (sendingAnim != null) {
                        holder.sendingIv.clearAnimation();
                        holder.sendingIv.setVisibility(View.GONE);
                    }
                    holder.resend.setVisibility(View.GONE);
                    break;
                case send_fail:
                    if (sendingAnim != null) {
                        holder.sendingIv.clearAnimation();
                        holder.sendingIv.setVisibility(View.GONE);
                    }
                    holder.resend.setVisibility(View.VISIBLE);
                    break;
                case send_going:
                    sendingTextOrVoice(holder, sendingAnim, msg);
                    break;
                default:
            }
            // 点击重发按钮，重发消息
            holder.resend.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    showResendDialog(holder, sendingAnim, msg);
                }
            });

        } else {
            if (mIsGroup) {
                holder.displayName.setVisibility(View.VISIBLE);
                holder.displayName.setText(msg.getFromName());
            }
        }
    }

    //正在发送文字或语音
    private void sendingTextOrVoice(ViewHolder holder, Animation sendingAnim, Message msg) {
        holder.sendingIv.setVisibility(View.VISIBLE);
        holder.sendingIv.startAnimation(sendingAnim);
        holder.resend.setVisibility(View.GONE);
        //消息正在发送，重新注册一个监听消息发送完成的Callback
        if (!msg.isSendCompleteCallbackExists()) {
            msg.setOnSendCompleteCallback(new BasicCallback() {
                @Override
                public void gotResult(final int status, final String desc) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (status != 0)
                                HandleResponseCode.onHandle(mContext, status, false);
                            notifyDataSetChanged();
                        }

                    });
                }
            });
        }
    }

    //重发对话框
    private void showResendDialog(final ViewHolder holder, final Animation sendingAnim, final Message msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.dialog_resend_msg, null);
        builder.setView(view);
        Button cancelBtn = (Button) view.findViewById(R.id.cancel_btn);
        Button resendBtn = (Button) view.findViewById(R.id.resend_btn);
        final Dialog dialog = builder.create();
        dialog.show();
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.cancel_btn:
                        dialog.dismiss();
                        break;
                    case R.id.resend_btn:
                        dialog.dismiss();
                        if (msg.getContentType().equals(ContentType.image)) {
                            sendImage(holder, sendingAnim, msg);
                        } else {
                            resendTextOrVoice(holder, sendingAnim, msg);
                        }
                        break;
                }
            }
        };
        cancelBtn.setOnClickListener(listener);
        resendBtn.setOnClickListener(listener);

    }

    // 处理图片
    private void handleImgMsg(final Message msg, final ViewHolder holder, final int position) {
        final ImageContent imgContent = (ImageContent) msg.getContent();
        // 先拿本地缩略图
        final String path = imgContent.getLocalThumbnailPath();
        // 接收图片
        if (msg.getDirect().equals(MessageDirect.receive)) {
            if (path == null) {
                //从服务器上拿缩略图
                imgContent.downloadThumbnailImage(msg,
                        new DownloadCompletionCallback() {
                            @Override
                            public void onComplete(int status, String desc, File file) {
                                if (status == 0) {
                                    android.os.Message handleMsg = myHandler.obtainMessage();
                                    handleMsg.what = UPDATE_IMAGEVIEW;
                                    handleMsg.obj = holder;
                                    Bundle bundle = new Bundle();
                                    bundle.putString("path", file.getAbsolutePath());
                                    handleMsg.setData(bundle);
                                    handleMsg.sendToTarget();
                                }
                            }
                        });
            } else {
                setPictureScale(path, holder.picture);
                Picasso.with(mContext).load(new File(path))
                        .into(holder.picture);
            }
            //群聊中显示昵称
            if (mIsGroup) {
                holder.displayName.setVisibility(View.VISIBLE);
                holder.displayName.setText(msg.getFromName());
            }

            switch (msg.getStatus()) {
                case receive_fail:
                    holder.picture.setBackgroundResource(R.drawable.fetch_failed);
                    break;
                default:
            }
            // 发送图片方，直接加载缩略图
        } else {
            try {
                Log.i(TAG, "msg.getID() + thumbnailPath : " + msg.getId() + " " + path);
                setPictureScale(path, holder.picture);
                Picasso.with(mContext).load(new File(path))
                        .into(holder.picture);
            } catch (NullPointerException e) {
                Picasso.with(mContext).load(R.drawable.friends_sends_pictures_no)
                        .into(holder.picture);
            }

            final Animation sendingAnim = AnimationUtils.loadAnimation(mContext, R.anim.rotate);
            LinearInterpolator lin = new LinearInterpolator();
            sendingAnim.setInterpolator(lin);
            //检查状态
            switch (msg.getStatus()) {
                case send_success:
                    if (sendingAnim != null) {
                        holder.sendingIv.clearAnimation();
                        holder.sendingIv.setVisibility(View.GONE);
                    }
                    holder.picture.setAlpha(1.0f);
                    holder.progressTv.setVisibility(View.GONE);
                    holder.resend.setVisibility(View.GONE);
                    break;
                case send_fail:
                    if (sendingAnim != null) {
                        holder.sendingIv.clearAnimation();
                        holder.sendingIv.setVisibility(View.GONE);
                    }
                    holder.picture.setAlpha(1.0f);
                    holder.progressTv.setVisibility(View.GONE);
                    holder.resend.setVisibility(View.VISIBLE);
                    break;
                case send_going:
                    sendingImage(holder, sendingAnim, msg, path);
                    break;
            }
            // 点击重发按钮，重发图片
            holder.resend.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
//                    sendImage(holder, sendingAnim, msg);
                    showResendDialog(holder, sendingAnim, msg);
                }
            });
        }
        if (holder.picture != null) {
            // 点击预览图片
            holder.picture.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    Intent intent = new Intent();
                    intent.putExtra(JPushDemoApplication.TARGET_ID, mTargetID);
                    intent.putExtra(JPushDemoApplication.POSITION, position);
                    intent.putExtra("msgID", msg.getId());
                    intent.putExtra(JPushDemoApplication.GROUP_ID, mGroupID);
                    intent.putExtra(JPushDemoApplication.IS_GROUP, mIsGroup);
                    intent.putExtra("fromChatActivity", true);
                    intent.setClass(mContext, BrowserViewPagerActivity.class);
                    mContext.startActivity(intent);
                }
            });

            holder.picture.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    View view = LayoutInflater.from(mContext).inflate(
                            R.layout.dialog_msg_alert, null);
                    builder.setView(view);
                    Button copyBtn = (Button) view
                            .findViewById(R.id.copy_msg_btn);
                    Button forwardBtn = (Button) view
                            .findViewById(R.id.forward_msg_btn);
                    View line1 = view.findViewById(R.id.forward_split_line);
                    View line2 = view.findViewById(R.id.delete_split_line);
                    Button deleteBtn = (Button) view.findViewById(R.id.delete_msg_btn);
                    final TextView title = (TextView) view
                            .findViewById(R.id.dialog_title);
                    copyBtn.setVisibility(View.GONE);
                    forwardBtn.setVisibility(View.GONE);
                    line1.setVisibility(View.GONE);
                    line2.setVisibility(View.GONE);
                    String name;
                    name = msg.getFromName();
                    title.setText(name);
                    final Dialog dialog = builder.create();
                    dialog.show();
                    dialog.getWindow().setLayout((int) (0.8 * mWidth), WindowManager.LayoutParams.WRAP_CONTENT);
                    OnClickListener listener = new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            switch (v.getId()) {
                                case R.id.copy_msg_btn:
                                    break;
                                case R.id.forward_msg_btn:
                                    dialog.dismiss();
                                    break;
                                case R.id.delete_msg_btn:
                                    mConv.deleteMessage(msg.getId());
                                    mMsgList.remove(position);
                                    notifyDataSetChanged();
                                    dialog.dismiss();
                                    break;
                            }
                        }
                    };
                    copyBtn.setOnClickListener(listener);
                    forwardBtn.setOnClickListener(listener);
                    deleteBtn.setOnClickListener(listener);
                    return true;
                }
            });

        }
    }

    private void sendingImage(final ViewHolder holder, Animation sendingAnim, Message msg, final String path) {
        holder.picture.setAlpha(0.75f);
        holder.sendingIv.setVisibility(View.VISIBLE);
        holder.sendingIv.startAnimation(sendingAnim);
        holder.progressTv.setVisibility(View.VISIBLE);
        holder.resend.setVisibility(View.GONE);
        //如果图片正在发送，重新注册上传进度Callback
        if (!msg.isContentUploadProgressCallbackExists()) {
            msg.setOnContentUploadProgressCallback(new ProgressUpdateCallback() {
                @Override
                public void onProgressUpdate(double v) {
                    Log.i(TAG, "progress v :" + v);
                    holder.progressTv.setText((int) (v * 100) + "%");
                }
            });
        }
        if (!msg.isSendCompleteCallbackExists()) {
            msg.setOnSendCompleteCallback(new BasicCallback() {
                @Override
                public void gotResult(final int status, String desc) {
                    Log.i("Send picture", "update: ");
                    notifyDataSetChanged();
                }
            });
        }
    }

    /**
     * 设置图片最小宽高
     *
     * @param path      图片路径
     * @param imageView 显示图片的View
     */
    private void setPictureScale(String path, ImageView imageView) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opts);
//                计算图片缩放比例
        double imageWidth = opts.outWidth;
        double imageHeight = opts.outHeight;
        if (imageWidth < 100 * mDensity) {
            imageHeight = imageHeight * (100 * mDensity / imageWidth);
            imageWidth = 100 * mDensity;
        }
        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        params.width = (int) imageWidth;
        params.height = (int) imageHeight;
        imageView.setLayoutParams(params);
    }

    private void resendTextOrVoice(final ViewHolder holder, Animation sendingAnim, Message msg) {
        holder.resend.setVisibility(View.GONE);
        holder.sendingIv.setVisibility(View.VISIBLE);
        holder.sendingIv.startAnimation(sendingAnim);

        if (!msg.isSendCompleteCallbackExists()) {
            msg.setOnSendCompleteCallback(new BasicCallback() {
                @Override
                public void gotResult(final int status, String desc) {
                    if (status != 0) {
                        HandleResponseCode.onHandle(mContext, status, false);
                        holder.sendingIv.clearAnimation();
                        holder.sendingIv.setVisibility(View.GONE);
                        holder.resend.setVisibility(View.VISIBLE);
                        Log.i(TAG, "Resend message failed!");
                    }
                    notifyDataSetChanged();
                }
            });
        }

        JMessageClient.sendMessage(msg);
    }

    private void sendImage(final ViewHolder viewHolder, Animation sendingAnim, Message msg) {
        ImageContent imgContent = (ImageContent) msg.getContent();
        final String path = imgContent.getLocalThumbnailPath();
        viewHolder.sendingIv.setVisibility(View.VISIBLE);
        viewHolder.sendingIv.startAnimation(sendingAnim);
        viewHolder.picture.setAlpha(0.75f);
        viewHolder.resend.setVisibility(View.GONE);
        viewHolder.progressTv.setVisibility(View.VISIBLE);
        try {

            // 显示上传进度
            msg.setOnContentUploadProgressCallback(new ProgressUpdateCallback() {
                @Override
                public void onProgressUpdate(final double progress) {
                    Log.i("Uploding picture", "progress: "
                            + progress);
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            viewHolder.progressTv.setText((int) (progress * 100) + "%");
                        }
                    });
                }
            });
            if (!msg.isSendCompleteCallbackExists()) {
                msg.setOnSendCompleteCallback(new BasicCallback() {
                    @Override
                    public void gotResult(final int status, String desc) {
                        if (status != 0)
                            HandleResponseCode.onHandle(mContext, status, false);
                        Picasso.with(mContext).load(new File(path)).into(viewHolder.picture);
                        Log.i("Send picture", "update: ");
                        notifyDataSetChanged();
                    }
                });
            }
            JMessageClient.sendMessage(msg);
        } catch (Exception e) {
        }
    }

    private void handleVoiceMsg(final Message msg, final ViewHolder holder,
                                final int position) {
        final VoiceContent content = (VoiceContent) msg.getContent();
        final MessageDirect msgDirect = msg.getDirect();
        int length = content.getDuration();
        holder.voiceLength.setText(length + "\"");
        //控制语音长度显示，长度增幅随语音长度逐渐缩小
        int width = (int) (-0.04 * length * length + 4.526 * length + 75.214);
        holder.txtContent.setWidth((int) (width * mDensity));
        if (msgDirect.equals(MessageDirect.send)) {
            holder.voice.setImageResource(R.drawable.send_3);
            final Animation sendingAnim = AnimationUtils.loadAnimation(mContext, R.anim.rotate);
            LinearInterpolator lin = new LinearInterpolator();
            sendingAnim.setInterpolator(lin);
            switch (msg.getStatus()) {
                case send_success:
                    if (sendingAnim != null) {
                        holder.sendingIv.clearAnimation();
                        holder.sendingIv.setVisibility(View.GONE);
                    }
                    holder.resend.setVisibility(View.GONE);
                    break;
                case send_fail:
                    if (sendingAnim != null) {
                        holder.sendingIv.clearAnimation();
                        holder.sendingIv.setVisibility(View.GONE);
                    }
                    holder.resend.setVisibility(View.VISIBLE);
                    break;
                case send_going:
                    sendingTextOrVoice(holder, sendingAnim, msg);
                    break;
                default:
            }

            holder.resend.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (msg.getContent() != null)
                        showResendDialog(holder, sendingAnim, msg);
                    else
                        Toast.makeText(mContext, mContext.getString(R.string.sdcard_not_exist_toast), Toast.LENGTH_SHORT).show();
                }
            });
        } else switch (msg.getStatus()) {
            case receive_success:
                if (mIsGroup) {
                    holder.displayName.setVisibility(View.VISIBLE);
                    holder.displayName.setText(msg.getFromName());
                }
                holder.voice.setImageResource(R.drawable.receive_3);
                // 收到语音，设置未读
                if (msg.getContent().getBooleanExtra("isReaded") == null
                        || !msg.getContent().getBooleanExtra("isReaded")) {
                    mConv.updateMessageExtra(msg, "isReaded", false);
                    holder.readStatus.setVisibility(View.VISIBLE);
                    if (mIndexList.size() > 0) {
                        if (!mIndexList.contains(position)) {
                            Log.i("mIndexList", "position: " + position);
                            addTolistAndSort(position);
                            Log.i("mIndexList", "mIndexList.size()" + mIndexList.size());
                        }
                    } else {
                        Log.i("mIndexList", "position: " + position);
                        addTolistAndSort(position);
                    }
                    Log.d("", "current position  = " + position);
                    if (nextPlayPosition == position && autoPlay) {
                        Log.d("", "nextPlayPosition = " + nextPlayPosition);
                        playVoiceThenRefresh(position, holder);
                    }
                } else if (msg.getContent().getBooleanExtra("isReaded").equals(true)) {
                    holder.readStatus.setVisibility(View.GONE);
                }
                break;
            case receive_fail:
                holder.voice.setImageResource(R.drawable.receive_3);
                // 接收失败，从服务器上下载
                mConv.deleteMessage(msg.getId());
                content.downloadVoiceFile(msg,
                        new DownloadCompletionCallback() {
                            @Override
                            public void onComplete(int status, String desc, File file) {
                                if (status != 0) {
                                    Toast.makeText(mContext, mContext.getString(R.string.voice_fetch_failed_toast),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.i("VoiceMessage", "reload success");
                                }
                            }
                        });
                break;
            case receive_going:
                break;
            default:
        }


        holder.txtContent.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
                if (!sdCardExist && msg.getDirect().equals(MessageDirect.send)) {
                    Toast.makeText(mContext, mContext.getString(R.string.sdcard_not_exist_toast), Toast.LENGTH_SHORT).show();
                    return;
                }
                // 如果之前存在播放动画，无论这次点击触发的是暂停还是播放，停止上次播放的动画
                if (mVoiceAnimation != null) {
                    mVoiceAnimation.stop();
                }
                // 播放中点击了正在播放的Item 则暂停播放
                if (mp.isPlaying() && mPosition == position) {
                    if (msgDirect.equals(MessageDirect.send)) {
                        holder.voice.setImageResource(R.anim.voice_send);
                    } else
                        holder.voice.setImageResource(R.anim.voice_receive);
                    mVoiceAnimation = (AnimationDrawable) holder.voice
                            .getDrawable();
                    pauseVoice();
                    mVoiceAnimation.stop();
                    // 开始播放录音
                } else if (msgDirect.equals(MessageDirect.send)) {
                    try {
                        holder.voice.setImageResource(R.anim.voice_send);
                        mVoiceAnimation = (AnimationDrawable) holder.voice
                                .getDrawable();

                        // 继续播放之前暂停的录音
                        if (mSetData && mPosition == position) {
                            playVoice();
                            // 否则重新播放该录音或者其他录音
                        } else {
                            mp.reset();
                            // 记录播放录音的位置
                            mPosition = position;
                            Log.i(TAG, "content.getLocalPath:"
                                    + content.getLocalPath());
                            mFIS = new FileInputStream(content
                                    .getLocalPath());
                            mFD = mFIS.getFD();
                            mp.setDataSource(mFD);
                            if (mIsEarPhoneOn) {
                                mp.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
                            } else {
                                mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            }

                            mp.prepare();
                            playVoice();
                        }
                    } catch (NullPointerException e) {
                        Toast.makeText(mActivity, mContext.getString(R.string.file_not_found_toast),
                                Toast.LENGTH_SHORT).show();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        Toast.makeText(mActivity, mContext.getString(R.string.file_not_found_toast),
                                Toast.LENGTH_SHORT).show();
                    } finally {
                        try {
                            if (mFIS != null) {
                                mFIS.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    // 语音接收方特殊处理，自动连续播放未读语音
                } else {
                    try {
                        // 继续播放之前暂停的录音
                        if (mSetData && mPosition == position) {
                            mVoiceAnimation.start();
                            mp.start();
                            // 否则开始播放另一条录音
                        } else {
                            // 选中的录音是否已经播放过，如果未播放，自动连续播放这条语音之后未播放的语音
                            if (msg.getContent().getBooleanExtra("isReaded") == null || msg.getContent().getBooleanExtra("isReaded") == false) {
                                autoPlay = true;
                                playVoiceThenRefresh(position, holder);
                                // 否则直接播放选中的语音
                            } else {
                                holder.voice.setImageResource(R.anim.voice_receive);
                                mVoiceAnimation = (AnimationDrawable) holder.voice.getDrawable();
                                mp.reset();
                                // 记录播放录音的位置
                                mPosition = position;
                                if (content.getLocalPath() != null) {
                                    try {
                                        mFIS = new FileInputStream(content
                                                .getLocalPath());
                                        mFD = mFIS.getFD();
                                        mp.setDataSource(mFD);
                                        mp.prepare();
                                        playVoice();
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } finally {
                                        try {
                                            if (mFIS != null) {
                                                mFIS.close();
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } else {
                                    Toast.makeText(mContext, mContext.getString(R.string.voice_fetch_failed_toast), Toast.LENGTH_SHORT).show();
                                }

                            }
                        }
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            }

            private void playVoice() {
                mVoiceAnimation.start();
                mp.start();
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer arg0) {
                        mVoiceAnimation.stop();
                        mp.reset();
                        mSetData = false;
                        // 播放完毕，恢复初始状态
                        if (msgDirect.equals(MessageDirect.send))
                            holder.voice
                                    .setImageResource(R.drawable.send_3);
                        else {
                            holder.voice
                                    .setImageResource(R.drawable.receive_3);
                            holder.readStatus.setVisibility(View.GONE);
                        }
                    }
                });
            }

            private void pauseVoice() {
                mp.pause();
                mSetData = true;
            }
        });
    }

    private void playVoiceThenRefresh(final int position, final ViewHolder holder) {
        Message message = mMsgList.get(position);
        //设为已读
        mConv.updateMessageExtra(message, "isReaded", true);
        mPosition = position;
        holder.readStatus.setVisibility(View.GONE);
        if (mVoiceAnimation != null) {
            mVoiceAnimation.stop();
            mVoiceAnimation = null;
        }
        holder.voice.setImageResource(R.anim.voice_receive);
        mVoiceAnimation = (AnimationDrawable) holder.voice.getDrawable();
        try {
            VoiceContent vc = (VoiceContent) message.getContent();
            mp.reset();
            mFIS = new FileInputStream(vc.getLocalPath());
            mFD = mFIS.getFD();
            mp.setDataSource(mFD);
            mp.prepare();
            mp.setOnPreparedListener(new OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mVoiceAnimation.start();
                    mp.start();
                }
            });
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mVoiceAnimation.stop();
                    mp.reset();
                    mSetData = false;
                    holder.voice
                            .setImageResource(R.drawable.receive_3);
                    int curCount = mIndexList.indexOf(position);
                    Log.d("", "curCount = " + curCount);
                    if (curCount + 1 >= mIndexList.size()) {
                        nextPlayPosition = -1;
                        autoPlay = false;
                    } else {
                        nextPlayPosition = mIndexList.get(curCount + 1);
                        notifyDataSetChanged();
                    }
                    mIndexList.remove(curCount);
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
        } finally {
            try {
                if (mFIS != null) {
                    mFIS.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addTolistAndSort(int position) {
        mIndexList.add(position);
        Collections.sort(mIndexList);
    }

    private void handleLocationMsg(Message msg, ViewHolder holder, int position) {

    }

    public void stopMediaPlayer() {
        if (mp.isPlaying())
            mp.stop();
    }

    public static class ViewHolder {
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
    }
}
