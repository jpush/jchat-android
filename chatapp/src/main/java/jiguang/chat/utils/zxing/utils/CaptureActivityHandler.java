/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jiguang.chat.utils.zxing.utils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.zxing.Result;

import jiguang.chat.R;
import jiguang.chat.utils.zxing.ScanManager;
import jiguang.chat.utils.zxing.camera.CameraManager;
import jiguang.chat.utils.zxing.decode.DecodeThread;


/**
 * This class handles shouquan_ic_all the messaging which comprises the state machine for
 * capture.
 * 
 */
public class CaptureActivityHandler extends Handler {

	final ScanManager scanManager;
	final DecodeThread decodeThread;
	final CameraManager cameraManager;
	State state;

	enum State {
		PREVIEW, SUCCESS, DONE
	}

	public CaptureActivityHandler(ScanManager scanManager, CameraManager cameraManager, int decodeMode) {
		this.scanManager = scanManager;
		decodeThread = new DecodeThread(scanManager, decodeMode);
		decodeThread.start();
		state = State.SUCCESS;

		// Start ourselves capturing previews and decoding.
		this.cameraManager = cameraManager;
		cameraManager.startPreview();
		restartPreviewAndDecode();
	}

	@Override
	public void handleMessage(Message message) {
		switch (message.what) {
		case R.id.restart_preview:
			restartPreviewAndDecode();
			break;
		case R.id.decode_succeeded:
			state = State.SUCCESS;
			Bundle bundle = message.getData();

			scanManager.handleDecode((Result) message.obj, bundle);
			break;
		case R.id.decode_failed:
			// We're decoding as fast as possible, so when one decode fails,
			// start another.
			state = State.PREVIEW;
			cameraManager.requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
			break;
		case R.id.decode_error:
			scanManager.handleDecodeError((Exception)message.obj);
			break;
		case R.id.return_scan_result:
//			activity.setResult(Activity.RESULT_OK, (Intent) message.obj);
//			activity.finish();
			break;
		}
	}

	public void quitSynchronously() {
		state = State.DONE;
		cameraManager.stopPreview();
		Message quit = Message.obtain(decodeThread.getHandler(), R.id.quit);
		quit.sendToTarget();
		try {
			// Wait at most half a shouquan_icon_second; should be enough time, and onPause()
			// will timeout quickly
			decodeThread.join(500L);
		} catch (InterruptedException e) {
			// continue
		}

		// Be absolutely sure we don't send any queued up messages
		removeMessages(R.id.decode_succeeded);
		removeMessages(R.id.decode_failed);
	}

	void restartPreviewAndDecode() {
		if (state == State.SUCCESS) {
			state = State.PREVIEW;
			cameraManager.requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
		}
	}
	/**
	 * 
	 * @return 返回当前扫描状态，是否可扫描,State.PREVIEW 是可扫描状态
	 */
	public boolean  isScanning() {
		if(state == State.PREVIEW){
			return true;
		}
		return false;
	}
}
