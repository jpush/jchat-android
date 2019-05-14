package jiguang.chat.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.io.File;
import java.util.List;

import cn.jpush.im.android.api.model.UserInfo;
import jiguang.chat.R;
import jiguang.chat.utils.BitmapLoader;

public class ChatRoomKeeperGridAdapter extends BaseAdapter {
    private Context context;
    private List<UserInfo> keeperList;
    private int avatarSize;

    public ChatRoomKeeperGridAdapter(Context context, List<UserInfo> keeperList, int avatarSize) {
        this.context = context;
        this.keeperList = keeperList;
        this.avatarSize = avatarSize;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_chatroom_avatar, null);
        }
        UserInfo userInfo = keeperList.get(position);
        if (userInfo != null) {
            File file = userInfo.getAvatarFile();
            if (file != null && file.exists()) {
                Bitmap bitmap = BitmapLoader.getBitmapFromFile(file.getAbsolutePath(), avatarSize, avatarSize);
                ((ImageView) convertView.findViewById(R.id.grid_avatar)).setImageBitmap(bitmap);
            }
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
