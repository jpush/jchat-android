package io.jchat.android.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.R;
import io.jchat.android.chatting.CircleImageView;
import io.jchat.android.chatting.utils.BitmapLoader;
import io.jchat.android.chatting.utils.HandleResponseCode;
import io.jchat.android.database.FriendEntry;
import io.jchat.android.tools.NativeImageLoader;

import java.io.File;
import java.util.List;



/**
 * Created by Ken on 2015/2/26.
 */
public class ContactsListAdapter extends BaseAdapter{

    private List<FriendEntry> mList;
    private Context mContext;
    private LayoutInflater mInflater;
    private float mDensity;

    public ContactsListAdapter(Context context, List<FriendEntry> list){
        this.mContext = context;
        this.mList = list;
        this.mInflater = LayoutInflater.from(context);
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        mDensity = dm.density;
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
        final ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_view_contact_item, null);
            holder = new ViewHolder();
            holder.alpha = (TextView) convertView.findViewById(R.id.alpha);
            holder.headIcon = (CircleImageView) convertView.findViewById(R.id.imageView);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final FriendEntry friend = mList.get(position);
//        int section = getSectionForPosition(position);
        if (!TextUtils.isEmpty(friend.avatar)) {
            Bitmap bitmap = NativeImageLoader.getInstance().getBitmapFromMemCache(friend.username);
            if (null != bitmap) {
                holder.headIcon.setImageBitmap(bitmap);
            } else {
                bitmap = BitmapLoader.getBitmapFromFile(friend.avatar, mDensity);
                holder.headIcon.setImageBitmap(bitmap);
                NativeImageLoader.getInstance().updateBitmapFromCache(friend.username, bitmap);
            }
        } else {
            holder.headIcon.setImageResource(R.drawable.jmui_head_icon);
        }

        holder.name.setText(friend.displayName);


        return convertView;
    }

//    private int getSectionForPosition(int position) {
//        return mList.get(position).getSort
//    }


    private static class ViewHolder {
        TextView alpha;
        CircleImageView headIcon;
        TextView name;
    }
}
