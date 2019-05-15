package jiguang.chat.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import jiguang.chat.R;

/**
 * Created by ${chenyn} on 2017/2/27.
 */

public class NickSignActivity extends BaseActivity {
    public static final String TYPE = "type";
    public static final String COUNT = "count";
    public static final String DESC = "desc";

    public enum Type {
        GROUP_NAME, GROUP_DESC, PERSON_SIGN, PERSON_NICK, CHAT_ROOM_NAME, CHAT_ROOM_DESC;
    }

    private EditText mEd_sign;
    private TextView mTv_title;
    private TextView mTv_count;
    private LinearLayout mLl_nickSign;

    private Button mJmui_commit_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nick_sign);

        initView();
        Intent intent = getIntent();
        Type type = (Type) intent.getSerializableExtra(TYPE);
        int count = intent.getIntExtra(COUNT, 0);
        switch (type) {
            case PERSON_SIGN:
                initViewSign("个性签名", count);
                break;
            case PERSON_NICK:
                initViewNick("修改昵称", count);
                break;
            case GROUP_DESC:
                initViewSign("群描述", count);
                break;
            case GROUP_NAME:
                initViewNick("群名称", count);
                break;
            case CHAT_ROOM_NAME:
            case CHAT_ROOM_DESC:
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mEd_sign.getLayoutParams();
                layoutParams.leftMargin = (int) (10 * mDensity);
                layoutParams.rightMargin = (int) (10 * mDensity);
                layoutParams.topMargin = (int) (5 * mDensity);
                if (type == Type.CHAT_ROOM_NAME) {
                    initViewNick("聊天室名称", count);
                    mTv_title.setText("聊天室名称");
                    layoutParams.bottomMargin = (int) (20 * mDensity);
                } else {
                    initViewSign("聊天室介绍", count);
                    mTv_title.setText("聊天室介绍");
                    layoutParams.bottomMargin = (int) (30 * mDensity);
                }
                mTv_count.setVisibility(View.GONE);
                mTv_title.setVisibility(View.VISIBLE);
                mEd_sign.setFocusable(false);
                mEd_sign.setFocusableInTouchMode(false);
                mEd_sign.setBackgroundColor(Color.parseColor("#FFE8EDF3"));
                mEd_sign.setLayoutParams(layoutParams);
                mLl_nickSign.setBackgroundColor(Color.WHITE);
                mJmui_commit_btn.setVisibility(View.GONE);
                break;

            default:
                break;
        }
        initData(count);
        initListener(type);
    }

    private void initListener(Type type) {
        mJmui_commit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sign = mEd_sign.getText().toString();
                Intent intent = new Intent();
                switch (type) {
                    case PERSON_NICK:
                        intent.putExtra(PersonalActivity.NICK_NAME_KEY, sign);
                        setResult(PersonalActivity.NICK_NAME, intent);//4
                        break;
                    case PERSON_SIGN:
                        intent.putExtra(PersonalActivity.SIGN_KEY, sign);
                        setResult(PersonalActivity.SIGN, intent);//1
                        break;
                    case GROUP_DESC:
                        intent.putExtra(ChatDetailActivity.GROUP_DESC_KEY, sign);
                        setResult(ChatDetailActivity.GROUP_DESC, intent);//70
                        break;
                    case GROUP_NAME:
                        intent.putExtra(ChatDetailActivity.GROUP_NAME_KEY, sign);
                        setResult(ChatDetailActivity.GROUP_NAME, intent);//72
                        break;
                    default:
                        break;
                }
                //做更新动作
                finish();
            }
        });
    }

    int input;

    private void initData(final int countNum) {
        mEd_sign.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                input = s.toString().substring(start).getBytes().length;
            }

            @Override
            public void afterTextChanged(Editable s) {
                int num = countNum - s.toString().getBytes().length;
                mTv_count.setText(num + "");
            }
        });
    }

    private void initView() {
        mEd_sign = (EditText) findViewById(R.id.ed_sign);
        mLl_nickSign = (LinearLayout) findViewById(R.id.ll_nickSign);
        mTv_count = (TextView) findViewById(R.id.tv_count);
        mJmui_commit_btn = (Button) findViewById(R.id.jmui_commit_btn);
        mTv_title = (TextView) findViewById(R.id.tv_title);
        mEd_sign.setText(getIntent().getStringExtra(DESC));
        mEd_sign.setSelection(mEd_sign.getText().length());

    }

    private void initViewSign(String str, int flag) {
        initTitle(true, true, str, "", true, "完成");
        //限制输入的最大长度
        mEd_sign.setFilters(new InputFilter[] {new MyLengthFilter(flag)});
        //如果初始有昵称/签名,控制右下字符数
        int length = mEd_sign.getText().toString().getBytes().length;
        mTv_count.setText(flag - length + "");
    }

    private void initViewNick(String str, int count) {
        initTitle(true, true, str, "", true, "完成");
        mEd_sign.setFilters(new InputFilter[] {new MyLengthFilter(count)});
        int length = mEd_sign.getText().toString().getBytes().length;
        mTv_count.setText(count - length + "");


        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        mLl_nickSign.setLayoutParams(params);
    }

    public static class MyLengthFilter implements InputFilter {
        private final int mMax;

        public MyLengthFilter(int max) {
            mMax = max;
        }

        public CharSequence filter(CharSequence source, int start, int end, Spanned dest,
                                   int dstart, int dend) {
            int keep = mMax - (dest.toString().getBytes().length - (dend - dstart));
            if (keep <= 0) {
                return "";
            } else if (keep >= source.toString().getBytes().length) {
                return null; // keep original
            } else {
                return "";
            }
        }

        /**
         * @return the maximum length enforced by this input filter
         */
        public int getMax() {
            return mMax;
        }
    }
}
