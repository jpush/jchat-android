package jiguang.chat.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.List;

import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.model.UserInfo;
import jiguang.chat.R;

public class ChatRoomKeeperGridAdapter extends BaseAdapter {
    private Context context;
    private List<UserInfo> keeperList;

    public ChatRoomKeeperGridAdapter(Context context, List<UserInfo> keeperList) {
        this.context = context;
        this.keeperList = keeperList;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView avatar;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_chatroom_avatar, null);
        }
        avatar = ((ImageView) convertView.findViewById(R.id.grid_avatar));
        UserInfo userInfo = keeperList.get(position);
        if (userInfo != null) {
            userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                @Override
                public void gotResult(int i, String s, Bitmap bitmap) {
                    if (i == 0) {
                        avatar.setImageBitmap(bitmap);
                    }
                }
            });
        }
        return convertView;
    }

    @Override
    public Object getItem(int position) {
        return keeperList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return keeperList != null ? keeperList.size() : 0;
    }
}
