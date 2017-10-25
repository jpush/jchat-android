package jiguang.chat.utils.zxing.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


public class BitmapUtil {


    /**
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        //压缩当前图片占用内存不超过应用可用内存的3/4
        //ARGB_8888  一个像素占用4个字节
        //1兆字节(mb)=1048576字节(b)
        while (reqHeight * reqWidth * 4 > AppliationUtil.FREE_MEMORY * 1048576 / 4 * 3) {
            reqHeight -= 50;
            reqWidth -= 50;
        }
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        if (inSampleSize == 0) return 1;
        return inSampleSize;
    }

    public static Bitmap decodeBitmapFromPath(String photo_path, int reqWidth, int reqHeight) {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        //这句要有要不会oom
        BitmapFactory.decodeFile(photo_path, options);
        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(photo_path, options);
    }
}