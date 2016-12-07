package io.jchat.android.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.R;
import io.jchat.android.chatting.CircleImageView;
import io.jchat.android.tools.NativeImageLoader;


public class SearchFriendListAdapter extends BaseAdapter {

    private Context mContext;
    private List<UserInfo> users = new ArrayList<UserInfo>();
    private LayoutInflater mInflater;

    public SearchFriendListAdapter(Context context, List<UserInfo> users) {
        this.mContext = context;
        this.users = users;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int i) {
        return users.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        final ViewHolder holder;
        if (null == view) {
            holder = new ViewHolder();
            view = mInflater.inflate(R.layout.item_search_result, null);
            holder.headIcon = (CircleImageView) view.findViewById(R.id.item_head_icon);
            holder.name = (TextView) view.findViewById(R.id.item_name);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        final UserInfo userInfo = users.get(position);
        Bitmap bitmap = NativeImageLoader.getInstance().getBitmapFromMemCache(userInfo.getUserName());
        if (null != bitmap) {
            holder.headIcon.setImageBitmap(bitmap);
        } else {
            userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                @Override
                public void gotResult(int status, String desc, Bitmap bitmap) {
                    if (status == 0) {
                        holder.headIcon.setImageBitmap(bitmap);
                        NativeImageLoader.getInstance().updateBitmapFromCache(userInfo.getUserName(), bitmap);
                    } else {
                        holder.headIcon.setImageResource(R.drawable.jmui_head_icon);
                    }
                }
            });
        }

        String nickname = userInfo.getNickname();
        if (null != nickname && !TextUtils.isEmpty(nickname)) {
            holder.name.setText(nickname);
        } else  {
            holder.name.setText(userInfo.getUserName());
        }

        return view;
    }

    private static class ViewHolder {
        CircleImageView headIcon;
        TextView name;

    }
}
