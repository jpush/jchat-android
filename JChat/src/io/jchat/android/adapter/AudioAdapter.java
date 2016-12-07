package io.jchat.android.adapter;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.ArrayList;
import java.util.List;

import io.jchat.android.R;
import io.jchat.android.entity.FileItem;


public class AudioAdapter extends BaseAdapter {

    private List<FileItem> mList;
    private Context mContext;
    private LayoutInflater mInflater;
    private SparseBooleanArray mSelectMap = new SparseBooleanArray();
    private List<String> mSelectedList = new ArrayList<String>();

    public AudioAdapter(Context context, List<FileItem> list) {
        this.mContext = context;
        this.mList = list;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        final ViewHolder holder;
        final FileItem item = mList.get(position);
        if (null == convertView) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_audio, null);
            holder.itemLl = (LinearLayout) convertView.findViewById(R.id.audio_item_ll);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.audio_cb);
            holder.icon = (ImageView) convertView.findViewById(R.id.audio_iv);
            holder.title = (TextView) convertView.findViewById(R.id.audio_title);
            holder.size = (TextView) convertView.findViewById(R.id.audio_size);
            holder.date = (TextView) convertView.findViewById(R.id.audio_date);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        holder.itemLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.checkBox.isChecked()) {
                    holder.checkBox.setChecked(false);
                    mSelectMap.delete(position);
                } else {
                    holder.checkBox.setChecked(true);
                    mSelectMap.put(position, true);
                    addAnimation(holder.checkBox);
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
            }
        });

        holder.checkBox.setChecked(mSelectMap.get(position));
        holder.title.setText(item.getFileName());
        holder.size.setText(item.getFileSize());
        holder.date.setText(item.getDate());

        return convertView;
    }

    private void addAnimation(View view) {
        float[] vaules = new float[]{0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f, 1.1f, 1.2f, 1.3f, 1.25f, 1.2f, 1.15f, 1.1f, 1.0f};
        AnimatorSet set = new AnimatorSet();
        set.playTogether(ObjectAnimator.ofFloat(view, "scaleX", vaules),
                ObjectAnimator.ofFloat(view, "scaleY", vaules));
        set.setDuration(150);
        set.start();
    }

    private class ViewHolder {
        LinearLayout itemLl;
        CheckBox checkBox;
        ImageView icon;
        TextView title;
        TextView size;
        TextView date;
    }
}
