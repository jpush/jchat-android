package io.jchat.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import io.jchat.android.R;

import java.util.List;



/**
 * Created by Ken on 2015/2/26.
 */
public class ContactsListAdapter extends BaseAdapter{

    private List<String> mList;
    private Context mContext;
    private LayoutInflater mInflater;

    public ContactsListAdapter(Context context, List<String> list){
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
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_view_contact_item, null);
            holder = new ViewHolder();
            holder.alpha = (TextView) convertView.findViewById(R.id.alpha);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        return null;
    }

    private static class ViewHolder {
        TextView alpha;
        ImageView headIcon;
        TextView name;
    }
}
