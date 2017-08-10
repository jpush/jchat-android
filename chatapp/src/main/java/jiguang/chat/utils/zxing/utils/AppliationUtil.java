package jiguang.chat.utils.zxing.utils;


public class AppliationUtil {
    //应用程序最大可用内存
    public static int MAX_MEMORY = ((int) Runtime.getRuntime().maxMemory())/1024/1024;
    //应用程序已获得内存
    public static long TOTAL_MEMORY = ((int) Runtime.getRuntime().totalMemory())/1024/1024;
    //应用程序已获得内存中未使用内存
    public static long FREE_MEMORY = ((int) Runtime.getRuntime().freeMemory())/1024/1024;



}