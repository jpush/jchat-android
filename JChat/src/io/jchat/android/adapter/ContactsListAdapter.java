package io.jchat.android.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.R;
import io.jchat.android.chatting.utils.HandleResponseCode;

import java.util.List;



/**
 * Created by Ken on 2015/2/26.
 */
public class ContactsListAdapter extends BaseAdapter{

    private List<UserInfo> mList;
    private Context mContext;
    private LayoutInflater mInflater;

    public ContactsListAdapter(Context context, List<UserInfo> list){
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
        final ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_view_contact_item, null);
            holder = new ViewHolder();
            holder.alpha = (TextView) convertView.findViewById(R.id.alpha);
            holder.headIcon = (ImageView) convertView.findViewById(R.id.imageView);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        UserInfo userInfo = mList.get(position);
        if (userInfo != null && !TextUtils.isEmpty(userInfo.getAvatar())) {
            userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                @Override
                public void gotResult(int status, String desc, Bitmap bitmap) {
                    if (status == 0) {
                        holder.headIcon.setImageBitmap(bitmap);
                    } else {
                        holder.headIcon.setImageResource(R.drawable.jmui_head_icon);
                        HandleResponseCode.onHandle(mContext, status, false);
                    }
                }
            });
        }
        String noteName = userInfo.getNotename();
        String nickName = userInfo.getNickname();
        if (null != noteName && !TextUtils.isEmpty(noteName)) {
            holder.name.setText(userInfo.getNoteText());
        } else if (null != nickName && !TextUtils.isEmpty(nickName)) {
            holder.name.setText(nickName);
        } else {
            holder.name.setText(userInfo.getUserName());
        }



        return null;
    }

    private static class ViewHolder {
        TextView alpha;
        ImageView headIcon;
        TextView name;
    }
}
