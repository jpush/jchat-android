package cn.jpush.im.android.demo.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import cn.jpush.im.android.demo.R;


/**
 * This is a circular ImageView, you can add the border for it.
 * 
 * @author lovecluo
 * 
 */
public class RoundImageView extends ImageView {

	private final String TAG = RoundImageView.class.getSimpleName();

	private Context mContext;

	// default border color
	private final int DEFAULT_COLOR = 0xFFFFFFFF;
	private final int DEFAUTL_BORDER_THICKNESS = 0;
	// border thickness
	private int mBorderThickness = 0;
	private int mBorderInsideThickness = 0;
	private int mBorderOutsideThickness = 0;

	// border Color,if you want have two border,you can save two colors.
	private int mBorderColor = 0;
	private int mBorderInsideColor = 0;
	private int mBorderOutsideColor = 0;

	// The Image View default size
	private int mDefaultWidth;
	private int mDefaultHeight;

	public RoundImageView(Context context) {
		super(context);
		this.mContext = context;
	}

	public RoundImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		this.setCustomAttributes(attrs);
	}

	public RoundImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
		this.setCustomAttributes(attrs);
	}

	/**
	 * Set custom attributes<br/>
	 * you can use custom label to define the attribute.
	 */
	private void setCustomAttributes(AttributeSet attrs) {
		TypedArray types = this.mContext.obtainStyledAttributes(attrs, R.styleable.roundedimageview);

		mBorderThickness = types.getDimensionPixelSize(R.styleable.roundedimageview_border_thickness, DEFAUTL_BORDER_THICKNESS);
		mBorderInsideThickness = types.getDimensionPixelSize(R.styleable.roundedimageview_border_inside_thickness, DEFAUTL_BORDER_THICKNESS);
		mBorderOutsideThickness = types.getDimensionPixelSize(R.styleable.roundedimageview_border_outside_thickness, DEFAUTL_BORDER_THICKNESS);
		// get the border thickness, Single attribute has higher priority
		if (mBorderInsideThickness == DEFAUTL_BORDER_THICKNESS) {
			mBorderInsideThickness = mBorderThickness;
		}
		if (mBorderOutsideThickness == DEFAUTL_BORDER_THICKNESS) {
			mBorderOutsideThickness = mBorderThickness;
		}

		mBorderColor = types.getColor(R.styleable.roundedimageview_border_color, DEFAULT_COLOR);
		mBorderInsideColor = types.getColor(R.styleable.roundedimageview_border_inside_color, DEFAULT_COLOR);
		mBorderOutsideColor = types.getColor(R.styleable.roundedimageview_border_outside_color, DEFAULT_COLOR);
		// get The border color,Single attribute has higher priority
		if (mBorderInsideColor == DEFAULT_COLOR) {
			mBorderInsideColor = mBorderColor;
		}
		if (mBorderOutsideColor == DEFAULT_COLOR) {
			mBorderOutsideColor = mBorderColor;
		}
	}

	public void setBorderThickness(int borderThickness) {
		this.mBorderThickness = borderThickness;
		this.mBorderOutsideThickness = borderThickness;
		this.mBorderInsideThickness = borderThickness;
	}

	public void setBorderInsideThickness(int borderInsideThickness) {
		this.mBorderInsideThickness = borderInsideThickness;
	}

	public void setBorderOutsideThickness(int borderOutsideThickness) {
		this.mBorderOutsideThickness = borderOutsideThickness;
	}

	public void setBorderColor(int borderColor) {
		this.mBorderColor = borderColor;
		this.mBorderOutsideColor = borderColor;
		this.mBorderInsideColor = borderColor;
	}

	public void setBorderInsideColor(int borderInsideColor) {
		this.mBorderInsideColor = borderInsideColor;
	}

	public void setBorderOutsideColor(int borderOutsideColor) {
		this.mBorderOutsideColor = borderOutsideColor;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Drawable drawable = this.getDrawable();
		if (drawable == null) {
			return;
		}

		if (getWidth() == 0 || getHeight() == 0) {
			return;
		}

		this.measure(0, 0);
		if (drawable.getClass() == NinePatchDrawable.class) {
			return;
		}

		Bitmap b = ((BitmapDrawable) drawable).getBitmap();
		if (b != null) {
			Bitmap bitmap = b.copy(Config.ARGB_8888, true);
			if (mDefaultWidth == 0) {
				mDefaultWidth = getWidth();

			}
			if (mDefaultHeight == 0) {
				mDefaultHeight = getHeight();
			}

			int minSize = Math.min(mDefaultHeight, mDefaultHeight);
			// radius = (Radius of the maximum circle that can be drawn in the
			// image) - (inside border thickness) - (outside border thickness)
			int radius = minSize / 2 - mBorderInsideThickness - mBorderOutsideThickness;

			// float cx = Math.max(defaultWidth, minSize) / 2.0f;
			// float cy = Math.max(defaultHeight, minSize) / 2.0f;
			float cx = mDefaultWidth / 2.0f;
			float cy = mDefaultHeight / 2.0f;
			// System.out.println(cx + "   " + cy + " " + radius + " " +
			// borderInsideThickness + " " + borderInsideColor);
			// drawInsideBorder
			drawCircleBorder(canvas, cx, cy, radius + mBorderInsideThickness / 2f, mBorderInsideColor, mBorderInsideThickness);
			// drawOutSideBorder
			drawCircleBorder(canvas, cx, cy, radius + mBorderInsideThickness + mBorderOutsideThickness / 2f, mBorderOutsideColor, mBorderOutsideThickness);
			Bitmap roundBitmap = getCroppedRoundBitmap(bitmap, radius);
			canvas.drawBitmap(roundBitmap, mDefaultWidth / 2f - radius, mDefaultHeight / 2f - radius, null);
		}

	}

	private Bitmap getCroppedRoundBitmap(Bitmap bmp, int radius) {
		Bitmap scaledSrcBmp;
		int diameter = radius * 2;
		int bmpWidth = bmp.getWidth();
		int bmpHeight = bmp.getHeight();
		int squareWidth = 0, squareHeight = 0;
		int x = 0, y = 0;
		Bitmap squareBitmap;
		if (bmpHeight > bmpWidth) {
			squareWidth = squareHeight = bmpWidth;
			x = 0;
			y = (bmpHeight - bmpWidth) / 2;
			squareBitmap = Bitmap.createBitmap(bmp, x, y, squareWidth, squareHeight);
		} else if (bmpHeight < bmpWidth) {
			squareWidth = squareHeight = bmpHeight;
			x = (bmpWidth - bmpHeight) / 2;
			y = 0;
			squareBitmap = Bitmap.createBitmap(bmp, x, y, squareWidth, squareHeight);
		} else {
			squareBitmap = bmp;
		}

		if (squareBitmap.getWidth() != diameter || squareBitmap.getHeight() != diameter) {
			scaledSrcBmp = Bitmap.createScaledBitmap(squareBitmap, diameter, diameter, true);

		} else {
			scaledSrcBmp = squareBitmap;
		}
		Bitmap output = Bitmap.createBitmap(scaledSrcBmp.getWidth(), scaledSrcBmp.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		Paint paint = new Paint();
		Rect rect = new Rect(0, 0, scaledSrcBmp.getWidth(), scaledSrcBmp.getHeight());

		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setDither(true);
		canvas.drawARGB(0, 0, 0, 0);
		canvas.drawCircle(scaledSrcBmp.getWidth() / 2, scaledSrcBmp.getHeight() / 2, scaledSrcBmp.getWidth() / 2, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(scaledSrcBmp, rect, rect, paint);

		bmp = null;
		squareBitmap = null;
		scaledSrcBmp = null;
		return output;
	}

	/**
	 * Draw circle border
	 */
	private void drawCircleBorder(Canvas canvas, float cx, float cy, float radius, int color, int thickness) {
		Paint paint = new Paint();
		/* Anti-aliasing */
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setDither(true);
		paint.setColor(color);
		/* Set the paint's style is STROKE: Hollow */
		paint.setStyle(Paint.Style.STROKE);
		/* set stroke width */
		paint.setStrokeWidth(thickness);
		canvas.drawCircle(cx, cy, radius, paint);
	}

}