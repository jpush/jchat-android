package io.jchat.android.view;

import android.content.Context;
import android.util.AttributeSet;

import io.jchat.android.view.shader.RoundedShader;
import io.jchat.android.view.shader.ShaderHelper;


public class RoundedImageView extends ShaderImageView {

    public RoundedImageView(Context context) {
        super(context);
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public ShaderHelper createImageViewHelper() {
        return new RoundedShader();
    }
}
