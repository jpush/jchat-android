package jiguang.chat.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.model.GroupInfo;
import jiguang.chat.R;
import jiguang.chat.utils.photochoose.SelectableRoundedImageView;

/**
 * Created by ${chenyn} on 2017/11/6.
 */

public class SearchAddOpenGroupActivity extends BaseActivity {

    private EditText mEt_searchGroup;
    private Button mBtn_search;
    private ImageView mIv_clear;
    private LinearLayout mLl_group;
    private SelectableRoundedImageView mSearch_group_avatar;
    private TextView mTv_groupName;
    private TextView mTv_groupID;
    private TextView mTv_groupDesc;
    private GroupInfo mGroupInfo;
    private TextView mTv_searchResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_add_open_group);

        mEt_searchGroup = (EditText) findViewById(R.id.et_searchGroup);
        mBtn_search = (Button) findViewById(R.id.btn_search);
        mIv_clear = (ImageView) findViewById(R.id.iv_clear);

        mLl_group = (LinearLayout) findViewById(R.id.ll_group);
        mSearch_group_avatar = (SelectableRoundedImageView) findViewById(R.id.search_group_avatar);
        mTv_groupName = (TextView) findViewById(R.id.tv_groupName);
        mTv_groupID = (TextView) findViewById(R.id.tv_groupID);
        mTv_groupDesc = (TextView) findViewById(R.id.tv_groupDesc);
        mTv_searchResult = (TextView) findViewById(R.id.tv_searchResult);

        initTitle(true, true, "加入公开群", "", false, "");

        initData();
    }

    private void initData() {
        mEt_searchGroup.addTextChangedListener(new TextChange());
        mIv_clear.setOnClickListener(v -> mEt_searchGroup.setText(""));
        ProgressDialog dialog = new ProgressDialog(SearchAddOpenGroupActivity.this);
        dialog.setMessage("正在加载...");
        dialog.setCanceledOnTouchOutside(false);
        mBtn_search.setOnClickListener(v -> {
            if (mEt_searchGroup.getText() != null) {
                String groupId = mEt_searchGroup.getText().toString().trim();
                JMessageClient.getGroupInfo(Long.parseLong(groupId), new GetGroupInfoCallback() {
                    @Override
                    public void gotResult(int i, String s, GroupInfo groupInfo) {
                        dialog.dismiss();
                        if (i == 0 && (groupInfo.getGroupFlag() == 2)) {
                            mGroupInfo = groupInfo;
                            mTv_searchResult.setVisibility(View.GONE);
                            mLl_group.setVisibility(View.VISIBLE);

                            if (groupInfo.getAvatarFile() != null) {
                                mSearch_group_avatar.setImageBitmap(BitmapFactory.decodeFile(groupInfo.getAvatarFile().getAbsolutePath()));
                            } else {
                                mSearch_group_avatar.setImageResource(R.drawable.jmui_head_icon);
                            }
                            mTv_groupName.setText(groupInfo.getGroupName());
                            mTv_groupID.setText(groupInfo.getGroupID() + "");
                            mTv_groupDesc.setText(groupInfo.getGroupDescription());
                        } else {
                            mTv_searchResult.setVisibility(View.VISIBLE);
                            mLl_group.setVisibility(View.GONE);
                        }
                    }
                });
            } else {
                Toast.makeText(SearchAddOpenGroupActivity.this, "请输入群组id", Toast.LENGTH_SHORT).show();
            }
        });

        mLl_group.setOnClickListener(v -> {
            //申请加入群
            Intent intent = new Intent(SearchAddOpenGroupActivity.this, GroupInfoActivity.class);
            if (mGroupInfo != null) {
                if (mGroupInfo.getAvatarFile() != null) {
                    intent.putExtra("groupAvatar", mGroupInfo.getAvatarFile().getAbsolutePath());
                }
                intent.putExtra("groupName", mGroupInfo.getGroupName());
                intent.putExtra("groupId", mGroupInfo.getGroupID());
                intent.putExtra("groupOwner", mGroupInfo.getGroupOwner());
                intent.putExtra("groupMember", mGroupInfo.getGroupMembers().size() + "");
                intent.putExtra("groupDesc", mGroupInfo.getGroupDescription());
                startActivity(intent);
            }
        });
    }

    private class TextChange implements TextWatcher {

        @Override
        public void afterTextChanged(Editable arg0) {
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
        }

        @Override
        public void onTextChanged(CharSequence cs, int start, int before,
                                  int count) {
            boolean feedback = mEt_searchGroup.getText().length() > 0;
            if (feedback) {
                mIv_clear.setVisibility(View.VISIBLE);
                mBtn_search.setEnabled(true);
            } else {
                mIv_clear.setVisibility(View.GONE);
                mBtn_search.setEnabled(false);
            }
        }
    }
}
