package io.jchat.android.tools;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import io.jchat.android.R;
import io.jchat.android.activity.LoginActivity;

/**
 * Created by Ken on 2015/3/25.
 */
public class HandleResponseCode {
    public static void onHandle(Context context, int status){
        switch (status){
            case 0:
                break;
            case 800002:
                Toast.makeText(context, context.getString(R.string.server_800002), Toast.LENGTH_SHORT).show();
                break;
            case 800003:
                Toast.makeText(context, context.getString(R.string.server_800003), Toast.LENGTH_SHORT).show();
                break;
            case 800004:
                Toast.makeText(context, context.getString(R.string.server_800004), Toast.LENGTH_SHORT).show();
                break;
            case 800005:
                Toast.makeText(context, context.getString(R.string.server_800005), Toast.LENGTH_SHORT).show();
                break;
            case 800006:
                Toast.makeText(context, context.getString(R.string.server_800006), Toast.LENGTH_SHORT).show();
                break;
            case 800012:
                Toast.makeText(context, context.getString(R.string.server_800012), Toast.LENGTH_SHORT).show();
                break;
            case 800013:
                Toast.makeText(context, context.getString(R.string.server_800013), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setClass(context, LoginActivity.class);
                context.startActivity(intent);
                break;
            case 801001:
            case 802001:
                Toast.makeText(context, context.getString(R.string.server_802001), Toast.LENGTH_SHORT).show();
                break;
            case 802002:
            case 898002:
            case 801003:
                Toast.makeText(context, context.getString(R.string.server_801003), Toast.LENGTH_SHORT).show();
                break;
            case 899004:
            case 801004:
                Toast.makeText(context, context.getString(R.string.server_801004), Toast.LENGTH_SHORT).show();
                break;
            case 803001:
                Toast.makeText(context, context.getString(R.string.server_803001), Toast.LENGTH_SHORT).show();
                break;
            case 803002:
                Toast.makeText(context, context.getString(R.string.server_803002), Toast.LENGTH_SHORT).show();
                break;
            case 803003:
                Toast.makeText(context, context.getString(R.string.server_803003), Toast.LENGTH_SHORT).show();
                break;
            case 803004:
                Toast.makeText(context, context.getString(R.string.server_803004), Toast.LENGTH_SHORT).show();
                break;
            case 803005:
                Toast.makeText(context, context.getString(R.string.server_803005), Toast.LENGTH_SHORT).show();
                break;
            case 808003:
                Toast.makeText(context, context.getString(R.string.server_808003), Toast.LENGTH_SHORT).show();
                break;
            case 808004:
                Toast.makeText(context, context.getString(R.string.server_808004), Toast.LENGTH_SHORT).show();
                break;
            case 810003:
                Toast.makeText(context, context.getString(R.string.server_810003), Toast.LENGTH_SHORT).show();
                break;
            case 810005:
                Toast.makeText(context, context.getString(R.string.server_810005), Toast.LENGTH_SHORT).show();
                break;
            case 810007:
                Toast.makeText(context, context.getString(R.string.server_810007), Toast.LENGTH_SHORT).show();
                break;
            case 810008:
                Toast.makeText(context, context.getString(R.string.server_810008), Toast.LENGTH_SHORT).show();
                break;
            case 810009:
                Toast.makeText(context, context.getString(R.string.server_810009), Toast.LENGTH_SHORT).show();
                break;
            case 811003:
                Toast.makeText(context, context.getString(R.string.server_811003), Toast.LENGTH_SHORT).show();
                break;
            case 899001:
                Toast.makeText(context, context.getString(R.string.sdk_http_899001), Toast.LENGTH_SHORT).show();
                break;
            case 898001:
            case 899006:
                Toast.makeText(context, context.getString(R.string.sdk_http_899001), Toast.LENGTH_SHORT).show();
                break;
            case 898005:
                Toast.makeText(context, context.getString(R.string.sdk_http_898005), Toast.LENGTH_SHORT).show();
                break;
            case 898006:
                Toast.makeText(context, context.getString(R.string.sdk_http_898006), Toast.LENGTH_SHORT).show();
                break;
            case 898008:
                Toast.makeText(context, context.getString(R.string.sdk_http_898008), Toast.LENGTH_SHORT).show();
                break;
            case 898009:
                Toast.makeText(context, context.getString(R.string.sdk_http_898009), Toast.LENGTH_SHORT).show();
                break;
            case 898010:
                Toast.makeText(context, context.getString(R.string.sdk_http_898010), Toast.LENGTH_SHORT).show();
                break;
            case 898030:
                Toast.makeText(context, context.getString(R.string.sdk_http_898030), Toast.LENGTH_SHORT).show();
                break;
            case 800009:
            case 871104:
                Toast.makeText(context, context.getString(R.string.sdk_87x_871104), Toast.LENGTH_SHORT).show();
                break;
            case 871303:
                Toast.makeText(context, context.getString(R.string.sdk_87x_871303), Toast.LENGTH_SHORT).show();
                break;
            case 871304:
                Toast.makeText(context, context.getString(R.string.sdk_87x_871304), Toast.LENGTH_SHORT).show();
                break;
            case 871305:
                Toast.makeText(context, context.getString(R.string.sdk_87x_871305), Toast.LENGTH_SHORT).show();
                break;
            case 871309:
                Toast.makeText(context, context.getString(R.string.sdk_87x_871309), Toast.LENGTH_SHORT).show();
                break;
            case 871310:
                Toast.makeText(context, context.getString(R.string.sdk_87x_871310), Toast.LENGTH_SHORT).show();
                break;
            case 871403:
                Toast.makeText(context, context.getString(R.string.sdk_87x_871403), Toast.LENGTH_SHORT).show();
                break;
            case 871404:
                Toast.makeText(context, context.getString(R.string.sdk_87x_871404), Toast.LENGTH_SHORT).show();
                break;
            case 871102:
            case 871201:
                Toast.makeText(context, context.getString(R.string.sdk_87x_871201), Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(context, "Response code: " + status, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
