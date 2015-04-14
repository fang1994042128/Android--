package com.guo.videoplayer;

/*
 * Copyright (C) 2006 The Android Open Source Project
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

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import java.io.IOException;
//��Ƶ���Ž���
public class VideoView extends SurfaceView implements MediaPlayerControl {
    private String TAG = "VideoView";
    //������
    private Context mContext;
    //��Ƶ·���ͳ���ʱ��
    private Uri         mUri;
    private int         mDuration;

    private SurfaceHolder mSurfaceHolder = null;
    private MediaPlayer mMediaPlayer = null;
    private boolean     mIsPrepared;
    //��Ƶ�ĸ߿�
    private int         mVideoWidth;
    private int         mVideoHeight;
    //���Ž���ĸ߿�
    private int         mSurfaceWidth;
    private int         mSurfaceHeight;
    //ý�������
    private MediaController mMediaController;
    //������ϼ�����
    private OnCompletionListener mOnCompletionListener;
    //����׼��������
    private MediaPlayer.OnPreparedListener mOnPreparedListener;
    private int         mCurrentBufferPercentage;
    //���������
    private OnErrorListener mOnErrorListener;
    private boolean     mStartWhenPrepared;
    private int         mSeekWhenPrepared;
    //�ߴ�ı������
    private MySizeChangeLinstener mMyChangeLinstener;
    //ȡ����Ƶ�Ŀ�
    public int getVideoWidth(){
    	return mVideoWidth;
    }
    //ȡ����Ƶ�ĸ�
    public int getVideoHeight(){
    	return mVideoHeight;
    }
    //������Ƶ���Ŵ��ڵĸ߿�
    public void setVideoScale(int width , int height){
    	LayoutParams lp = getLayoutParams();
    	lp.height = height;
		lp.width = width;
		setLayoutParams(lp);
    }   

    //���캯��
    public VideoView(Context context) {
        super(context);
        mContext = context;
        //��ʼ����Ƶ����
        initVideoView();
    }
    //�����Թ��캯��
    public VideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
        //��ʼ����Ƶ����
        initVideoView();
    }
    //�����ԡ���ʽ���캯��
    public VideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        //��ʼ����Ƶ����
        initVideoView();
    }
    //��ʼ������
    private void initVideoView() {
        mVideoWidth = 0;
        mVideoHeight = 0;
        getHolder().addCallback(mSHCallback);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setFocusable(true);
        setFocusableInTouchMode(true);
        //���󽹵�
        requestFocus();
    }   
    //surfaceview�ص�����
    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback()
    {
        public void surfaceChanged(SurfaceHolder holder, int format,
                                    int w, int h)
        {
        	//ȡ�ò��Ž���ĳߴ�
            mSurfaceWidth = w;
            mSurfaceHeight = h;
            if (mMediaPlayer != null && mIsPrepared && mVideoWidth == w && mVideoHeight == h) {
                if (mSeekWhenPrepared != 0) {
                    mMediaPlayer.seekTo(mSeekWhenPrepared);
                    mSeekWhenPrepared = 0;
                }
                //��ʼ������Ƶ
                mMediaPlayer.start();
                //����ʾ����������
                if (mMediaController != null) {
                    mMediaController.show();
                }
            }
        }
        //����Ƶ
        public void surfaceCreated(SurfaceHolder holder)
        {
            mSurfaceHolder = holder;
            openVideo();
        }
        //��������
        public void surfaceDestroyed(SurfaceHolder holder)
        {
            //�ͷ�ý�岥������Դ
            mSurfaceHolder = null;
            if (mMediaController != null) mMediaController.hide();
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        }
    };
  
    //����Ƶ
    private void openVideo() {
        if (mUri == null || mSurfaceHolder == null) {
            //��ǰδ׼������
            return;
        }
        //��ͣ
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        mContext.sendBroadcast(i);
        //�ͷ���Դ
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        try {
        	//�½�meidiaplayer
            mMediaPlayer = new MediaPlayer();
            //����׼��������
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            //���óߴ�ı������
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mIsPrepared = false;
            //������Ƶ����ʱ��
            mDuration = -1;
            //���ò�����ɼ�����
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            //���ô��������
            mMediaPlayer.setOnErrorListener(mErrorListener);
            //���û�����¼�����
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mCurrentBufferPercentage = 0;
            //������Ƶ�ļ�·��
            mMediaPlayer.setDataSource(mContext, mUri);
            mMediaPlayer.setDisplay(mSurfaceHolder);
            //������Ƶ������
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            //���ò���ʱ��Ļһֱ����
            mMediaPlayer.setScreenOnWhilePlaying(true);
            //�첽׼��
            mMediaPlayer.prepareAsync();
            //��ý�������
            attachMediaController();
            //�쳣����
        } catch (IOException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            return;
        } catch (IllegalArgumentException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            return;
        }
    }
    //����ý�岥����������
    public void setMediaController(MediaController controller) {
        if (mMediaController != null) {
            mMediaController.hide();
        }
        mMediaController = controller;
      //��ý�������
        attachMediaController();
    }
    //��ý�������
    private void attachMediaController() {
        if (mMediaPlayer != null && mMediaController != null) {
            mMediaController.setMediaPlayer(this);
            View anchorView = this.getParent() instanceof View ?
                    (View)this.getParent() : this;
            mMediaController.setAnchorView(anchorView);
            mMediaController.setEnabled(mIsPrepared);
        }
    }
    //�Զ���ص������ӿ�
    public interface MySizeChangeLinstener{
    	public void doMyThings();
    }
    //ȡ�óߴ�ı������
    public void setMySizeChangeLinstener(MySizeChangeLinstener l){
    	mMyChangeLinstener = l;
    }
    //�ߴ�ı������
    MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
        new MediaPlayer.OnVideoSizeChangedListener() {
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            	//ȡ�õ�ǰ�ĳߴ�
                mVideoWidth = mp.getVideoWidth();
                mVideoHeight = mp.getVideoHeight();               
                if(mMyChangeLinstener!=null){
                	mMyChangeLinstener.doMyThings();
                }
                //���óߴ�
                if (mVideoWidth != 0 && mVideoHeight != 0) {
                    getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                }
            }
    };
    //ý�岥����׼��������
    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            mIsPrepared = true;
            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared(mMediaPlayer);
            }
            //���ø���ͼ�Ŀ���״̬
            if (mMediaController != null) {
                mMediaController.setEnabled(true);
            }
            //ȡ����Ƶ�ļ��Ŀ����Ϣ
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                //Ϊ�������ø߿���Ϣ
                getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
                    //���ò��ų�ʼλ��
                    if (mSeekWhenPrepared != 0) {
                        mMediaPlayer.seekTo(mSeekWhenPrepared);
                        mSeekWhenPrepared = 0;
                    }
                    //��׼����ϣ�������Ƶ
                    if (mStartWhenPrepared) {
                        mMediaPlayer.start();
                        mStartWhenPrepared = false;
                        //��ʾ������
                        if (mMediaController != null) {
                            mMediaController.show();
                        }
                    } else if (!isPlaying() &&
                            (mSeekWhenPrepared != 0 || getCurrentPosition() > 0)) {
                       if (mMediaController != null) {
                           // ����ͣʱ��ʾ������
                           mMediaController.show(0);
                       }
                   }
                }
            } else {
                //δ֪��Ƶ�ߴ�ʱ��Ȼ���Ÿ���Ƶ
                if (mSeekWhenPrepared != 0) {
                    mMediaPlayer.seekTo(mSeekWhenPrepared);
                    mSeekWhenPrepared = 0;
                }
                //��ʼ����
                if (mStartWhenPrepared) {
                    mMediaPlayer.start();
                    mStartWhenPrepared = false;
                }
            }
        }
    };
    //�������ʱ����
    private MediaPlayer.OnCompletionListener mCompletionListener =
        new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
        	//���ؿ�����
            if (mMediaController != null) {
                mMediaController.hide();
            }
            if (mOnCompletionListener != null) {
                mOnCompletionListener.onCompletion(mMediaPlayer);
            }
        }
    };
    //���������
    private MediaPlayer.OnErrorListener mErrorListener =
        new MediaPlayer.OnErrorListener() {
        public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
            //���ؿ�����
            if (mMediaController != null) {
                mMediaController.hide();
            }
            if (mOnErrorListener != null) {
                if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
                    return true;
                }
            }
            return true;
        }
    };
    //���������
    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
        new MediaPlayer.OnBufferingUpdateListener() {
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
        	//���õ�ǰ����ٷֱ�
            mCurrentBufferPercentage = percent;
        }
    };
    //����׼��������
    public void setOnPreparedListener(MediaPlayer.OnPreparedListener l)
    {
        mOnPreparedListener = l;
    }
    //���ò�����ϼ�����
    public void setOnCompletionListener(OnCompletionListener l)
    {
        mOnCompletionListener = l;
    }
    //���ó��������
    public void setOnErrorListener(OnErrorListener l)
    {
        mOnErrorListener = l;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //���ݳߴ���Ϣ
        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        setMeasuredDimension(width,height);
    }
    //��������ߴ���Ӧ�ֱ���
    public int resolveAdjustedSize(int desiredSize, int measureSpec) {
        int result = desiredSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize =  MeasureSpec.getSize(measureSpec);

        switch (specMode) {
        //������
        case MeasureSpec.UNSPECIFIED:
            result = desiredSize;
            break;
        //���ܳ������Ƴߴ�
        case MeasureSpec.AT_MOST:
            result = Math.min(desiredSize, specSize);
            break;
        //��ȷ���óߴ�
        case MeasureSpec.EXACTLY:
            result = specSize;
            break;
        }
        return result;
}
    //������Ƶ·��
    public void setVideoPath(String path) {
        setVideoURI(Uri.parse(path));
    }
    //������Ƶuri��ַ
    public void setVideoURI(Uri uri) {
        mUri = uri;
        mStartWhenPrepared = false;
        mSeekWhenPrepared = 0;
        //����Ƶ
        openVideo();
        requestLayout();
        //���½���
        invalidate();
    }
    //ֹͣ����
    public void stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
    //��Ӧ�����¼�����ͣ/�����л�
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mIsPrepared && mMediaPlayer != null && mMediaController != null) {
            toggleMediaControlsVisiblity();
        }
        return false;
    }
    //��Ƶ���ſ��������ء���ʾ�л�
    private void toggleMediaControlsVisiblity() {
        if (mMediaController.isShowing()) {
            mMediaController.hide();
        } else {
            mMediaController.show();
        }
    }
    //��ʼ����
    public void start() {
        if (mMediaPlayer != null && mIsPrepared) {
                mMediaPlayer.start();
                mStartWhenPrepared = false;
        } else {
            mStartWhenPrepared = true;
        }
    }
    //��ͣ����
    public void pause() {
        if (mMediaPlayer != null && mIsPrepared) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
        }
        mStartWhenPrepared = false;
    }
    //ȡ����Ƶ�ĳ���ʱ��
    public int getDuration() {
        if (mMediaPlayer != null && mIsPrepared) {
            if (mDuration > 0) {
                return mDuration;
            }
            //��ò���ʱ��
            mDuration = mMediaPlayer.getDuration();
            return mDuration;
        }
        mDuration = -1;
        return mDuration;
    }
    //ȡ�õ�ǰ���ŵ�λ��
    public int getCurrentPosition() {
        if (mMediaPlayer != null && mIsPrepared) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }
    //��ת��ָ������
    public void seekTo(int msec) {
        if (mMediaPlayer != null && mIsPrepared) {
            mMediaPlayer.seekTo(msec);
        } else {
            mSeekWhenPrepared = msec;
        }
    }
    //���ز��������ŵ�״̬
    public boolean isPlaying() {
        if (mMediaPlayer != null && mIsPrepared) {
            return mMediaPlayer.isPlaying();
        }
        return false;
    }

    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }
	@Override
	public boolean canPause() {
		return false;
	}
	@Override
	public boolean canSeekBackward() {
		return false;
	}
	@Override
	public boolean canSeekForward() {
		return false;
	}
}