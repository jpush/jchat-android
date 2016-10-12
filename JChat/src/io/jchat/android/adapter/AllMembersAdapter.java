package io.jchat.android.adapter;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import io.jchat.android.R;
import io.jchat.android.activity.FriendInfoActivity;
import io.jchat.android.activity.MeInfoActivity;
import io.jchat.android.activity.MembersInChatActivity;
import io.jchat.android.application.JChatDemoApplication;
import io.jchat.android.chatting.utils.DialogCreator;
import io.jchat.android.chatting.utils.HandleResponseCode;
import io.jchat.android.chatting.CircleImageView;
import io.jchat.android.activity.MembersInChatActivity.ItemModel;

/**
 * Created by Ken on 2015/11/25.
 */
public class AllMembersAdapter extends BaseAdapter implements AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener {

    private MembersInChatActivity mContext;
    private List<ItemModel> mMemberList = new ArrayList<ItemModel>();
    private boolean mIsDeleteMode;
    private List<String> mSelectedList = new ArrayList<String>();
    private SparseBooleanArray mSelectMap = new SparseBooleanArray();
    private Dialog mDialog;
    private Dialog mLoadingDialog;
    private boolean mIsCreator;
    private long mGroupId;
    private int mWidth;

    public AllMembersAdapter(MembersInChatActivity context, List<ItemModel> memberList, boolean isDeleteMode,
                             boolean isCreator, long groupId, int width) {
        this.mContext = context;
        this.mMemberList = memberList;
        this.mIsDeleteMode = isDeleteMode;
        this.mIsCreator = isCreator;
        this.mGroupId = groupId;
        this.mWidth = width;
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

        final ItemModel itemModel = mMemberList.get(position);
        final UserInfo userInfo = itemModel.data;
        if (mIsDeleteMode) {
            if (position > 0) {
                viewHolder.checkBox.setVisibility(View.VISIBLE);
                viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (viewHolder.checkBox.isChecked()) {
                            mSelectedList.add(userInfo.getUserName());
                            mSelectMap.put(position, true);
                            addAnimation(viewHolder.checkBox);
                        } else {
                            mSelectedList.remove(userInfo.getUserName());
                            mSelectMap.delete(position);
                        }
                    }
                });
                viewHolder.checkBox.setChecked(mSelectMap.get(position));
            } else {
                viewHolder.checkBox.setVisibility(View.INVISIBLE);
            }

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
                        viewHolder.icon.setImageResource(R.drawable.jmui_head_icon);
                        HandleResponseCode.onHandle(mContext, status, false);
                    }
                }
            });
        } else {
            viewHolder.icon.setImageResource(R.drawable.jmui_head_icon);
        }
        viewHolder.displayName.setText(itemModel.highlight);
        return convertView;
    }

    /**
     * 给CheckBox加点击动画，利用开源库nineoldandroids设置动画
     */
    private void addAnimation(View view) {
        float[] vaules = new float[]{0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f, 1.1f, 1.2f, 1.3f, 1.25f, 1.2f, 1.15f, 1.1f, 1.0f};
        AnimatorSet set = new AnimatorSet();
        set.playTogether(ObjectAnimator.ofFloat(view, "scaleX", vaules),
                ObjectAnimator.ofFloat(view, "scaleY", vaules));
        set.setDuration(150);
        set.start();
    }

    public List<String> getSelectedList() {
        Log.d("AllMembersAdapter", "SelectedList: " + mSelectedList.toString());
        return mSelectedList;
    }

    public void setOnCheck(int position) {
        UserInfo userInfo = mMemberList.get(position).data;
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

    public void updateListView(List<ItemModel> filterList) {
        mSelectMap.clear();
        mMemberList = filterList;
        notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        UserInfo userInfo = mMemberList.get(position).data;
        String userName = userInfo.getUserName();
        Intent intent = new Intent();
        if (userName.equals(JMessageClient.getMyInfo().getUserName())) {
            intent.setClass(mContext, MeInfoActivity.class);
            mContext.startActivity(intent);
        } else {
            intent.setClass(mContext, FriendInfoActivity.class);
            intent.putExtra(JChatDemoApplication.TARGET_APP_KEY, userInfo.getAppKey());
            intent.putExtra(JChatDemoApplication.TARGET_ID,
                    userInfo.getUserName());
            intent.putExtra(JChatDemoApplication.GROUP_ID, mGroupId);
            mContext.startActivity(intent);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        if (mIsCreator && !mIsDeleteMode && position != 0) {
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.jmui_cancel_btn:
                            mDialog.dismiss();
                            break;
                        case R.id.jmui_commit_btn:
                            mDialog.dismiss();
                            mLoadingDialog = DialogCreator.createLoadingDialog(mContext,
                                    mContext.getString(R.string.deleting_hint));
                            mLoadingDialog.show();
                            List<String> list = new ArrayList<String>();
                            list.add(mMemberList.get(position).data.getUserName());
                            JMessageClient.removeGroupMembers(mGroupId, list, new BasicCallback() {
                                @Override
                                public void gotResult(int status, String desc) {
                                    mLoadingDialog.dismiss();
                                    if (status == 0) {
                                        mContext.refreshMemberList();
                                    } else {
                                        HandleResponseCode.onHandle(mContext, status, false);
                                    }
                                }
                            });
                            break;

                    }
                }
            };
            mDialog = DialogCreator.createDeleteMemberDialog(mContext, listener, true);
            mDialog.getWindow().setLayout((int) (0.8 * mWidth), WindowManager.LayoutParams.WRAP_CONTENT);
            mDialog.show();
        }
        return true;
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
