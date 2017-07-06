package jiguang.chat.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.eventbus.EventBus;
import jiguang.chat.R;
import jiguang.chat.application.JGApplication;
import jiguang.chat.entity.Event;
import jiguang.chat.entity.EventType;
import jiguang.chat.model.SearchResult;
import jiguang.chat.utils.photochoose.SelectableRoundedImageView;
import jiguang.chat.utils.pinyin.CharacterParser;
import jiguang.chat.utils.query.TextSearcher;


public class SearchMoreFriendsActivity extends Activity implements AdapterView.OnItemClickListener {

    private String mFilterString;

    private EditText mSearchEditText;
    private ListView mFriendListView;
    private TextView mSearchNoResultsTextView;
    private LinearLayout mPressBackImageView;
    private LinearLayout mFriendListResultLinearLayout;

    private AsyncTask mAsyncTask;
    private ThreadPoolExecutor mExecutor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_friends_detail_info);

        Intent intent = getIntent();
        mFilterString = intent.getStringExtra("filterString");

        initView();
        initData();
    }


    public void initView() {
        mSearchEditText = (EditText) findViewById(R.id.ac_et_search);
        mFriendListView = (ListView) findViewById(R.id.ac_lv_friend_list_detail_info);
        mSearchNoResultsTextView = (TextView) findViewById(R.id.ac_tv_search_no_results);
        mPressBackImageView = (LinearLayout) findViewById(R.id.ac_iv_press_back);
        mFriendListResultLinearLayout = (LinearLayout) findViewById(R.id.ac_ll_friend_list_result);
    }

    public void initData() {

        mExecutor = new ThreadPoolExecutor(3, 5, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mFilterString = s.toString();
                mAsyncTask = new AsyncTask<String, Void, SearchResult>() {

                    @Override
                    protected SearchResult doInBackground(String... params) {
                        return filterInfo(mFilterString);
                    }

                    @Override
                    protected void onPostExecute(SearchResult searchResult) {

                        if (searchResult.getFilterStr().equals(mFilterString)) {
                            List<UserInfo> filterFriendList = searchResult.getFriendList();
                            if (filterFriendList.size() > 0) {
                                mFriendListResultLinearLayout.setVisibility(View.VISIBLE);
                                mFriendListView.setVisibility(View.VISIBLE);
                                FriendListAdapter friendListAdapter = new FriendListAdapter(filterFriendList);
                                mFriendListView.setAdapter(friendListAdapter);
                            } else {
                                mFriendListResultLinearLayout.setVisibility(View.GONE);
                                mFriendListView.setVisibility(View.GONE);
                            }

                            if (mFilterString.equals("")) {
                                mSearchNoResultsTextView.setVisibility(View.GONE);
                            }
                            if (filterFriendList.size() == 0) {
                                if (mFilterString.equals("")) {
                                    mSearchNoResultsTextView.setVisibility(View.GONE);
                                } else {
                                    mSearchNoResultsTextView.setVisibility(View.VISIBLE);
                                    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                                    spannableStringBuilder.append(getResources().getString(R.string.ac_search_no_result_pre));
                                    SpannableStringBuilder colorFilterStr = new SpannableStringBuilder(mFilterString);
                                    colorFilterStr.setSpan(new ForegroundColorSpan(Color.parseColor("#2DD0CF")), 0, mFilterString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                                    spannableStringBuilder.append(colorFilterStr);
                                    spannableStringBuilder.append(getResources().getString(R.string.ac_search_no_result_suffix));
                                    mSearchNoResultsTextView.setText(spannableStringBuilder);
                                }
                            } else {
                                mSearchNoResultsTextView.setVisibility(View.GONE);
                            }
                        }
                    }
                }.executeOnExecutor(mExecutor, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mSearchEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (mSearchEditText.getRight() - 2 * mSearchEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        filterInfo("");
                        mSearchEditText.setText("");
                        return true;
                    }
                }
                return false;
            }
        });

        mPressBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchMoreFriendsActivity.this.finish();
            }
        });

        mFriendListView.setOnItemClickListener(this);

        mSearchEditText.setText(mFilterString);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object selectObject = parent.getItemAtPosition(position);
        if (selectObject instanceof UserInfo) {
            UserInfo friend = (UserInfo) selectObject;
            final Intent intent = new Intent(SearchMoreFriendsActivity.this, ChatActivity.class);
            //创建会话
            intent.putExtra(JGApplication.TARGET_ID, friend.getUserName());
            intent.putExtra(JGApplication.TARGET_APP_KEY, friend.getAppKey());
            String notename = friend.getNotename();
            if (TextUtils.isEmpty(notename)) {
                notename = friend.getNickname();
                if (TextUtils.isEmpty(notename)) {
                    notename = friend.getUserName();
                }
            }
            intent.putExtra(JGApplication.CONV_TITLE, notename);
            Conversation conv = JMessageClient.getSingleConversation(friend.getUserName(), friend.getAppKey());
            //如果会话为空，使用EventBus通知会话列表添加新会话
            if (conv == null) {
                conv = Conversation.createSingleConversation(friend.getUserName(), friend.getAppKey());
                EventBus.getDefault().post(new Event.Builder()
                        .setType(EventType.createConversation)
                        .setConversation(conv)
                        .build());
            }
            startActivity(intent);
        }
    }

    private class FriendListAdapter extends BaseAdapter {

        private List<UserInfo> filterFriendList;

        public FriendListAdapter(List<UserInfo> filterFriendList) {
            this.filterFriendList = filterFriendList;
        }

        @Override
        public int getCount() {
            if (filterFriendList != null) {
                return filterFriendList.size();
            }
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            UserInfo friend = (UserInfo) getItem(position);
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = View.inflate(SearchMoreFriendsActivity.this, R.layout.item_filter_friend_list, null);
                viewHolder.portraitImageView = (SelectableRoundedImageView) convertView.findViewById(R.id.item_aiv_friend_image);
                viewHolder.nameSingleTextView = (TextView) convertView.findViewById(R.id.item_tv_friend_name_single);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (friend != null) {
                viewHolder.nameSingleTextView.setVisibility(View.VISIBLE);
                String noteName = friend.getNotename();
                String nickName = friend.getNickname();
                String userName = friend.getUserName();
                String name = "";
                if (TextSearcher.contains(false, noteName, mFilterString)) {
                    name = noteName;
                } else if (TextSearcher.contains(false, nickName, mFilterString)) {
                    name = nickName;
                } else if (TextSearcher.contains(false, userName, mFilterString)) {
                    name = userName;
                }
                friend.getAvatarBitmap(new GetAvatarBitmapCallback() {
                    @Override
                    public void gotResult(int responseCode, String responseMessage, Bitmap avatarBitmap) {
                        if (responseCode == 0) {
                            viewHolder.portraitImageView.setImageBitmap(avatarBitmap);
                        } else {
                            //没有头像给个默认的
                            viewHolder.portraitImageView.setImageResource(R.drawable.jmui_head_icon);
                        }
                    }
                });
                viewHolder.nameSingleTextView.setText(CharacterParser.getInstance().getColoredName(mFilterString, name));
            }

            return convertView;
        }

        @Override
        public Object getItem(int position) {
            if (filterFriendList == null)
                return null;

            if (position >= filterFriendList.size())
                return null;

            return filterFriendList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }

    class ViewHolder {
        SelectableRoundedImageView portraitImageView;
        TextView nameSingleTextView;
    }

    @Override
    protected void onDestroy() {
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
            mAsyncTask = null;
        }
        super.onDestroy();
    }

    private synchronized SearchResult filterInfo(String filterStr) {
        List<UserInfo> filterFriendList = new ArrayList<>();
        SearchResult searchResult = new SearchResult();

        if (filterStr.equals("")) {
            SearchResult result = new SearchResult();
            result.setFilterStr("");
            result.setFriendList(filterFriendList);
            return result;
        }
        if (filterStr.contains("'")) {
            SearchResult result = new SearchResult();
            result.setFilterStr(filterStr);
            result.setFriendList(filterFriendList);
            return result;
        }

        //所有好友名单
        List<UserInfo> mFriendInfoList = JGApplication.mFriendInfoList;
        for (UserInfo friendInfo : mFriendInfoList) {
            if (TextSearcher.contains(false, friendInfo.getNotename(), filterStr) ||
                    TextSearcher.contains(false, friendInfo.getNickname(), filterStr) ||
                    TextSearcher.contains(false, friendInfo.getUserName(), filterStr)) {
                filterFriendList.add(friendInfo);
            }
        }

        searchResult.setFriendList(filterFriendList);
        searchResult.setFilterStr(filterStr);
        return searchResult;
    }
}
