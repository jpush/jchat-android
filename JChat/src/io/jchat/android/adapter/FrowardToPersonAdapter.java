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

import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.R;
import io.jchat.android.chatting.utils.HandleResponseCode;
import io.jchat.android.database.FriendEntry;
import io.jchat.android.tools.NativeImageLoader;
import io.jchat.android.tools.ViewHolder;

/**
 * Created by ${chenyn} on 2017/6/3.
 */

public class FrowardToPersonAdapter extends BaseAdapter {
    private Context mContext;
    List<FriendEntry> mFriend;


    public FrowardToPersonAdapter(Context context, List<FriendEntry> friend) {
        this.mContext = context;
        this.mFriend = friend;
    }

    @Override
    public int getCount() {
        return mFriend.size();
    }

    @Override
    public Object getItem(int position) {
        return mFriend.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_contact, null);
        }

        final ImageView headIcon = ViewHolder.get(convertView, R.id.head_icon_iv);
        final TextView name = ViewHolder.get(convertView, R.id.name);
        FriendEntry friendEntry = mFriend.get(position);
        JMessageClient.getUserInfo(friendEntry.username, new GetUserInfoCallback() {
            @Override
            public void gotResult(int i, String s, final UserInfo userInfo) {
                if (i == 0) {
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
                                        HandleResponseCode.onHandle(mContext, status, false);
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
                }
            }
        });


        return convertView;
    }


}
