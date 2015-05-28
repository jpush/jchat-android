package io.jchat.android.adapter;

import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import io.jchat.android.R;

import java.io.File;
import java.util.List;

import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.enums.ConversationType;

import io.jchat.android.activity.ConversationListFragment;
import io.jchat.android.tools.BitmapLoader;
import io.jchat.android.tools.NativeImageLoader;
import io.jchat.android.tools.TimeFormat;
import io.jchat.android.view.RoundImageView;

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
            if (conv.getType().equals(ConversationType.single)){
                File file = conv.getAvatar();
                if(file != null){
                    Bitmap bitmap = BitmapLoader.getBitmapFromFile(file.getAbsolutePath(), (int)(50 * density), (int)(50 * density));
                    NativeImageLoader.getInstance().updateBitmapFromCache(conv.getTargetId(), bitmap);
                }
            }
        }
    }

    public void refresh(List<Conversation> data) {
        mDatas.clear();
        this.mDatas = data;
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
        Conversation convItem = mDatas.get(position);
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext.getActivity()).inflate(
                    R.layout.conv_item_row, null);
            viewHolder = new ViewHolder();
            viewHolder.headIcon = (RoundImageView) convertView
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
        if (convItem.getLastMsgDate() != 0) {
            viewHolder.datetime.setText(timeFormat.getTime());
        } else viewHolder.datetime.setText("");
        // 按照最后一条消息的消息类型进行处理
        switch (convItem.getLatestType()) {
            case image:
                viewHolder.content.setText(mContext.getString(R.string.picture));
                break;
            case voice:
                viewHolder.content.setText(mContext.getString(R.string.voice));
                break;
            case location:
                viewHolder.content.setText(mContext.getString(R.string.location));
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
            viewHolder.groupName.setText(convItem.getDisplayName());
            Bitmap bitmap = NativeImageLoader.getInstance().getBitmapFromMemCache(convItem.getTargetId());
            if (bitmap != null)
                viewHolder.headIcon.setImageBitmap(bitmap);
            else viewHolder.headIcon.setImageResource(R.drawable.head_icon);
        }
        // 群聊
        else {
            viewHolder.headIcon.setImageResource(R.drawable.head_icon);
            final String displayName = convItem.getDisplayName();
            viewHolder.groupName.setText(displayName);
        }

        // TODO 更新Message的数量,
        if (convItem.getUnReadMsgCnt() > 0) {
            viewHolder.newMsgNumber.setVisibility(View.VISIBLE);
            if(convItem.getUnReadMsgCnt() < 100)
                viewHolder.newMsgNumber.setText(String.valueOf(convItem
                    .getUnReadMsgCnt()));
            else viewHolder.newMsgNumber.setText("99");
        } else {
            viewHolder.newMsgNumber.setVisibility(View.GONE);
        }

        return convertView;
    }

    private class ViewHolder {
        RoundImageView headIcon;
        TextView groupName;
        TextView content;
        TextView datetime;
        TextView newMsgNumber;
    }

}
