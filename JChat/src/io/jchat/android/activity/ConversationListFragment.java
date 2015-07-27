package io.jchat.android.activity;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;

import java.io.File;

import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.event.ConversationRefreshEvent;
import cn.jpush.im.android.api.model.Message;
import io.jchat.android.R;
import io.jchat.android.application.JPushDemoApplication;
import io.jchat.android.controller.ConversationListController;
import io.jchat.android.controller.MenuItemController;
import io.jchat.android.tools.NativeImageLoader;
import io.jchat.android.view.ConversationListView;
import io.jchat.android.view.MenuItemView;

/*
 * 会话列表界面
 */
public class ConversationListFragment extends BaseFragment {

    private static String TAG = ConversationListFragment.class.getSimpleName();
    private View mRootView;
    private ConversationListView mConvListView;
    private ConversationListController mConvListController;
    private PopupWindow mMenuPopWindow;
    private View mMenuView;
    private MenuItemView mMenuItemView;
    private MenuItemController mMenuController;
    //MainActivity要实现的接口，用来显示或者隐藏ActionBar中新消息提示
    public OnNewMsgReceiverListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        JMessageClient.registerEventReceiver(this);
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        mRootView = layoutInflater.inflate(R.layout.fragment_conv_list,
                (ViewGroup) getActivity().findViewById(R.id.main_view),
                false);
        mConvListView = new ConversationListView(mRootView, this.getActivity());
        mConvListView.initModule();
        mMenuView = getActivity().getLayoutInflater().inflate(R.layout.drop_down_menu, null);
        mConvListController = new ConversationListController(mConvListView, this);
        mConvListView.setListener(mConvListController);
        mConvListView.setItemListeners(mConvListController);
        mConvListView.setLongClickListener(mConvListController);
        mMenuPopWindow = new PopupWindow(mMenuView, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
        mMenuItemView = new MenuItemView(mMenuView);
        mMenuItemView.initModule();
        mMenuController = new MenuItemController(mMenuItemView, this, mConvListController);
        mMenuItemView.setListeners(mMenuController);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            mListener = (OnNewMsgReceiverListener) activity;
        }catch (ClassCastException e){
            throw new ClassCastException(activity.toString() + "must implement OnNewMsgReceiverListener");
        }
    }

    //显示下拉菜单
    public void showMenuPopWindow() {
        mMenuPopWindow.setTouchable(true);
        mMenuPopWindow.setOutsideTouchable(true);
        mMenuPopWindow.setBackgroundDrawable(new BitmapDrawable(getResources(),
                (Bitmap) null));
        if (mMenuPopWindow.isShowing()) {
            mMenuPopWindow.dismiss();
        } else mMenuPopWindow.showAsDropDown(mRootView.findViewById(R.id.create_group_btn));
    }

    /**
     * 当触发GetUserInfo后，得到Conversation后，刷新界面
     * 通常触发的情况是新会话创建时刷新目标头像
     *
     * @param conversationRefreshEvent
     */
    public void onEvent(ConversationRefreshEvent conversationRefreshEvent) {
        Log.i(TAG, "ConversationRefreshEvent execute");
        Conversation conv = conversationRefreshEvent.getConversation();
        if (conv.getType() == ConversationType.single) {
            File file = conv.getAvatarFile();
            if (file != null) {
                mConvListController.loadAvatarAndRefresh(conv.getTargetId(), file.getAbsolutePath());
            }
        } else {
            mConvListController.getAdapter().notifyDataSetChanged();
        }
    }

    /**
     * 在会话列表中接收消息
     *
     * @param event
     */
    public void onEventMainThread(MessageEvent event) {
        Log.i(TAG, "onEventMainThread MessageEvent execute");
        Message msg = event.getMessage();
        String targetID = msg.getTargetID();
        ConversationType convType = msg.getTargetType();
        Conversation conv;
        if (convType == ConversationType.group) {
            conv = JMessageClient.getGroupConversation(Integer.parseInt(targetID));
        } else {
            conv = JMessageClient.getSingleConversation(targetID);
        }
        if (conv != null && convType == ConversationType.single) {
            //如果缓存了头像，直接刷新会话列表
            if (NativeImageLoader.getInstance().getBitmapFromMemCache(targetID) != null) {
                Log.i("Test", "conversation ");
                mConvListController.refreshConvList();
                //没有头像，从Conversation拿
            } else {
                File file = conv.getAvatarFile();
                //拿到后缓存并刷新
                if (file != null) {
                    mConvListController.loadAvatarAndRefresh(targetID, file.getAbsolutePath());
                    //conversation中没有头像，直接刷新，SDK会在后台获得头像，拿到后会执行onEvent(ConversationRefreshEvent conversationRefreshEvent)
                } else mConvListController.refreshConvList();
            }
        } else {
            mConvListController.refreshConvList();
        }

        mListener.onNewMsgReceived();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        ViewGroup p = (ViewGroup) mRootView.getParent();
        if (p != null) {
            p.removeAllViewsInLayout();
        }
        return mRootView;
    }

    @Override
    public void onResume() {
        //当前用户信息为空，需要重新登录
        if (null == JMessageClient.getMyInfo() || TextUtils.isEmpty(JMessageClient.getMyInfo().getUserName())) {
//            Intent intent = new Intent();
//            intent.setClass(this.getActivity(), LoginActivity.class);
//            startActivity(intent);
//            getActivity().finish();
        } else {
            dismissPopWindow();
            mConvListController.refreshConvList();
        }
        mConvListController.checkHasNewMessage();
        super.onResume();
    }

    public void dismissPopWindow() {
        if (mMenuPopWindow.isShowing()) {
            mMenuPopWindow.dismiss();
        }
    }


    @Override
    public void onDestroy() {
        JMessageClient.unRegisterEventReceiver(this);
        super.onDestroy();
    }


    public void StartCreateGroupActivity() {
        Intent intent = new Intent();
        intent.setClass(getActivity(), CreateGroupActivity.class);
        startActivity(intent);
    }

    public interface OnNewMsgReceiverListener {
        void onNewMsgReceived();
        void onClearNewMsgFlag();
    }


}
