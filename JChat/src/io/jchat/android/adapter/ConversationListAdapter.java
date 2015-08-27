package io.jchat.android.adapter;

import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.R;

import java.io.File;
import java.util.List;

import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.enums.ConversationType;

import io.jchat.android.activity.ConversationListFragment;
import io.jchat.android.tools.BitmapLoader;
import io.jchat.android.tools.NativeImageLoader;
import io.jchat.android.tools.TimeFormat;
import io.jchat.android.view.CircleImageView;

public class ConversationListAdapter extends BaseAdapter {

    List<Conversation> mDatas;
    private ConversationListFragment mContext;

    public ConversationListAdapter(ConversationListFragment context,
                                   List<Conversation> data) {
        this.mContext = context;
        this.mDatas = data;
        DisplayMetrics dm = new DisplayMetrics();
        (context.getActivity()).getWindowManager().getDefaultDisplay().getMetrics(dm);
        double density = dm.density;
        for (Conversation conv : mDatas) {
            if (conv.getType().equals(ConversationType.single)) {
                File file = conv.getAvatarFile();
                if (file != null) {
                    Bitmap bitmap = BitmapLoader.getBitmapFromFile(file.getAbsolutePath(),
                            (int) (50 * density), (int) (50 * density));
                    NativeImageLoader.getInstance().updateBitmapFromCache(conv.getTargetId(),
                            bitmap);
                }
            }
        }
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
                mContext.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
                return;
            }
        }
        //如果是新的会话
        mDatas.add(0, conv);
        mContext.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    public void addNewConversation(Conversation conv) {
        mDatas.add(0, conv);
        notifyDataSetChanged();
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
            convertView = LayoutInflater.from(mContext.getActivity()).inflate(
                    R.layout.conversation_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.headIcon = (CircleImageView) convertView
                    .findViewById(R.id.msg_item_head_icon);
            viewHolder.groupName = (TextView) convertView
                    .findViewById(R.id.conv_item_group_name);
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
        TimeFormat timeFormat = new TimeFormat(mContext.getActivity(), convItem.getLastMsgDate());
        viewHolder.datetime.setText(timeFormat.getTime());
        // 按照最后一条消息的消息类型进行处理
        switch (convItem.getLatestType()) {
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
            default:
                viewHolder.content.setText(convItem.getLatestText());
        }
//		viewHolder.headIcon.setImageResource(R.drawable.head_icon);
        // 如果是单聊
        if (convItem.getType().equals(ConversationType.single)) {
            viewHolder.groupName.setText(convItem.getTitle());
            Bitmap bitmap = NativeImageLoader.getInstance().getBitmapFromMemCache(convItem.getTargetId());
            if (bitmap != null)
                viewHolder.headIcon.setImageBitmap(bitmap);
            else viewHolder.headIcon.setImageResource(R.drawable.head_icon);
        }
        // 群聊
        else {
            viewHolder.headIcon.setImageResource(R.drawable.group);
            viewHolder.groupName.setText(convItem.getTitle());
        }

        // TODO 更新Message的数量,
        if (convItem.getUnReadMsgCnt() > 0) {
            viewHolder.newMsgNumber.setVisibility(View.VISIBLE);
            if (convItem.getUnReadMsgCnt() < 100)
                viewHolder.newMsgNumber.setText(String.valueOf(convItem
                        .getUnReadMsgCnt()));
            else viewHolder.newMsgNumber.setText("99");
        } else {
            viewHolder.newMsgNumber.setVisibility(View.GONE);
        }

        return convertView;
    }

    private class ViewHolder {
        CircleImageView headIcon;
        TextView groupName;
        TextView content;
        TextView datetime;
        TextView newMsgNumber;
    }

}
