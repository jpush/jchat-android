package io.jchat.android.tools.zxing;

import android.os.Bundle;

import com.google.zxing.Result;


/**
 * 二维码结果监听返回
 *
 */
public interface ScanListener {
	/**
	 * 返回扫描结果
	 * @param rawResult  结果对象
	 * @param bundle  存放了截图，或者是空的
	 */
	public void scanResult(Result rawResult, Bundle bundle);
	/**
	 * 扫描抛出的异常
	 * @param e
	 */
	public void scanError(Exception e);
	
}
