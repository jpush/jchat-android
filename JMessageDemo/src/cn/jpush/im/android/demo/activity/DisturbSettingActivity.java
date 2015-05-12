package cn.jpush.im.android.demo.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import cn.jpush.im.android.demo.R;

import cn.jpush.im.android.demo.Listener.OnChangedListener;
import cn.jpush.im.android.demo.view.SlipButton;

/*
勿扰模式界面
 */
public class DisturbSettingActivity extends BaseActivity implements OnChangedListener{

    private ImageButton mReturnBtn;
    private TextView mTitle;
    private ImageButton mMenuBtn;
    private SlipButton mDisturbSwitchBtn;
    private LinearLayout mTimeSettingLl;
    private RelativeLayout mBeginTimeLl;
    private RelativeLayout mEndTimeLl;
    private TextView mBeginTime;
    private TextView mEndTime;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disturb_setting);

        mContext = this;
        mReturnBtn = (ImageButton) findViewById(R.id.return_btn);
        mTitle = (TextView) findViewById(R.id.title);
        mMenuBtn = (ImageButton) findViewById(R.id.right_btn);
        mDisturbSwitchBtn = (SlipButton) findViewById(R.id.disturb_mode_switch);
        mTimeSettingLl = (LinearLayout) findViewById(R.id.time_setting_ll);
        mBeginTimeLl = (RelativeLayout) findViewById(R.id.begin_time_ll);
        mEndTimeLl = (RelativeLayout) findViewById(R.id.end_time_ll);
        mBeginTime = (TextView) findViewById(R.id.begin_time);
        mEndTime = (TextView) findViewById(R.id.end_time);

        mReturnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mTitle.setText(mContext.getString(R.string.dnd_sub_hit));
        mMenuBtn.setVisibility(View.GONE);
        mDisturbSwitchBtn.setOnChangedListener(R.id.disturb_mode_switch, this);
        mDisturbSwitchBtn.setChecked(false);
        mTimeSettingLl.setOnClickListener(listener);
        mBeginTimeLl.setOnClickListener(listener);
        mEndTimeLl.setOnClickListener(listener);
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            View view = View.inflate(mContext, R.layout.dialog_time_picker, null);
            final TimePicker timePicker = (TimePicker) view.findViewById(R.id.time_picker);
            final Button commit = (Button) view.findViewById(R.id.commit_btn);
            builder.setView(view);
            timePicker.setIs24HourView(false);
            switch (v.getId()){
                case R.id.begin_time_ll:
                    timePicker.setCurrentHour(23);
                    timePicker.setCurrentMinute(0);
                    final Dialog dialog = builder.create();
                    dialog.show();
                    commit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            StringBuffer sb = new StringBuffer();
                            if(timePicker.getCurrentHour() < 12)
                                sb.append(mContext.getString(R.string.morning) + " ");
                                else if (timePicker.getCurrentHour() < 18)
                                sb.append(mContext.getString(R.string.afternoon) + " ");
                                else sb.append(mContext.getString(R.string.night) + " ");
                            if(timePicker.getCurrentHour() < 10)
                                sb.append("0");
                            sb.append(timePicker.getCurrentHour()).append(":");
                            if(timePicker.getCurrentMinute() < 10)
                                sb.append("0");
                            sb.append(timePicker.getCurrentMinute());
                            mBeginTime.setText(sb);
                            dialog.cancel();
                        }
                    });
                    break;
                case R.id.end_time_ll:
                    timePicker.setCurrentHour(11);
                    timePicker.setCurrentMinute(0);
                    final Dialog dialog1 = builder.create();
                    dialog1.show();

                    commit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            StringBuffer sb = new StringBuffer();
                            if(timePicker.getCurrentHour() < 12)
                                sb.append(mContext.getString(R.string.morning) + " ");
                            else if (timePicker.getCurrentHour() < 18)
                                sb.append(mContext.getString(R.string.afternoon) + " ");
                            else sb.append(mContext.getString(R.string.night) + " ");
                            if(timePicker.getCurrentHour() < 10)
                                sb.append("0");
                            sb.append(timePicker.getCurrentHour()).append(":");
                            if(timePicker.getCurrentMinute() < 10)
                                sb.append("0");
                            sb.append(timePicker.getCurrentMinute());
                            mEndTime.setText(sb);
                            dialog1.cancel();
                        }
                    });
                    break;
            }
        }
    };

    @Override
    public void OnChanged(int id, boolean flag) {
        if(flag){
            mTimeSettingLl.setVisibility(View.VISIBLE);
        }else
            mTimeSettingLl.setVisibility(View.GONE);
    }
}
