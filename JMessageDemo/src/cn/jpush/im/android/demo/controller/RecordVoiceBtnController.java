package cn.jpush.im.android.demo.controller;

/*
 * 录音按钮，之所以放到Controller包下，是因为涉及到文件读写、消息发送等事件
 */

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.content.VoiceContent;
import cn.jpush.im.android.demo.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import cn.jpush.im.android.demo.adapter.MsgListAdapter;
import cn.jpush.im.android.demo.tools.HandleResponseCode;
import cn.jpush.im.api.BasicCallback;

public class RecordVoiceBtnController extends Button {
    
    private static final String TAG = "RecordVoiceBtnController";

    private File myRecAudioFile;

    private MsgListAdapter mMsgListAdapter;
    private static final int MIN_INTERVAL_TIME = 1000;// 1s
    //依次为按下录音键坐标、手指离开屏幕坐标、手指移动坐标
    float mTouchY1, mTouchY2, mTouchY;
    private final float MIN_CANCEL_DISTANCE = 300f;
    //依次为开始录音时刻，按下录音时刻，松开录音按钮时刻
    private long startTime, time1, time2;

    private Dialog recordIndicator;

    private static int[] res = {R.drawable.mic_1, R.drawable.mic_2,
            R.drawable.mic_3, R.drawable.mic_4, R.drawable.mic_5, R.drawable.cancel_record};

    private static ImageView view;
    private ImageView mVolumeIv;
    private TextView mRecordHintTv;

    private MediaRecorder recorder;

    private ObtainDecibelThread mThread;

    private Handler mVolumeHandler;
    private boolean sdCardExist;
    public static boolean mIsPressed = false;
    private Context mContext;
    private Conversation mConv;
    private Timer timer = new Timer();
	private Timer mCountTimer;
    private boolean isTimerCanceled = false;
    private boolean mTimeUp = false;

    public RecordVoiceBtnController(Context context) {
        super(context);
//		this.mContext = context;
        init();
    }

    public RecordVoiceBtnController(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        init();
    }

