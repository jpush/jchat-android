package io.jchat.android.tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;

/**
 * Created by Ken on 2015/2/9.
 */
public class BitmapLoader {

//    private static BitmapLoader bitmapLoader = new BitmapLoader();
//
//    public static BitmapLoader getInstance() {
//        return bitmapLoader;
//    }

    public static Bitmap getBitmapFromFile(String path, int width, int height) {
        BitmapFactory.Options opts = null;
        if (path != null) {
            if (width > 0 && height > 0) {
                opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(path, opts);
                final int minSideLength = Math.min(width, height);
                opts.inSampleSize = computeSampleSize(opts, minSideLength,
                        width * height);
                opts.inJustDecodeBounds = false;
            }
            return BitmapFactory.decodeFile(path, opts);
        } else return null;
    }

    public static boolean verifyPictureSize(String path) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opts);
        if (opts.outHeight <= 1280 && opts.outWidth <= 720)
            return true;
        else return false;
    }

    /**
     * 根据bitmap保存图片到本地
     *
     * @param bitmap
     * @return
     */
    public static String saveBitmapToLocal(Bitmap bitmap) {
        if (null == bitmap) {
            return null;
        }
        String filePath;
        FileOutputStream fileOutput = null;
        File imgFile = null;
        try {
            String dir = "sdcard/JPushDemo/pictures/";
            File desDir = new File(dir);
            if (!desDir.exists()) {
                desDir.mkdirs();
            }
            //使用随机数使得发送的图片的缩略图文件名不相同
            imgFile = new File(desDir.getAbsoluteFile() + "/" + String.valueOf(bitmap.hashCode() + Math.random()));
            imgFile.createNewFile();
            fileOutput = new FileOutputStream(imgFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fileOutput);
            fileOutput.flush();
            filePath = imgFile.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            filePath = null;
        } catch (IOException e) {
            e.printStackTrace();
            filePath = null;
        } finally {
            if (null != fileOutput) {
                try {
                    fileOutput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return filePath;
    }

    public static int computeSampleSize(BitmapFactory.Options options,
                                        int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
                                                int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
                .sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math
                .floor(w / minSideLength), Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    public static String getPictureSize(List<String> pathList) {
        long totalSize = 0;
        for (String path : pathList) {
            File file = new File(path);
            if (file.exists() && file.isFile())
                totalSize += file.length();
        }
        NumberFormat ddf1 = NumberFormat.getNumberInstance();
        //保留小数点后两位
        ddf1.setMaximumFractionDigits(2);
        double size = totalSize / 1048576.0;
        return ddf1.format(size) + "M";
    }
}
