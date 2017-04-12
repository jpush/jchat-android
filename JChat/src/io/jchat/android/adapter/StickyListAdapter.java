package io.jchat.android.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.R;
import io.jchat.android.activity.FriendInfoActivity;
import io.jchat.android.application.JChatDemoApplication;
import io.jchat.android.chatting.CircleImageView;
import io.jchat.android.chatting.utils.BitmapLoader;
import io.jchat.android.database.FriendEntry;
import io.jchat.android.tools.NativeImageLoader;

public class StickyListAdapter extends BaseAdapter implements StickyListHeadersAdapter, SectionIndexer {

    private Context mContext;
    private boolean mIsSelectMode;
    private LayoutInflater mInflater;
    private List<FriendEntry> mData;
    private int[] mSectionIndices;
    private String[] mSectionLetters;
    private SparseBooleanArray mSelectMap = new SparseBooleanArray();
    private TextView mSelectedNum;
    private float mDensity;

    public StickyListAdapter(Context context, List<FriendEntry> list, boolean isSelectMode) {
        this.mContext = context;
        this.mData = list;
        this.mIsSelectMode = isSelectMode;
        this.mInflater = LayoutInflater.from(context);
        Activity activity = (Activity) mContext;
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        mDensity = dm.density;
        mSelectedNum = (TextView) activity.findViewById(R.id.selected_num);
        mSectionIndices = getSectionIndices();
        mSectionLetters = getSectionLetters();
    }

    private int[] getSectionIndices() {
        ArrayList<Integer> sectionIndices = new ArrayList<Integer>();
        if (mData.size() > 0) {
            char lastFirstChar = mData.get(0).letter.charAt(0);
            sectionIndices.add(0);
            for (int i = 1; i < mData.size(); i++) {
                if (mData.get(i).letter.charAt(0) != lastFirstChar) {
                    lastFirstChar = mData.get(i).letter.charAt(0);
                    sectionIndices.add(i);
                }
            }
            int[] sections = new int[sectionIndices.size()];
            for (int i = 0; i < sectionIndices.size(); i++) {
                sections[i] = sectionIndices.get(i);
            }
            return sections;
        }
        return null;
    }

    private String[] getSectionLetters() {
        if (null != mSectionIndices) {
            String[] letters = new String[mSectionIndices.length];
            for (int i = 0; i < mSectionIndices.length; i++) {
                letters[i] = mData.get(mSectionIndices[i]).letter;
            }
            return letters;
        }
        return null;
    }

