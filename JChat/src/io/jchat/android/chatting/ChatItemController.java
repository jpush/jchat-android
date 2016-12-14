package io.jchat.android.chatting;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.DownloadCompletionCallback;
import cn.jpush.im.android.api.callback.ProgressUpdateCallback;
import cn.jpush.im.android.api.content.CustomContent;
import cn.jpush.im.android.api.content.EventNotificationContent;
import cn.jpush.im.android.api.content.FileContent;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.LocationContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.content.VoiceContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.enums.MessageDirect;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import io.jchat.android.activity.BrowserViewPagerActivity;
import io.jchat.android.activity.SendLocationActivity;
import io.jchat.android.application.JChatDemoApplication;
import io.jchat.android.chatting.utils.FileHelper;
import io.jchat.android.chatting.utils.HandleResponseCode;
import io.jchat.android.chatting.utils.IdHelper;
import io.jchat.android.chatting.MsgListAdapter.ViewHolder;
import io.jchat.android.entity.FileType;

public class ChatItemController {

    private MsgListAdapter mAdapter;
    private Context mContext;
    private Conversation mConv;
    private List<Message> mMsgList;
    private MsgListAdapter.ContentLongClickListener mLongClickListener;
    private float mDensity;
    public Animation mSendingAnim;
    private boolean mSetData = false;
    private final MediaPlayer mp = new MediaPlayer();
    private AnimationDrawable mVoiceAnimation;
    private int mPosition = -1;// 和mSetData一起组成判断播放哪条录音的依据
    private List<Integer> mIndexList = new ArrayList<Integer>();//语音索引
    private FileInputStream mFIS;
    private FileDescriptor mFD;
    private boolean autoPlay = false;
    private int nextPlayPosition = 0;
    private boolean mIsEarPhoneOn;
    private int mSendMsgId;
    private Queue<Message> mMsgQueue = new LinkedList<Message>();

