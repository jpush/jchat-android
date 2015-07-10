package io.jchat.android.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.jchat.android.R;


public class DialogCreator {
    public Dialog createLoadingDialog(Context context, String msg) {
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

    public static Dialog createBaseCostomDialog(Context context, String title, String text,
                                                View.OnClickListener onClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.dialog_base, null);
        builder.setView(v);
        Dialog baseDialog = builder.create();
        TextView titleTv = (TextView) v.findViewById(R.id.dialog_base_title_tv);
        TextView textTv = (TextView) v.findViewById(R.id.dialog_base_text_tv);
        Button confirmBtn = (Button) v.findViewById(R.id.dialog_base_confirm_btn);
        titleTv.setText(title);
        textTv.setText(text);
        confirmBtn.setOnClickListener(onClickListener);
        baseDialog.setCancelable(false);
        return baseDialog;
    }
}
