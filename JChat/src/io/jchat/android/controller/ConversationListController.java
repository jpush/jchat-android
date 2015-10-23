package io.jchat.android.controller;

import android.app.Dialog;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.R;
import io.jchat.android.activity.ChatActivity;
import io.jchat.android.activity.ConversationListFragment;
import io.jchat.android.adapter.ConversationListAdapter;
import io.jchat.android.application.JPushDemoApplication;
import io.jchat.android.tools.DialogCreator;
import io.jchat.android.tools.NativeImageLoader;
import io.jchat.android.tools.SortConvList;
import io.jchat.android.view.ConversationListView;

public class ConversationListController implements OnClickListener,
        OnItemClickListener, OnItemLongClickListener {

    private ConversationListView mConvListView;
    private ConversationListFragment mContext;
    private List<Conversation> mDatas = new ArrayList<Conversation>();
    private ConversationListAdapter mListAdapter;
    private double mDensity;
    private int mDensityDpi;
    private int mWidth;
    private Dialog mDialog;

    public ConversationListController(ConversationListView listView, ConversationListFragment context,
                                      float density, int densityDpi, int width) {
        this.mConvListView = listView;
        this.mContext = context;
        this.mDensity = density;
        this.mDensityDpi = densityDpi;
        this.mWidth = width;
        initConvListAdapter();
    }

    // 得到会话列表
    private void initConvListAdapter() {
        mDatas = JMessageClient.getConversationList();
        //对会话列表进行时间排序
        if (mDatas.size() > 1) {
            SortConvList sortList = new SortConvList();
            Collections.sort(mDatas, sortList);
        }

        mListAdapter = new ConversationListAdapter(mContext.getActivity(), mDatas, mDensity, mDensityDpi);
        mConvListView.setConvListAdapter(mListAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_group_btn:
                mContext.showMenuPopWindow();
                break;
        }
    }

    // 点击会话列表
    @Override
    public void onItemClick(AdapterView<?> viewAdapter, View view, int position, long id) {
        // TODO Auto-generated method stub
        final Intent intent = new Intent();
        // 当前点击的会话是否为群组
        if (mDatas.get(position).getType().equals(ConversationType.group)) {
            long groupID = ((GroupInfo) mDatas.get(position).getTargetInfo()).getGroupID();
            intent.putExtra(JPushDemoApplication.IS_GROUP, true);
            intent.putExtra(JPushDemoApplication.GROUP_ID, groupID);
            intent.setClass(mContext.getActivity(), ChatActivity.class);
            mContext.getActivity().startActivity(intent);
            return;
        } else {
            String targetID = ((UserInfo) mDatas.get(position).getTargetInfo()).getUserName();
            intent.putExtra(JPushDemoApplication.TARGET_ID, targetID);
            intent.putExtra(JPushDemoApplication.IS_GROUP, false);
        }
        intent.setClass(mContext.getActivity(), ChatActivity.class);
        mContext.getActivity().startActivity(intent);

    }

    /**
     * 在会话列表界面收到消息或者新建会话，将该会话置顶
     *
     * @param conv 收到消息的Conversation
     */
    public void refreshConvList(final Conversation conv) {
        mListAdapter.setToTop(conv);
    }

    /**
     * 加载头像并刷新
     *
     * @param targetID 用户名
     * @param path     头像路径
     */
    public void loadAvatarAndRefresh(String targetID, String path) {
        int size = (int) (50 * mDensity);
        NativeImageLoader.getInstance().putUserAvatar(targetID, path, size);
        mListAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> viewAdapter, View view,
                                   final int position, long id) {
        final Conversation conv = mDatas.get(position);
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (conv.getType().equals(ConversationType.group)){
                    JMessageClient.deleteGroupConversation(((GroupInfo) conv.getTargetInfo())
                            .getGroupID());
                }
                else {
                    JMessageClient.deleteSingleConversation(((UserInfo) conv.getTargetInfo())
                            .getUserName());
                }
                mDatas.remove(position);
                mListAdapter.notifyDataSetChanged();
                mDialog.dismiss();
            }
        };
        mDialog = DialogCreator.createDelConversationDialog(mContext.getActivity(), conv.getTitle(),
                listener);
        mDialog.show();
        mDialog.getWindow().setLayout((int) (0.8 * mWidth), WindowManager.LayoutParams.WRAP_CONTENT);
        return true;
    }

    public ConversationListAdapter getAdapter() {
        return mListAdapter;
    }

}
