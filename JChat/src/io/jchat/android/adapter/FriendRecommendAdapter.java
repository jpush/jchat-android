package io.jchat.android.adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.ContactManager;
import cn.jpush.im.android.eventbus.EventBus;
import cn.jpush.im.api.BasicCallback;
import io.jchat.android.R;
import io.jchat.android.chatting.CircleImageView;
import io.jchat.android.chatting.utils.BitmapLoader;
import io.jchat.android.chatting.utils.DialogCreator;
import io.jchat.android.chatting.utils.HandleResponseCode;
import io.jchat.android.chatting.utils.SharePreferenceManager;
import io.jchat.android.database.FriendRecommendEntry;
import io.jchat.android.entity.Event;
import io.jchat.android.entity.FriendInvitation;
import io.jchat.android.tools.NativeImageLoader;


public class FriendRecommendAdapter extends BaseAdapter {

    private Context mContext;
    private List<FriendRecommendEntry> mList = new ArrayList<FriendRecommendEntry>();
    private LayoutInflater mInflater;
    private float mDensity;

    public FriendRecommendAdapter(Context context, List<FriendRecommendEntry> list, float density) {
        this.mContext = context;
        this.mList = list;
        mInflater = LayoutInflater.from(mContext);
        mDensity = density;
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
    public View getView(int position, View view, ViewGroup viewGroup) {
        final ViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.friend_recomend_list_item, null);
            holder = new ViewHolder();
            holder.headIcon = (CircleImageView) view.findViewById(R.id.item_head_icon);
            holder.name = (TextView) view.findViewById(R.id.item_name);
            holder.reason = (TextView) view.findViewById(R.id.item_reason);
            holder.addBtn = (ImageButton) view.findViewById(R.id.item_add_btn);
            holder.state = (TextView) view.findViewById(R.id.item_state);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        final FriendRecommendEntry item = mList.get(position);
        Bitmap bitmap = NativeImageLoader.getInstance().getBitmapFromMemCache(item.username);
        if (null == bitmap) {
            String path = item.avatar;
            if (null == path || TextUtils.isEmpty(path)) {
                holder.headIcon.setImageResource(R.drawable.jmui_head_icon);
            } else {
                bitmap = BitmapLoader.getBitmapFromFile(path, (int) (50 * mDensity), (int) (50 * mDensity));
                NativeImageLoader.getInstance().updateBitmapFromCache(item.username, bitmap);
                holder.headIcon.setImageBitmap(bitmap);
            }
        } else {
            holder.headIcon.setImageBitmap(bitmap);
        }

        holder.name.setText(item.displayName);
        holder.reason.setText(item.reason);
        if (item.state.equals(FriendInvitation.INVITED.getValue())) {
            holder.addBtn.setVisibility(View.VISIBLE);
            holder.state.setVisibility(View.GONE);
            holder.addBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Dialog dialog = DialogCreator.createLoadingDialog(mContext, mContext.getString(R.string.jmui_loading));
                    dialog.show();
                    ContactManager.acceptInvitation(item.username, item.appKey, new BasicCallback() {
                        @Override
                        public void gotResult(int status, String desc) {
                            dialog.dismiss();
                            if (status == 0) {
                                item.state = FriendInvitation.ACCEPTED.getValue();
                                item.save();
                                holder.addBtn.setVisibility(View.GONE);
                                holder.state.setVisibility(View.VISIBLE);
                                holder.state.setTextColor(mContext.getResources().getColor(R.color.contacts_pinner_txt));
                                holder.state.setText(mContext.getString(R.string.added));
                                EventBus.getDefault().post(new Event.AddFriendEvent(item.getId()));
                            } else {
                                HandleResponseCode.onHandle(mContext, status, false);
                            }
                        }
                    });
                }
            });
        } else if (item.state.equals(FriendInvitation.ACCEPTED.getValue())) {
            holder.addBtn.setVisibility(View.GONE);
            holder.state.setVisibility(View.VISIBLE);
            holder.state.setTextColor(mContext.getResources().getColor(R.color.contacts_pinner_txt));
            holder.state.setText(mContext.getString(R.string.added));
        } else if (item.state.equals(FriendInvitation.INVITING.getValue())) {
            holder.addBtn.setVisibility(View.GONE);
            holder.state.setVisibility(View.VISIBLE);
            holder.state.setTextColor(mContext.getResources().getColor(R.color.finish_btn_clickable_color));
            holder.state.setText(mContext.getString(R.string.friend_inviting));
        } else {
            holder.addBtn.setVisibility(View.GONE);
            holder.state.setVisibility(View.VISIBLE);
            holder.state.setTextColor(mContext.getResources().getColor(R.color.header_normal));
            holder.state.setText(mContext.getString(R.string.decline_friend_invitation));
        }

        return view;
    }

    public void clearAll() {
        mList.clear();
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        CircleImageView headIcon;
        TextView name;
        TextView reason;
        ImageButton addBtn;
        TextView state;
    }
}
