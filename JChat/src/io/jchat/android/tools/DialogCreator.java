package io.jchat.android.tools;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.jpush.im.android.api.JMessageClient;
import io.jchat.android.R;
import io.jchat.android.activity.ResetPasswordActivity;


public class DialogCreator {
    public static Dialog createLoadingDialog(Context context, String msg) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.loading_view, null);
        RelativeLayout layout = (RelativeLayout) v.findViewById(R.id.dialog_view);
        ImageView mLoadImg = (ImageView) v.findViewById(R.id.loading_img);
        TextView mLoadText = (TextView) v.findViewById(R.id.loading_txt);
        AnimationDrawable mDrawable = (AnimationDrawable) mLoadImg.getDrawable();
        mDrawable.start();
        mLoadText.setText(msg);
        final Dialog loadingDialog = new Dialog(context, R.style.LoadingDialog);
        loadingDialog.setCancelable(true);
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        return loadingDialog;
    }

    public static Dialog createBaseCustomDialog(Context context, String title, String text,
                                                View.OnClickListener onClickListener) {
        Dialog baseDialog = new Dialog(context, R.style.default_dialog_style);
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.dialog_base, null);
        baseDialog.setContentView(v);
        TextView titleTv = (TextView) v.findViewById(R.id.dialog_base_title_tv);
        TextView textTv = (TextView) v.findViewById(R.id.dialog_base_text_tv);
        Button confirmBtn = (Button) v.findViewById(R.id.dialog_base_confirm_btn);
        titleTv.setText(title);
        textTv.setText(text);
        confirmBtn.setOnClickListener(onClickListener);
        baseDialog.setCancelable(false);
        return baseDialog;
    }

    public static Dialog createDelConversationDialog(Context context, String title,
                                                     View.OnClickListener listener){
        Dialog dialog = new Dialog(context, R.style.default_dialog_style);
        View v = LayoutInflater.from(context).inflate(
                R.layout.dialog_delete_conv, null);
        dialog.setContentView(v);
        TextView titleTv = (TextView) v.findViewById(R.id.dialog_title);
        final LinearLayout deleteLl = (LinearLayout) v.findViewById(R.id.delete_conv_ll);
        titleTv.setText(title);
        deleteLl.setOnClickListener(listener);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    public static Dialog createLongPressMessageDialog(Context context, String title, boolean hide,
                                                      View.OnClickListener listener){
        Dialog dialog = new Dialog(context, R.style.default_dialog_style);
        View view = LayoutInflater.from(context).inflate(
                R.layout.dialog_msg_alert, null);
        dialog.setContentView(view);
        Button copyBtn = (Button) view
                .findViewById(R.id.copy_msg_btn);
        Button forwardBtn = (Button) view
                .findViewById(R.id.forward_msg_btn);
        View line1 = view.findViewById(R.id.forward_split_line);
        View line2 = view.findViewById(R.id.delete_split_line);
        Button deleteBtn = (Button) view.findViewById(R.id.delete_msg_btn);
        final TextView titleTv = (TextView) view
                .findViewById(R.id.dialog_title);
        if (hide) {
            copyBtn.setVisibility(View.GONE);
            forwardBtn.setVisibility(View.GONE);
            line1.setVisibility(View.GONE);
            line2.setVisibility(View.GONE);
        }
        titleTv.setText(title);
        copyBtn.setOnClickListener(listener);
        forwardBtn.setOnClickListener(listener);
        deleteBtn.setOnClickListener(listener);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    public static Dialog createResendDialog(Context context, View.OnClickListener listener){
        Dialog dialog = new Dialog(context, R.style.default_dialog_style);
        View view = LayoutInflater.from(context).inflate(
                R.layout.dialog_base_with_button, null);
        dialog.setContentView(view);
        Button cancelBtn = (Button) view.findViewById(R.id.cancel_btn);
        Button resendBtn = (Button) view.findViewById(R.id.commit_btn);
        cancelBtn.setOnClickListener(listener);
        resendBtn.setOnClickListener(listener);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    public static Dialog createDeleteMessageDialog(Context context, View.OnClickListener listener){
        Dialog dialog = new Dialog(context, R.style.default_dialog_style);
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.dialog_base_with_button, null);
        dialog.setContentView(v);
        TextView title = (TextView) v.findViewById(R.id.title);
        title.setText(context.getString(R.string.clear_history_confirm_title));
        final Button cancel = (Button) v.findViewById(R.id.cancel_btn);
        final Button commit = (Button) v.findViewById(R.id.commit_btn);
        commit.setText(context.getString(R.string.confirm));
        cancel.setOnClickListener(listener);
        commit.setOnClickListener(listener);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    public static Dialog createExitGroupDialog(Context context, View.OnClickListener listener){
        Dialog dialog = new Dialog(context, R.style.default_dialog_style);
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.dialog_base_with_button, null);
        dialog.setContentView(v);
        TextView title = (TextView) v.findViewById(R.id.title);
        title.setText(context.getString(R.string.delete_group_confirm_title));
        final Button cancel = (Button) v.findViewById(R.id.cancel_btn);
        final Button commit = (Button) v.findViewById(R.id.commit_btn);
        commit.setText(context.getString(R.string.confirm));
        cancel.setOnClickListener(listener);
        commit.setOnClickListener(listener);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    public static Dialog createSetAvatarDialog(Context context, View.OnClickListener listener){
        Dialog dialog = new Dialog(context, R.style.default_dialog_style);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_set_avatar, null);
        dialog.setContentView(view);
        Button takePhotoBtn = (Button) view.findViewById(R.id.take_photo_btn);
        Button pickPictureBtn = (Button) view.findViewById(R.id.pick_picture_btn);
        takePhotoBtn.setOnClickListener(listener);
        pickPictureBtn.setOnClickListener(listener);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    public static Dialog createLogoutDialog(Context context, View.OnClickListener listener){
        Dialog dialog = new Dialog(context, R.style.default_dialog_style);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_base_with_button, null);
        dialog.setContentView(view);
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(context.getString(R.string.logout_confirm));
        final Button cancel = (Button) view.findViewById(R.id.cancel_btn);
        final Button commit = (Button) view.findViewById(R.id.commit_btn);
        cancel.setOnClickListener(listener);
        commit.setOnClickListener(listener);
        commit.setText(context.getString(R.string.confirm));
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    public static Dialog createResetPwdDialog(final Context context){
        final Dialog dialog = new Dialog(context, R.style.default_dialog_style);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_reset_password, null);
        dialog.setContentView(view);
        final EditText pwdEt = (EditText) view.findViewById(R.id.password_et);
        final Button cancel = (Button) view.findViewById(R.id.cancel_btn);
        final Button commit = (Button) view.findViewById(R.id.commit_btn);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.cancel_btn:
                        dialog.cancel();
                        break;
                    case R.id.commit_btn:
                        String input = pwdEt.getText().toString().trim();
                        if(JMessageClient.isCurrentUserPasswordValid(input)){
                            Intent intent = new Intent();
                            intent.putExtra("oldPassword", input);
                            intent.setClass(context, ResetPasswordActivity.class);
                            context.startActivity(intent);
                            dialog.cancel();
                        }else {
                            Toast toast = Toast.makeText(context, context.getString(R.string.input_password_error_toast), Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                        break;
                }
            }
        };
        cancel.setOnClickListener(listener);
        commit.setOnClickListener(listener);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    public static Dialog createDeleteMemberDialog(Context context, View.OnClickListener listener,
                                                  boolean isSingle) {
        Dialog dialog = new Dialog(context, R.style.default_dialog_style);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_base_with_button, null);
        dialog.setContentView(view);
        TextView title = (TextView) view.findViewById(R.id.title);
        if (isSingle) {
            title.setText(context.getString(R.string.delete_member_confirm_hint));
        } else {
            title.setText(context.getString(R.string.delete_confirm_hint));
        }
        final Button cancel = (Button) view.findViewById(R.id.cancel_btn);
        final Button commit = (Button) view.findViewById(R.id.commit_btn);
        cancel.setOnClickListener(listener);
        commit.setOnClickListener(listener);
        commit.setText(context.getString(R.string.confirm));
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

}
