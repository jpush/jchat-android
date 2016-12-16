package io.jchat.android.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.ContactManager;
import cn.jpush.im.android.eventbus.EventBus;
import cn.jpush.im.api.BasicCallback;
import io.jchat.android.R;
import io.jchat.android.activity.FriendInfoActivity;
import io.jchat.android.activity.SearchFriendDetailActivity;
import io.jchat.android.application.JChatDemoApplication;
import io.jchat.android.chatting.CircleImageView;
import io.jchat.android.chatting.utils.BitmapLoader;
import io.jchat.android.chatting.utils.DialogCreator;
import io.jchat.android.chatting.utils.HandleResponseCode;
import io.jchat.android.database.FriendEntry;
import io.jchat.android.database.FriendRecommendEntry;
import io.jchat.android.entity.Event;
import io.jchat.android.entity.EventType;
import io.jchat.android.entity.FriendInvitation;
import io.jchat.android.tools.NativeImageLoader;
import io.jchat.android.tools.ViewHolder;


public class FriendRecommendAdapter extends BaseAdapter {

    private Context mContext;
    private List<FriendRecommendEntry> mList = new ArrayList<FriendRecommendEntry>();
    private LayoutInflater mInflater;
    private float mDensity;
    private Dialog mDialog;
    private int mWidth;

