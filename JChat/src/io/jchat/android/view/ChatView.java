package io.jchat.android.view;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import io.jchat.android.R;

import cn.jpush.im.android.api.model.Conversation;

import io.jchat.android.adapter.MsgListAdapter;
import io.jchat.android.controller.ChatController;
import io.jchat.android.controller.RecordVoiceBtnController;
import io.jchat.android.tools.SharePreferenceManager;

public class ChatView extends RelativeLayout{

	private LinearLayout mBackground;
	private TableLayout mMoreMenuTl;
	private ListView mChatListView;
	private ImageButton mReturnBtn;
	private ImageButton mRightBtn;
	private TextView mChatTitle;
	private RecordVoiceBtnController mVoiceBtn;
	public EditText mChatInputEt;
	private ImageButton mSwitchIb;
	private ImageButton mExpressionIb;
	private ImageButton mAddFileIb;
	private ImageButton mTakePhotoIb;
	private ImageButton mPickPictureIb;
	private ImageButton mLocationIb;
	private ImageButton mSendVoiceIb;
	private Button mSendMsgBtn;
	private View mLoadingMessage;
	Context mContext;

	public ChatView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		this.mContext = context;
	}


	public void initModule(){
		mChatListView = (ListView) findViewById(R.id.chat_list);
		mReturnBtn = (ImageButton) findViewById(R.id.return_btn);
		mRightBtn = (ImageButton) findViewById(R.id.right_btn);
		mChatTitle = (TextView) findViewById(R.id.title);
		mVoiceBtn = (RecordVoiceBtnController) findViewById(R.id.voice_btn);
		mChatInputEt = (EditText) findViewById(R.id.chat_input_et);
		mSwitchIb = (ImageButton) findViewById(R.id.switch_voice_ib);
		mExpressionIb = (ImageButton) findViewById(R.id.expression_btn);
		mAddFileIb = (ImageButton) findViewById(R.id.add_file_btn);
		mTakePhotoIb = (ImageButton) findViewById(R.id.pick_from_camera_btn);
		mPickPictureIb = (ImageButton) findViewById(R.id.pick_from_local_btn);
		mLocationIb = (ImageButton) findViewById(R.id.send_location_btn);
		mSendVoiceIb = (ImageButton) findViewById(R.id.send_voice_btn);
		mSendMsgBtn = (Button) findViewById(R.id.send_msg_btn);
		mBackground = (LinearLayout) findViewById(R.id.chat_background);
		mMoreMenuTl = (TableLayout) findViewById(R.id.more_menu_tl);
		mBackground.requestFocus();
		mLoadingMessage = LayoutInflater.from(mContext).inflate(R.layout.loading_message_view,null);
		mChatInputEt.addTextChangedListener(watcher);
		mChatInputEt.setOnFocusChangeListener(listener);
        mChatInputEt.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        mChatInputEt.setSingleLine(false);
        mChatInputEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    dismissMoreMenu();
                    Log.i("ChatView", "dismissMoreMenu()----------");
                }
                return false;
            }
        });
        mChatInputEt.setMaxLines(4);
        setMoreMenuHeight();
	}

    public void setMoreMenuHeight() {
        int softKeyboardHeight = SharePreferenceManager.getCachedKeyboardHeight();
        if(softKeyboardHeight > 0){
            mMoreMenuTl.setLayoutParams(new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, softKeyboardHeight));
        }

    }


