package com.example.videoview;

import java.io.IOException;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;

public class VideoView  extends SurfaceView implements MediaPlayerControl{
	private String TAG="VideoView";
	private Context mContext;
	private  Uri mUri;
	private int mDuration;
	
	private SurfaceHolder mSurfaceHolder=null;
	private MediaPlayer mMediaPlayer=null;
	private boolean mIsPrepared;
	
	private int mVideoWidth;
	private int mVideoHeight;
	
	private int mSurfaceWide;
	private int mSurfaceHeight;
	
	private MediaController mMediaController;
	private OnCompletionListener  mOnCompletionListener;
	private OnPreparedListener mOnPreparedListener;
	private int mCurrentBufferPercentage;
	
	
	private OnErrorListener  mOnErrorListener;
	private boolean  mStartWhenPrepared;
	private int mSeekWhenPrepared;
	
	private MySizeChangeLinstener mMySizeChangeLinstener;
	
	public int getVideoWidth(){
		return mVideoWidth;
	}
	public int getVideoHeight(){
		return mVideoHeight;
	}
	
	public void setVideoScale(int width,int height){
		android.view.ViewGroup.LayoutParams lp=getLayoutParams();
		lp.height=height;
		lp.width=width;
		setLayoutParams(lp);
	}
	
	public VideoView(Context context) {
		super(context);
		mContext=context;
		initVideoView();
	}

