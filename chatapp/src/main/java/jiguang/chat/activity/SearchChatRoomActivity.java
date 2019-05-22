package jiguang.chat.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

import cn.jpush.im.android.api.ChatRoomManager;
import cn.jpush.im.android.api.callback.RequestCallback;
import cn.jpush.im.android.api.model.ChatRoomInfo;
import jiguang.chat.R;
import jiguang.chat.utils.CommonUtils;

/**
 * Created by ${chenyn} on 2017/11/9.
 */

public class SearchChatRoomActivity extends BaseActivity {

    private LinearLayout mLl_chatRoomItem;
    private ImageView mIv_chatRoomAvatar;
    private TextView mTv_chatRoomName;
    private TextView mTv_chatRoomDesc;
    private EditText mSearchEditText;
    private LinearLayout mAc_iv_press_back;
    private TextView mTv_search;
    private long mRoomID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search_chat_room);

        initView();
        initData();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initData() {
        //清空EditText
        mSearchEditText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (mSearchEditText.getRight() - 2 * mSearchEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    mSearchEditText.setText("");
                    mSearchEditText.clearFocus();
                    mLl_chatRoomItem.setVisibility(View.GONE);
                    return true;
                }
            }
            return false;
        });

        mAc_iv_press_back.setOnClickListener(v -> {
            finish();
            CommonUtils.hideKeyboard(SearchChatRoomActivity.this);
        });
        mTv_search.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(mSearchEditText.getText())) {
                String roomId = mSearchEditText.getText().toString().trim();
                try {
                    long id = Long.parseLong(roomId);
                    ChatRoomManager.getChatRoomInfos(Collections.singleton(id), new RequestCallback<List<ChatRoomInfo>>() {
                        @Override
                        public void gotResult(int i, String s, List<ChatRoomInfo> chatRoomInfos) {
                            if (i == 0) {
                                mRoomID = chatRoomInfos.get(0).getRoomID();
                                mLl_chatRoomItem.setVisibility(View.VISIBLE);
                                mTv_chatRoomDesc.setText(chatRoomInfos.get(0).getDescription());
                                mTv_chatRoomName.setText(chatRoomInfos.get(0).getName());
                            }else {
                                mLl_chatRoomItem.setVisibility(View.GONE);
                                Toast.makeText(SearchChatRoomActivity.this, "搜索的聊天室不存在", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (NumberFormatException e) {
                    Toast.makeText(SearchChatRoomActivity.this, "搜索的聊天室不存在", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SearchChatRoomActivity.this, "请输入聊天室ID", Toast.LENGTH_SHORT).show();
            }
        });

        mLl_chatRoomItem.setOnClickListener(v -> {
            Intent intent = new Intent(SearchChatRoomActivity.this, ChatRoomDetailActivity.class);
            intent.putExtra("chatRoomId", mRoomID);
            startActivity(intent);
        });

    }

    private void initView() {
        mLl_chatRoomItem = (LinearLayout) findViewById(R.id.ll_chatRoomItem);
        mIv_chatRoomAvatar = (ImageView) findViewById(R.id.iv_chatRoomAvatar);
        mTv_chatRoomName = (TextView) findViewById(R.id.tv_chatRoomName);
        mTv_chatRoomDesc = (TextView) findViewById(R.id.tv_chatRoomDesc);
        mTv_search = (TextView) findViewById(R.id.tv_search);

        mAc_iv_press_back = (LinearLayout) findViewById(R.id.ac_iv_press_back);
        mSearchEditText = (EditText) findViewById(R.id.ac_et_search);
    }

}
