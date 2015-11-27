package io.jchat.android.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.R;
import io.jchat.android.tools.HandleResponseCode;
import io.jchat.android.view.CircleImageView;

/**
 * Created by Ken on 2015/11/25.
 */
public class AllMembersAdapter extends BaseAdapter {

    private Context mContext;
    private List<UserInfo> mMemberList = new ArrayList<UserInfo>();
    private boolean mIsDeleteMode;
    private List<String> mSelectedList = new ArrayList<String>();
    private SparseBooleanArray mSelectMap = new SparseBooleanArray();

    public AllMembersAdapter(Context context, List<UserInfo> memberList, boolean isDeleteMode) {
        this.mContext = context;
        this.mMemberList = memberList;
        this.mIsDeleteMode = isDeleteMode;
    }

    public void refreshMemberList(long groupId){
        Conversation conv = JMessageClient.getGroupConversation(groupId);
        GroupInfo groupInfo = (GroupInfo)conv.getTargetInfo();
        mMemberList = groupInfo.getGroupMembers();
        mSelectMap.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mMemberList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.all_member_item, null);
            viewHolder = new ViewHolder((CircleImageView) convertView.findViewById(R.id.icon_iv),
                    (TextView) convertView.findViewById(R.id.name),
                    (CheckBox) convertView.findViewById(R.id.check_box_cb));
            convertView.setTag(viewHolder);
        } else {
           viewHolder = (ViewHolder)convertView.getTag();
        }

        final UserInfo userInfo = mMemberList.get(position);
        if (mIsDeleteMode && position > 0) {
            viewHolder.checkBox.setVisibility(View.VISIBLE);
            viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (viewHolder.checkBox.isChecked()) {
                        mSelectedList.add(userInfo.getUserName());
                        mSelectMap.put(position, true);
                    } else {
                        mSelectedList.remove(userInfo.getUserName());
                        mSelectMap.delete(position);
                    }
                }
            });
            viewHolder.checkBox.setChecked(mSelectMap.get(position));
        } else {
            viewHolder.checkBox.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(userInfo.getAvatar())) {
            userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                @Override
                public void gotResult(int status, String desc, Bitmap bitmap) {
                    if (status == 0) {
                        viewHolder.icon.setImageBitmap(bitmap);
                    } else {
                        viewHolder.icon.setImageResource(R.drawable.head_icon);
                        HandleResponseCode.onHandle(mContext, status, false);
                    }
                }
            });
        } else {
            viewHolder.icon.setImageResource(R.drawable.head_icon);
        }
        String displayName = userInfo.getNickname();
        if (TextUtils.isEmpty(displayName)) {
            viewHolder.displayName.setText(userInfo.getUserName());
        } else {
            viewHolder.displayName.setText(userInfo.getNickname());
        }

        return convertView;
    }

    public List<String> getSelectedList() {
        Log.d("AllMembersAdapter", "SelectedList: " + mSelectedList.toString());
        return mSelectedList;
    }

    public void setOnCheck(int position) {
        UserInfo userInfo = mMemberList.get(position);
        if (mSelectedList.contains(userInfo.getUserName())) {
//            View view = getView(position, null, null);
//            CheckBox checkBox = (CheckBox) view.findViewById(R.id.check_box_cb);
//            checkBox.setChecked(false);
            mSelectedList.remove(position);
            mSelectMap.delete(position);
        } else {
            mSelectedList.add(userInfo.getUserName());
            mSelectMap.put(position, true);
        }
        notifyDataSetChanged();
    }

    public void updateListView(List<UserInfo> filterList) {
        this.mMemberList = filterList;
        notifyDataSetChanged();
    }

    class ViewHolder {

        protected CircleImageView icon;
        protected TextView displayName;
        protected CheckBox checkBox;

        public ViewHolder(CircleImageView icon, TextView name, CheckBox checkBox) {
            this.icon = icon;
            this.displayName = name;
            this.checkBox = checkBox;
        }
    }
}