	public VideoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext=context;
		initVideoView();
	}

	public VideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext=context;
		initVideoView();
	}
	
	public void  initVideoView(){
		mVideoHeight=0;
		mVideoWidth=0;
		getHolder().addCallback(mSHCallback);
		getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		setFocusable(true);
		setFocusableInTouchMode(true);
		requestFocus();
	}
	
	SurfaceHolder.Callback mSHCallback =new SurfaceHolder.Callback() {
		
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			mSurfaceHolder=null;
			if(mMediaController !=null) mMediaController.hide();
			if(mMediaPlayer !=null){
				mMediaPlayer.reset();
				mMediaPlayer.release();
				mMediaPlayer =null;
			}
			
		}
		
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			mSurfaceHolder=holder;
			openvideo();
			
		}
		
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			mSurfaceHeight =height;
			mSurfaceWide =width;
			if(mMediaPlayer !=null && mIsPrepared && mVideoHeight==height && mVideoWidth ==width){
				if(mSeekWhenPrepared !=0){
					mMediaPlayer.seekTo(mSeekWhenPrepared);
					mSeekWhenPrepared=0;
				}
				mMediaPlayer.start();
				if(mMediaController !=null){
					mMediaController.show();
				}
				
			}
			
		}
	};
	
	private void openvideo(){
		if(mUri ==null || mSurfaceHolder ==null){
			return ;
		}
		Intent i=new Intent("com.android.music.musicservicecommand");
		i.putExtra("command","pause");
		mContext.sendBroadcast(i);
		if(mMediaPlayer !=null){
			mMediaPlayer.reset();
			mMediaPlayer.release();
			mMediaPlayer=null;
		}
		try{
			mMediaPlayer=new MediaPlayer();
			mMediaPlayer.setOnPreparedListener(mPreparedListener);
			mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
			mIsPrepared=false;
			mDuration=-1;
			mMediaPlayer.setOnCompletionListener(mCompletionListener);
			mMediaPlayer.setOnErrorListener(mErrorListener);
			mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
			mCurrentBufferPercentage=0;
			mMediaPlayer.setDataSource(mContext, mUri);
			mMediaPlayer.setDisplay(mSurfaceHolder);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mMediaPlayer.setScreenOnWhilePlaying(true);
			mMediaPlayer.prepareAsync();
			attachMediaController();
		}catch (IOException e) {
			Log.w(TAG, "Unable to open content: "+mUri,e);
			return ;
		}catch(IllegalArgumentException ex){
			Log.w(TAG, "Unable to open content: "+mUri,ex);
			return ;	
		}
	}
	
	public void setMediaController(MediaController controller){
		if(mMediaController !=null){
			mMediaController.hide();
		}
		
		mMediaController =controller;
		attachMediaController();
	}
	
	private void attachMediaController() {
		if(mMediaController !=null && mMediaPlayer !=null){
			mMediaController.setMediaPlayer(this);
			View anchorView =this.getParent()  instanceof  View ? (View)this.getParent() : this;
			mMediaController.setAnchorView(anchorView);
			mMediaController.setEnabled(mIsPrepared);
		}
		
	}

	MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener=new OnVideoSizeChangedListener() {
		
		@Override
		public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
			mVideoHeight=mp.getVideoHeight();
			mVideoWidth=mp.getVideoWidth();
			if(mMySizeChangeLinstener !=null){
				mMySizeChangeLinstener.doMyThings();
			}
			if(mVideoHeight !=0 && mVideoWidth !=0){
				getHolder().setFixedSize(mVideoWidth, mVideoHeight);
			}
			
		}
	};
	
	private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener=new OnBufferingUpdateListener() {
		
		@Override
		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			mCurrentBufferPercentage=percent;
			
		}
	};
	MediaPlayer.OnPreparedListener mPreparedListener=new MediaPlayer.OnPreparedListener() {
		
		@Override
		public void onPrepared(MediaPlayer mp) {
			mIsPrepared=true;
			if(mOnPreparedListener !=null){
				mOnPreparedListener.onPrepared(mMediaPlayer);
			}
			if(mMediaController !=null){
				mMediaController.setEnabled(true);
			}
			
			mVideoHeight=mp.getVideoHeight();
			mVideoWidth=mp.getVideoWidth();
			if(mVideoHeight !=0 && mVideoWidth !=0){
				getHolder().setFixedSize(mVideoWidth, mVideoHeight);
				if(mSurfaceWide ==mVideoWidth && mSurfaceHeight ==mVideoHeight){
					if(mSeekWhenPrepared !=0){
						mMediaPlayer.seekTo(mSeekWhenPrepared);
						mSeekWhenPrepared=0;
					}
					
					if(mStartWhenPrepared){
						mMediaPlayer.start();
						mStartWhenPrepared=false;
						if(mMediaController !=null){
							mMediaController.show();
						}
					}else if(!isPlaying() && (mSeekWhenPrepared !=0 || getCurrentPosition()>0)){
						if(mMediaController !=null){
							mMediaController.show(0);
						}
					}
				}
			}else{
				if(mSeekWhenPrepared !=0){
					mMediaPlayer.seekTo(mSeekWhenPrepared);
					mSeekWhenPrepared=0;
				}
				if(mStartWhenPrepared){
					mMediaPlayer.start();
					mStartWhenPrepared=false;
				}
			}	
		}
	};
	
	private MediaPlayer.OnCompletionListener mCompletionListener=new OnCompletionListener() {
		
		@Override
		public void onCompletion(MediaPlayer mp) {
			if(mMediaController !=null){
				mMediaController.hide();
			}
			if(mOnCompletionListener !=null){
				mOnCompletionListener.onCompletion(mMediaPlayer);
			}
			
		}
	};
	
	
	private MediaPlayer.OnErrorListener mErrorListener=new OnErrorListener() {
		
		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			if(mMediaController !=null){
				mMediaController.hide();
			}
			if(mOnErrorListener !=null){
				if(mOnErrorListener.onError(mMediaPlayer, what, extra)){
					return true;
				}
			}
			return true;
		}
	};
	
	
	public void setOnPreparedListener(OnPreparedListener mOnPreparedListener) {
		this.mOnPreparedListener = mOnPreparedListener;
	}
	
	public void setOnErrorListener(OnErrorListener mOnErrorListener) {
		this.mOnErrorListener = mOnErrorListener;
	}
	
	public void setPreparedListener(MediaPlayer.OnPreparedListener mPreparedListener) {
		this.mPreparedListener = mPreparedListener;
	}
	
	
	@Override
	public boolean canPause() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canSeekBackward() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canSeekForward() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getBufferPercentage() {
		return 0;
	}

	@Override
	public int getCurrentPosition() {
		return 0;
	}

	@Override
	public int getDuration() {
		return 0;
	}

	@Override
	public boolean isPlaying() {
		return false;
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void seekTo(int pos) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}
	
	public void setMySizeChangeLinstener(MySizeChangeLinstener l){
		mMySizeChangeLinstener=l;
	}
	
	public interface MySizeChangeLinstener{
		
		public void doMyThings();
	}

}