//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int height = MeasureSpec.getSize(heightMeasureSpec);
//        Rect rect = new Rect();
//        Activity activity = (Activity)getContext();
//        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
//        int statusBarHeight = rect.top;
//        int screenHeight = activity.getWindowManager().getDefaultDisplay().getHeight();
//        int diff = (screenHeight - statusBarHeight) - height;
//        if(mListener != null){
//            mListener.onSoftKeyboardShown(diff>128, diff);
//        }
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//    }

    private TextWatcher watcher = new TextWatcher(){
		private CharSequence temp = "";
		@Override
		public void afterTextChanged(Editable arg0) {
			// TODO Auto-generated method stub
			if(temp.length() > 0){
				mAddFileIb.setVisibility(View.GONE);
				mSendMsgBtn.setVisibility(View.VISIBLE);
			}else{
				mAddFileIb.setVisibility(View.VISIBLE);
				mSendMsgBtn.setVisibility(View.GONE);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			temp = s;
		}

	};

    public void focusToInput(boolean inputFocus){
        if(inputFocus){
            mChatInputEt.requestFocus();
            Log.i("ChatView", "show softInput");
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }else {
            mAddFileIb.requestFocusFromTouch();
        }
    }

	OnFocusChangeListener listener = new OnFocusChangeListener() {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(hasFocus){
                Log.i("ChatView", "Input focus");
                showMoreMenu();
//				invisibleMoreMenu();
                ChatController.mIsShowMoreMenu = true;
			}
		}
	};

	public void setListeners(OnClickListener onClickListener){
		mReturnBtn.setOnClickListener(onClickListener);
		mRightBtn.setOnClickListener(onClickListener);
        mChatInputEt.setOnClickListener(onClickListener);
		mSendMsgBtn.setOnClickListener(onClickListener);
		mSwitchIb.setOnClickListener(onClickListener);
		mVoiceBtn.setOnClickListener(onClickListener);
        mExpressionIb.setOnClickListener(onClickListener);
		mAddFileIb.setOnClickListener(onClickListener);
		mTakePhotoIb.setOnClickListener(onClickListener);
		mPickPictureIb.setOnClickListener(onClickListener);
		mLocationIb.setOnClickListener(onClickListener);
		mSendVoiceIb.setOnClickListener(onClickListener);
	}

    public void setOnTouchListener(OnTouchListener listener){
        mChatListView.setOnTouchListener(listener);
    }

	public void setChatListAdapter(MsgListAdapter adapter) {
		mChatListView.setAdapter(adapter);
		setToBottom();
	}

	public void setOnScrollListener(OnScrollListener onScrollChangedListener){
		mChatListView.setOnScrollListener(onScrollChangedListener);
	}

	//如果是文字输入
	public void isKeyBoard(){
		mSwitchIb.setBackgroundResource(R.drawable.voice);
		mChatInputEt.setVisibility(View.VISIBLE);
		mVoiceBtn.setVisibility(View.GONE);
		mExpressionIb.setVisibility(View.GONE);
        if(mChatInputEt.getText().length() > 0){
            mSendMsgBtn.setVisibility(View.VISIBLE);
            mAddFileIb.setVisibility(View.GONE);
        }else {
            mSendMsgBtn.setVisibility(View.GONE);
            mAddFileIb.setVisibility(View.VISIBLE);
        }
	}

	//语音输入
	public void notKeyBoard(Conversation conv, MsgListAdapter adapter) {
		mChatInputEt.setVisibility(View.GONE);
		mSwitchIb.setBackgroundResource(R.drawable.keyboard);
		mVoiceBtn.setVisibility(View.VISIBLE);
		mVoiceBtn.initConv(conv, adapter);
		mExpressionIb.setVisibility(View.GONE);
        mSendMsgBtn.setVisibility(View.GONE);
        mAddFileIb.setVisibility(View.VISIBLE);
	}

	public String getChatInput(){
		return mChatInputEt.getText().toString();
	}



	public void setChatTitle(String targetId){
		mChatTitle.setText(targetId);
	}

	public void clearInput() {
		mChatInputEt.setText("");
	}

	public void setToBottom() {
			mChatListView.post(new Runnable() {
				@Override
				public void run() {
					mChatListView.setSelection(mChatListView.getBottom());
				}
			});
	}

	public void removeHeadView() {
		mChatListView.removeHeaderView(mLoadingMessage);
	}

	public void addHeadView() {
		mChatListView.addHeaderView(mLoadingMessage);
	}

	public void setGroupIcon() {
		mRightBtn.setImageResource(R.drawable.group_chat_detail);
	}


	public void showMoreMenu() {
		mMoreMenuTl.setVisibility(View.VISIBLE);
	}

    public void invisibleMoreMenu(){
        mMoreMenuTl.setVisibility(View.INVISIBLE);
    }

	public void dismissMoreMenu(){
		mMoreMenuTl.setVisibility(View.GONE);
	}

    public void dismissRightBtn() {
        mRightBtn.setVisibility(View.GONE);
    }

    public void showRightBtn() {
        mRightBtn.setVisibility(View.VISIBLE);
    }

    public void dismissRecordDialog() {
     mVoiceBtn.dismissDialog();
    }

    public void releaseRecorder() {
        mVoiceBtn.releaseRecorder();
    }

    public void resetMoreMenuHeight() {
        mMoreMenuTl.setLayoutParams(new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 0));
    }
}
