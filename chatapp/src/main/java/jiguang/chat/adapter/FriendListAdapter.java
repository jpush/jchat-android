package jiguang.chat.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.List;

import jiguang.chat.R;
import jiguang.chat.database.FriendEntry;

/**
 * Created by ${chenyn} on 2017/9/21.
 */

public class FriendListAdapter extends BaseAdapter implements StickyListHeadersAdapter, SectionIndexer {
    private List<FriendEntry> mData;
    private Context mContext;
    private int[] mSectionIndices;
    private String[] mSectionLetters;
    private LayoutInflater mInflater;

    public FriendListAdapter(Context context, List<FriendEntry> list) {
        this.mData = list;
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
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
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_contact, parent, false);
            holder.headAvatar = (ImageView) convertView.findViewById(R.id.head_icon_iv);
            holder.displayName = (TextView) convertView.findViewById(R.id.name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        FriendEntry entry = mData.get(position);
        holder.headAvatar.setImageBitmap(BitmapFactory.decodeFile(entry.avatar));
        holder.displayName.setText(entry.displayName);

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
        ImageView headAvatar;
        TextView displayName;
    }
}
