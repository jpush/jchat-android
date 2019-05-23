package jiguang.chat.activity;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.jpush.im.android.api.ChatRoomManager;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.callback.RequestCallback;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import jiguang.chat.R;
import jiguang.chat.application.JGApplication;
import jiguang.chat.model.InfoModel;
import jiguang.chat.utils.ToastUtil;
import jiguang.chat.utils.dialog.LoadDialog;
import jiguang.chat.utils.photochoose.SelectableRoundedImageView;

public class SearchForChatRoomActivity extends BaseActivity implements View.OnClickListener {
    private EditText mEtSearchUser;
    private Button mBtnSearch;
    private Button mSearchAddBtn;
    private TextView mTvAddNot;
    private LinearLayout mSearch_result;
    private SelectableRoundedImageView mSearch_header;
    private TextView mSearchName;
    private ImageView mIvClear;
    private long roomId;
    private List<Long> keeperList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_for_chat_room);
        roomId = getIntent().getLongExtra(JGApplication.ROOM_ID, 0);
        mEtSearchUser = (EditText) findViewById(R.id.et_searchUser);
        mEtSearchUser.addTextChangedListener(new TextChange());
        mBtnSearch = (Button) findViewById(R.id.btn_search);
        mBtnSearch.setOnClickListener(this);
        mSearchAddBtn = (Button) findViewById(R.id.search_addBtn);
        mSearchAddBtn.setOnClickListener(this);
        mTvAddNot = (TextView) findViewById(R.id.search_addNot);
        mSearch_result = (LinearLayout) findViewById(R.id.search_result);
        mSearch_header = (SelectableRoundedImageView) findViewById(R.id.search_header);
        mSearchName = (TextView) findViewById(R.id.search_name);
        mIvClear = (ImageView) findViewById(R.id.iv_clear);
        mIvClear.setOnClickListener(this);

        initTitle(true, true, "添加管理员", "", false, "");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_search:
                hintKbTwo();
                String searchUserName = mEtSearchUser.getText().toString();
                if (!TextUtils.isEmpty(searchUserName)) {
                    LoadDialog.show(this);
                    ChatRoomManager.getChatRoomAdminList(roomId, new RequestCallback<List<UserInfo>>() {
                        @Override
                        public void gotResult(int responseCode, String responseMessage, List<UserInfo> userInfos) {
                            if (responseCode == 0) {
                                keeperList.clear();
                                for (UserInfo userInfo : userInfos) {
                                    keeperList.add(userInfo.getUserID());
                                }
                            }
                            JMessageClient.getUserInfo(searchUserName, new GetUserInfoCallback() {
                                @Override
                                public void gotResult(int responseCode, String responseMessage, UserInfo info) {
                                    LoadDialog.dismiss(SearchForChatRoomActivity.this);
                                    if (responseCode == 0) {
                                        InfoModel.getInstance().friendInfo = info;
                                        mSearch_result.setVisibility(View.VISIBLE);
                                        //已经是管理员则不显示"添加"按钮
                                        if (keeperList.contains(info.getUserID())) {
                                            mSearchAddBtn.setVisibility(View.GONE);
                                            mTvAddNot.setVisibility(View.VISIBLE);
                                            mTvAddNot.setText("已添加");
                                        } else {
                                            mSearchAddBtn.setVisibility(View.VISIBLE);
                                            mTvAddNot.setVisibility(View.GONE);
                                        }
                                        File avatarFile = info.getAvatarFile();
                                        if (avatarFile != null) {
                                            mSearch_header.setImageBitmap(BitmapFactory.decodeFile(avatarFile.getAbsolutePath()));
                                        } else {
                                            mSearch_header.setImageResource(R.drawable.rc_default_portrait);
                                        }
                                        mSearchName.setText(info.getDisplayName());
                                    } else {
                                        ToastUtil.shortToast(SearchForChatRoomActivity.this, "该用户不存在");
                                        mSearch_result.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }
                    });
                }
                break;
            case R.id.search_addBtn:
                ChatRoomManager.addChatRoomAdmin(roomId, Collections.singletonList(InfoModel.getInstance().friendInfo), new BasicCallback() {
                    @Override
                    public void gotResult(int i, String s) {
                        if (i == 0) {
                            ToastUtil.shortToast(SearchForChatRoomActivity.this, "添加成功");
                        } else {
                            handleErrorCode(i);
                        }

                        mSearch_result.setVisibility(View.GONE);
                    }
                });
                break;
            case R.id.iv_clear:
                mEtSearchUser.setText("");
                break;
            default:

        }
    }

    private void handleErrorCode(int code) {
        String result = "添加失败,code:" + code;
        switch (code) {
            case 7130004:
                result = "添加失败，管理员人数已达上限";
                break;
            case 7130006:
                result = "添加失败，用户不在聊天室中";
                break;
            default:
        }
        ToastUtil.shortToast(SearchForChatRoomActivity.this, result);
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
            boolean feedback = mEtSearchUser.getText().length() > 0;
            if (feedback) {
                mIvClear.setVisibility(View.VISIBLE);
                mBtnSearch.setEnabled(true);
            } else {
                mIvClear.setVisibility(View.GONE);
                mBtnSearch.setEnabled(false);
            }
        }
    }

    private void hintKbTwo() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && getCurrentFocus() != null) {
            if (getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }
}
