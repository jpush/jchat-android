package io.jchat.android.controller;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.TextView;

import io.jchat.android.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.enums.ConversationType;
import io.jchat.android.activity.ChatActivity;
import io.jchat.android.activity.ConversationListFragment;
import io.jchat.android.adapter.ConversationListAdapter;
import io.jchat.android.application.JPushDemoApplication;
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
    private int mWidth;

    public ConversationListController(ConversationListView listView,
                                      ConversationListFragment context) {
        this.mConvListView = listView;
        this.mContext = context;
        DisplayMetrics dm = new DisplayMetrics();
        mContext.getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        mDensity = dm.density;
        mWidth = dm.widthPixels;
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

        mListAdapter = new ConversationListAdapter(mContext, mDatas);
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
    public void onItemClick(AdapterView<?> viewAdapter, View view,
                            int position, long id) {
        // TODO Auto-generated method stub
        final Intent intent = new Intent();
        String targetID = mDatas.get(position).getTargetId();
        intent.putExtra(JPushDemoApplication.TARGET_ID, targetID);
        mDatas.get(position).resetUnreadCount();
        // 当前点击的会话是否为群组
        if (mDatas.get(position).getType().equals(ConversationType.group)) {
            intent.putExtra(JPushDemoApplication.IS_GROUP, true);
            intent.putExtra(JPushDemoApplication.GROUP_ID, Long.parseLong(targetID));
            intent.setClass(mContext.getActivity(), ChatActivity.class);
            mContext.startActivity(intent);
            return;
        } else
            intent.putExtra(JPushDemoApplication.IS_GROUP, false);
        intent.setClass(mContext.getActivity(), ChatActivity.class);
        mContext.startActivity(intent);

    }

    /*
     * 刷新会话列表
     */
    public void refreshConvList() {
        mDatas = JMessageClient.getConversationList();
        SortConvList sortList = new SortConvList();
        Collections.sort(mDatas, sortList);
        mContext.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mListAdapter.refresh(mDatas);
            }
        });
    }

    /**
     * 加载头像并刷新
     * @param targetID 用户名
     * @param path 头像路径
     */
    public void loadAvatarAndRefresh(String targetID, String path) {
        int size = (int) (50 * mDensity);
        NativeImageLoader.getInstance().putUserAvatar(targetID, path, size);
        refreshConvList();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> viewAdapter, View view,
                                   final int position, long id) {
        final Conversation conv = mDatas.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(
                mContext.getActivity());
        final View v = LayoutInflater.from(mContext.getActivity()).inflate(
                R.layout.dialog_delete_conv, null);
        builder.setView(v);
        final TextView title = (TextView) v.findViewById(R.id.dialog_title);
        final Button deleteBtn = (Button) v.findViewById(R.id.delete_conv_btn);
        title.setText(conv.getTitle());
        final Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setLayout((int)(0.8 * mWidth), WindowManager.LayoutParams.WRAP_CONTENT);
        deleteBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (conv.getType().equals(ConversationType.group))
                    JMessageClient.deleteGroupConversation(Integer.parseInt(conv.getTargetId()));
                else
                    JMessageClient.deleteSingleConversation(conv.getTargetId());
                mDatas.remove(position);
                mListAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        return true;
    }

    public ConversationListAdapter getAdapter() {
        return mListAdapter;
    }
}
