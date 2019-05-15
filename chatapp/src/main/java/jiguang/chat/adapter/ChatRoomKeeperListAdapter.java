package jiguang.chat.adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import cn.jpush.im.android.api.ChatRoomManager;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import jiguang.chat.R;
import jiguang.chat.activity.ChatRoomKeeperActivity;
import jiguang.chat.utils.DialogCreator;

public class ChatRoomKeeperListAdapter extends BaseAdapter {
    private List<ChatRoomKeeperActivity.ItemModel> keepers;
    private Context context;
    private LayoutInflater mInflater;
    private long roomId;
    private boolean isOwner;

    public ChatRoomKeeperListAdapter(Context context, List<ChatRoomKeeperActivity.ItemModel> keepers, long roomId, boolean isOwner) {
        this.context = context;
        this.keepers = keepers;
        this.mInflater = LayoutInflater.from(context);
        this.roomId = roomId;
        this.isOwner = isOwner;
    }

    @Override
    public Object getItem(int position) {
        return keepers.get(position);
    }

    @Override
    public int getCount() {
        return keepers.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_chat_room_keeper, null);
            holder.iv_keeperAvatar = convertView.findViewById(R.id.icon_iv);
            holder.tv_keeperName = convertView.findViewById(R.id.name);
            holder.bt_remove = convertView.findViewById(R.id.bt_removeKeeper);
            if (!isOwner) {
                holder.bt_remove.setVisibility(View.GONE);
            } else {
                holder.bt_remove.setOnClickListener((v)-> {
                    Dialog dialog = DialogCreator.createLoadingDialog(context, "移出中");
                    dialog.show();
                    ChatRoomManager.delChatRoomAdmin(roomId, Collections.singletonList(keepers.get(position).data), new BasicCallback() {
                        @Override
                        public void gotResult(int i, String s) {
                            dialog.dismiss();
                            if (i == 0) {
                                keepers.remove(position);
                                notifyDataSetChanged();
                            }
                        }
                    });
                });
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        UserInfo userInfo = keepers.get(position).data;
        userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
            @Override
            public void gotResult(int i, String s, Bitmap bitmap) {
                if (i == 0) {
                    holder.iv_keeperAvatar.setImageBitmap(bitmap);
                }
            }
        });
        holder.tv_keeperName.setText(keepers.get(position).highlight);
        return convertView;
    }

    class ViewHolder {
        ImageView iv_keeperAvatar;
        TextView tv_keeperName;
        Button bt_remove;
    }
}
