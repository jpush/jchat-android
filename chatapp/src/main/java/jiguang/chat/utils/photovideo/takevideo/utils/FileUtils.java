package jiguang.chat.utils.photovideo.takevideo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;


public class FileUtils {
    private static final String TAG = FileUtils.class.getSimpleName();
    /**
     * 随机命名
     */
    private static final String RANDOM_STRING = "abcdefghijklmnopqrstuvwxyz0123456789";
    /**
     * 时间命名
     */
    private static final String TIME_STRING = "yyyyMMdd_HHmmss";
    /**
     * 限制图片最大宽度进行压缩
     */
    private static final int MAX_WIDTH = 720;
    /**
     * 限制图片最大高度进行压缩
     */
    private static final int MAX_HEIGHT = 1280;
    /**
     * 上传最大图片限制
     */
    private static final int MAX_UPLOAD_PHOTO_SIZE = 300 * 1024;

    private FileUtils() {
    }

    /**
     * 缓存文件根目录名
     */
    private static final String FILE_DIR = "you";
    /**
     * 上传的照片文件路径
     */
    private static final String UPLOAD_FILE = "upload";

    /**
     * SD卡是否存在
     */
    public static boolean isSDCardExist() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取缓存目录路径
     *
     * @return
     */
    public static String getCacheDirPath(Context context) {
        if (isSDCardExist()) {
            String path = Environment.getExternalStorageDirectory() + File.separator + FILE_DIR + File.separator;
            File directory = new File(path);
            if (!directory.exists()) directory.mkdirs();
            return path;
        } else {
            File directory = new File(context.getCacheDir(), FileUtils.FILE_DIR);
            if (!directory.exists()) directory.mkdirs();
            return directory.getAbsolutePath();
        }
    }

    /**
     * 获取缓存目录
     *
     * @return
     */
    public static File getCacheDir(Context context) {
        if (isSDCardExist()) {
            String path = Environment.getExternalStorageDirectory() + File.separator + FILE_DIR + File.separator;
            File directory = new File(path);
            if (!directory.exists()) directory.mkdirs();
            return directory;
        } else {
            File directory = new File(context.getCacheDir(), FileUtils.FILE_DIR);
            if (!directory.exists()) directory.mkdirs();
            return directory;
        }
    }

    /**
     * 获取上传的路径
     *
     * @return
     */
    public static String getUploadCachePath(Context context) {
        if (isSDCardExist()) {
            String path = Environment.getExternalStorageDirectory() + File.separator + FILE_DIR + File.separator + UPLOAD_FILE + File.separator;
            File directory = new File(path);
            if (!directory.exists()) directory.mkdirs();
            return path;
        } else {
            File directory = new File(context.getCacheDir(), FileUtils.FILE_DIR + File.separator + UPLOAD_FILE);
            if (!directory.exists()) directory.mkdirs();
            return directory.getAbsolutePath();
        }
    }

    /**
     * jpg文件名
     * @param context
     * @return
     */
    public static String getUploadPhotoFile(Context context) {
        return getUploadCachePath(context) + getTimeString() + ".jpg";
    }

    /**
     * mp4文件名
     * @param context
     * @return
     */
    public static String getUploadVideoFile(Context context) {
        return getUploadCachePath(context) + getTimeString() + ".mp4";
    }

    /**
     * 保存拍摄图片
     * @param photoPath
     * @param data
     * @param isFrontFacing 是否为前置拍摄
     * @return
     */
    public static boolean savePhoto(String photoPath, byte[] data, boolean isFrontFacing) {
        if (photoPath != null && data != null) {
            int degree = getOrientation(data);
            FileOutputStream fos = null;
            try {
                Bitmap preBitmap = compressBitmap(data, MAX_WIDTH, MAX_HEIGHT);
                Matrix matrix = new Matrix();
                matrix.postRotate(degree);
                if (isFrontFacing) {
                    matrix.postScale(1, -1);
                }
                Bitmap newBitmap = Bitmap.createBitmap(preBitmap, 0, 0, preBitmap.getWidth(), preBitmap.getHeight(), matrix, true);
                if (preBitmap != newBitmap) {
                    preBitmap.recycle();
                    preBitmap = newBitmap;
                }
                byte[] newDatas = compressBitmapToBytes(preBitmap, MAX_UPLOAD_PHOTO_SIZE);
                fos = new FileOutputStream(photoPath);
                fos.write(newDatas);
                LogUtils.i("compress over ");
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.i(e);
            } finally {
                closeCloseable(fos);
            }
        }
        return false;
    }

