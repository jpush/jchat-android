package cn.jpush.im.android.demo.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import cn.jpush.im.android.demo.R;

import java.io.File;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;

import cn.jpush.im.android.demo.tools.BitmapLoader;

/**
 * Created by Ken on 2015/1/26.
 */
public class LoginDialog {
    public Dialog createLoadingDialog(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.dialog_login, null);
        RelativeLayout layout = (RelativeLayout) v.findViewById(R.id.dialog_login_view);
        RoundImageView avatarIv = (RoundImageView) v.findViewById(R.id.login_iv);
        UserInfo userInfo = JMessageClient.getMyInfo();
        if(userInfo != null){
            File file = userInfo.getAvatar();
            if (file != null){
                DisplayMetrics dm = new DisplayMetrics();
                ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(dm);
                double density = dm.density;
                Bitmap bitmap = BitmapLoader.getBitmapFromFile(file.getAbsolutePath(), (int)(100 * density), (int)(100 * density));
                avatarIv.setImageBitmap(bitmap);
            }
        }
        final Dialog loadingDialog = new Dialog(context, R.style.login_dialog_style);
        loadingDialog.setContentView(layout, new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));
        return loadingDialog;
    }
}
