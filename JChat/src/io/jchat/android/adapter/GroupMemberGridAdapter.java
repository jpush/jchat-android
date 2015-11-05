package io.jchat.android.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.R;
import io.jchat.android.tools.BitmapLoader;
import io.jchat.android.tools.HandleResponseCode;
import io.jchat.android.view.CircleImageView;

public class GroupMemberGridAdapter extends BaseAdapter {

    private static final String TAG = "GroupMemberGridAdapter";

    private LayoutInflater mInflater;
    //群成员列表
    private List<UserInfo> mMemberList = new ArrayList<UserInfo>();
    private boolean mIsCreator = false;
    private boolean mIsShowDelete;
    //群成员个数
    private int mCurrentNum;
    //记录空白项的数组
    private int[] mRestArray = new int[]{2, 1, 0, 3};
    //用群成员项数余4得到，作为下标查找mRestArray，得到空白项
    private int mRestNum;
    private boolean mIsGroup;
    private String mTargetId;
    private Context mContext;
    private int mAvatarSize;

    //群聊
    public GroupMemberGridAdapter(Context context, List<UserInfo> memberList, boolean isCreator,
                                  int size) {
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
        mIsGroup = true;
        this.mMemberList = memberList;
        mCurrentNum = mMemberList.size();
        this.mIsCreator = isCreator;
        this.mAvatarSize = size;
        mIsShowDelete = false;
        initBlankItem();
    }

    //单聊
    public GroupMemberGridAdapter(Context context, String targetId) {
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
        this.mTargetId = targetId;
    }

    public void initBlankItem() {
        mCurrentNum = mMemberList.size();
        mRestNum = mRestArray[mCurrentNum % 4];
    }

    public void refreshMemberList(long groupId){
        Conversation conv = JMessageClient.getGroupConversation(groupId);
        GroupInfo groupInfo = (GroupInfo)conv.getTargetInfo();
        mMemberList = groupInfo.getGroupMembers();
        mCurrentNum = mMemberList.size();
        mRestNum = mRestArray[mCurrentNum % 4];
        notifyDataSetChanged();
    }

    public void setIsShowDelete(boolean isShowDelete) {
        this.mIsShowDelete = isShowDelete;
        notifyDataSetChanged();
    }