    public FriendRecommendAdapter(Context context, List<FriendRecommendEntry> list, float density,
                                  int width) {
        this.mContext = context;
        this.mList = list;
        this.mInflater = LayoutInflater.from(mContext);
        this.mDensity = density;
        this.mWidth = width;
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
    public View getView(final int position, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = mInflater.inflate(R.layout.item_friend_recomend, null);
        }
        CircleImageView headIcon = ViewHolder.get(view, R.id.item_head_icon);
        TextView name = ViewHolder.get(view, R.id.item_name);
        TextView reason = ViewHolder.get(view, R.id.item_reason);
        final Button addBtn = ViewHolder.get(view, R.id.item_add_btn);
        final Button refuseBtn = ViewHolder.get(view, R.id.item_refuse_btn);
        final TextView state = ViewHolder.get(view, R.id.item_state);
        LinearLayout itemLl = ViewHolder.get(view, R.id.friend_verify_item_ll);

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
            refuseBtn.setVisibility(View.VISIBLE);
            state.setVisibility(View.GONE);
            addBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Dialog dialog = DialogCreator.createLoadingDialog(mContext,
                            mContext.getString(R.string.adding_hint));
                    dialog.show();
                    ContactManager.acceptInvitation(item.username, item.appKey, new BasicCallback() {
                        @Override
                        public void gotResult(int status, String desc) {
                            dialog.dismiss();
                            if (status == 0) {
                                item.state = FriendInvitation.ACCEPTED.getValue();
                                item.save();
                                addBtn.setVisibility(View.GONE);
                                refuseBtn.setVisibility(View.GONE);
                                state.setVisibility(View.VISIBLE);
                                state.setTextColor(mContext.getResources().getColor(R.color.contacts_pinner_txt));
                                state.setText(mContext.getString(R.string.added));
                                EventBus.getDefault().post(new Event.Builder().setType(EventType.addFriend)
                                        .setFriendId(item.getId()).build());
                            } else {
                                HandleResponseCode.onHandle(mContext, status, false);
                            }
                        }
                    });
                }
            });

            refuseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDialog = new Dialog(mContext, R.style.jmui_default_dialog_style);
                    View v = mInflater.inflate(R.layout.dialog_refuse_invitation, null);
                    mDialog.setContentView(v);
                    final EditText reasonEt = (EditText) v.findViewById(R.id.reason_et);
                    Button cancelBtn = (Button) v.findViewById(R.id.cancel_btn);
                    Button sendBtn = (Button) v.findViewById(R.id.send_btn);
                    mDialog.getWindow().setLayout((int) (0.8 * mWidth), WindowManager.LayoutParams.WRAP_CONTENT);
                    mDialog.show();
                    cancelBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mDialog.dismiss();
                        }
                    });

                    sendBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String reason = reasonEt.getText().toString();
                            if (TextUtils.isEmpty(reason)) {
                                Toast.makeText(mContext, mContext.getString(R.string.reason_is_empty_hint),
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                            mDialog.dismiss();
                            final Dialog dialog = DialogCreator.createLoadingDialog(mContext,
                                    mContext.getString(R.string.processing));
                            dialog.show();
                            ContactManager.declineInvitation(item.username, item.appKey,
                                    reason, new BasicCallback() {
                                        @Override
                                        public void gotResult(int status, String desc) {
                                            dialog.dismiss();
                                            if (status == 0) {
                                                item.state = FriendInvitation.REFUSED.getValue();
                                                item.save();
                                                addBtn.setVisibility(View.GONE);
                                                refuseBtn.setVisibility(View.GONE);
                                                state.setVisibility(View.VISIBLE);
                                                state.setTextColor(mContext.getResources()
                                                        .getColor(R.color.refuse_btn_default));
                                                state.setText(mContext.getString(R.string.refused));
                                            }
                                        }
                                    });
                        }
                    });
                }
            });
        } else if (item.state.equals(FriendInvitation.ACCEPTED.getValue())) {
            addBtn.setVisibility(View.GONE);
            refuseBtn.setVisibility(View.GONE);
            state.setVisibility(View.VISIBLE);
            state.setTextColor(mContext.getResources().getColor(R.color.contacts_pinner_txt));
            state.setText(mContext.getString(R.string.added));
        } else if (item.state.equals(FriendInvitation.INVITING.getValue())) {
            addBtn.setVisibility(View.GONE);
            refuseBtn.setVisibility(View.GONE);
            state.setVisibility(View.VISIBLE);
            state.setTextColor(mContext.getResources().getColor(R.color.finish_btn_clickable_color));
            state.setText(mContext.getString(R.string.friend_inviting));
        } else if (item.state.equals(FriendInvitation.BE_REFUSED.getValue())) {
            addBtn.setVisibility(View.GONE);
            refuseBtn.setVisibility(View.GONE);
            reason.setTextColor(mContext.getResources().getColor(R.color.refuse_btn_default));
            state.setVisibility(View.VISIBLE);
            state.setTextColor(mContext.getResources().getColor(R.color.refuse_btn_default));
            state.setText(mContext.getString(R.string.decline_friend_invitation));
        } else {
            addBtn.setVisibility(View.GONE);
            refuseBtn.setVisibility(View.GONE);
            state.setVisibility(View.VISIBLE);
            state.setTextColor(mContext.getResources().getColor(R.color.refuse_btn_default));
            state.setText(mContext.getString(R.string.refused));
        }

        itemLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendRecommendEntry entry = mList.get(position);
                Intent intent;
                if (entry.state.equals(FriendInvitation.ACCEPTED.getValue())) {
                    intent = new Intent(mContext, FriendInfoActivity.class);
                    intent.putExtra("fromContact", true);
                } else {
                    intent = new Intent(mContext, SearchFriendDetailActivity.class);
                }
                intent.putExtra(JChatDemoApplication.TARGET_ID, entry.username);
                intent.putExtra(JChatDemoApplication.TARGET_APP_KEY, entry.appKey);
                mContext.startActivity(intent);
            }
        });

        itemLl.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final FriendRecommendEntry entry = mList.get(position);
                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FriendRecommendEntry.deleteEntry(entry);
                        mList.remove(position);
                        notifyDataSetChanged();
                        mDialog.dismiss();
                    }
                };
                mDialog = DialogCreator.createDelRecommendDialog(mContext, listener);
                mDialog.getWindow().setLayout((int) (0.8 * mWidth), WindowManager.LayoutParams.WRAP_CONTENT);
                mDialog.show();
                return true;
            }
        });

        return view;
    }

    public void clearAll() {
        mList.clear();
        notifyDataSetChanged();
    }

}
