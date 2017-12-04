package jiguang.chat.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;
import jiguang.chat.R;
import jiguang.chat.activity.GroupMemberListActivity;
import jiguang.chat.utils.pinyin.HanziToPinyin;

/**
 * Created by ${chenyn} on 2017/11/3.
 */

public class GroupMemberListAdapter extends BaseAdapter implements StickyListHeadersAdapter, SectionIndexer {

    private List<UserInfo> mMemberList;
    private final LayoutInflater mInflater;

    private int[] mSectionIndices;
    private String[] mSectionLetters;
    private long mGroupID;

    public GroupMemberListAdapter(GroupMemberListActivity context, List<UserInfo> memberInfoList, long groupId) {
        mInflater = LayoutInflater.from(context);
        this.mMemberList = memberInfoList;
        this.mGroupID = groupId;

        //想要给listView增加分组,数据源要排序才行
        mSectionIndices = getSectionIndices();
        mSectionLetters = getSectionLetters();
    }

    @Override
    public int getCount() {
        return mMemberList == null ? 0 : mMemberList.size();
    }

    @Override
    public Object getItem(int position) {
        return mMemberList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewholder;
        if (convertView == null) {
            viewholder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_group_member_list, parent, false);
            viewholder.iv_memberAvatar = convertView.findViewById(R.id.iv_memberAvatar);
            viewholder.tv_memberName = convertView.findViewById(R.id.tv_memberName);
            viewholder.iv_gag = convertView.findViewById(R.id.iv_silence);
            convertView.setTag(viewholder);
        } else {
            viewholder = (ViewHolder) convertView.getTag();
        }

        UserInfo userInfo = mMemberList.get(position);
        File file = userInfo.getAvatarFile();
        if (file != null) {
            if (file.exists()) {
                viewholder.iv_memberAvatar.setImageBitmap(BitmapFactory.decodeFile(userInfo.getAvatarFile().getPath()));
            } else {
                userInfo.getBigAvatarBitmap(new GetAvatarBitmapCallback() {
                    @Override
                    public void gotResult(int i, String s, Bitmap bitmap) {
                        if (i == 0) {
                            viewholder.iv_memberAvatar.setImageBitmap(bitmap);
                        }
                    }
                });
            }
        } else {
            viewholder.iv_memberAvatar.setImageResource(R.drawable.jmui_head_icon);
        }

        viewholder.tv_memberName.setText(userInfo.getDisplayName());

        JMessageClient.getGroupInfo(mGroupID, new GetGroupInfoCallback() {
            @Override
            public void gotResult(int i, String s, GroupInfo groupInfo) {
                if (i == 0) {
                    boolean keepSilence = groupInfo.isKeepSilence(userInfo.getUserName(), userInfo.getAppKey());
                    if (keepSilence) {
                        viewholder.iv_gag.setVisibility(View.VISIBLE);
                    }else {
                        viewholder.iv_gag.setVisibility(View.GONE);
                    }
                }
            }
        });

        return convertView;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        convertView = mInflater.inflate(R.layout.header, parent, false);
        TextView headView = convertView.findViewById(R.id.section_tv);
        UserInfo userInfo = mMemberList.get(position);

        int forPosition = getSectionForPosition(position);
        headView.setText(getLetter(userInfo.getDisplayName()));
        if (position == getPositionForSection(forPosition)) {
            headView.setText(getLetter(userInfo.getDisplayName()));
        }
        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        String letter = getLetter(mMemberList.get(position).getDisplayName());
        return letter.charAt(0);
    }

    class ViewHolder {
        ImageView iv_memberAvatar;
        TextView tv_memberName;
        ImageView iv_gag;
    }

    private int[] getSectionIndices() {
        ArrayList<Integer> sectionIndices = new ArrayList<Integer>();
        if (mMemberList.size() > 0) {
            char lastFirstChar = getLetter(mMemberList.get(0).getDisplayName()).charAt(0);
            sectionIndices.add(0);
            for (int i = 1; i < mMemberList.size(); i++) {
                if (getLetter(mMemberList.get(i).getDisplayName()).charAt(0) != lastFirstChar) {
                    lastFirstChar = getLetter(mMemberList.get(i).getDisplayName()).charAt(0);
                    sectionIndices.add(i);
                }
            }
            int[] sections = new int[sectionIndices.size()];
            for (int i = 0; i < sectionIndices.size(); i++) {
                sections[i] = sectionIndices.get(i);
            }
            return sections;
        }
        return null;
    }

    private String[] getSectionLetters() {
        if (null != mSectionIndices) {
            String[] letters = new String[mSectionIndices.length];
            for (int i = 0; i < mSectionIndices.length; i++) {
                letters[i] = getLetter(mMemberList.get(mSectionIndices[i]).getDisplayName());
            }
            return letters;
        }
        return null;
    }

    @Override
    public Object[] getSections() {
        return mSectionLetters;
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        if (mSectionIndices == null || mSectionIndices.length == 0) {
            return 0;
        }
        if (sectionIndex >= mSectionIndices.length) {
            sectionIndex = mSectionIndices.length - 1;
        } else if (sectionIndex < 0) {
            sectionIndex = 0;
        }
        return mSectionIndices[sectionIndex];
    }

    @Override
    public int getSectionForPosition(int position) {
        if (null != mSectionIndices) {
            for (int i = 0; i < mSectionIndices.length; i++) {
                if (position < mSectionIndices[i]) {
                    return i - 1;
                }
            }
            return mSectionIndices.length - 1;
        }
        return -1;
    }

    public int getSectionForLetter(String letter) {
        if (null != mSectionIndices) {
            for (int i = 0; i < mSectionIndices.length; i++) {
                if (mSectionLetters[i].equals(letter)) {
                    return mSectionIndices[i] + 1;
                }
            }
        }
        return -1;
    }

    public String getLetter(String name) {
        String letter;
        ArrayList<HanziToPinyin.Token> tokens = HanziToPinyin.getInstance()
                .get(name);
        StringBuilder sb = new StringBuilder();
        if (tokens != null && tokens.size() > 0) {
            for (HanziToPinyin.Token token : tokens) {
                if (token.type == HanziToPinyin.Token.PINYIN) {
                    sb.append(token.target);
                } else {
                    sb.append(token.source);
                }
            }
        }
        String sortString = sb.toString().substring(0, 1).toUpperCase();
        if (sortString.matches("[A-Z]")) {
            letter = sortString.toUpperCase();
        } else {
            letter = "#";
        }
        return letter;
    }
}