    public void updateListView(List<FriendEntry> list) {
        this.mData = list;
        notifyDataSetChanged();
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        FriendEntry model = mData.get(position);
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = mInflater.inflate(R.layout.header, parent, false);
            if (Build.VERSION.SDK_INT >= 11) {
                convertView.setAlpha(0.85f);
            }
            holder.text = (TextView) convertView.findViewById(R.id.section_tv);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        //根据position获取分类的首字母的Char ascii值
        int section = getSectionForPosition(position);
        holder.text.setText(model.letter);
        //如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
        if (position == getPositionForSection(section)) {
            holder.text.setText(model.letter);
        }
        //设置通讯录名片之间的字母背景
        convertView.setBackgroundColor(Color.parseColor("#FFE9E6E6"));

//         set header text as first char in name
//        CharSequence headerChar = model.getNickname().subSequence(0, 1);
//        holder.text.setText(headerChar);

        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        return mData.get(position).letter.charAt(0);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_select_friend, parent, false);
            holder.itemLl = (LinearLayout) convertView.findViewById(R.id.select_friend_item_ll);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.selected_cb);
            holder.avatar = (CircleImageView) convertView.findViewById(R.id.jmui_avatar_iv);
            holder.displayName = (TextView) convertView.findViewById(R.id.jmui_display_name_tv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final FriendEntry friend = mData.get(position);
        if (mIsSelectMode) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.itemLl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.checkBox.isChecked()) {
                        holder.checkBox.setChecked(false);
                        mSelectMap.delete(position);
                    } else {
                        holder.checkBox.setChecked(true);
                        mSelectMap.put(position, true);
                        addAnimation(holder.checkBox);
                    }
                    if (mSelectMap.size() > 0) {
                        mSelectedNum.setVisibility(View.VISIBLE);
                        mSelectedNum.setText(String.format(mContext.getString(R.string.selected_num),
                                mSelectMap.size() + "/" + mData.size()));
                    } else {
                        mSelectedNum.setVisibility(View.GONE);
                    }
                }
            });

            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.checkBox.isChecked()) {
                        mSelectMap.put(position, true);
                        addAnimation(holder.checkBox);
                    } else {
                        mSelectMap.delete(position);
                    }
                    if (mSelectMap.size() > 0) {
                        mSelectedNum.setVisibility(View.VISIBLE);
                        mSelectedNum.setText(String.format(mContext.getString(R.string.selected_num),
                                mSelectMap.size() + "/" + mData.size()));
                    } else {
                        mSelectedNum.setVisibility(View.GONE);
                    }
                }
            });

            holder.checkBox.setChecked(mSelectMap.get(position));
        } else {
            holder.checkBox.setVisibility(View.GONE);
            holder.itemLl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, FriendInfoActivity.class);
                    intent.putExtra("fromContact", true);
                    intent.putExtra(JChatDemoApplication.TARGET_ID, friend.username);
                    intent.putExtra(JChatDemoApplication.TARGET_APP_KEY, friend.appKey);
                    mContext.startActivity(intent);
                }
            });
        }

        holder.displayName.setText(friend.displayName);
        if (null != friend.avatar && !TextUtils.isEmpty(friend.avatar)) {
            Bitmap bitmap = NativeImageLoader.getInstance().getBitmapFromMemCache(friend.username);
            if (null != bitmap) {
                holder.avatar.setImageBitmap(bitmap);
            } else {
                bitmap = BitmapLoader.getBitmapFromFile(friend.avatar, mDensity);
                if (null != bitmap) {
                    holder.avatar.setImageBitmap(bitmap);
                    NativeImageLoader.getInstance().updateBitmapFromCache(friend.username, bitmap);
                } else {
                    JMessageClient.getUserInfo(friend.username, friend.appKey, new GetUserInfoCallback() {
                        @Override
                        public void gotResult(int i, String s, final UserInfo userInfo) {
                            if (i == 0) {
                                userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                                    @Override
                                    public void gotResult(int i, String s, Bitmap bitmap) {
                                        if (i == 0) {
                                            friend.avatar = userInfo.getAvatarFile().getAbsolutePath();
                                            friend.save();
                                            holder.avatar.setImageBitmap(bitmap);
                                            NativeImageLoader.getInstance().updateBitmapFromCache(friend.username, bitmap);
                                        }
                                    }
                                });
                            }
                        }
                    });
                    holder.avatar.setImageResource(R.drawable.jmui_head_icon);
                }
            }
        } else {
            holder.avatar.setImageResource(R.drawable.jmui_head_icon);
        }

        return convertView;
    }

    @Override
    public String[] getSections() {
        return mSectionLetters;
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        if (null == mSectionIndices || mSectionIndices.length == 0) {
            return 0;
        }

        if (sectionIndex >= mSectionIndices.length) {
            sectionIndex = mSectionIndices.length - 1;
        } else if (sectionIndex < 0) {
            sectionIndex = 0;
        }
        return mSectionIndices[sectionIndex];
    }

    public int getSectionForLetter(String letter) {
        if (null != mSectionIndices) {
            for (int i = 0; i < mSectionIndices.length; i++) {
                if (mSectionLetters[i].equals(letter)) {
                    return mSectionIndices[i] + 1;
                }
            }
        }
        return -1;
    }

    @Override
    public int getSectionForPosition(int position) {
        if (null != mSectionIndices) {
            for (int i = 0; i < mSectionIndices.length; i++) {
                if (position < mSectionIndices[i]) {
                    return i - 1;
                }
            }
            return mSectionIndices.length - 1;
        }
        return -1;
    }

    private void addAnimation(View view) {
        float[] vaules = new float[] {0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f, 1.1f, 1.2f, 1.3f, 1.25f, 1.2f, 1.15f, 1.1f, 1.0f};
        AnimatorSet set = new AnimatorSet();
        set.playTogether(ObjectAnimator.ofFloat(view, "scaleX", vaules),
                ObjectAnimator.ofFloat(view, "scaleY", vaules));
        set.setDuration(150);
        set.start();
    }

    public ArrayList<String> getSelectedUser() {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < mSelectMap.size(); i++) {
            list.add(mData.get(mSelectMap.keyAt(i)).username);
        }
        return list;
    }

    public ArrayList<FriendEntry> getSelectedFriends() {
        ArrayList<FriendEntry> list = new ArrayList<FriendEntry>();
        for (int i = 0; i < mSelectMap.size(); i++) {
            list.add(mData.get(mSelectMap.keyAt(i)));
        }
        return list;
    }

    private static class HeaderViewHolder {
        TextView text;
        RelativeLayout recommendRl;
        RelativeLayout groupRl;
    }

    private static class ViewHolder {
        LinearLayout itemLl;
        CheckBox checkBox;
        TextView displayName;
        CircleImageView avatar;
    }
}
