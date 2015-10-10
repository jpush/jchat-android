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

import io.jchat.android.R;
import io.jchat.android.activity.SelectFriendActivity;
import io.jchat.android.adapter.StickyListAdapter;
import io.jchat.android.entity.TestSortModel;
import io.jchat.android.tools.HanziToPinyin;
import io.jchat.android.tools.PinyinComparator;
import io.jchat.android.view.SelectFriendView;
import io.jchat.android.view.SideBar;

/**
 * Created by jpush on 2015/10/9.
 */
public class SelectFriendController implements View.OnClickListener,
        SideBar.OnTouchingLetterChangedListener, TextWatcher{

    private SelectFriendView mSelectFriendView;
    private SelectFriendActivity mContext;
    private StickyListAdapter mAdapter;
    private List<TestSortModel> mData;

    public SelectFriendController(SelectFriendView view, SelectFriendActivity context) {
        this.mSelectFriendView = view;
        this.mContext = context;
        mData = initData(context.getResources().getStringArray(R.array.date));
        Collections.sort(mData, new PinyinComparator());
        mAdapter = new StickyListAdapter(context, mData, true);
        mSelectFriendView.setAdapter(mAdapter);
    }

    private List<TestSortModel> initData(String[] data) {
        List<TestSortModel> list = new ArrayList<TestSortModel>();
        for (int i = 0; i < data.length; i++) {
            TestSortModel sortModel = new TestSortModel();
            sortModel.setName(data[i]);

            ArrayList<HanziToPinyin.Token> tokens = HanziToPinyin.getInstance().get(data[i]);
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
                sortModel.setSortLetters(sortString.toUpperCase());
            } else {
                sortModel.setSortLetters("#");
            }

            list.add(sortModel);
        }
        return list;
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
    private void filterData(String filterStr){
        List<TestSortModel> filterDateList = new ArrayList<TestSortModel>();

        if(TextUtils.isEmpty(filterStr)){
            filterDateList = mData;
        }else{
            filterDateList.clear();
            for(TestSortModel sortModel : mData){
                String name = sortModel.getName();
                if(name.contains(filterStr) || name.startsWith(filterStr)
                        || sortModel.getSortLetters().equals(filterStr.substring(0, 1).toUpperCase())){
                    filterDateList.add(sortModel);
                }
            }
        }

        // 根据a-z进行排序
        Collections.sort(filterDateList, new PinyinComparator());
        mAdapter.updateListView(filterDateList);
    }

    public List<TestSortModel> getSelectedData(){
        List<TestSortModel> list = new ArrayList<TestSortModel>();
        SparseBooleanArray array = mAdapter.getSelectedMap();
        for (int i = 0; i < array.size(); i++){
            list.add(mData.get(array.keyAt(i)));
        }
        return list;
    }

}
