package io.jchat.android.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import java.util.ArrayList;
import java.util.List;
import io.jchat.android.R;
import io.jchat.android.entity.UserLetterBean;
import io.jchat.android.view.CircleImageView;

public class StickyListAdapter extends BaseAdapter implements StickyListHeadersAdapter, SectionIndexer{

    private Context mContext;
    private boolean mIsSelectMode;
    private LayoutInflater mInflater;
    private List<UserLetterBean> mData;
    private int[] mSectionIndices;
    private Character[] mSectionLetters;
    private SparseBooleanArray mSelectMap = new SparseBooleanArray();
    private TextView mSelectedNum;

    public StickyListAdapter(Context context, List<UserLetterBean> list, boolean isSelectMode){
        this.mContext = context;
        this.mData = list;
        this.mIsSelectMode = isSelectMode;
        this.mInflater = LayoutInflater.from(context);
        Activity activity = (Activity) mContext;
        mSelectedNum = (TextView) activity.findViewById(R.id.selected_num);
        mSectionIndices = getSectionIndices();
        mSectionLetters = getSectionLetters();
    }

    private int[] getSectionIndices() {
        ArrayList<Integer> sectionIndices = new ArrayList<Integer>();
        char lastFirstChar = mData.get(0).getLetter().charAt(0);
        sectionIndices.add(0);
        for (int i = 1; i < mData.size(); i++) {
            if (mData.get(i).getLetter().charAt(0) != lastFirstChar) {
                lastFirstChar = mData.get(i).getLetter().charAt(0);
                sectionIndices.add(i);
            }
        }
        int[] sections = new int[sectionIndices.size()];
        for (int i = 0; i < sectionIndices.size(); i++) {
            sections[i] = sectionIndices.get(i);
        }
        return sections;
    }

    private Character[] getSectionLetters() {
        Character[] letters = new Character[mSectionIndices.length];
        for (int i = 0; i < mSectionIndices.length; i++) {
            letters[i] = mData.get(mSectionIndices[i]).getLetter().charAt(0);
        }
        return letters;
    }

    public void updateListView(List<UserLetterBean> list){
        this.mData = list;
        notifyDataSetChanged();
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        UserLetterBean model = mData.get(position);
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = mInflater.inflate(R.layout.header, parent, false);
            if (Build.VERSION.SDK_INT >= 11){
                convertView.setAlpha(0.85f);
            }
            holder.text = (TextView) convertView.findViewById(R.id.section_tv);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        //根据position获取分类的首字母的Char ascii值
        int section = getSectionForPosition(position);
        holder.text.setText(model.getLetter());
        //如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
        if (position == getPositionForSection(section)){
            holder.text.setText(model.getLetter());
        }

//         set header text as first char in name
//        CharSequence headerChar = model.getNickname().subSequence(0, 1);
//        holder.text.setText(headerChar);

        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        return mData.get(position).getLetter().charAt(0);
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
            convertView = mInflater.inflate(R.layout.select_friend_item, parent, false);
            holder.itemLl = (LinearLayout) convertView.findViewById(R.id.select_friend_item_ll);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.selected_cb);
            holder.avatar = (CircleImageView) convertView.findViewById(R.id.avatar_iv);
            holder.displayName = (TextView) convertView.findViewById(R.id.display_name_tv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.itemLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.checkBox.isChecked()) {
                    holder.checkBox.setChecked(false);
                    mSelectMap.delete(position);
                } else {
                    holder.checkBox.setChecked(true);
                    Toast.makeText(mContext, "Position " + position + " checked!", Toast.LENGTH_SHORT).show();
                    mSelectMap.put(position, true);
                    addAnimation(holder.checkBox);
                }
                if (mSelectMap.size() > 0){
                    mSelectedNum.setVisibility(View.VISIBLE);
                    mSelectedNum.setText(String.format(mContext.getString(R.string.selected_num),
                            mSelectMap.size() + "/" + mData.size()));
                }else mSelectedNum.setVisibility(View.GONE);
            }
        });

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.checkBox.isChecked()){
                    Toast.makeText(mContext, "Position " + position + " checked!", Toast.LENGTH_SHORT).show();
                    mSelectMap.put(position, true);
                    addAnimation(holder.checkBox);
                }else {
                    mSelectMap.delete(position);
                }
                if (mSelectMap.size() > 0){
                    mSelectedNum.setVisibility(View.VISIBLE);
                    mSelectedNum.setText(String.format(mContext.getString(R.string.selected_num),
                            mSelectMap.size() + "/" + mData.size()));
                }else mSelectedNum.setVisibility(View.GONE);
            }
        });

        holder.checkBox.setChecked(mSelectMap.get(position));
        holder.displayName.setText(mData.get(position).getNickname());

        return convertView;
    }

    @Override
    public Object[] getSections() {
        return mSectionLetters;
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        if (mSectionIndices.length == 0) {
            return 0;
        }

        if (sectionIndex >= mSectionIndices.length) {
            sectionIndex = mSectionIndices.length - 1;
        } else if (sectionIndex < 0) {
            sectionIndex = 0;
        }
        return mSectionIndices[sectionIndex];
    }

    public int getSectionForLetter(String letter){
        for (int i = 0; i < mSectionIndices.length; i++){
            if (mSectionLetters[i] == letter.charAt(0)){
                return mSectionIndices[i];
            }
        }
        return -1;
    }

    @Override
    public int getSectionForPosition(int position) {
        for (int i = 0; i < mSectionIndices.length; i++) {
            if (position < mSectionIndices[i]) {
                return i - 1;
            }
        }
        return mSectionIndices.length - 1;
    }

    private void addAnimation(View view) {
        float[] vaules = new float[]{0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f, 1.1f, 1.2f, 1.3f, 1.25f, 1.2f, 1.15f, 1.1f, 1.0f};
        AnimatorSet set = new AnimatorSet();
        set.playTogether(ObjectAnimator.ofFloat(view, "scaleX", vaules),
                ObjectAnimator.ofFloat(view, "scaleY", vaules));
        set.setDuration(150);
        set.start();
    }

    public SparseBooleanArray getSelectedMap(){
        return mSelectMap;
    }

    class HeaderViewHolder {
        TextView text;
    }

    class ViewHolder {
        LinearLayout itemLl;
        CheckBox checkBox;
        TextView displayName;
        CircleImageView avatar;
    }
}
