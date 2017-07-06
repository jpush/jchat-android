package jiguang.chat.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.model.UserInfo;
import jiguang.chat.R;
import jiguang.chat.utils.NativeImageLoader;
import jiguang.chat.utils.ViewHolder;

public class AtMemberAdapter extends BaseAdapter {

    private Context mContext;
    private List<UserInfo> mList = new ArrayList<UserInfo>();

    public AtMemberAdapter(Context context, List<UserInfo> list) {
        this.mContext = context;
        this.mList = list;
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
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        if (null == convertView) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_contact, null);
        }
        final ImageView headIcon = ViewHolder.get(convertView, R.id.head_icon_iv);
        TextView name = ViewHolder.get(convertView, R.id.name);

        final UserInfo userInfo = mList.get(position);
        if (!TextUtils.isEmpty(userInfo.getAvatar())) {
            Bitmap bitmap = NativeImageLoader.getInstance().getBitmapFromMemCache(userInfo.getUserName());
            if (null == bitmap) {
                userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                    @Override
                    public void gotResult(int status, String desc, Bitmap bitmap) {
                        if (status == 0) {
                            headIcon.setImageBitmap(bitmap);
                            NativeImageLoader.getInstance().updateBitmapFromCache(userInfo.getUserName(),
                                    bitmap);
                        } else {
                            headIcon.setImageResource(R.drawable.jmui_head_icon);
                        }
                    }
                });
            } else {
                headIcon.setImageBitmap(bitmap);
            }

        }

        String displayName = userInfo.getNotename();
        if (TextUtils.isEmpty(displayName)) {
            displayName = userInfo.getNickname();
            if (TextUtils.isEmpty(displayName)) {
                displayName = userInfo.getUserName();
            }
        }

        name.setText(displayName);

        return convertView;
    }
}