    /**
     * 把字节流按照图片方式大小进行压缩
     * @param datas
     * @param w
     * @param h
     * @return
     */
    public static Bitmap compressBitmap(byte[] datas, int w, int h) {
        if (datas != null) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(datas, 0, datas.length, opts);
            if (opts.outWidth != 0 && opts.outHeight != 0) {
                LogUtils.i(opts.outWidth +" "+opts.outHeight);
                int scaleX = opts.outWidth / w;
                int scaleY = opts.outHeight / h;
                int scale = 1;
                if (scaleX >= scaleY && scaleX >= 1) {
                    scale = scaleX;
                }
                if (scaleX < scaleY && scaleY >= 1) {
                    scale = scaleY;
                }
                opts.inJustDecodeBounds = false;
                opts.inSampleSize = scale;
                LogUtils.i("compressBitmap inSampleSize "+datas.length+" "+scale);
                return BitmapFactory.decodeByteArray(datas, 0, datas.length, opts);
            }
        }
        return null;
    }

    /**
     * 质量压缩图片
     * @param bitmap
     * @param maxSize
     * @return
     */
    public static byte[] compressBitmapToBytes(Bitmap bitmap, int maxSize) {
        if (bitmap == null) return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] datas = baos.toByteArray();
        int options = 80;
        int longs = datas.length;
        while (longs > maxSize && options > 0) {
            LogUtils.i("compressBitmapToBytes "+longs+"  "+options);
            baos.reset();
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
            datas = baos.toByteArray();
            longs = datas.length;
            options -= 20;
        }
        return datas;
    }

    /**
     * 获取文件路径下所有文件大小, 适当放到子线程中执行
     *
     * @param file
     * @return
     */
    public static long getFileSize(File file) {
        if (file == null || !file.exists()) return 0;
        long totalSize = 0;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                totalSize += getFileSize(f);
            }
            return totalSize;
        } else {
            return file.length();
        }
    }

    /**
     * 删除文件夹下所有文件,适当放到子线程中执行
     *
     * @param file
     */
    public static void delteFiles(File file) {
        if (file == null || !file.exists()) return;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                if (!f.isDirectory()) {
                    f.delete();
                }
            }
        } else {
            file.delete();
        }
    }

    /**
     * 关闭资源
     *
     * @param close
     */
    public static void closeCloseable(Closeable close) {
        if (close != null) {
            try {
                close.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 文件大小
     *
     * @param fileS
     * @return
     */
    public static String formetFileSize(long fileS) {
        if (fileS <= 0) return "0B";
        if (fileS < 1024) {
            return new DecimalFormat("#.00").format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            return new DecimalFormat("#.00").format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            return new DecimalFormat("#.00").format((double) fileS / 1048576) + "MB";
        } else {
            return new DecimalFormat("#.00").format((double) fileS / 1073741824) + "GB";
        }
    }

    /**
     * sd卡容量
     *
     * @return
     */
    public static long getAvailableStorage() {
        long availableSize = 0;
        if (isSDCardExist()) {
            File sdFile = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(sdFile.getPath());
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR2) {
                availableSize = stat.getAvailableBytes();
            } else {
                availableSize = ((long) stat.getAvailableBlocks() * stat.getBlockSize());
            }
        }
        return availableSize;
    }

    /**
     * 获取随机文件名称字符串
     *
     * @param length 生成字符串的长度
     * @return
     */
    public static String getRandomString(int length) {
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(RANDOM_STRING.length());
            sb.append(RANDOM_STRING.charAt(number));
        }
        return sb.toString();
    }

    public static String getTimeString() {
        return new SimpleDateFormat(TIME_STRING).format(new Date());
    }

    /**
     * 将字符串写入文件中
     *
     * @param str    需要写入的字符串
     * @param file   写入文件的路径
     * @param append true 追加写入 false 覆盖写入
     * @return
     */
    public static boolean writeFile(String str, File file, boolean append) {
        if (TextUtils.isEmpty(str) || file == null) return false;
        FileWriter fw = null;
        try {
            fw = new FileWriter(file, append);
            fw.write(str);
            fw.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeCloseable(fw);
        }
        return false;
    }

    public static int getOrientation(byte[] jpeg) {
        if (jpeg == null) {
            return 0;
        }

        int offset = 0;
        int length = 0;

        // ISO/IEC 10918-1:1993(E)
        while (offset + 3 < jpeg.length && (jpeg[offset++] & 0xFF) == 0xFF) {
            int marker = jpeg[offset] & 0xFF;

            // Check if the marker is a padding.
            if (marker == 0xFF) {
                continue;
            }
            offset++;

            // Check if the marker is SOI or TEM.
            if (marker == 0xD8 || marker == 0x01) {
                continue;
            }
            // Check if the marker is EOI or SOS.
            if (marker == 0xD9 || marker == 0xDA) {
                break;
            }

            // Get the length and check if it is reasonable.
            length = pack(jpeg, offset, 2, false);
            if (length < 2 || offset + length > jpeg.length) {
                Log.e("69523", "Invalid length");
                return 0;
            }

            // Break if the marker is EXIF in APP1.
            if (marker == 0xE1 && length >= 8 &&
                    pack(jpeg, offset + 2, 4, false) == 0x45786966 &&
                    pack(jpeg, offset + 6, 2, false) == 0) {
                offset += 8;
                length -= 8;
                break;
            }

            // Skip other markers.
            offset += length;
            length = 0;
        }

        // JEITA CP-3451 Exif Version 2.2
        if (length > 8) {
            // Identify the byte order.
            int tag = pack(jpeg, offset, 4, false);
            if (tag != 0x49492A00 && tag != 0x4D4D002A) {
                Log.e("69523", "Invalid byte order");
                return 0;
            }
            boolean littleEndian = (tag == 0x49492A00);

            // Get the offset and check if it is reasonable.
            int count = pack(jpeg, offset + 4, 4, littleEndian) + 2;
            if (count < 10 || count > length) {
                Log.e(TAG, "Invalid offset");
                return 0;
            }
            offset += count;
            length -= count;

            // Get the count and go through all the elements.
            count = pack(jpeg, offset - 2, 2, littleEndian);
            while (count-- > 0 && length >= 12) {
                // Get the tag and check if it is orientation.
                tag = pack(jpeg, offset, 2, littleEndian);
                if (tag == 0x0112) {
                    // We do not really care about type and count, do we?
                    int orientation = pack(jpeg, offset + 8, 2, littleEndian);
                    switch (orientation) {
                        case 1:
                            return 0;
                        case 3:
                            return 180;
                        case 6:
                            return 90;
                        case 8:
                            return 270;
                    }
                    Log.i(TAG, "Unsupported orientation");
                    return 0;
                }
                offset += 12;
                length -= 12;
            }
        }

        Log.i(TAG, "Orientation not found");
        return 0;
    }

    private static int pack(byte[] bytes, int offset, int length,
                            boolean littleEndian) {
        int step = 1;
        if (littleEndian) {
            offset += length - 1;
            step = -1;
        }

        int value = 0;
        while (length-- > 0) {
            value = (value << 8) | (bytes[offset] & 0xFF);
            offset += step;
        }
        return value;
    }

}
