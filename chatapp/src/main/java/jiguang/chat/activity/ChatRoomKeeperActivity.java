package jiguang.chat.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.ChatRoomManager;
import cn.jpush.im.android.api.callback.RequestCallback;
import cn.jpush.im.android.api.event.ChatRoomNotificationEvent;
import cn.jpush.im.android.api.model.UserInfo;
import jiguang.chat.R;
import jiguang.chat.adapter.ChatRoomKeeperListAdapter;
import jiguang.chat.application.JGApplication;
import jiguang.chat.utils.DialogCreator;
import jiguang.chat.utils.HandleResponseCode;
import jiguang.chat.utils.pinyin.HanyuPinyin;

public class ChatRoomKeeperActivity extends BaseActivity {
    private TextView mTvNullKeeper;
    private ListView mLvKeeper;
    private EditText mEtSearch;
    private List<UserInfo> mKeepers = new ArrayList<>();
    private List<ItemModel> mShowKeeperList = new ArrayList<>();
    private List<String> mPinyinList = new ArrayList<String>();
    private ChatRoomKeeperListAdapter mAdapter;
    private String mSearchText;
    private MyHandler myHandler;
    private static final int SEARCH_KEEPER = 0;
    private static final int SEARCH_MEMBER_SUCCESS = 1;
    private long roomID;
    public static final String IS_OWNER = "isOwner";
    private boolean isOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room_keeper);
        initData();
    }

    private void initData() {
        initTitle(true, true, "管理员", "", true, "添加");
        roomID = getIntent().getLongExtra(JGApplication.ROOM_ID, 0);
        isOwner = getIntent().getBooleanExtra(IS_OWNER, false);
        if (!isOwner) {
            mJmui_commit_btn.setVisibility(View.GONE);
        }
        mJmui_commit_btn.setOnClickListener((v) -> {
            Intent intent = new Intent(ChatRoomKeeperActivity.this, SearchForChatRoomActivity.class);
            intent.putExtra(JGApplication.ROOM_ID, roomID);
            startActivity(intent);
        });
        mTvNullKeeper = (TextView) findViewById(R.id.null_chatRoomKeeper);
        mLvKeeper = (ListView) findViewById(R.id.lv_chatRoomKeeper);
        mEtSearch = (EditText) findViewById(R.id.search_et);
        mEtSearch.addTextChangedListener(watcher);
        myHandler = new MyHandler(getMainLooper(), this);
        Dialog loadingDialog = DialogCreator.createLoadingDialog(this, "正在加载...");
        loadingDialog.show();
        ChatRoomManager.getChatRoomAdminList(roomID, new RequestCallback<List<UserInfo>>() {
            @Override
            public void gotResult(int i, String s, List<UserInfo> userInfos) {
                if (i == 0) {
                    if (userInfos.size() > 0) {
                        mKeepers.clear();
                        mKeepers.addAll(userInfos);
                        addAll(false);
                    } else {
                        mTvNullKeeper.setVisibility(View.VISIBLE);
                    }
                    mAdapter = new ChatRoomKeeperListAdapter(ChatRoomKeeperActivity.this, mShowKeeperList, roomID, isOwner);
                    mLvKeeper.setAdapter(mAdapter);
                    loadingDialog.dismiss();
                } else {
                    loadingDialog.dismiss();
                    HandleResponseCode.onHandle(ChatRoomKeeperActivity.this, i, false);
                }
            }
        });

    }

    TextWatcher watcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mSearchText = s.toString().trim();
            myHandler.removeMessages(SEARCH_KEEPER);
            myHandler.sendMessageDelayed(myHandler.obtainMessage(SEARCH_KEEPER), 200);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    public class ItemModel {
        public UserInfo data;
        public SpannableString highlight;
        public int sortIndex;
    }

    public void onEventMainThread(ChatRoomNotificationEvent event) {
        Log.d("69523", "event type:" + event.getType() + "mAdapter:" + mAdapter);
        switch (event.getType()) {
            case add_chatroom_admin:
            case del_chatroom_admin:
                if (mAdapter != null) {
                    ChatRoomManager.getChatRoomAdminList(roomID, new RequestCallback<List<UserInfo>>() {
                        @Override
                        public void gotResult(int i, String s, List<UserInfo> userInfos) {
                            if (i == 0) {
                                mKeepers.clear();
                                mKeepers.addAll(userInfos);
                                Log.d("69523", "userinfo size:" + userInfos.size());
                                addAll(true);
                                mTvNullKeeper.setVisibility(userInfos.size() > 0 ? View.GONE : View.VISIBLE);
                            }
                        }
                    });
                }
                break;
            default:
                break;
        }

    }

    private class MyHandler extends Handler {
        private final WeakReference<ChatRoomKeeperActivity> mActivity;
        public MyHandler(Looper looper, ChatRoomKeeperActivity mActivity) {
            super(looper);
            this.mActivity = new WeakReference<>(mActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SEARCH_KEEPER:
                    if (mShowKeeperList != null) {
                        mShowKeeperList.clear();
                    }
                    filterData();
                    break;
                case SEARCH_MEMBER_SUCCESS:
                    Log.d("69523", "cccccccccccccccccccccc");
                    ChatRoomKeeperActivity activity = mActivity.get();
                    if (activity != null && activity.mAdapter != null) {
                        Log.d("69523", "dddddddddddddddd");
                        activity.mAdapter.notifyDataSetChanged();
                    }
                    break;
                default:
                    break;

            }
        }
    }


    /**
     * 根据输入框输入的字符过滤群成员
     */
    private void filterData() {
        if (TextUtils.isEmpty(mSearchText)) {
            addAll(true);
        } else {
            String nickname, pinyin;
            int sort;
            SpannableString result;
            ItemModel model;
            UserInfo userInfo;
            for (int i = 0; i < mPinyinList.size(); i++) {
                sort = 0;
                userInfo = mKeepers.get(i);
                nickname = userInfo.getNickname();
                if (TextUtils.isEmpty(nickname)) {
                    nickname = userInfo.getUserName();
                }
                result = new SpannableString(nickname);
                //先进行拼音匹配
                pinyin = mPinyinList.get(i).toLowerCase();
                int offset = pinyin.indexOf(mSearchText.toLowerCase());
                if (offset != -1) {
                    model = new ItemModel();
                    sort += mSearchText.length();
                    result.setSpan(new ForegroundColorSpan(Color.RED), offset,
                            offset + mSearchText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    //进行直接匹配
                    int index = nickname.indexOf(mSearchText);
                    if (index != -1) {
                        sort += mSearchText.length();
                        result.setSpan(new ForegroundColorSpan(Color.RED), index,
                                index + mSearchText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        model.data = userInfo;
                        model.highlight = result;
                        model.sortIndex = sort;
                        mShowKeeperList.add(model);
                        continue;
                    }
                    model.data = userInfo;
                    model.highlight = result;
                    model.sortIndex = sort;
                    mShowKeeperList.add(model);
                    //进行直接匹配
                } else {
                    int index = nickname.indexOf(mSearchText);
                    if (index != -1) {
                        sort += mSearchText.length();
                        result.setSpan(new ForegroundColorSpan(Color.RED), index,
                                index + mSearchText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        model = new ItemModel();
                        model.data = userInfo;
                        model.highlight = result;
                        model.sortIndex = sort;
                        mShowKeeperList.add(model);
                    }
                }
            }
        }

        myHandler.sendEmptyMessage(SEARCH_MEMBER_SUCCESS);
    }

    private void addAll(boolean needSendMessage) {
        String nickname, pinyin;
        ItemModel itemModel;
        mPinyinList.clear();
        mShowKeeperList.clear();
        Log.d("69523", "aaaaaaaaaaaaaaaaaaaa");
        for (UserInfo userInfo: mKeepers) {
            itemModel = new ItemModel();
            itemModel.data = userInfo;
            nickname = userInfo.getNickname();
            if (TextUtils.isEmpty(nickname)) {
                nickname = userInfo.getUserName();
            }
            pinyin = HanyuPinyin.getInstance().getStringPinYin(nickname);
            mPinyinList.add(pinyin);
            itemModel.highlight = new SpannableString(nickname);
            mShowKeeperList.add(itemModel);
        }
        if (needSendMessage) {
            Log.d("69523", "bbbbbbbbbbbbbbbbbbbbbbbbb size:" + mShowKeeperList.size());
            myHandler.sendEmptyMessage(SEARCH_MEMBER_SUCCESS);
        }
    }
}