    public RecordVoiceBtnController(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    private void init() {
        mVolumeHandler = new ShowVolumeHandler();
    }

    public void initConv(Conversation conv, MsgListAdapter adapter) {
        this.mConv = conv;
        this.mMsgListAdapter = adapter;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.setPressed(true);
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                this.setText(mContext.getString(R.string.send_voice_hint));
                mIsPressed = true;
                time1 = System.currentTimeMillis();
                mTouchY1 = event.getY();
                //检查sd卡是否存在
                sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
                if (sdCardExist) {
                    if (isTimerCanceled) {
                        timer = createTimer();
                    }
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            android.os.Message msg = handler.obtainMessage();
                            msg.what = 7;
                            msg.sendToTarget();
                        }
                    }, 500);
                } else {
                    Toast.makeText(this.getContext(), mContext.getString(R.string.sdcard_not_exist_toast), Toast.LENGTH_SHORT).show();
                    this.setPressed(false);
                    this.setText(mContext.getString(R.string.record_voice_hint));
                    mIsPressed = false;
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
                this.setText(mContext.getString(R.string.record_voice_hint));
                mIsPressed = false;
                this.setPressed(false);
                mTouchY2 = event.getY();
                time2 = System.currentTimeMillis();
                if (time2 - time1 < 500) {
                    cancelTimer();
                    return true;
                } else if (time2 - time1 < 1000) {
                    cancelRecord();
                } else if (mTouchY1 - mTouchY2 > MIN_CANCEL_DISTANCE){
                    cancelRecord();
				}else if(time2 - time1 < 60000)		
					finishRecord();
                break;
            case MotionEvent.ACTION_MOVE:
                mTouchY = event.getY();
                //手指上滑到超出限定后，显示松开取消发送提示
                if (mTouchY1 - mTouchY > MIN_CANCEL_DISTANCE) {
                    this.setText(mContext.getString(R.string.cancel_record_voice_hint));
                    mVolumeHandler.sendEmptyMessage(5);
                    if (mThread != null)
                        mThread.exit();
                    mThread = null;
                } else {
                    this.setText(mContext.getString(R.string.send_voice_hint));
                    if (mThread == null) {
                        mThread = new ObtainDecibelThread();
                        mThread.start();
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:// 当手指移动到view外面，会cancel
//			mTouchY = event.getY();
//			if(mTouchY1 - mTouchY > MIN_CANCEL_DISTANCE){
//				android.os.Message msg = handler.obtainMessage();
//				msg.what = 5;
//				msg.sendToTarget();
//				return true;
//			}
                break;
        }

        return true;
    }


    private void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            isTimerCanceled = true;
			}
		if (mCountTimer != null){
			mCountTimer.cancel();
			mCountTimer.purge();
		}
    }

    private Timer createTimer() {
        timer = new Timer();
        isTimerCanceled = false;
        return timer;
    }

    private void initDialogAndStartRecord() {
        //存放录音文件目录
        String filePath = "sdcard/JPushDemo/voice/";
        File destDir = new File(filePath);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        //录音文件的命名格式
        myRecAudioFile = new File(filePath, new DateFormat().format("yyyyMMdd_hhmmss",
                Calendar.getInstance(Locale.CHINA)) + ".amr");
        if (myRecAudioFile == null) {
            cancelTimer();
            stopRecording();
            Toast.makeText(mContext, mContext.getString(R.string.create_file_failed), Toast.LENGTH_SHORT).show();
        }
        Log.i("FileCreate", "Create file success file path: " + myRecAudioFile.getAbsolutePath());
//        recordIndicator = new Dialog(getContext(),
//                R.style.record_voice_dialog);
//        view = new ImageView(getContext());
//        view.setImageResource(R.drawable.mic_2);
//        view.setAlpha(215);
//        recordIndicator.setContentView(view, new LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.MATCH_PARENT));
//        recordIndicator.setOnDismissListener(onDismiss);
//        LayoutParams lp = recordIndicator.getWindow().getAttributes();
//        lp.gravity = Gravity.CENTER;
//        startRecording();
//        recordIndicator.show();
        recordIndicator = new Dialog(getContext(), R.style.record_voice_dialog);
        recordIndicator.setContentView(R.layout.dialog_record_voice);
        mVolumeIv = (ImageView) recordIndicator.findViewById(R.id.volume_hint_iv);
        mRecordHintTv = (TextView) recordIndicator.findViewById(R.id.record_voice_tv);
        mRecordHintTv.setText(mContext.getString(R.string.move_to_cancel_hint));
        startRecording();
        recordIndicator.show();
    }

    //录音完毕加载 ListView item
    private void finishRecord() {
        cancelTimer();
        stopRecording();
        if (recordIndicator != null)
            recordIndicator.dismiss();

        long intervalTime = System.currentTimeMillis() - startTime;
        if (intervalTime < MIN_INTERVAL_TIME) {
            Toast.makeText(getContext(), mContext.getString(R.string.time_too_short_toast), Toast.LENGTH_SHORT).show();
            myRecAudioFile.delete();
            return;
        } else {
            if(myRecAudioFile != null && myRecAudioFile.exists()){
                Log.i(TAG, "myRecAudioFile.getAbsolutePath(): " + myRecAudioFile.getAbsolutePath());
                MediaPlayer mp = MediaPlayer.create(mContext, Uri.parse(myRecAudioFile.getAbsolutePath()));
                //某些手机会限制录音，如果用户拒接使用录音，则需判断mp是否存在
                if (mp != null) {
                    int duration = mp.getDuration() / 1000;//即为时长 是s
                    if (duration < 1)
                        duration = 1;
                    else if(duration > 60)
                        duration = 60;
                    try{
                        VoiceContent content = new VoiceContent(myRecAudioFile, duration);
                        Message msg = mConv.createSendMessage(content);
                        msg.setOnSendCompleteCallback(new BasicCallback() {

                            @Override
                            public void gotResult(int status, String desc) {
                                //Callback返回时刷新界面
                                android.os.Message msg = handler.obtainMessage();
                                msg.what = 6;
                                Bundle bundle = new Bundle();
                                bundle.putInt("status", status);
                                bundle.putString("desc", desc);
                                msg.setData(bundle);
                                msg.sendToTarget();
                            }
                        });
                        JMessageClient.sendMessage(msg);
                        mMsgListAdapter.addMsgToList(msg);
                    }catch (FileNotFoundException e){
                        Log.i(TAG, "Enter unexpected exception, File Not Found");
                    }
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.record_voice_permission_toast), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

   //取消录音，清除计时
    private void cancelRecord() {
        //可能在消息队列中还存在HandlerMessage，移除剩余消息
        mVolumeHandler.removeMessages(56, null);
        mVolumeHandler.removeMessages(57, null);
        mVolumeHandler.removeMessages(58, null);
        mVolumeHandler.removeMessages(59, null);
        mTimeUp = false;
        cancelTimer();
        stopRecording();
        if (recordIndicator != null)
            recordIndicator.dismiss();
        if(myRecAudioFile != null)
            myRecAudioFile.delete();
    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        recorder.setOutputFile(myRecAudioFile.getAbsolutePath());

        try {
            myRecAudioFile.createNewFile();
            recorder.prepare();
            recorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
                @Override
                public void onError(MediaRecorder mediaRecorder, int i, int i2) {
                    Log.i("RecordVoiceController", "recorder prepare failed!");
                }
            });
            recorder.start();
            startTime = System.currentTimeMillis();
            mCountTimer = new Timer();
            mCountTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mTimeUp = true;
                    android.os.Message msg = mVolumeHandler.obtainMessage();
                    msg.what = 55;
                    Bundle bundle = new Bundle();
                    bundle.putInt("restTime", 5);
                    msg.setData(bundle);
                    msg.sendToTarget();
                    mCountTimer.cancel();
                }
            }, 56000);

        } catch (IOException e) {
            e.printStackTrace();
        }catch (RuntimeException e){
            cancelTimer();
            dismissDialog();
            if (mThread != null) {
                mThread.exit();
                mThread = null;
            }
            if(myRecAudioFile != null)
                myRecAudioFile.delete();
            recorder.release();
            recorder = null;
            Log.e(TAG, "UnExcepted error Start MediaRecorder failed");
        }


        mThread = new ObtainDecibelThread();
        mThread.start();

    }

    //停止录音，隐藏录音动画
    private void stopRecording() {
        if (mThread != null) {
            mThread.exit();
            mThread = null;
        }
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }

    public void releaseRecorder() {
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }

    private class ObtainDecibelThread extends Thread {

        private volatile boolean running = true;

        public void exit() {
            running = false;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (recorder == null || !running) {
                    break;
                }
                int x = recorder.getMaxAmplitude();
                if (x != 0) {
                    int f = (int) (10 * Math.log(x) / Math.log(10));
                    if (f < 20) {
                        mVolumeHandler.sendEmptyMessage(0);
                    } else if (f < 26) {
                        mVolumeHandler.sendEmptyMessage(1);
                    } else if (f < 32) {
                        mVolumeHandler.sendEmptyMessage(2);
                    } else if (f < 38) {
                        mVolumeHandler.sendEmptyMessage(3);
                    } else {
                        mVolumeHandler.sendEmptyMessage(4);
                    }
                }

            }
        }

    }

    private OnDismissListener onDismiss = new OnDismissListener() {

        @Override
        public void onDismiss(DialogInterface dialog) {
            stopRecording();
        }
    };

    public void dismissDialog() {
        if (recordIndicator != null)
            recordIndicator.dismiss();
        this.setText(mContext.getString(R.string.record_voice_hint));
    }

    /**
     * 录音动画控制
     */
    private class ShowVolumeHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            int restTime = msg.getData().getInt("restTime", -1);
            // 若restTime>0, 进入倒计时
            if (restTime > 0) {
                mTimeUp = true;
                android.os.Message msg1 = mVolumeHandler.obtainMessage();
                msg1.what = 60 - restTime + 1;
                Bundle bundle = new Bundle();
                bundle.putInt("restTime", restTime - 1);
                msg1.setData(bundle);
                //创建一个延迟一秒执行的HandlerMessage，用于倒计时
                mVolumeHandler.sendMessageDelayed(msg1, 1000);
                mRecordHintTv.setText(String.format(mContext.getString(R.string.rest_record_time_hint), restTime));
                // 倒计时结束，发送语音, 重置状态
            } else if (restTime == 0) {
                finishRecord();
                RecordVoiceBtnController.this.setPressed(false);
                mTimeUp = false;
                // restTime = -1, 一般情况
            } else {
                // 没有进入倒计时状态
                if (!mTimeUp) {
                    if (msg.what < 5)
                        mRecordHintTv.setText(mContext
                                .getString(R.string.move_to_cancel_hint));
                    else
                        mRecordHintTv.setText(mContext
                                .getString(R.string.cancel_record_voice_hint));
                    // 进入倒计时
                } else {
                    if (msg.what == 5) {
                        mRecordHintTv.setText(mContext
                                .getString(R.string.cancel_record_voice_hint));
                        if (!mIsPressed)
                            cancelRecord();
                    }
                }
                mVolumeIv.setImageResource(res[msg.what]);
            }
            // view.setImageResource(res[msg.what]);
        }
    }

    Handler handler = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 6:
                    int status = msg.getData().getInt("status", -1);
                    HandleResponseCode.onHandle(mContext, status);
                    Log.i("RecordVoiceController", "desc：" + msg.getData().getString("desc"));
                    Log.i("RecordVoiceController", "refreshing!");
                    mMsgListAdapter.refresh();
                    break;
                case 7:
                    if (mIsPressed)
                        initDialogAndStartRecord();
                    break;
            }
        }
    };
}
