package io.jchat.android.tools;

import android.app.Activity;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by jpush on 2015/7/3.
 */
public class ActivityManager {
    public static List<Activity> activityList = new LinkedList<Activity>();

    public static void addActivity(Activity activity){
        activityList.add(activity);
    }

    public static void removeActivity(Activity activity){
        activityList.remove(activity);
    }

    public static void clearList(){
        for(Activity activity : activityList){
            activity.finish();
        }
        activityList.clear();
    }
}
