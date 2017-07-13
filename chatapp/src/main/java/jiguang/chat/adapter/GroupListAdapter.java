package jiguang.chat.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.eventbus.EventBus;
import jiguang.chat.R;
import jiguang.chat.activity.ChatActivity;
import jiguang.chat.application.JGApplication;
import jiguang.chat.entity.Event;
import jiguang.chat.entity.EventType;

/**
 * Created by ${chenyn} on 2017/4/26.
 */

public class GroupListAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private String groupName;
    private Map<Long, String> mGroupName = new HashMap<>();
    private List<GroupInfo> mGroupInfo;

    public GroupListAdapter(Context context, List<GroupInfo> groupInfo) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mGroupInfo = groupInfo;

    }

    @Override
    public int getCount() {
        return mGroupInfo.size();
    }

    @Override
    public Object getItem(int position) {
        return mGroupInfo.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private String getGroupName(List<UserInfo> groupMembers, StringBuilder builder) {
        for (UserInfo info : groupMembers) {
            String noteName = info.getNotename();
            if (TextUtils.isEmpty(noteName)) {
                noteName = info.getNickname();
                if (TextUtils.isEmpty(noteName)) {
                    noteName = info.getUserName();
                }
            }
            builder.append(noteName);
            builder.append(",");
        }

        return builder.substring(0, builder.lastIndexOf(","));
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_group_list, parent, false);
            holder.itemLl = (LinearLayout) convertView.findViewById(R.id.group_ll);
            holder.avatar = (ImageView) convertView.findViewById(R.id.group_iv);
            holder.groupName = (TextView) convertView.findViewById(R.id.group_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final GroupInfo groupInfo = mGroupInfo.get(position);
        if (TextUtils.isEmpty(groupInfo.getGroupName())) {
            //Conversation groupConversation = JMessageClient.getGroupConversation(groupId);
            //群组名是null的话,手动拿出5个名字拼接
            List<UserInfo> groupMembers = groupInfo.getGroupMembers();

            StringBuilder builder = new StringBuilder();
            if (groupMembers.size() <= 5) {
                groupName = getGroupName(groupMembers, builder);
            } else {
                List<UserInfo> newGroupMember = groupMembers.subList(0, 5);
                groupName = getGroupName(newGroupMember, builder);
            }
        } else {
            groupName = groupInfo.getGroupName();
        }

        mGroupName.put(groupInfo.getGroupID(), groupName);
        holder.groupName.setText(groupName);
        holder.avatar.setImageResource(R.drawable.group);

        holder.itemLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Conversation groupConversation = JMessageClient.getGroupConversation(groupInfo.getGroupID());
                if (groupConversation == null) {
                    groupConversation = Conversation.createGroupConversation(groupInfo.getGroupID());
                    EventBus.getDefault().post(new Event.Builder()
                            .setType(EventType.createConversation)
                            .setConversation(groupConversation)
                            .build());
                }

                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra(JGApplication.CONV_TITLE, mGroupName.get(groupInfo.getGroupID()));
                intent.putExtra(JGApplication.GROUP_ID, groupInfo.getGroupID());
                mContext.startActivity(intent);

            }
        });

        return convertView;
    }


    private static class ViewHolder {
        LinearLayout itemLl;
        TextView groupName;
        ImageView avatar;
    }
}
