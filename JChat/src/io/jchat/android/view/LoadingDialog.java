package io.jchat.android.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.jchat.android.R;


public class LoadingDialog {
	public  Dialog createLoadingDialog(Context context, String msg) {
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
}
