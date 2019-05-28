package jiguang.chat.activity.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.model.GroupInfo;
import jiguang.chat.R;
import jiguang.chat.activity.ApplyGroupInfoActivity;
import jiguang.chat.activity.GroupInfoActivity;
import jiguang.chat.adapter.GroupVerificationAdapter;
import jiguang.chat.application.JGApplication;
import jiguang.chat.database.GroupApplyEntry;
import jiguang.chat.database.RefuseGroupEntry;
import jiguang.chat.database.UserEntry;

/**
 * Created by ${chenyn} on 2017/11/7.
 */

public class GroupFragment extends BaseFragment {
    private Activity mContext;
    private ListView mListView;
    private List<GroupApplyEntry> mRecommends;
    private GroupVerificationAdapter mAdapter;
    private List<RefuseGroupEntry> mRefuseGroupEntryList;
    private LayoutInflater mInflater;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = getActivity();
    }

    private void initData() {
        UserEntry user = JGApplication.getUserEntry();
        if (user != null) {
            mRecommends = user.getGroupApplyRecommends();
            mAdapter = new GroupVerificationAdapter(mContext, mRecommends);
            mListView.setAdapter(mAdapter);

            mRefuseGroupEntryList = user.getRefuseGroupRecommends();
            if (mRefuseGroupEntryList != null && mRefuseGroupEntryList.size() > 0) {
                mListView.setAdapter(new RefuseGroupAdapter());
            }
        }

        //点击事件要分类型
        mListView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent();
            Object itemAtPosition = parent.getItemAtPosition(position);
            if (itemAtPosition instanceof GroupApplyEntry) {
                intent.setClass(mContext, ApplyGroupInfoActivity.class);
                GroupApplyEntry entry = (GroupApplyEntry) itemAtPosition;
                intent.putExtra("toName", entry.toUsername);
                intent.putExtra("reason", entry.reason);
                startActivity(intent);
            }else {
                intent.setClass(mContext, GroupInfoActivity.class);
                RefuseGroupEntry entry = (RefuseGroupEntry) itemAtPosition;
                intent.setFlags(1);
                intent.putExtra("groupId", entry.groupId);
                startActivity(intent);
            }
        });

        mListView.setOnItemLongClickListener((parent, view, position, id) -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("是否删除?").setPositiveButton("确定", (dialog, which) -> {

                GroupApplyEntry entry = (GroupApplyEntry) parent.getItemAtPosition(position);
                entry.delete();
                mRecommends.remove(entry);
                mAdapter.notifyDataSetChanged();

            }).setNegativeButton("取消", (dialog, which) -> {
            }).show();

            return true;
        });
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mInflater = LayoutInflater.from(mContext);
        return initView();
    }

    private View initView() {
        View view = View.inflate(mContext, R.layout.verification_group, null);
        mListView = view.findViewById(R.id.group_recommend_list_view);
        initData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    class RefuseGroupAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mRefuseGroupEntryList.size();
        }

        @Override
        public Object getItem(int position) {
            return mRefuseGroupEntryList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            RefuseGroupEntry entry = mRefuseGroupEntryList.get(position);

            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.item_refuse_group, null);
                holder.groupAvatar = convertView.findViewById(R.id.groupAvatar);
                holder.groupName = convertView.findViewById(R.id.groupName);
                holder.refuseJoin = convertView.findViewById(R.id.refuseJoin);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.refuseJoin.setText("群主拒绝 " + entry.displayName + " 入群");
            JMessageClient.getGroupInfo(Long.parseLong(entry.groupId), new GetGroupInfoCallback() {
                @Override
                public void gotResult(int i, String s, GroupInfo groupInfo) {
                    if (i == 0) {
                        holder.groupName.setText(groupInfo.getGroupName());
                        if (groupInfo.getAvatar() != null) {
                            holder.groupAvatar.setImageBitmap(BitmapFactory.decodeFile(groupInfo.getAvatarFile().getPath()));
                        } else {
                            holder.groupAvatar.setImageResource(R.drawable.group);
                        }
                    }
                }
            });
            return convertView;
        }

        class ViewHolder {
            ImageView groupAvatar;
            TextView groupName;
            TextView refuseJoin;
        }
    }
}
