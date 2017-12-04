package jiguang.chat.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.event.GroupApprovalEvent;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.api.BasicCallback;
import jiguang.chat.R;
import jiguang.chat.database.GroupApplyEntry;

/**
 * Created by ${chenyn} on 2017/11/7.
 */

public class GroupVerificationAdapter extends BaseAdapter {
    private Activity mContext;
    private final LayoutInflater mInflater;
    private List<GroupApplyEntry> recommends;
    private File mFile;

    public GroupVerificationAdapter(Activity context, List<GroupApplyEntry> recommends) {
        this.mContext = context;
        this.recommends = recommends;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return recommends.size();
    }

    @Override
    public Object getItem(int position) {
        return recommends.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GroupApplyEntry entry = recommends.get(position);

        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_group_owner_recomend, null);
            holder.iv_groupAvatar = convertView.findViewById(R.id.item_head_icon);
            holder.item_name = convertView.findViewById(R.id.item_name);
            holder.item_reason = convertView.findViewById(R.id.item_reason);
            holder.tv_groupInvite = convertView.findViewById(R.id.tv_groupInvite);
            holder.item_add_btn = convertView.findViewById(R.id.item_add_btn);
            holder.item_state = convertView.findViewById(R.id.item_state);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (entry.Avatar != null) {
            mFile = new File(entry.Avatar);
            if (mFile.exists()) {
                holder.iv_groupAvatar.setImageBitmap(BitmapFactory.decodeFile(entry.Avatar));
            } else {
                JMessageClient.getUserInfo(entry.toUsername, new GetUserInfoCallback() {
                    @Override
                    public void gotResult(int i, String s, UserInfo userInfo) {
                        if (i == 0) {
                            userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                                @Override
                                public void gotResult(int i, String s, Bitmap bitmap) {
                                    if (i == 0) {
                                        holder.iv_groupAvatar.setImageBitmap(bitmap);
                                    } else {
                                        holder.iv_groupAvatar.setImageResource(R.drawable.jmui_head_icon);
                                    }
                                }
                            });
                        }
                    }
                });
            }
        } else {
            holder.iv_groupAvatar.setImageResource(R.drawable.jmui_head_icon);

        }


        JMessageClient.getGroupInfo(Long.parseLong(entry.groupName), new GetGroupInfoCallback() {
            @Override
            public void gotResult(int i, String s, GroupInfo groupInfo) {
                holder.item_reason.setText("申请加入群 " + groupInfo.getGroupName());
                //邀请
                if (entry.groupType == 0) {
                    holder.item_name.setText(entry.toDisplayName);
                    holder.tv_groupInvite.setText("邀请人：" + entry.fromDisplayName);//事件发起人
                } else {
                    holder.item_name.setText(entry.toDisplayName);
                    holder.tv_groupInvite.setText(entry.reason);
                }
            }
        });

        //申请
        //0为初始状态
        GroupApprovalEvent event = JsonUtil.fromJson(entry.eventJson, GroupApprovalEvent.class);
        if (entry.btnState == 0) {
            holder.item_add_btn.setBackgroundColor(Color.parseColor("#2DD0CF"));
            holder.item_add_btn.setTextColor(Color.parseColor("#FFFFFF"));
            holder.item_add_btn.setText("同意");
            holder.item_add_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //邀请
                    event.acceptGroupApproval(entry.toUsername, entry.appKey, new BasicCallback() {
                        @Override
                        public void gotResult(int i, String s) {
                            if (i == 0) {
                                //同意加入群组
                                Toast.makeText(mContext, "添加成功", Toast.LENGTH_SHORT).show();
                                holder.item_add_btn.setBackgroundColor(Color.parseColor("#FFFFFF"));
                                holder.item_add_btn.setTextColor(Color.parseColor("#B5B5B6"));
                                holder.item_add_btn.setText("已同意");
                                entry.btnState = 1;
                                entry.save();
                                notifyDataSetChanged();
                            } else {
                                Toast.makeText(mContext, "添加失败1" + s + i, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            });
            //同意
        } else if (entry.btnState == 1) {
            holder.item_add_btn.setBackgroundColor(Color.parseColor("#FFFFFF"));
            holder.item_add_btn.setTextColor(Color.parseColor("#B5B5B6"));
            holder.item_add_btn.setText("已同意");
            event.acceptGroupApproval(entry.toUsername, entry.appKey, new BasicCallback() {
                @Override
                public void gotResult(int i, String s) {
                    if (i == 0) {
                        //同意加入群组
                        Toast.makeText(mContext, "添加成功", Toast.LENGTH_SHORT).show();
                        holder.item_add_btn.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        holder.item_add_btn.setTextColor(Color.parseColor("#B5B5B6"));
                        holder.item_add_btn.setText("已同意");
                        entry.btnState = 1;
                        entry.save();
                        notifyDataSetChanged();
                    } else if (i == 856001) {
                        holder.item_add_btn.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        holder.item_add_btn.setTextColor(Color.parseColor("#B5B5B6"));
                        holder.item_add_btn.setText("已同意");
                    } else {
                        Toast.makeText(mContext, "添加失败..." + s + i, Toast.LENGTH_SHORT).show();
                    }
                }
            });

            //拒绝
        } else if (entry.btnState == 2) {
            event.refuseGroupApproval(entry.toUsername, entry.appKey, "拒绝加入", new BasicCallback() {
                @Override
                public void gotResult(int i, String s) {
                    if (i == 0) {
                        Toast.makeText(mContext, "拒绝成功", Toast.LENGTH_SHORT).show();
                        holder.item_add_btn.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        holder.item_add_btn.setTextColor(Color.parseColor("#B5B5B6"));
                        holder.item_add_btn.setText("已拒绝");
                        entry.btnState = 2;
                        entry.save();
                    } else if (i == 856002) {
                        holder.item_add_btn.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        holder.item_add_btn.setTextColor(Color.parseColor("#B5B5B6"));
                        holder.item_add_btn.setText("已拒绝");
                    } else {
                        Toast.makeText(mContext, "拒绝失败" + s + i, Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }


        return convertView;
    }

    class ViewHolder {
        ImageView iv_groupAvatar;
        TextView item_name;
        TextView item_reason;
        TextView tv_groupInvite;
        TextView item_add_btn;
        TextView item_state;
    }


}
