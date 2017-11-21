package io.jchat.android.tools.imagepicker;

import android.app.Activity;
import android.net.Uri;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

import io.jchat.android.R;


public class GlideImageLoader implements ImageLoader {

    @Override
    public void displayImages(Activity activity, String path, ImageView imageView, int width, int height) {
//        Glide.with(activity)                             //配置上下文
//                .load(Uri.fromFile(new File(path)))      //设置图片路径(fix #8,文件名包含%符号 无法识别和显示)
//                .error(R.mipmap.default_image)           //设置错误图片
//                .placeholder(R.mipmap.black)     //设置占位图片   这里换成了黑色图片.否则在相册中快速滑动时候会跳闪
//                .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存全尺寸
//                .into(imageView);
        Picasso.with(activity)
                .load(Uri.fromFile(new File(path)))
                .error(R.drawable.default_image)
                .into(imageView);
    }


    @Override
    public void clearMemoryCache() {
    }
}
