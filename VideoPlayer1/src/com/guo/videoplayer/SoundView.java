package com.guo.videoplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
//�������ƽ���
public class SoundView extends View{
	public final static String TAG = "SoundView";
	//������
	private Context mContext;
	private Bitmap bm , bm1;
	private int bitmapWidth , bitmapHeight;
	//������С
	private int index;
	//�����ı������
	private OnVolumeChangedListener mOnVolumeChangedListener;	
	private final static int HEIGHT = 11;
	public final static int MY_HEIGHT = 163;
	public final static int MY_WIDTH = 44;
	//�����ı������
	public interface OnVolumeChangedListener{
		public void setYourVolume(int index);
	}
	//���������ı������
	public void setOnVolumeChangeListener(OnVolumeChangedListener l){
		mOnVolumeChangedListener = l;
	}
	//���캯��
	public SoundView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		//�����ԡ���ʽ��ʼ������
		init();
	}
	//���캯��
	public SoundView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		//�����Գ�ʼ������
		init();
	}
	//���캯��
	public SoundView(Context context) {
		super(context);
		mContext = context;
		//�������ԡ���ʽ��ʼ������
		init();
	}
	//��ʼ��
	private void init(){
		bm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.sound_line);
		bm1 = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.sound_line1);
		//ȡ��ͼƬ�ĸ߿�
		bitmapWidth = bm.getWidth();
		bitmapHeight = bm.getHeight();
		AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		//���õ�ǰ����
		setIndex(am.getStreamVolume(AudioManager.STREAM_MUSIC));
	}
	//�����¼�
	@Override
	public boolean onTouchEvent(MotionEvent event) { 
		//���ݴ���λ�þ���������С
		int y = (int) event.getY();
		int n = y * 15 / MY_HEIGHT;
		setIndex(15-n);
		return true;
	}
	@Override
	protected void onDraw(Canvas canvas) {
		int reverseIndex = 15 - index;
		//���Ʋ�ɫ������
		for(int i = 0;i!=reverseIndex;++i){
			canvas.drawBitmap(bm1, new Rect(0,0,bitmapWidth,bitmapHeight), 
					new Rect(0,i*HEIGHT,bitmapWidth,i*HEIGHT+bitmapHeight), null);
		}
		//���ƻ�ɫ������
		for(int i = reverseIndex;i!=15;++i){
			canvas.drawBitmap(bm, new Rect(0,0,bitmapWidth,bitmapHeight), 
					new Rect(0,i*HEIGHT,bitmapWidth,i*HEIGHT+bitmapHeight), null);
		}	
		super.onDraw(canvas);  
	}
	//�ı�������С
	private void setIndex(int n){
		if(n>15){
			n = 15;
		}
		else if(n<0){
			n = 0;
		}
		if(index!=n){
			index = n;
			if(mOnVolumeChangedListener!=null){
				mOnVolumeChangedListener.setYourVolume(n);
			}
		}
		//�ػ����
		invalidate();
	}
}