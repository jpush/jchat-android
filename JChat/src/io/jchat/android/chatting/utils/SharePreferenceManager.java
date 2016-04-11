package io.jchat.android.chatting.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharePreferenceManager {
    static SharedPreferences sp;

    public static void init(Context context, String name) {
        sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    private static final String KEY_CACHED_USERNAME = "jchat_cached_username";

    public static void setCachedUsername(String username) {
        if (null != sp) {
            sp.edit().putString(KEY_CACHED_USERNAME, username).apply();
        }
    }

    public static String getCachedUsername() {
        if (null != sp) {
            return sp.getString(KEY_CACHED_USERNAME, null);
        }
        return null;
    }

    private static final String KEY_CACHED_AVATAR_PATH = "jchat_cached_avatar_path";

    public static void setCachedAvatarPath(String path) {
        if (null != sp) {
            sp.edit().putString(KEY_CACHED_AVATAR_PATH, path).apply();
        }
    }

    public static String getCachedAvatarPath() {
        if (null != sp) {
            return sp.getString(KEY_CACHED_AVATAR_PATH, null);
        }
        return null;
    }

    private static final String KEY_CACHED_FIX_PROFILE_FLAG = "fixProfileFlag";

    public static void setCachedFixProfileFlag(boolean value) {
        if(null != sp){
            sp.edit().putBoolean(KEY_CACHED_FIX_PROFILE_FLAG, value).apply();
        }
    }

    public static boolean getCachedFixProfileFlag(){
        return null != sp && sp.getBoolean(KEY_CACHED_FIX_PROFILE_FLAG, false);
    }

    private static final String SOFT_KEYBOARD_HEIGHT = "SoftKeyboardHeight";

    public static void setCachedKeyboardHeight(int height){
        if(null != sp){
            sp.edit().putInt(SOFT_KEYBOARD_HEIGHT, height).apply();
        }
    }

    public static int getCachedKeyboardHeight(){
        if(null != sp){
            return sp.getInt(SOFT_KEYBOARD_HEIGHT, 0);
        }
        return 0;
    }

    private static final String WRITABLE_FLAG = "writable";
    public static void setCachedWritableFlag(boolean value){
        if(null != sp){
            sp.edit().putBoolean(WRITABLE_FLAG, value).apply();
        }
    }

    public static boolean getCachedWritableFlag(){
        return null == sp || sp.getBoolean(WRITABLE_FLAG, true);
    }

    private static final String CACHED_APP_KEY = "CachedAppKey";

    public static void setCachedAppKey(String appKey) {
        if (null != sp) {
            sp.edit().putString(CACHED_APP_KEY, appKey).apply();
        }
    }

    public static String getCachedAppKey() {
        if (null != sp) {
            return sp.getString(CACHED_APP_KEY, "default");
        }
        return "default";
    }
}
