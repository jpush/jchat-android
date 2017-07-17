package jiguang.chat.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.UserInfo;
import jiguang.chat.R;
import jiguang.chat.database.FriendEntry;
import jiguang.chat.utils.NativeImageLoader;

/**
 * Created by ${chenyn} on 2017/7/16.
 */

public class ForwardMsgAdapter extends BaseAdapter implements StickyListHeadersAdapter, SectionIndexer {
    private Context mContext;
    private List<FriendEntry> mFriends;
    private int[] mSectionIndices;
    private String[] mSectionLetters;

    public ForwardMsgAdapter(Context context, List<FriendEntry> friends) {
        this.mContext = context;
        this.mFriends = friends;
        mSectionIndices = getSectionIndices();
        mSectionLetters = getSectionLetters();
    }

    private int[] getSectionIndices() {
        ArrayList<Integer> sectionIndices = new ArrayList<Integer>();
        if (mFriends.size() > 0) {
            char lastFirstChar = mFriends.get(0).letter.charAt(0);
            sectionIndices.add(0);
            for (int i = 1; i < mFriends.size(); i++) {
                if (mFriends.get(i).letter.charAt(0) != lastFirstChar) {
                    lastFirstChar = mFriends.get(i).letter.charAt(0);
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
                letters[i] = mFriends.get(mSectionIndices[i]).letter;
            }
            return letters;
        }
        return null;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        FriendEntry friendEntry = mFriends.get(position);
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.header, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.section_tv);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }
        holder.text.setText(friendEntry.letter);
        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        return mFriends.get(position).letter.charAt(0);
    }

    @Override
    public int getCount() {
        return mFriends.size();
    }

    @Override
    public Object getItem(int position) {
        return mFriends.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (null == convertView) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_contact, null);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.avatar = (ImageView) convertView.findViewById(R.id.head_icon_iv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        final FriendEntry friendEntry = mFriends.get(position);

        if (friendEntry.avatar != null && !TextUtils.isEmpty(friendEntry.avatar)) {
            Bitmap bitmap = NativeImageLoader.getInstance().getBitmapFromMemCache(friendEntry.username);
            if (bitmap != null) {
                holder.avatar.setImageBitmap(bitmap);
            } else {
                holder.avatar.setImageResource(R.drawable.jmui_head_icon);
            }
        } else {
            JMessageClient.getUserInfo(friendEntry.username, friendEntry.appKey, new GetUserInfoCallback() {
                @Override
                public void gotResult(int i, String s, final UserInfo userInfo) {
                    if (i == 0) {
                        userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                            @Override
                            public void gotResult(int i, String s, Bitmap bitmap) {
                                if (i == 0) {
                                    friendEntry.avatar = userInfo.getAvatarFile().getAbsolutePath();
                                    friendEntry.save();
                                    holder.avatar.setImageBitmap(bitmap);
                                    NativeImageLoader.getInstance().updateBitmapFromCache(friendEntry.username, bitmap);
                                }
                            }
                        });
                    }
                }
            });
            holder.avatar.setImageResource(R.drawable.jmui_head_icon);
        }

        holder.name.setText(friendEntry.displayName);

        return convertView;
    }

    @Override
    public Object[] getSections() {
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

    private static class HeaderViewHolder {
        TextView text;
    }

    private static class ViewHolder {
        TextView name;
        ImageView avatar;

    }


}
