package io.jchat.android.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.content.CustomContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.R;
import io.jchat.android.chatting.utils.HandleResponseCode;
import io.jchat.android.tools.SortConvList;
import io.jchat.android.chatting.utils.TimeFormat;
import io.jchat.android.chatting.CircleImageView;

public class ConversationListAdapter extends BaseAdapter {

    List<Conversation> mDatas;
    private Activity mContext;
    private Map<String, String> mDraftMap = new HashMap<String, String>();
    private UIHandler mUIHandler = new UIHandler(this);
    private static final int REFRESH_CONVERSATION_LIST = 0x3001;

    public ConversationListAdapter(Activity context, List<Conversation> data) {
        this.mContext = context;
        this.mDatas = data;
    }

    /**
     * 收到消息后将会话置顶
     *
     * @param conv 要置顶的会话
     */
    public void setToTop(Conversation conv) {
        for (Conversation conversation : mDatas) {
            if (conv.getId().equals(conversation.getId())) {
                mDatas.remove(conversation);
                mDatas.add(0, conv);
                mUIHandler.removeMessages(REFRESH_CONVERSATION_LIST);
                mUIHandler.sendEmptyMessageDelayed(REFRESH_CONVERSATION_LIST, 200);
                return;
            }
        }
        //如果是新的会话
        mDatas.add(0, conv);
        mUIHandler.removeMessages(REFRESH_CONVERSATION_LIST);
        mUIHandler.sendEmptyMessageDelayed(REFRESH_CONVERSATION_LIST, 200);
    }

    public void sortConvList() {
        SortConvList sortConvList = new SortConvList();
        Collections.sort(mDatas, sortConvList);
        notifyDataSetChanged();
    }

    public void addNewConversation(Conversation conv) {
        mDatas.add(0, conv);
        notifyDataSetChanged();
    }

    public void deleteConversation(long groupId) {
        for (Conversation conv : mDatas) {
            if (conv.getType() == ConversationType.group
                    && Long.parseLong(conv.getTargetId()) == groupId) {
                mDatas.remove(conv);
                return;
            }
        }
    }

    public void putDraftToMap(String convId, String draft) {
        mDraftMap.put(convId, draft);
    }

    public void delDraftFromMap(String convId) {
        mDraftMap.remove(convId);
        notifyDataSetChanged();
    }

    public String getDraft(String convId) {
        return mDraftMap.get(convId);
    }

    @Override
    public int getCount() {
        if (mDatas == null) {
            return 0;
        }
        return mDatas.size();
    }

    @Override
    public Conversation getItem(int position) {
        if (mDatas == null) {
            return null;
        }
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Conversation convItem = mDatas.get(position);
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_conversation,
                    null);
            viewHolder = new ViewHolder();
            viewHolder.headIcon = (CircleImageView) convertView
                    .findViewById(R.id.msg_item_head_icon);
            viewHolder.convName = (TextView) convertView
                    .findViewById(R.id.conv_item_name);
            viewHolder.content = (TextView) convertView
                    .findViewById(R.id.msg_item_content);
            viewHolder.datetime = (TextView) convertView
                    .findViewById(R.id.msg_item_date);
            viewHolder.newMsgNumber = (TextView) convertView
                    .findViewById(R.id.new_msg_number);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();

        }
        String draft = mDraftMap.get(convItem.getId());
        //如果该会话草稿为空，显示最后一条消息
        if (TextUtils.isEmpty(draft)) {
            Message lastMsg = convItem.getLatestMessage();
            if (lastMsg != null) {
                TimeFormat timeFormat = new TimeFormat(mContext, lastMsg.getCreateTime());
                viewHolder.datetime.setText(timeFormat.getTime());
                // 按照最后一条消息的消息类型进行处理
                switch (lastMsg.getContentType()) {
                    case image:
                        viewHolder.content.setText(mContext.getString(R.string.type_picture));
                        break;
                    case voice:
                        viewHolder.content.setText(mContext.getString(R.string.type_voice));
                        break;
                    case location:
                        viewHolder.content.setText(mContext.getString(R.string.type_location));
                        break;
                    case eventNotification:
                        viewHolder.content.setText(mContext.getString(R.string.group_notification));
                        break;
                    case custom:
                        CustomContent content = (CustomContent) lastMsg.getContent();
                        Boolean isBlackListHint = content.getBooleanValue("blackList");
                        if (isBlackListHint != null && isBlackListHint) {
                            viewHolder.content.setText(mContext.getString(R.string.jmui_server_803008));
                        } else {
                            viewHolder.content.setText(mContext.getString(R.string.type_custom));
                        }
                        break;
                    default:
                        viewHolder.content.setText(((TextContent) lastMsg.getContent()).getText());
                }
            }else {
                TimeFormat timeFormat = new TimeFormat(mContext, convItem.getLastMsgDate());
                viewHolder.datetime.setText(timeFormat.getTime());
                viewHolder.content.setText("");
            }
        } else {
            String content = mContext.getString(R.string.draft) + draft;
            SpannableStringBuilder builder = new SpannableStringBuilder(content);
            builder.setSpan(new ForegroundColorSpan(Color.RED), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            viewHolder.content.setText(builder);
        }

        // 如果是单聊
        if (convItem.getType().equals(ConversationType.single)) {
            viewHolder.convName.setText(convItem.getTitle());
            UserInfo userInfo = (UserInfo) convItem.getTargetInfo();
            if (userInfo != null && !TextUtils.isEmpty(userInfo.getAvatar())) {
                userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                    @Override
                    public void gotResult(int status, String desc, Bitmap bitmap) {
                        if (status == 0) {
                            viewHolder.headIcon.setImageBitmap(bitmap);
                        }else {
                            viewHolder.headIcon.setImageResource(R.drawable.jmui_head_icon);
                            HandleResponseCode.onHandle(mContext, status, false);
                        }
                    }
                });
            }else {
                viewHolder.headIcon.setImageResource(R.drawable.jmui_head_icon);
            }
        } else {
            viewHolder.headIcon.setImageResource(R.drawable.group);
            viewHolder.convName.setText(convItem.getTitle());
            Log.d("ConversationListAdapter", "Conversation title: " + convItem.getTitle());
        }

        // TODO 更新Message的数量,
        if (convItem.getUnReadMsgCnt() > 0) {
            viewHolder.newMsgNumber.setVisibility(View.VISIBLE);
            if (convItem.getUnReadMsgCnt() < 100) {
                viewHolder.newMsgNumber.setText(String.valueOf(convItem.getUnReadMsgCnt()));
            }
            else {
                viewHolder.newMsgNumber.setText(mContext.getString(R.string.hundreds_of_unread_msgs));
            }
        } else {
            viewHolder.newMsgNumber.setVisibility(View.GONE);
        }

        return convertView;
    }

    static class UIHandler extends Handler {

        private final WeakReference<ConversationListAdapter> mAdapter;

        public UIHandler(ConversationListAdapter adapter) {
            mAdapter = new WeakReference<ConversationListAdapter>(adapter);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            ConversationListAdapter adapter = mAdapter.get();
            if (adapter != null) {
                switch (msg.what) {
                    case REFRESH_CONVERSATION_LIST:
                        adapter.notifyDataSetChanged();
                        break;
                }
            }
        }
    }

    private class ViewHolder {
        CircleImageView headIcon;
        TextView convName;
        TextView content;
        TextView datetime;
        TextView newMsgNumber;
    }

}
