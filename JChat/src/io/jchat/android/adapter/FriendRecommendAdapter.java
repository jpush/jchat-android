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
import io.jchat.android.database.FriendEntry;
import io.jchat.android.database.FriendRecommendEntry;
import io.jchat.android.entity.Event;
import io.jchat.android.entity.FriendInvitation;
import io.jchat.android.tools.NativeImageLoader;
import io.jchat.android.tools.ViewHolder;


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
        if (view == null) {
            view = mInflater.inflate(R.layout.item_friend_recomend, null);
        }
        CircleImageView headIcon = ViewHolder.get(view, R.id.item_head_icon);
        TextView name = ViewHolder.get(view, R.id.item_name);
        TextView reason = ViewHolder.get(view, R.id.item_reason);
        final ImageButton addBtn = ViewHolder.get(view, R.id.item_add_btn);
        final TextView state = ViewHolder.get(view, R.id.item_state);

        final FriendRecommendEntry item = mList.get(position);
        Bitmap bitmap = NativeImageLoader.getInstance().getBitmapFromMemCache(item.username);
        if (null == bitmap) {
            String path = item.avatar;
            if (null == path || TextUtils.isEmpty(path)) {
                headIcon.setImageResource(R.drawable.jmui_head_icon);
            } else {
                bitmap = BitmapLoader.getBitmapFromFile(path, (int) (50 * mDensity), (int) (50 * mDensity));
                NativeImageLoader.getInstance().updateBitmapFromCache(item.username, bitmap);
                headIcon.setImageBitmap(bitmap);
            }
        } else {
            headIcon.setImageBitmap(bitmap);
        }

        name.setText(item.displayName);
        reason.setText(item.reason);
        if (item.state.equals(FriendInvitation.INVITED.getValue())) {
            addBtn.setVisibility(View.VISIBLE);
            state.setVisibility(View.GONE);
            addBtn.setOnClickListener(new View.OnClickListener() {
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
                                addBtn.setVisibility(View.GONE);
                                state.setVisibility(View.VISIBLE);
                                state.setTextColor(mContext.getResources().getColor(R.color.contacts_pinner_txt));
                                state.setText(mContext.getString(R.string.added));
                                EventBus.getDefault().post(new Event.AddFriendEvent(item.getId()));
                            } else {
                                HandleResponseCode.onHandle(mContext, status, false);
                            }
                        }
                    });
                }
            });
        } else if (item.state.equals(FriendInvitation.ACCEPTED.getValue())) {
            addBtn.setVisibility(View.GONE);
            state.setVisibility(View.VISIBLE);
            state.setTextColor(mContext.getResources().getColor(R.color.contacts_pinner_txt));
            state.setText(mContext.getString(R.string.added));
        } else if (item.state.equals(FriendInvitation.INVITING.getValue())) {
            addBtn.setVisibility(View.GONE);
            state.setVisibility(View.VISIBLE);
            state.setTextColor(mContext.getResources().getColor(R.color.finish_btn_clickable_color));
            state.setText(mContext.getString(R.string.friend_inviting));
        } else {
            addBtn.setVisibility(View.GONE);
            state.setVisibility(View.VISIBLE);
            state.setTextColor(mContext.getResources().getColor(R.color.header_normal));
            state.setText(mContext.getString(R.string.decline_friend_invitation));
        }

        return view;
    }

    public void clearAll() {
        mList.clear();
        notifyDataSetChanged();
    }

}
