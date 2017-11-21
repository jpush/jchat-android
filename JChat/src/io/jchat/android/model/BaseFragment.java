package io.jchat.android.model;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.View;

import io.jchat.android.view.UIView;


public abstract class BaseFragment extends Fragment {
    private static final Handler handler = new Handler();

    private int containerId;

    private boolean destroyed;

    public int getContainerId() {
        return containerId;
    }

    public void setContainerId(int containerId) {
        this.containerId = containerId;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        destroyed = false;
    }

    public void onDestroy() {
        super.onDestroy();


        destroyed = true;
    }

    protected final Handler getHandler() {
        return handler;
    }


    protected <T extends View> T findView(int resId) {
        return (T) (getView().findViewById(resId));
    }

    protected void setTitle(int titleId) {
        if (getActivity() != null && getActivity() instanceof UIView) {
            getActivity().setTitle(titleId);
        }
    }
}
