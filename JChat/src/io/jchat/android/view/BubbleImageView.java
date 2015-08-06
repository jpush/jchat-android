package io.jchat.android.view;

import android.content.Context;
import android.util.AttributeSet;

import io.jchat.android.view.shader.BubbleShader;
import io.jchat.android.view.shader.ShaderHelper;


public class BubbleImageView extends ShaderImageView {

    public BubbleImageView(Context context) {
        super(context);
    }

    public BubbleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BubbleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public ShaderHelper createImageViewHelper() {
        return new BubbleShader();
    }
}
