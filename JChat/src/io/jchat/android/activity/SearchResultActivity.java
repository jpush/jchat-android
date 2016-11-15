package io.jchat.android.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import io.jchat.android.R;

public class SearchResultActivity extends BaseActivity {

    private ImageButton mReturnBtn;
    private TextView mTitle;
    private Button mRightBtn;
    private Context mContext;
    private ListView mSearchLV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_search_result);
        mReturnBtn = (ImageButton) findViewById(R.id.return_btn);
        mTitle = (TextView) findViewById(R.id.jmui_title_tv);
        mTitle.setText(mContext.getString(R.string.search_friend_title_bar));
        mRightBtn = (Button) findViewById(R.id.jmui_commit_btn);
        mRightBtn.setVisibility(View.GONE);
        mSearchLV = (ListView) findViewById(R.id.search_list_view);
        mSearchLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
    }
}
