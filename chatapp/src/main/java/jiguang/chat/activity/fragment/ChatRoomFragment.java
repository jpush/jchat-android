package jiguang.chat.activity.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jiguang.chat.R;
import jiguang.chat.controller.ChatRoomController;
import jiguang.chat.view.ChatRoomView;

/**
 * Created by ${chenyn} on 2017/10/31.
 */

public class ChatRoomFragment extends BaseFragment {
    private Context mContext;
    private View mRootView;
    private ChatRoomView mChatRoomView;
    private ChatRoomController mRoomController;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getActivity();
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        mRootView = layoutInflater.inflate(R.layout.fragment_chat_room,
                getActivity().findViewById(R.id.main_view), false);

        mChatRoomView = mRootView.findViewById(R.id.chat_room_view);
        mChatRoomView.initModule();
        mRoomController = new ChatRoomController(mChatRoomView, mContext);

        mChatRoomView.setListener(mRoomController);
        mChatRoomView.setClickListener(mRoomController);
        mChatRoomView.setPtrHandler(mRoomController);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup p = (ViewGroup) mRootView.getParent();
        if (p != null) {
            p.removeAllViewsInLayout();
        }
        return mRootView;
    }
}
