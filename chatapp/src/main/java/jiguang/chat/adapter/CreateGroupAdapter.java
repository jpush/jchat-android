package jiguang.chat.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.UserInfo;
import jiguang.chat.R;

/**
 * Created by ${chenyn} on 2017/5/3.
 */


public class CreateGroupAdapter extends BaseAdapter {

    private Context context;

    private List<String> selectItem;


    public CreateGroupAdapter(Context context) {
        this.context = context;
        this.selectItem = new ArrayList<>();
        selectItem.add(null);
    }

    @Override
    public int getCount() {
        return selectItem.size();
    }

    @Override
    public Object getItem(int position) {
        return selectItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ImageView imageView;
        if (convertView == null) {
            GalleryItemViewHolder holder = new GalleryItemViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.create_group_select, null);
            imageView = (ImageView) convertView.findViewById(R.id.contact_select_area_image);
            holder.imageView = imageView;
            convertView.setTag(holder);
        } else {
            GalleryItemViewHolder holder = (GalleryItemViewHolder) convertView.getTag();
            imageView = holder.imageView;
        }

        String item = selectItem.get(position);
        if (item == null) {
//            imageView.setBackgroundResource(R.drawable.nim_contact_select_dot_avatar);
            imageView.setImageDrawable(null);
        } else {
            JMessageClient.getUserInfo(item, new GetUserInfoCallback() {
                @Override
                public void gotResult(int responseCode, String responseMessage, UserInfo info) {
                    if (responseCode == 0) {
                        info.getAvatarBitmap(new GetAvatarBitmapCallback() {
                            @Override
                            public void gotResult(int responseCode, String responseMessage, Bitmap avatarBitmap) {
                                if (responseCode == 0) {
                                    imageView.setImageBitmap(avatarBitmap);
                                }else {
                                    imageView.setImageResource(R.drawable.jmui_head_icon);
                                }
                            }
                        });

                    }
                }
            });
        }

        return convertView;
    }

    public void setContact(ArrayList<String> contact) {
        this.selectItem = contact;
    }


    private class GalleryItemViewHolder {
        ImageView imageView;
    }
}