    public ChatItemController(MsgListAdapter adapter, Context context, Conversation conv, List<Message> msgList,
                              float density, MsgListAdapter.ContentLongClickListener longClickListener) {
        this.mAdapter = adapter;
        this.mContext = context;
        this.mConv = conv;
        this.mMsgList = msgList;
        this.mLongClickListener = longClickListener;
        this.mDensity = density;
        mSendingAnim = AnimationUtils.loadAnimation(mContext, IdHelper.getAnim(mContext, "jmui_rotate"));
        LinearInterpolator lin = new LinearInterpolator();
        mSendingAnim.setInterpolator(lin);

        AudioManager audioManager = (AudioManager) mContext
                .getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_NORMAL);
        if (audioManager.isSpeakerphoneOn()) {
            audioManager.setSpeakerphoneOn(true);
        } else {
            audioManager.setSpeakerphoneOn(false);
        }
        mp.setAudioStreamType(AudioManager.STREAM_RING);
        mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });
    }

    public void handleTextMsg(final Message msg, final ViewHolder holder, int position) {
        final String content = ((TextContent) msg.getContent()).getText();
        holder.txtContent.setText(content);
        holder.txtContent.setTag(position);
        holder.txtContent.setOnLongClickListener(mLongClickListener);
        // 检查发送状态，发送方有重发机制
        if (msg.getDirect() == MessageDirect.send) {
            switch (msg.getStatus()) {
                case send_success:
                    holder.sendingIv.clearAnimation();
                    holder.sendingIv.setVisibility(View.GONE);
                    holder.resend.setVisibility(View.GONE);
                    break;
                case send_fail:
                    holder.sendingIv.clearAnimation();
                    holder.sendingIv.setVisibility(View.GONE);
                    holder.resend.setVisibility(View.VISIBLE);
                    break;
                case send_going:
                    sendingTextOrVoice(holder, msg);
                    break;
                default:
            }

        } else {
            if (mConv.getType() == ConversationType.group) {
                holder.displayName.setVisibility(View.VISIBLE);
                if (TextUtils.isEmpty(msg.getFromUser().getNickname())) {
                    holder.displayName.setText(msg.getFromUser().getUserName());
                } else {
                    holder.displayName.setText(msg.getFromUser().getNickname());
                }
            }
        }
        if (holder.resend != null) {
            holder.resend.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mAdapter.showResendDialog(holder, msg);
                }
            });
        }
    }

    // 处理图片
    public void handleImgMsg(final Message msg, final ViewHolder holder, final int position) {
        final ImageContent imgContent = (ImageContent) msg.getContent();
        // 先拿本地缩略图
        final String path = imgContent.getLocalThumbnailPath();
        // 接收图片
        if (msg.getDirect() == MessageDirect.receive) {
            if (path == null) {
                //从服务器上拿缩略图
                imgContent.downloadThumbnailImage(msg, new DownloadCompletionCallback() {
                    @Override
                    public void onComplete(int status, String desc, File file) {
                        if (status == 0) {
                            Picasso.with(mContext).load(file).into(holder.picture);
                        }
                    }
                });
            } else {
                setPictureScale(path, holder.picture);
                Picasso.with(mContext).load(new File(path)).into(holder.picture);
            }
            //群聊中显示昵称
            if (mConv.getType() == ConversationType.group) {
                holder.displayName.setVisibility(View.VISIBLE);
                if (TextUtils.isEmpty(msg.getFromUser().getNickname())) {
                    holder.displayName.setText(msg.getFromUser().getUserName());
                } else {
                    holder.displayName.setText(msg.getFromUser().getNickname());
                }
            }

            switch (msg.getStatus()) {
                case receive_fail:
                    holder.picture.setImageResource(IdHelper.getDrawable(mContext, "jmui_fetch_failed"));
                    break;
                default:
            }
            // 发送图片方，直接加载缩略图
        } else {
            try {
                setPictureScale(path, holder.picture);
                Picasso.with(mContext).load(new File(path)).into(holder.picture);
            } catch (NullPointerException e) {
                Picasso.with(mContext).load(IdHelper.getDrawable(mContext, "jmui_picture_not_found"))
                        .into(holder.picture);
            }
            //检查状态
            switch (msg.getStatus()) {
                case send_success:
                    holder.sendingIv.clearAnimation();
                    holder.sendingIv.setVisibility(View.GONE);
                    holder.picture.setAlpha(1.0f);
                    holder.progressTv.setVisibility(View.GONE);
                    holder.resend.setVisibility(View.GONE);
                    break;
                case send_fail:
                    holder.sendingIv.clearAnimation();
                    holder.sendingIv.setVisibility(View.GONE);
                    holder.picture.setAlpha(1.0f);
                    holder.progressTv.setVisibility(View.GONE);
                    holder.resend.setVisibility(View.VISIBLE);
                    break;
                case send_going:
                    sendingImage(msg, holder);
                    break;
                default:
                    holder.picture.setAlpha(0.75f);
                    holder.sendingIv.setVisibility(View.VISIBLE);
                    holder.sendingIv.startAnimation(mSendingAnim);
                    holder.progressTv.setVisibility(View.VISIBLE);
                    holder.progressTv.setText("0%");
                    holder.resend.setVisibility(View.GONE);
                    //从别的界面返回聊天界面，继续发送
                    if (!mMsgQueue.isEmpty()) {
                        Message message = mMsgQueue.element();
                        if (message.getId() == msg.getId()) {
                            JMessageClient.sendMessage(message);
                            mSendMsgId = message.getId();
                            sendingImage(message, holder);
                        }
                    }
            }
        }
        if (holder.picture != null) {
            // 点击预览图片
            holder.picture.setOnClickListener(new BtnOrTxtListener(position, holder));
            holder.picture.setTag(position);
            holder.picture.setOnLongClickListener(mLongClickListener);

        }
        if (holder.resend != null) {
            holder.resend.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mAdapter.showResendDialog(holder, msg);
                }
            });
        }
    }

    private void sendingImage(final Message msg, final ViewHolder holder) {
        holder.picture.setAlpha(0.75f);
        holder.sendingIv.setVisibility(View.VISIBLE);
        holder.sendingIv.startAnimation(mSendingAnim);
        holder.progressTv.setVisibility(View.VISIBLE);
        holder.progressTv.setText("0%");
        holder.resend.setVisibility(View.GONE);
        //如果图片正在发送，重新注册上传进度Callback
        if (!msg.isContentUploadProgressCallbackExists()) {
            msg.setOnContentUploadProgressCallback(new ProgressUpdateCallback() {
                @Override
                public void onProgressUpdate(double v) {
                    String progressStr = (int) (v * 100) + "%";
                    holder.progressTv.setText(progressStr);
                }
            });
        }
        if (!msg.isSendCompleteCallbackExists()) {
            msg.setOnSendCompleteCallback(new BasicCallback() {
                @Override
                public void gotResult(final int status, String desc) {
                    if (!mMsgQueue.isEmpty() && mMsgQueue.element().getId() == mSendMsgId) {
                        mMsgQueue.poll();
                        if (!mMsgQueue.isEmpty()) {
                            Message nextMsg = mMsgQueue.element();
                            JMessageClient.sendMessage(nextMsg);
                            mSendMsgId = nextMsg.getId();
                        }
                    }
                    holder.picture.setAlpha(1.0f);
                    holder.sendingIv.clearAnimation();
                    holder.sendingIv.setVisibility(View.GONE);
                    holder.progressTv.setVisibility(View.GONE);
                    if (status == 803008) {
                        CustomContent customContent = new CustomContent();
                        customContent.setBooleanValue("blackList", true);
                        Message customMsg = mConv.createSendMessage(customContent);
                        mAdapter.addMsgToList(customMsg);
                    } else if (status != 0) {
                        HandleResponseCode.onHandle(mContext, status, false);
                        holder.resend.setVisibility(View.VISIBLE);
                    }

                    Message message = mConv.getMessage(msg.getId());
                    mMsgList.set(mMsgList.indexOf(msg), message);
//                    notifyDataSetChanged();
                }
            });

        }
    }

    public void handleVoiceMsg(final Message msg, final ViewHolder holder, final int position) {
        final VoiceContent content = (VoiceContent) msg.getContent();
        final MessageDirect msgDirect = msg.getDirect();
        int length = content.getDuration();
        String lengthStr = length + mContext.getString(IdHelper.getString(mContext, "jmui_symbol_second"));
        holder.voiceLength.setText(lengthStr);
        //控制语音长度显示，长度增幅随语音长度逐渐缩小
        int width = (int) (-0.04 * length * length + 4.526 * length + 75.214);
        holder.txtContent.setWidth((int) (width * mDensity));
        holder.txtContent.setTag(position);
        holder.txtContent.setOnLongClickListener(mLongClickListener);
        if (msgDirect == MessageDirect.send) {
            holder.voice.setImageResource(IdHelper.getDrawable(mContext, "jmui_send_3"));
            switch (msg.getStatus()) {
                case send_success:
                    holder.sendingIv.clearAnimation();
                    holder.sendingIv.setVisibility(View.GONE);
                    holder.resend.setVisibility(View.GONE);
                    break;
                case send_fail:
                    holder.sendingIv.clearAnimation();
                    holder.sendingIv.setVisibility(View.GONE);
                    holder.resend.setVisibility(View.VISIBLE);
                    break;
                case send_going:
                    sendingTextOrVoice(holder, msg);
                    break;
                default:
            }
        } else switch (msg.getStatus()) {
            case receive_success:
                if (mConv.getType() == ConversationType.group) {
                    holder.displayName.setVisibility(View.VISIBLE);
                    if (TextUtils.isEmpty(msg.getFromUser().getNickname())) {
                        holder.displayName.setText(msg.getFromUser().getUserName());
                    } else {
                        holder.displayName.setText(msg.getFromUser().getNickname());
                    }
                }
                holder.voice.setImageResource(IdHelper.getDrawable(mContext, "jmui_receive_3"));
                // 收到语音，设置未读
                if (msg.getContent().getBooleanExtra("isReaded") == null
                        || !msg.getContent().getBooleanExtra("isReaded")) {
                    mConv.updateMessageExtra(msg, "isReaded", false);
                    holder.readStatus.setVisibility(View.VISIBLE);
                    if (mIndexList.size() > 0) {
                        if (!mIndexList.contains(position)) {
                            addToListAndSort(position);
                        }
                    } else {
                        addToListAndSort(position);
                    }
                    if (nextPlayPosition == position && autoPlay) {
                        playVoice(position, holder, false);
                    }
                } else if (msg.getContent().getBooleanExtra("isReaded").equals(true)) {
                    holder.readStatus.setVisibility(View.GONE);
                }
                break;
            case receive_fail:
                holder.voice.setImageResource(IdHelper.getDrawable(mContext, "jmui_receive_3"));
                // 接收失败，从服务器上下载
                content.downloadVoiceFile(msg,
                        new DownloadCompletionCallback() {
                            @Override
                            public void onComplete(int status, String desc, File file) {
                                if (status != 0) {
                                    Toast.makeText(mContext, IdHelper.getString(mContext,
                                            "jmui_voice_fetch_failed_toast"),
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

        if (holder.resend != null) {
            holder.resend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (msg.getContent() != null) {
                        mAdapter.showResendDialog(holder, msg);
                    } else {
                        Toast.makeText(mContext, mContext.getString(IdHelper.getString(mContext,
                                "jmui_sdcard_not_exist_toast")),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        holder.txtContent.setOnClickListener(new BtnOrTxtListener(position, holder));
    }

    public void handleLocationMsg(final Message msg, final ViewHolder holder, int position) {
        LocationContent content = (LocationContent) msg.getContent();
        String path = content.getStringExtra("path");

        holder.location.setText(content.getAddress());
        if (msg.getDirect() == MessageDirect.receive) {
            switch (msg.getStatus()) {
                case receive_going:
                    break;
                case receive_success:

                    break;
                case receive_fail:
                    break;
            }
        } else {
            if (path != null && holder.picture != null) {
                try {
                    File file = new File(path);
                    if (file.exists() && file.isFile()) {
                        Picasso.with(mContext).load(file).into(holder.picture);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            switch (msg.getStatus()) {
                case send_going:
                    sendingTextOrVoice(holder, msg);
                    break;
                case send_success:
                    holder.sendingIv.clearAnimation();
                    holder.sendingIv.setVisibility(View.GONE);
                    holder.resend.setVisibility(View.GONE);
                    break;
                case send_fail:
                    holder.sendingIv.clearAnimation();
                    holder.sendingIv.setVisibility(View.GONE);
                    holder.resend.setVisibility(View.VISIBLE);
                    break;
            }
        }
        if (holder.picture != null) {
            holder.picture.setOnClickListener(new BtnOrTxtListener(position, holder));
            holder.picture.setTag(position);
            holder.picture.setOnLongClickListener(mLongClickListener);

        }

        if (holder.resend != null) {
            holder.resend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (msg.getContent() != null) {
                        mAdapter.showResendDialog(holder, msg);
                    } else {
                        Toast.makeText(mContext, mContext.getString(IdHelper.getString(mContext,
                                "jmui_sdcard_not_exist_toast")),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    //正在发送文字或语音
    private void sendingTextOrVoice(final ViewHolder holder, Message msg) {
        holder.sendingIv.setVisibility(View.VISIBLE);
        holder.sendingIv.startAnimation(mSendingAnim);
        holder.resend.setVisibility(View.GONE);
        //消息正在发送，重新注册一个监听消息发送完成的Callback
        if (!msg.isSendCompleteCallbackExists()) {
            msg.setOnSendCompleteCallback(new BasicCallback() {
                @Override
                public void gotResult(final int status, final String desc) {
                    holder.sendingIv.setVisibility(View.GONE);
                    holder.sendingIv.clearAnimation();
                    if (status == 803008) {
                        CustomContent customContent = new CustomContent();
                        customContent.setBooleanValue("blackList", true);
                        Message customMsg = mConv.createSendMessage(customContent);
                        mAdapter.addMsgToList(customMsg);
                    } else if (status != 0) {
                        HandleResponseCode.onHandle(mContext, status, false);
                        holder.resend.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    public void handleFileMsg(final Message msg, final ViewHolder holder, int position) {
        final FileContent content = (FileContent) msg.getContent();
        holder.txtContent.setText(content.getFileName());
        String fileType = content.getStringExtra("fileType");
        String fileSize = content.getStringExtra("fileSize");
        holder.sizeTv.setText(fileSize);
        Drawable drawable;
        if (fileType.equals(FileType.audio.toString())) {
            drawable = mContext.getResources().getDrawable(IdHelper.getDrawable(mContext, "jmui_audio"));

        } else if (fileType.equals(FileType.other.toString())) {
            drawable = mContext.getResources().getDrawable(IdHelper.getDrawable(mContext, "jmui_other"));
        } else {
            drawable = mContext.getResources().getDrawable(IdHelper.getDrawable(mContext, "jmui_document"));
        }
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        holder.txtContent.setCompoundDrawables(null, null, drawable, null);


        if (msg.getDirect() == MessageDirect.send) {
            switch (msg.getStatus()) {
                case send_going:
                    holder.contentLl.setBackgroundColor(Color.parseColor("#86222222"));
                    holder.progressTv.setVisibility(View.VISIBLE);
                    holder.progressTv.setText("0%");
                    holder.resend.setVisibility(View.GONE);
                    if (!msg.isContentUploadProgressCallbackExists()) {
                        msg.setOnContentUploadProgressCallback(new ProgressUpdateCallback() {
                            @Override
                            public void onProgressUpdate(double v) {
                                String progressStr = (int) (v * 100) + "%";
                                holder.progressTv.setText(progressStr);
                            }
                        });
                    }
                    if (!msg.isSendCompleteCallbackExists()) {
                        msg.setOnSendCompleteCallback(new BasicCallback() {
                            @Override
                            public void gotResult(int status, String desc) {
                                holder.contentLl.setBackground(mContext.getDrawable(IdHelper
                                        .getDrawable(mContext, "jmui_msg_send_bg")));
                                holder.progressTv.setVisibility(View.GONE);
                                if (status == 803008) {
                                    CustomContent customContent = new CustomContent();
                                    customContent.setBooleanValue("blackList", true);
                                    Message customMsg = mConv.createSendMessage(customContent);
                                    mAdapter.addMsgToList(customMsg);
                                } else if (status != 0) {
                                    HandleResponseCode.onHandle(mContext, status, false);
                                    holder.resend.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                    break;
                case send_success:
                    holder.contentLl.setBackground(mContext.getDrawable(IdHelper
                            .getDrawable(mContext, "jmui_msg_send_bg")));
                    holder.progressTv.setVisibility(View.GONE);
                    holder.resend.setVisibility(View.GONE);
                    break;
                case send_fail:
                    holder.contentLl.setBackground(mContext.getDrawable(IdHelper
                            .getDrawable(mContext, "jmui_msg_send_bg")));
                    holder.progressTv.setVisibility(View.GONE);
                    holder.resend.setVisibility(View.VISIBLE);
                    break;
            }
        } else {
            switch (msg.getStatus()) {
                case receive_going:
                    holder.contentLl.setBackgroundColor(Color.parseColor("#86222222"));
                    holder.progressTv.setVisibility(View.VISIBLE);
                    holder.resend.setVisibility(View.GONE);
                    if (!msg.isContentDownloadProgressCallbackExists()) {
                        msg.setOnContentDownloadProgressCallback(new ProgressUpdateCallback() {
                            @Override
                            public void onProgressUpdate(double v) {
                                if (v < 1) {
                                    String progressStr = (int) (v * 100) + "%";
                                    holder.progressTv.setText(progressStr);
                                } else {
                                    holder.progressTv.setVisibility(View.GONE);
                                    holder.contentLl.setBackground(mContext.getDrawable(IdHelper
                                            .getDrawable(mContext, "jmui_receive_msg")));
                                }

                            }
                        });
                    }
                    break;
                case receive_fail:
                    holder.progressTv.setVisibility(View.GONE);
                    holder.contentLl.setBackground(mContext.getDrawable(IdHelper
                            .getDrawable(mContext, "jmui_receive_msg")));
                    holder.resend.setVisibility(View.VISIBLE);
                    break;
                case receive_success:
                    holder.progressTv.setVisibility(View.GONE);
                    holder.contentLl.setBackground(mContext.getDrawable(IdHelper
                            .getDrawable(mContext, "jmui_receive_msg")));
                    break;
            }
        }

        if (holder.resend != null) {
            holder.resend.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (msg.getDirect() == MessageDirect.send) {
                        mAdapter.showResendDialog(holder, msg);
                    } else {
                        holder.contentLl.setBackgroundColor(Color.parseColor("#86222222"));
                        holder.progressTv.setText("0%");
                        holder.progressTv.setVisibility(View.VISIBLE);
                        holder.resend.setVisibility(View.GONE);
                        if (!msg.isContentDownloadProgressCallbackExists()) {
                            msg.setOnContentDownloadProgressCallback(new ProgressUpdateCallback() {
                                @Override
                                public void onProgressUpdate(double v) {
                                    String progressStr = (int) (v * 100) + "%";
                                    holder.progressTv.setText(progressStr);
                                }
                            });
                        }
                        content.downloadFile(msg, new DownloadCompletionCallback() {
                            @Override
                            public void onComplete(int status, String desc, File file) {
                                holder.progressTv.setVisibility(View.GONE);
                                holder.contentLl.setBackground(mContext.getDrawable(IdHelper
                                        .getDrawable(mContext, "jmui_receive_msg")));
                                if (status != 0) {
                                    holder.resend.setVisibility(View.VISIBLE);
                                    Toast.makeText(mContext, IdHelper.getString(mContext,
                                            "download_file_failed"),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(mContext, IdHelper.getString(mContext,
                                            "download_file_succeed"), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
        }
        holder.contentLl.setOnClickListener(new BtnOrTxtListener(position, holder));
    }

    public void handleGroupChangeMsg(Message msg, ViewHolder holder) {
        UserInfo myInfo = JMessageClient.getMyInfo();
        GroupInfo groupInfo = (GroupInfo) msg.getTargetInfo();
        String content = ((EventNotificationContent) msg.getContent()).getEventText();
        EventNotificationContent.EventNotificationType type = ((EventNotificationContent) msg
                .getContent()).getEventNotificationType();
        switch (type) {
            case group_member_added:
                holder.groupChange.setText(content);
                holder.groupChange.setVisibility(View.VISIBLE);
                break;
            case group_member_exit:
                holder.groupChange.setVisibility(View.GONE);
                holder.msgTime.setVisibility(View.GONE);
                break;
            case group_member_removed:
                List<String> userNames = ((EventNotificationContent) msg.getContent()).getUserNames();
                //被删除的人显示EventNotification
                if (userNames.contains(myInfo.getNickname()) || userNames.contains(myInfo.getUserName())) {
                    holder.groupChange.setText(content);
                    holder.groupChange.setVisibility(View.VISIBLE);
                    //群主亦显示
                } else if (myInfo.getUserName().equals(groupInfo.getGroupOwner())) {
                    holder.groupChange.setText(content);
                    holder.groupChange.setVisibility(View.VISIBLE);
                } else {
                    holder.groupChange.setVisibility(View.GONE);
                    holder.msgTime.setVisibility(View.GONE);
                }
                break;
        }
    }

    public void handleCustomMsg(Message msg, ViewHolder holder) {
        CustomContent content = (CustomContent) msg.getContent();
        Boolean isBlackListHint = content.getBooleanValue("blackList");
        if (isBlackListHint != null && isBlackListHint) {
            holder.groupChange.setText(IdHelper.getString(mContext, "jmui_server_803008"));
        } else {
            holder.groupChange.setVisibility(View.GONE);
        }
    }


    public class BtnOrTxtListener implements View.OnClickListener {

        private int position;
        private ViewHolder holder;

        public BtnOrTxtListener(int index, ViewHolder viewHolder) {
            this.position = index;
            this.holder = viewHolder;
        }

        @Override
        public void onClick(View v) {
            Message msg = mMsgList.get(position);
            MessageDirect msgDirect = msg.getDirect();
            switch (msg.getContentType()) {
                case voice:
                    if (!FileHelper.isSdCardExist()) {
                        Toast.makeText(mContext, IdHelper.getString(mContext, "jmui_sdcard_not_exist_toast"),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // 如果之前存在播放动画，无论这次点击触发的是暂停还是播放，停止上次播放的动画
                    if (mVoiceAnimation != null) {
                        mVoiceAnimation.stop();
                    }
                    // 播放中点击了正在播放的Item 则暂停播放
                    if (mp.isPlaying() && mPosition == position) {
                        if (msgDirect == MessageDirect.send) {
                            holder.voice.setImageResource(IdHelper.getAnim(mContext, "jmui_voice_send"));
                        } else {
                            holder.voice.setImageResource(IdHelper.getAnim(mContext, "jmui_voice_receive"));
                        }
                        mVoiceAnimation = (AnimationDrawable) holder.voice.getDrawable();
                        pauseVoice();
                        mVoiceAnimation.stop();
                        // 开始播放录音
                    } else if (msgDirect == MessageDirect.send) {
                        holder.voice.setImageResource(IdHelper.getAnim(mContext, "jmui_voice_send"));
                        mVoiceAnimation = (AnimationDrawable) holder.voice.getDrawable();

                        // 继续播放之前暂停的录音
                        if (mSetData && mPosition == position) {
                            mVoiceAnimation.start();
                            mp.start();
                            // 否则重新播放该录音或者其他录音
                        } else {
                            playVoice(position, holder, true);
                        }
                        // 语音接收方特殊处理，自动连续播放未读语音
                    } else {
                        try {
                            // 继续播放之前暂停的录音
                            if (mSetData && mPosition == position) {
                                if (mVoiceAnimation != null) {
                                    mVoiceAnimation.start();
                                }
                                mp.start();
                                // 否则开始播放另一条录音
                            } else {
                                // 选中的录音是否已经播放过，如果未播放，自动连续播放这条语音之后未播放的语音
                                if (msg.getContent().getBooleanExtra("isReaded") == null
                                        || !msg.getContent().getBooleanExtra("isReaded")) {
                                    autoPlay = true;
                                    playVoice(position, holder, false);
                                    // 否则直接播放选中的语音
                                } else {
                                    holder.voice.setImageResource(IdHelper.getAnim(mContext, "jmui_voice_receive"));
                                    mVoiceAnimation = (AnimationDrawable) holder.voice.getDrawable();
                                    playVoice(position, holder, false);
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
                    break;
                case image:
                    if (holder.picture != null && v.getId() == holder.picture.getId()) {
                        Intent intent = new Intent();
                        intent.putExtra(JChatDemoApplication.TARGET_ID, mConv.getTargetId());
                        intent.putExtra("msgId", msg.getId());
                        if (mConv.getType() == ConversationType.group) {
                            GroupInfo groupInfo = (GroupInfo) mConv.getTargetInfo();
                            intent.putExtra(JChatDemoApplication.GROUP_ID, groupInfo.getGroupID());
                        }
                        intent.putExtra(JChatDemoApplication.TARGET_APP_KEY, mConv.getTargetAppKey());
                        intent.putExtra("msgCount", mMsgList.size());
                        intent.putIntegerArrayListExtra(JChatDemoApplication.MsgIDs, getImgMsgIDList());
                        intent.putExtra("fromChatActivity", true);
                        intent.setClass(mContext, BrowserViewPagerActivity.class);
                        mContext.startActivity(intent);
                    }
                    break;
                case location:
                    if (holder.picture != null && v.getId() == holder.picture.getId()) {
                        Intent intent = new Intent(mContext, SendLocationActivity.class);
                        LocationContent locationContent = (LocationContent) msg.getContent();
                        intent.putExtra("latitude", locationContent.getLatitude().doubleValue());
                        intent.putExtra("longitude", locationContent.getLongitude().doubleValue());
                        intent.putExtra("locDesc", locationContent.getAddress());
                        intent.putExtra("sendLocation", false);
                        mContext.startActivity(intent);
                    }
                    break;
                case file:
                    FileContent content = (FileContent) msg.getContent();
                    if (!TextUtils.isEmpty(content.getLocalPath())) {
                        final String fileName = content.getFileName();
                        final String path = content.getLocalPath();
                        if (msg.getDirect() == MessageDirect.send) {
                            browseDocument(fileName, path);
                        } else {
                            final String newPath = JChatDemoApplication.FILE_DIR + fileName;
                            File file = new File(newPath);
                            if (file.exists() && file.isFile()) {
                                browseDocument(fileName, newPath);
                            } else {
                                FileHelper.getInstance().copyFile(fileName, path, (Activity) mContext,
                                        new FileHelper.CopyFileCallback() {
                                            @Override
                                            public void copyCallback(Uri uri) {
                                                Toast.makeText(mContext, mContext.getString(IdHelper
                                                        .getString(mContext, "file_already_copy_hint")),
                                                        Toast.LENGTH_SHORT).show();
                                                browseDocument(fileName, newPath);
                                            }
                                        });
                            }
                        }
                    } else {
                        if (msg.getDirect() == MessageDirect.receive) {
                            holder.contentLl.setBackgroundColor(Color.parseColor("#86222222"));
                            holder.progressTv.setText("0%");
                            holder.progressTv.setVisibility(View.VISIBLE);
                            msg.setOnContentDownloadProgressCallback(new ProgressUpdateCallback() {
                                @Override
                                public void onProgressUpdate(double v) {
                                    String progressStr = (int) (v * 100) + "%";
                                    holder.progressTv.setText(progressStr);
                                }
                            });
                            content.downloadFile(msg, new DownloadCompletionCallback() {
                                @Override
                                public void onComplete(int status, String desc, File file) {
                                    holder.progressTv.setVisibility(View.GONE);
                                    holder.contentLl.setBackground(mContext.getDrawable(IdHelper
                                            .getDrawable(mContext, "jmui_receive_msg")));
                                    if (status != 0) {
                                        holder.resend.setVisibility(View.VISIBLE);
                                        Toast.makeText(mContext, IdHelper.getString(mContext,
                                                "download_file_failed"),
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(mContext, IdHelper.getString(mContext,
                                                "download_file_succeed"), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(mContext, IdHelper.getString(mContext, "jmui_file_not_found_toast"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
            }

        }
    }

    public void playVoice(final int position, final ViewHolder holder, final boolean isSender) {
        // 记录播放录音的位置
        mPosition = position;
        Message msg = mMsgList.get(position);
        if (autoPlay) {
            mConv.updateMessageExtra(msg, "isReaded", true);
            holder.readStatus.setVisibility(View.GONE);
            if (mVoiceAnimation != null) {
                mVoiceAnimation.stop();
                mVoiceAnimation = null;
            }
            holder.voice.setImageResource(IdHelper.getAnim(mContext, "jmui_voice_receive"));
            mVoiceAnimation = (AnimationDrawable) holder.voice.getDrawable();
        }
        try {
            mp.reset();
            VoiceContent vc = (VoiceContent) msg.getContent();
            mFIS = new FileInputStream(vc.getLocalPath());
            mFD = mFIS.getFD();
            mp.setDataSource(mFD);
            if (mIsEarPhoneOn) {
                mp.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
            } else {
                mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            }
            mp.prepare();
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
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
                    if (isSender) {
                        holder.voice.setImageResource(IdHelper.getDrawable(mContext, "jmui_send_3"));
                    } else {
                        holder.voice.setImageResource(IdHelper.getDrawable(mContext, "jmui_receive_3"));
                    }
                    if (autoPlay) {
                        int curCount = mIndexList.indexOf(position);
                        if (curCount + 1 >= mIndexList.size()) {
                            nextPlayPosition = -1;
                            autoPlay = false;
                        } else {
                            nextPlayPosition = mIndexList.get(curCount + 1);
                            mAdapter.notifyDataSetChanged();
                        }
                        mIndexList.remove(curCount);
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(mContext, IdHelper.getString(mContext, "jmui_file_not_found_toast"),
                    Toast.LENGTH_SHORT).show();
            VoiceContent vc = (VoiceContent) msg.getContent();
            vc.downloadVoiceFile(msg, new DownloadCompletionCallback() {
                @Override
                public void onComplete(int status, String desc, File file) {
                    if (status == 0) {
                        Toast.makeText(mContext, IdHelper.getString(mContext, "download_completed_toast"),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext, IdHelper.getString(mContext, "file_fetch_failed"),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
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
        //计算图片缩放比例
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
        } else {
            mIsEarPhoneOn = true;
            audioManager.setSpeakerphoneOn(false);
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, currVolume,
                    AudioManager.STREAM_VOICE_CALL);
        }
    }

    public void releaseMediaPlayer() {
        if (mp != null)
            mp.release();
    }

    public void initMediaPlayer() {
        mp.reset();
    }

    public void stopMediaPlayer() {
        if (mp.isPlaying())
            mp.stop();
    }

    private void pauseVoice() {
        mp.pause();
        mSetData = true;
    }


    private void addToListAndSort(int position) {
        mIndexList.add(position);
        Collections.sort(mIndexList);
    }

    private ArrayList<Integer> getImgMsgIDList() {
        ArrayList<Integer> imgMsgIDList = new ArrayList<Integer>();
        for (Message msg : mMsgList) {
            if (msg.getContentType() == ContentType.image) {
                imgMsgIDList.add(msg.getId());
            }
        }
        return imgMsgIDList;
    }

    private void browseDocument(String fileName, String path) {
        try {
            String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            String mime = mimeTypeMap.getMimeTypeFromExtension(ext);
            File file = new File(path);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), mime);
            mContext.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, mContext.getString(IdHelper.getString(mContext,
                    "file_not_support_hint")), Toast.LENGTH_SHORT).show();
        }
    }

}
