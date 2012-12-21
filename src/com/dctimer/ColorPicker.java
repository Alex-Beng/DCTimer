package com.dctimer;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Shader.TileMode;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class ColorPicker extends Dialog {
	Context context;
	private int mInitialColor;//��ʼ��ɫ
	private OnColorChangedListener mListener;
	
	/**
     * 
     * @param context
     * @param initialColor ��ʼ��ɫ
     * @param listener �ص�
     */
    public ColorPicker(Context context, int initialColor, 
    		OnColorChangedListener listener) {
        super(context);
        this.context = context;
        mListener = listener;
        mInitialColor = initialColor;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager manager = getWindow().getWindowManager();
        int size=Math.min(manager.getDefaultDisplay().getHeight(), manager.getDefaultDisplay().getWidth());
		int height = (int) (size * 0.8);
		int width = (int) (size * 0.8);
		ColorPickerView myView = new ColorPickerView(context, height, width);
        setContentView(myView);
    }
    
    private class ColorPickerView extends View {
    	private Paint mPaint;//�߲�ͼ����
    	private Paint mShaderPaint;//���ͶȻ���
    	private Paint mRectPaint;//���䷽�黭��
    	private Paint mLeftPaint;//ѡ��黭��
    	private Paint mRightPaint;
    	
    	private int[] mCircleColors;//����ͼ��ɫ
    	private int[] mRectColors;//���䷽����ɫ
    	
    	private int mHeight;//View��
    	private int mWidth;//View��
    	private int hue; //ɫ��
    	private double saturation; //���Ͷ�
    	private double lum; //����
    	
    	private boolean downInCRect = true;//�����߲�ͼ��
    	private boolean downInRect;//���ڽ��䷽����
    	private int downInBottom; //����ѡ�����
    	
    	public ColorPickerView(Context context, int height, int width) {
    		super(context);
			this.mHeight = height;
			this.mWidth = width;
			setMinimumHeight(height);
			setMinimumWidth(width);
			
			//�߲�ͼ����
			mCircleColors = new int[] {0xffff0000, 0xffffff00, 0xff00ff00, 0xff00ffff,
		            0xff0000ff, 0xffff00ff, 0xffff0000};
			float[] op = {0, 0.16667f, 0.33333f, 0.5f, 0.66667f, 0.83333f, 1};
			LinearGradient lg = new LinearGradient(0, 0, (int)(width*0.82), 0, mCircleColors, op,
		            TileMode.MIRROR);
			mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mPaint.setShader(lg);
			lg = new LinearGradient(0, 0, 0, (int)(height*0.67), 0x00808080,
		            0xff808080, TileMode.MIRROR);
			mShaderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mShaderPaint.setShader(lg);
			
			//ѡ������
			mLeftPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mLeftPaint.setColor(mInitialColor);
			mRightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mRightPaint.setColor(mInitialColor);

			//�������
			mRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			
			double[] hsl = rgbToHSL(mInitialColor);
			hue = (int)hsl[0];
			saturation = hsl[1];
    		lum = hsl[2];
    	}
    	
    	@Override
    	protected void onDraw(Canvas canvas) {
    		//���߲�ͼ
    		canvas.drawRect(0, 0, (int)(mWidth*0.82), (int)(mHeight*0.67), mPaint);
    		canvas.drawRect(0, 0, (int)(mWidth*0.82), (int)(mHeight*0.67), mShaderPaint);
    		
    		//��������
    		int x = hslToRgb(hue, saturation, 0.5);
    		mRectColors = new int[] {0xffffffff, x, 0xff000000};
    		float[] op = new float[]{0, 0.5f, 1};
    		LinearGradient lg = new LinearGradient(0, 0, 0, (int)(mHeight*0.67), mRectColors, op, TileMode.MIRROR);
    		mRectPaint.setShader(lg);
    		canvas.drawRect((int)(mWidth*0.84), 0, mWidth, (int)(mHeight*0.67), mRectPaint);
    		
    		//��ѡ���
    		canvas.drawRect(0, (int)(mHeight*0.69), (int)(mWidth*0.49), (int)(mHeight*0.85), mLeftPaint);
    		canvas.drawRect((int)(mWidth*0.51), (int)(mHeight*0.69), mWidth, (int)(mHeight*0.85), mRightPaint);
    		super.onDraw(canvas);
    		Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    		textPaint.setTextSize(mHeight/16);
    		textPaint.setTextAlign(Align.CENTER);
    		textPaint.setColor((0xffffff - mLeftPaint.getColor()&0xffffff)|0xff000000);
    		canvas.drawText(context.getResources().getString(R.string.btn_ok), mWidth/4, (int)(mHeight*0.79), textPaint);
    		textPaint.setColor((0xffffff - mRightPaint.getColor()&0xffffff)|0xff000000);
    		canvas.drawText(context.getResources().getString(R.string.btn_cancel), mWidth*3/4, (int)(mHeight*0.79), textPaint);
    	}
    	
    	@Override
    	public boolean onTouchEvent(MotionEvent event) {
    		float x = event.getX();
    		float y = event.getY();
    		boolean inCRect = inColorRect(x, y);
    		int inBottom = inBottom(x, y);
    		boolean inRect = inRect(x, y);
    		
    		switch (event.getAction()) {
    		case MotionEvent.ACTION_DOWN:
    			downInCRect = inCRect;
    			downInRect = inRect;
    			downInBottom = inBottom;
    		case MotionEvent.ACTION_MOVE:
    			if(downInCRect && inCRect) { //down���ڽ���ɫ����, ��moveҲ�ڽ���ɫ����
    				hue = getHue(x);
    				saturation = getSl(y);
    				mLeftPaint.setColor(hslToRgb(hue, saturation, lum));
    			}else if(downInRect && inRect) { //down�ڽ��䷽����, ��moveҲ�ڽ��䷽����
    				lum = getSl(y);
    				mLeftPaint.setColor(hslToRgb(hue, saturation, lum));
    				//Log.v("text", hue+","+saturation+","+lum);
    			}
    			if(downInBottom == 1 && inBottom !=1){
    				downInBottom = 0;
    			}
    			else if(downInBottom == 2 && inBottom !=2){
    				downInBottom = 0;
    			}
    			invalidate();
            	break;
    		case MotionEvent.ACTION_UP:
    			if(downInBottom == 1){
    				if(mListener != null) {
    					mListener.colorChanged(mLeftPaint.getColor());
    					ColorPicker.this.dismiss();
    				}
    			}
    			else if(downInBottom == 2) {
    				ColorPicker.this.dismiss();
    			}
    			break;
    		}
    		return true;
    	}
    	
    	@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			super.onMeasure(mWidth, mHeight);
		}
    	
    	/**
		 * �����Ƿ����߲�ͼ��
		 * @param x ����
		 * @param y ����
		 * @return
		 */
		private boolean inColorRect(float x, float y) {
			return x > 0 && x < mWidth * 0.82 && y>0 && y < mHeight * 0.67;
		}
		
		/**
		 * �����Ƿ���ѡ�����
		 * @param x ����
		 * @param y ����
		 * @return 0:����ѡ���
		 * 	1:���ѡ���
		 *  2:�ұ�ѡ���
		 */
		private int inBottom(float x, float y) {
			if(y < mHeight * 0.69)return 0;
			else if(y < mHeight * 0.85){
				if(x > 0 && x < mWidth / 2)return 1;
				else if(x > mWidth / 2 && x < mWidth)return 2;
			}
			return 0;
		}
		
		/**
		 * �����Ƿ��ڽ���ɫ��
		 * @param x
		 * @param y
		 * @return
		 */
		private boolean inRect(float x, float y) {
			return x > mWidth * 0.84 && x < mWidth && y > 0 && y < mHeight * 0.67;
		}
		
		/**
		 * ��ȡ�߲�ͼɫ��
		 * @param x
		 * @return
		 */
		private int getHue(float x) {
            return (int) (x * 439.02439 / mWidth);
        }
		
		/**
		 * ��ȡ�߲�ͼ���Ͷȡ�����
		 * @param y
		 * @return
		 */
		private double getSl(float y) {
            return 1 - y / mWidth / 0.67;
        }
    }
    
    /**
     * �ص��ӿ�
     * @author <a href="clarkamx@gmail.com">LynK</a>
     * 
     * Create on 2012-1-6 ����8:21:05
     *
     */
    public interface OnColorChangedListener {
    	/**
    	 * �ص�����
    	 * @param color ѡ�е���ɫ
    	 */
        void colorChanged(int color);
    }
    
    public int getmInitialColor() {
		return mInitialColor;
	}

	public void setmInitialColor(int mInitialColor) {
		this.mInitialColor = mInitialColor;
	}

	public OnColorChangedListener getmListener() {
		return mListener;
	}

	public void setmListener(OnColorChangedListener mListener) {
		this.mListener = mListener;
	}
	
	public static int hslToRgb(int h, double s, double l){
		double r, g, b;
		if(s == 0) r = g = b = l;
		else {
			double q, p, tr, tg, tb;
			if(l<0.5) q = l * (1 + s);
			else q = l + s - l * s;
			p = 2 * l - q;
			double H = h/360D;
			tr = H + 1/3D;
			tg = H;
			tb = H - 1/3D;
			r = toRGB(tr, q, p, H);
			g = toRGB(tg, q, p, H);
			b = toRGB(tb, q, p, H);
		}
		r = r * 255 + 0.5;
		g = g * 255 + 0.5;
		b = b * 255 + 0.5;
		return Color.rgb((int)r, (int)g, (int)b);
	}
	
	public static double[] rgbToHSL(int rgb){
		double R = ((rgb>>16) & 0xff) / 255D;
		double G = ((rgb>>8) & 0xff) / 255D;
		double B = (rgb & 0xff) / 255D;
		double h = 0, s = 0, l;
		double max = Math.max(Math.max(R, G), B);
		double min = Math.min(Math.min(R, G), B);
		if(max == min) h = 0;
		else if(max == R && G >= B) h = 60 * ((G - B) / (max - min));
		else if(max == R && G < B) h = 60 * ((G - B) / (max - min)) + 360;
		else if(max == G) h = 60 * ((B - R) / (max - min)) + 120;
		else if(max == B) h = 60 * ((R - G) / (max - min)) + 240;
		l = (max + min) / 2;
		if(l == 0 || max == min) s = 0;
		else if(l > 0 && l <= 0.5)s = (max - min) / (max + min);
		else if(l > 0.5) s = (max - min) / (2 - (max + min));
		return new double[]{h, s, l};
	}
	
	private static double toRGB(double tc, double q, double p, double H){
		if(tc < 0)tc += 1;
		if(tc > 1)tc -= 1;
		if(tc < 1/6D)
			return p + (q - p) * 6 * tc;
		else if(tc < 0.5)
			return q;
		else if(tc < 2/3D)
			return p + (q - p) * 6 * (2/3D - tc);
		else return p;
	}
}