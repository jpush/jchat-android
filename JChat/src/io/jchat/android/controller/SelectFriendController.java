package io.jchat.android.controller;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.R;
import io.jchat.android.activity.SelectFriendActivity;
import io.jchat.android.adapter.StickyListAdapter;
import io.jchat.android.entity.UserLetterBean;
import io.jchat.android.tools.HanziToPinyin;
import io.jchat.android.tools.PinyinComparator;
import io.jchat.android.view.SelectFriendView;
import io.jchat.android.view.SideBar;

/**
 * Created by Ken on 2015/10/9.
 */
public class SelectFriendController implements View.OnClickListener,
        SideBar.OnTouchingLetterChangedListener, TextWatcher{

    private SelectFriendView mSelectFriendView;
    private SelectFriendActivity mContext;
    private StickyListAdapter mAdapter;
    private List<UserInfo> mData;
    private List<UserLetterBean> mSortBean;

    public SelectFriendController(SelectFriendView view, SelectFriendActivity context) {
        this.mSelectFriendView = view;
        this.mContext = context;
        initData();
        Collections.sort(mSortBean, new PinyinComparator());
        mAdapter = new StickyListAdapter(context, mSortBean, true);
        mSelectFriendView.setAdapter(mAdapter);
    }

    private void initData() {
        mData = new ArrayList<UserInfo>();
        mSortBean = new ArrayList<UserLetterBean>();
        for (int i = 0; i < mData.size(); i++) {
            mSortBean.get(i).setNickname(mData.get(i).getNickname());
            ArrayList<HanziToPinyin.Token> tokens = HanziToPinyin.getInstance()
                    .get(mSortBean.get(i).getNickname());
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
                mSortBean.get(i).setLetter(sortString.toUpperCase());
            } else {
                mSortBean.get(i).setLetter("#");
            }

        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.cancel_btn:
                mContext.finish();
                break;
        }
    }

    @Override
    public void onTouchingLetterChanged(String s) {
        //该字母首次出现的位置
        int position = mAdapter.getSectionForLetter(s);
        Log.d("SelectFriendController", "Section position: " + position);
        if (position != -1 && position < mAdapter.getCount()) {
            mSelectFriendView.setSelection(position);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        filterData(s.toString());
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    /**
     * 根据输入框中的值来过滤数据并更新ListView
     * @param filterStr
     */
    private void filterData(String filterStr) {
        List<UserLetterBean> filterDateList = new ArrayList<UserLetterBean>();

        if (TextUtils.isEmpty(filterStr)) {
            filterDateList = mSortBean;
        }else {
            filterDateList.clear();
            for(UserLetterBean sortBean : mSortBean) {
                String name = sortBean.getNickname();
                if (name.contains(filterStr) || name.startsWith(filterStr)
                        || sortBean.getLetter().equals(filterStr.substring(0, 1).toUpperCase())) {
                    filterDateList.add(sortBean);
                }
            }
        }

        // 根据a-z进行排序
//        Collections.sort(filterDateList, new PinyinComparator());
        mAdapter.updateListView(filterDateList);
    }

    public List<UserInfo> getSelectedData(){
        List<UserInfo> list = new ArrayList<UserInfo>();
        SparseBooleanArray array = mAdapter.getSelectedMap();
        for (int i = 0; i < array.size(); i++) {
            list.add(mData.get(array.keyAt(i)));
        }
        return list;
    }

}
