package io.jchat.android.tools;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import io.jchat.android.R;
import io.jchat.android.activity.LoginActivity;

/**
 * Created by Ken on 2015/3/25.
 */
public class HandleResponseCode {
    public static void onHandle(Context context, int status, boolean isCenter){
        Toast toast = new Toast(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.toast_default_style, null);
        TextView content = (TextView) view.findViewById(R.id.toast_content_tv);
        switch (status){
            case 0:
                break;
            case 800002:
                content.setText(context.getString(R.string.server_800002));
                break;
            case 800003:
                content.setText(context.getString(R.string.server_800003));
                break;
            case 800004:
                content.setText(context.getString(R.string.server_800004));
                break;
            case 800005:
                content.setText(context.getString(R.string.server_800005));
                break;
            case 800006:
                content.setText(context.getString(R.string.server_800006));
                break;
            case 800012:
                content.setText(context.getString(R.string.server_800012));
                break;
            case 800013:
                content.setText(context.getString(R.string.server_800013));
                Intent intent = new Intent();
                intent.setClass(context, LoginActivity.class);
                context.startActivity(intent);
                break;
            case 801001:
            case 802001:
                content.setText(context.getString(R.string.server_802001));
                break;
            case 802002:
            case 898002:
            case 801003:
            case 899002:
                content.setText(context.getString(R.string.server_801003));
                break;
            case 899004:
            case 801004:
                content.setText(context.getString(R.string.server_801004));
                break;
            case 803001:
                content.setText(context.getString(R.string.server_803001));
                break;
            case 803002:
                content.setText(context.getString(R.string.server_803002));
                break;
            case 803003:
                content.setText(context.getString(R.string.server_803003));
                break;
            case 803004:
                content.setText(context.getString(R.string.server_803004));
                break;
            case 803005:
                content.setText(context.getString(R.string.server_803005));
                break;
            case 803008:
                content.setText(context.getString(R.string.server_803008));
                break;
            case 808003:
                content.setText(context.getString(R.string.server_808003));
                break;
            case 808004:
                content.setText(context.getString(R.string.server_808004));
                break;
            case 810003:
                content.setText(context.getString(R.string.server_810003));
                break;
            case 810005:
                content.setText(context.getString(R.string.server_810005));
                break;
            case 810007:
                content.setText(context.getString(R.string.server_810007));
                break;
            case 810008:
                content.setText(context.getString(R.string.server_810008));
                break;
            case 810009:
                content.setText(context.getString(R.string.server_810009));
                break;
            case 811003:
                content.setText(context.getString(R.string.server_811003));
                break;
            case 812002:
                content.setText(context.getString(R.string.server_812002));
                break;
            case 818001:
                content.setText(context.getString(R.string.server_818001));
                break;
            case 818002:
                content.setText(context.getString(R.string.server_818002));
                break;
            case 818003:
                content.setText(context.getString(R.string.server_818003));
                break;
            case 818004:
                content.setText(context.getString(R.string.server_818004));
                break;
            case 899001:
            case 898001:
                content.setText(context.getString(R.string.sdk_http_899001));
                break;
            case 898005:
                content.setText(context.getString(R.string.sdk_http_898005));
                break;
            case 898006:
                content.setText(context.getString(R.string.sdk_http_898006));
                break;
            case 898008:
                content.setText(context.getString(R.string.sdk_http_898008));
                break;
            case 898009:
                content.setText(context.getString(R.string.sdk_http_898009));
                break;
            case 898010:
                content.setText(context.getString(R.string.sdk_http_898010));
                break;
            case 898030:
                content.setText(context.getString(R.string.sdk_http_898030));
                break;
            case 800009:
            case 871104:
                content.setText(context.getString(R.string.sdk_87x_871104));
                break;
            case 871303:
                content.setText(context.getString(R.string.sdk_87x_871303));
                break;
            case 871304:
                content.setText(context.getString(R.string.sdk_87x_871304));
                break;
            case 871305:
                content.setText(context.getString(R.string.sdk_87x_871305));
                break;
            case 871309:
                content.setText(context.getString(R.string.sdk_87x_871309));
                break;
            case 871310:
                content.setText(context.getString(R.string.sdk_87x_871310));
                break;
            case 871311:
                content.setText(context.getString(R.string.sdk_87x_871311));
                break;
            case 871312:
                content.setText(context.getString(R.string.sdk_87x_871312));
                break;
            case 871403:
                content.setText(context.getString(R.string.sdk_87x_871403));
                break;
            case 871404:
                content.setText(context.getString(R.string.sdk_87x_871404));
                break;
            case 871102:
            case 871201:
                content.setText(context.getString(R.string.sdk_87x_871201));
                break;
            default:
                content.setText("Response code: " + status);
                break;
        }
        if(isCenter){
            toast.setGravity(Gravity.CENTER, 0, 0);
        }
        view.getBackground().setAlpha(150);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }
}
