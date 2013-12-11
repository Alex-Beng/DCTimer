package sina.weibo;

import com.dctimer.DCTimer;
import com.dctimer.R;
import com.sina.weibo.sdk.api.*;
import com.sina.weibo.sdk.api.share.*;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.exception.WeiboShareException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Toast;

public class WBShareActivity extends Activity implements IWeiboHandler.Response {
	private IWeiboShareAPI  mWeiboShareAPI = null;
	public static Bitmap bitmap;
	public static String text;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.share_mblog_view);
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this, DCTimer.APP_KEY);
        if (savedInstanceState != null) {
            mWeiboShareAPI.handleWeiboResponse(getIntent(), this);
        }
        
        if (!mWeiboShareAPI.isWeiboAppInstalled()) {
			Toast.makeText(this, "û�а�װ΢���ͻ���", Toast.LENGTH_SHORT).show();
			this.finish();
		} else try {
			mWeiboShareAPI.registerApp();
			if (mWeiboShareAPI.isWeiboAppSupportAPI()) {
				int supportApi = mWeiboShareAPI.getWeiboAppSupportAPI();
				if (supportApi >= 10351 /*ApiUtils.BUILD_INT_VER_2_2*/) {
					sendMultiMessage();
				} else sendSingleMessage();
			} else {
	            Toast.makeText(this, "΢���ͻ��˲�֧�� SDK �����΢���ͻ���δ��װ��΢���ͻ����Ƿǹٷ��汾��", Toast.LENGTH_SHORT).show();
	        }
		} catch (WeiboShareException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
	}
	
	@Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // �ӵ�ǰӦ�û���΢�������з���󣬷��ص���ǰӦ��ʱ����Ҫ�ڴ˴����øú���
        // ������΢���ͻ��˷��ص����ݣ�ִ�гɹ������� true��������
        // {@link IWeiboHandler.Response#onResponse}��ʧ�ܷ��� false�������������ص�
        mWeiboShareAPI.handleWeiboResponse(intent, this);
    }
	
	
	@Override
	public void onResponse(BaseResponse baseResp) {
		switch (baseResp.errCode) {
        case WBConstants.ErrorCode.ERR_OK:
            Toast.makeText(this, getString(R.string.send_sucess), Toast.LENGTH_SHORT).show();
            break;
        case WBConstants.ErrorCode.ERR_CANCEL:
            Toast.makeText(this, getString(R.string.send_cancel), Toast.LENGTH_SHORT).show();
            break;
        case WBConstants.ErrorCode.ERR_FAIL:
            Toast.makeText(this, 
                    getString(R.string.send_failed) + "\nError Message: " + baseResp.errMsg, 
                    Toast.LENGTH_LONG).show();
            break;
        }
		this.finish();
	}

	private void sendMultiMessage() {
		// 1. ��ʼ��΢���ķ�����Ϣ
		WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
		weiboMessage.textObject = getTextObj();
		weiboMessage.imageObject = getImageObj();
		// 2. ��ʼ���ӵ�������΢������Ϣ����
		SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
		// ��transactionΨһ��ʶһ������
		request.transaction = String.valueOf(System.currentTimeMillis());
		request.multiMessage = weiboMessage;
		// 3. ����������Ϣ��΢��������΢���������
		mWeiboShareAPI.sendRequest(request);
	}
	
	private void sendSingleMessage() {
		// 1. ��ʼ��΢���ķ�����Ϣ
		// �û����Է����ı���ͼƬ����ҳ�����֡���Ƶ�е�һ��
		WeiboMessage weiboMessage = new WeiboMessage();
		weiboMessage.mediaObject = getTextObj();
		// 2. ��ʼ���ӵ�������΢������Ϣ����
		SendMessageToWeiboRequest request = new SendMessageToWeiboRequest();
		// ��transactionΨһ��ʶһ������
		request.transaction = String.valueOf(System.currentTimeMillis());
		request.message = weiboMessage;
		// 3. ����������Ϣ��΢��������΢���������
		mWeiboShareAPI.sendRequest(request);
	}

	private TextObject getTextObj() {
        TextObject textObject = new TextObject();
        textObject.text = text;
        return textObject;
    }
	
	private ImageObject getImageObj() {
        ImageObject imageObject = new ImageObject();
        imageObject.setImageObject(bitmap);
        return imageObject;
    }
}