    public void setIsShowDelete(boolean isShowDelete, int restNum) {
        this.mIsShowDelete = isShowDelete;
        mRestNum = restNum;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        //如果是普通成员，并且群组成员余4等于3，特殊处理，隐藏下面一栏空白
        if (mCurrentNum % 4 == 3 && !mIsCreator)
            return mCurrentNum + 1;
        else return mCurrentNum + mRestNum + 2;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ItemViewTag viewTag;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.group_grid_view_item, null);
            viewTag = new ItemViewTag((CircleImageView) convertView.findViewById(R.id.grid_avatar),
                    (TextView) convertView.findViewById(R.id.grid_name),
                    (ImageView) convertView.findViewById(R.id.grid_delete_icon));
            convertView.setTag(viewTag);
        } else {
            viewTag = (ItemViewTag) convertView.getTag();
        }
        //群聊
        if (mIsGroup) {
            //群成员
            if (position < mMemberList.size()) {
                UserInfo userInfo = mMemberList.get(position);
                viewTag.icon.setVisibility(View.VISIBLE);
                viewTag.name.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(userInfo.getAvatar())){
                    File file = userInfo.getAvatarFile();
                    if (file != null && file.isFile()) {
                        Bitmap bitmap = BitmapLoader.getBitmapFromFile(file.getAbsolutePath(),
                                mAvatarSize, mAvatarSize);
                        viewTag.icon.setImageBitmap(bitmap);
                    }else {
                        userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                            @Override
                            public void gotResult(int status, String desc, Bitmap bitmap) {
                                if (status == 0) {
                                    viewTag.icon.setImageBitmap(bitmap);
                                }else {
                                    viewTag.icon.setImageResource(R.drawable.head_icon);
                                    HandleResponseCode.onHandle(mContext, status, false);
                                }
                            }
                        });
                    }
                }else {
                    viewTag.icon.setImageResource(R.drawable.head_icon);
                }

                if (TextUtils.isEmpty(userInfo.getNickname())) {
                    viewTag.name.setText(userInfo.getUserName());
                } else {
                    viewTag.name.setText(userInfo.getNickname());
                }
            }
            //是Delete状态
            if (mIsShowDelete) {
                if (position < mCurrentNum) {
                    UserInfo userInfo = mMemberList.get(position);
                    //群主不能删除自己
                    if (userInfo.getUserName().equals(JMessageClient.getMyInfo().getUserName()))
                        viewTag.deleteIcon.setVisibility(View.GONE);
                    else viewTag.deleteIcon.setVisibility(View.VISIBLE);

                } else {
                    viewTag.deleteIcon.setVisibility(View.INVISIBLE);
                    viewTag.icon.setVisibility(View.INVISIBLE);
                    viewTag.name.setVisibility(View.INVISIBLE);
                }
                //非Delete状态
            } else {
                viewTag.deleteIcon.setVisibility(View.INVISIBLE);
                if (position < mCurrentNum) {
                    viewTag.icon.setVisibility(View.VISIBLE);
                    viewTag.name.setVisibility(View.VISIBLE);
                } else if (position == mCurrentNum) {
                    viewTag.icon.setImageResource(R.drawable.chat_detail_add);
                    viewTag.icon.setVisibility(View.VISIBLE);
                    viewTag.name.setVisibility(View.INVISIBLE);

                    //设置删除群成员按钮
                } else if (position == mCurrentNum + 1) {
                    if (mIsCreator && mCurrentNum > 1) {
                        viewTag.icon.setImageResource(R.drawable.chat_detail_del);
                        viewTag.icon.setVisibility(View.VISIBLE);
                        viewTag.name.setVisibility(View.INVISIBLE);
                    } else {
                        viewTag.icon.setVisibility(View.GONE);
                        viewTag.name.setVisibility(View.GONE);
                    }
                    //空白项
                } else {
                    viewTag.icon.setVisibility(View.INVISIBLE);
                    viewTag.name.setVisibility(View.INVISIBLE);
                }
            }
        } else {
            if (position == 0) {
                Conversation conv = JMessageClient.getSingleConversation(mTargetId);
                UserInfo userInfo = (UserInfo) conv.getTargetInfo();
                if (!TextUtils.isEmpty(userInfo.getAvatar())) {
                    userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                        @Override
                        public void gotResult(int status, String desc, Bitmap bitmap) {
                            if (status == 0) {
                                Log.d(TAG, "Get small avatar success");
                                viewTag.icon.setImageBitmap(bitmap);
                            } else {
                                HandleResponseCode.onHandle(mContext, status, false);
                            }
                        }
                    });
                }
                if (TextUtils.isEmpty(userInfo.getNickname())){
                    viewTag.name.setText(userInfo.getUserName());
                }else {
                    viewTag.name.setText(userInfo.getNickname());
                }
                viewTag.icon.setVisibility(View.VISIBLE);
                viewTag.name.setVisibility(View.VISIBLE);
            } else {
                viewTag.icon.setImageResource(R.drawable.chat_detail_add);
                viewTag.icon.setVisibility(View.VISIBLE);
                viewTag.name.setVisibility(View.INVISIBLE);
            }

        }

        return convertView;
    }

    public void setCreator(boolean isCreator) {
        mIsCreator = isCreator;
        notifyDataSetChanged();
    }

    class ItemViewTag {

        protected CircleImageView icon;
        protected ImageView deleteIcon;
        protected TextView name;

        public ItemViewTag(CircleImageView icon, TextView name, ImageView deleteIcon) {
            this.icon = icon;
            this.deleteIcon = deleteIcon;
            this.name = name;
        }
    }
}
