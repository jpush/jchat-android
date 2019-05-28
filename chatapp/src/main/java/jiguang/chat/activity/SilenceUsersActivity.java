package jiguang.chat.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import jiguang.chat.R;
import jiguang.chat.utils.DialogCreator;

/**
 * Created by ${chenyn} on 2017/11/28.
 */

public class SilenceUsersActivity extends BaseActivity {

    private ListView mLv_silenceUsers;
    private List<UserInfo> mSilenceMembers;
    private String mGroupOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_silence_users);

        initView();
        initData();
    }

    private void initView() {
        initTitle(true, true, "群禁言列表", "", false, "");
        mLv_silenceUsers = (ListView) findViewById(R.id.lv_silenceUsers);
    }

    private void initData() {
        long groupID = getIntent().getLongExtra("groupID", 0);
        Dialog loadingDialog = DialogCreator.createLoadingDialog(SilenceUsersActivity.this, "正在加载...");
        loadingDialog.show();
        JMessageClient.getGroupInfo(groupID, new GetGroupInfoCallback() {
            @Override
            public void gotResult(int i, String s, GroupInfo groupInfo) {
                loadingDialog.dismiss();
                if (i == 0) {
                    mGroupOwner = groupInfo.getGroupOwner();
                    mSilenceMembers = groupInfo.getGroupSilenceMembers();
                    mLv_silenceUsers.setAdapter(new SilenceMembersAdapter(groupInfo));
                } else {
                    Toast.makeText(SilenceUsersActivity.this, "没有禁言列表", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mLv_silenceUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UserInfo userInfo = (UserInfo) parent.getItemAtPosition(position);
                Intent intent = new Intent(SilenceUsersActivity.this, GroupUserInfoActivity.class);
                intent.putExtra("groupID", groupID);
                intent.putExtra("groupUserName", userInfo.getUserName());
                intent.putExtra("groupOwner", mGroupOwner);
                startActivity(intent);
            }
        });
    }

    class SilenceMembersAdapter extends BaseAdapter {
        private GroupInfo mGroupInfo;

        public SilenceMembersAdapter(GroupInfo groupInfo) {
            this.mGroupInfo = groupInfo;
        }

        @Override
        public int getCount() {
            return mSilenceMembers == null ? 0 : mSilenceMembers.size();
        }

        @Override
        public Object getItem(int position) {
            return mSilenceMembers.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            UserInfo userInfo = mSilenceMembers.get(position);
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(SilenceUsersActivity.this, R.layout.item_silence_member, null);
                holder.iv_userAvatar = convertView.findViewById(R.id.iv_userAvatar);
                holder.tv_userName = convertView.findViewById(R.id.tv_userName);
                holder.tv_delSilence = convertView.findViewById(R.id.tv_delSilence);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            String groupOwner = mGroupInfo.getGroupOwner();
            if (groupOwner.equals(JMessageClient.getMyInfo().getUserName())) {
                holder.tv_delSilence.setVisibility(View.VISIBLE);
            } else {
                holder.tv_delSilence.setVisibility(View.GONE);
            }

            File avatarFile = userInfo.getAvatarFile();
            if (avatarFile == null) {
                userInfo.getBigAvatarBitmap(new GetAvatarBitmapCallback() {
                    @Override
                    public void gotResult(int i, String s, Bitmap bitmap) {
                        if (i == 0) {
                            holder.iv_userAvatar.setImageBitmap(bitmap);
                        } else {
                            holder.iv_userAvatar.setImageResource(R.drawable.jmui_head_icon);
                        }
                    }
                });
            } else {
                holder.iv_userAvatar.setImageBitmap(BitmapFactory.decodeFile(avatarFile.getPath()));
            }

            holder.tv_userName.setText(userInfo.getDisplayName());
            holder.tv_delSilence.setOnClickListener(v -> mGroupInfo.setGroupMemSilence(userInfo.getUserName(), userInfo.getAppKey(), false, new BasicCallback() {
                @Override
                public void gotResult(int i, String s) {
                    if (i == 0) {
                        Toast.makeText(SilenceUsersActivity.this, "取消成功", Toast.LENGTH_SHORT).show();
                        mSilenceMembers.remove(userInfo);
                    } else {
                        Toast.makeText(SilenceUsersActivity.this, "取消失败", Toast.LENGTH_SHORT).show();
                    }
                    notifyDataSetChanged();
                }
            }));

            return convertView;
        }

        class ViewHolder {
            ImageView iv_userAvatar;
            TextView tv_userName;
            TextView tv_delSilence;
        }
    }
}
