package com.guo.videoplayer;

import java.io.File;
import java.io.FileFilter;
import java.util.LinkedList;
import com.guo.videoplayer.SoundView.OnVolumeChangedListener;
import com.guo.videoplayer.VideoView.MySizeChangeLinstener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue.IdleHandler;
import android.provider.MediaStore;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
//������Ƶ������
public class VideoPlayerActivity extends Activity {
	//�����б�
	public static LinkedList<MovieInfo> playList = new LinkedList<MovieInfo>();
	//��Ƶ��Ϣ��
	public class MovieInfo{
		//��Ӱ����
		String displayName; 
		//�ļ�·��
		String path;
	}
	//ý���ļ����ݿ��ѯ��ַ
	private Uri videoListUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
	//���Ž�����λ��
	private static int position ;
	//����ʱ��
	private int playedTime;
	//��Ƶ��ͼ����
	private VideoView vv = null;
	//������
	private SeekBar seekBar = null;  
	//��Ƶ��ʱ��
	private TextView durationTextView = null;
	//�Ѳ���ʱ��
	private TextView playedTextView = null;
	//���Ƽ����
	private GestureDetector mGestureDetector = null;
	//��������
	private AudioManager mAudioManager = null;  
	//�����������ǰ����
	private int maxVolume = 0;
	private int currentVolume = 0;  
	//��Ƶ�б�
	private ImageButton bn1 = null;
	//ǰһ����Ƶ
	private ImageButton bn2 = null;
	//����/��ͣ
	private ImageButton bn3 = null;
	//��һ����Ƶ
	private ImageButton bn4 = null;
	//��������
	private ImageButton bn5 = null;
	//������ͼ
	private View controlView = null;
	//��������
	private PopupWindow controler = null;
	//����������ͼ
	private SoundView mSoundView = null;
	private PopupWindow mSoundWindow = null;
	//��Ļ�߿�
	private static int screenWidth = 0;
	private static int screenHeight = 0;
	private static int controlHeight = 0;  
	
	private final static int TIME = 6868;  
	//��־λ
	private boolean isControllerShow = true;
	private boolean isPaused = false;
	private boolean isFullScreen = false;
	private boolean isSilent = false;
	private boolean isSoundShow = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {  	
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.main);
        //���idle������
        Looper.myQueue().addIdleHandler(new IdleHandler(){
        	//����ǰ��Ϣ�����е���������Ϣ��ִ�������
			@Override
			public boolean queueIdle() {				
				//��ʾ������
				if(controler != null && vv.isShown()){
					controler.showAtLocation(vv, Gravity.BOTTOM, 0, 0);
					//���¿��Ƶ�λ��
					controler.update(0, 0, screenWidth, controlHeight);
				}
				//����false��ʹ��handler����һ��֮���Ƴ�
				return false;  
			}
        });       
        //���ͳ�������
        controlView = getLayoutInflater().inflate(R.layout.controler, null);
        controler = new PopupWindow(controlView);
        //��Ƶ����ʱ��
        durationTextView = (TextView) controlView.findViewById(R.id.duration);
        //�Ѳ���ʱ��
        playedTextView = (TextView) controlView.findViewById(R.id.has_played);
        //�������ƽ���
        mSoundView = new SoundView(this);
        mSoundView.setOnVolumeChangeListener(new OnVolumeChangedListener(){
        	//��������
			@Override
			public void setYourVolume(int index) {
				//�Ƴ���Ϣ���е���Ϣ
				cancelDelayHide();
				//����������С
				updateVolume(index);
				//�ӳ����ؿ�����
				hideControllerDelay();
			}
        });
        //����������ƽ���
        mSoundWindow = new PopupWindow(mSoundView);      
        position = -1;
        //��ʼ��5����ť
        bn1 = (ImageButton) controlView.findViewById(R.id.button1);
        bn2 = (ImageButton) controlView.findViewById(R.id.button2);
        bn3 = (ImageButton) controlView.findViewById(R.id.button3);
        bn4 = (ImageButton) controlView.findViewById(R.id.button4);
        bn5 = (ImageButton) controlView.findViewById(R.id.button5);
        //��ʼ����Ƶ���Ž���
        vv = (VideoView) findViewById(R.id.vv);
        //ȡ����Ƶ·��
        Uri uri = getIntent().getData();
        if(uri!=null){
        	if(vv.getVideoHeight()==0){
        		vv.setVideoURI(uri);
        	}
        	bn3.setImageResource(R.drawable.pause);
        }else{
        	bn3.setImageResource(R.drawable.play);
        }
        //ȡ��ָ��·���µ�������Ƶ�б�
        getVideoFile(playList, new File("/sdcard/"));
        //ȡ��ý����е���Ƶ��Ϣ
        Cursor cursor = getContentResolver().query(videoListUri, new String[]{"_display_name","_data"}, null, null, null);
        int n = cursor.getCount();
        cursor.moveToFirst();
        LinkedList<MovieInfo> playList2 = new LinkedList<MovieInfo>();
        //�����õ������ݣ���ý����Ϣ���浽playList2��
        for(int i = 0 ; i != n ; ++i){
        	MovieInfo mInfo = new MovieInfo();
        	mInfo.displayName = cursor.getString(cursor.getColumnIndex("_display_name"));
        	mInfo.path = cursor.getString(cursor.getColumnIndex("_data"));
        	playList2.add(mInfo);
        	cursor.moveToNext();
        }
        //�Ƚ������ַ�ʽ��õ���Ƶ��Ŀ��ȡ�ϴ���
        if(playList2.size() > playList.size()){
        	playList = playList2;
        }
        //��������Ƶ���ڴ�С�ı�ʱ
        vv.setMySizeChangeLinstener(new MySizeChangeLinstener(){
			@Override
			public void doMyThings() {
				//������Ƶ���Ŵ��ڸ߿�
				setVideoScale(SCREEN_DEFAULT);
			}
        	
        });
        //���ð�����͸����    
        bn1.setAlpha(0xBB);
        bn2.setAlpha(0xBB);  
        bn3.setAlpha(0xBB);
        bn4.setAlpha(0xBB);
        //ȡ������������
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        //���ݵ�ǰ������С���ð�����͸����
        bn5.setAlpha(findAlphaFromSound());
        //����Ƶ�����б�ť
        bn1.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(VideoPlayerActivity.this, VideoChooseActivity.class);
				//��ʾ��Ƶ�����б�
				VideoPlayerActivity.this.startActivityForResult(intent, 0);				
				cancelDelayHide();
			}       	
        });
        //����ǰһ����Ƶ
        bn4.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// ȡ��ǰһ����Ƶ������
				int n = playList.size();
				if(++position < n){
					vv.setVideoPath(playList.get(position).path);
					cancelDelayHide();
					hideControllerDelay();
				}else{
					VideoPlayerActivity.this.finish();
				}
			}
        	
        });
        //���š���ͣ�л���ť
        bn3.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				cancelDelayHide();
				//��ǰ��ͣ�����Ϊ����
				if(isPaused){
					vv.start();
					bn3.setImageResource(R.drawable.pause);
					hideControllerDelay();
				}else{
					vv.pause();
					bn3.setImageResource(R.drawable.play);
				}
				//�ı���ͣ���ű�־λ
				isPaused = !isPaused;				
			}       	
        });
        //��һ����Ƶ
        bn2.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				//������һ����Ƶ
				if(--position>=0){
					vv.setVideoPath(playList.get(position).path);
					cancelDelayHide();
					hideControllerDelay();
				}else{
					VideoPlayerActivity.this.finish();
				}
			}        	
        });
        //��ʾ�������ƽ���
        bn5.setOnClickListener(new OnClickListener(){
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			cancelDelayHide();
			//����Ѿ���ʾ������
			if(isSoundShow){
				mSoundWindow.dismiss();
			}else{
				//��ʾ�������ƽ���
				if(mSoundWindow.isShowing()){
					mSoundWindow.update(15,0,SoundView.MY_WIDTH,SoundView.MY_HEIGHT);
				}else{
					mSoundWindow.showAtLocation(vv, Gravity.RIGHT|Gravity.CENTER_VERTICAL, 15, 0);
					mSoundWindow.update(15,0,SoundView.MY_WIDTH,SoundView.MY_HEIGHT);
				}
			}
			isSoundShow = !isSoundShow;
			hideControllerDelay();
		}   
       });
        //��������������������
        bn5.setOnLongClickListener(new OnLongClickListener(){
			@Override
			public boolean onLongClick(View arg0) {
				//�������Ǿ����л�
				if(isSilent){
					bn5.setImageResource(R.drawable.soundenable);
				}else{
					bn5.setImageResource(R.drawable.sounddisable);
				}
				isSilent = !isSilent;
				//��������
				updateVolume(currentVolume);
				//ȥ��������Ϣ
				cancelDelayHide();
				//����������Ϣ
				hideControllerDelay();
				return true;
			}       	
        });
        //������
        seekBar = (SeekBar) controlView.findViewById(R.id.seekbar);
        //���������ȸı������
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
				@Override
				public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
					//�ı䵽ָ��λ��			
					if(fromUser){						
						vv.seekTo(progress);
					}
					
				}
				//���µ�ʱ�򴥷�
				@Override
				public void onStartTrackingTouch(SeekBar arg0) {
					myHandler.removeMessages(HIDE_CONTROLER);
				}
				//�뿪��������ʱ�򴥷�
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
					myHandler.sendEmptyMessageDelayed(HIDE_CONTROLER, TIME);
				}
        	});
        //ȡ����Ļ��С
        getScreenSize();
       //���Ƽ����
        mGestureDetector = new GestureDetector(new SimpleOnGestureListener(){
        	//˫��
			@Override
			public boolean onDoubleTap(MotionEvent e) {
				//������Ƶ���Ŵ��ڴ�С�л�
				if(isFullScreen){
					setVideoScale(SCREEN_DEFAULT);
				}else{
					setVideoScale(SCREEN_FULL);
				}
				//ȫ����ʾ��־λ
				isFullScreen = !isFullScreen;	
				//��ʾ����������
				if(isControllerShow){
					showController();
				}
				return true;
			}
			//����
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				//��ʾ����������
				if(!isControllerShow){
					showController();
					hideControllerDelay();
				}else {
					//���ؿ���������
					cancelDelayHide();
					hideController();
				}
				return true;
			}
			//����
			@Override
			public void onLongPress(MotionEvent e) {
				//���š���ͣ�л�
				if(isPaused){
					vv.start();
					bn3.setImageResource(R.drawable.pause);
					cancelDelayHide();
					hideControllerDelay();
				}else{
					vv.pause();
					bn3.setImageResource(R.drawable.play);
					cancelDelayHide();
					showController();
				}
				//������ͣ��־λ
				isPaused = !isPaused;
			}	
        });
        //��ý���ļ�����ʱ����     
        vv.setOnPreparedListener(new OnPreparedListener(){
				@Override
				public void onPrepared(MediaPlayer arg0) {
					//������Ƶ���ųߴ�					
					setVideoScale(SCREEN_DEFAULT);
					//Ĭ�ϳߴ粥��
					isFullScreen = false; 
					if(isControllerShow){
						showController();  
					}
					//�����Ƶ����ʱ��
					int i = vv.getDuration();
					seekBar.setMax(i);
					i/=1000;
					int minute = i/60;
					int hour = minute/60;
					int second = i%60;
					minute %= 60;
					//��ʾ��Ƶ����ʱ��
					durationTextView.setText(String.format("%02d:%02d:%02d", hour,minute,second));					
					vv.start();  
					bn3.setImageResource(R.drawable.pause);
					hideControllerDelay();
					myHandler.sendEmptyMessage(PROGRESS_CHANGED);
				}	
	        });
        //��Ƶ������ϵ���
        vv.setOnCompletionListener(new OnCompletionListener(){       		
				@Override
				public void onCompletion(MediaPlayer arg0) {
					//ȡ�ò����б��С
					int n = playList.size();
					//ѭ������
					if(++position < n){
						vv.setVideoPath(playList.get(position).path);
					}else{
						VideoPlayerActivity.this.finish();
					}
				}
        	});
    }
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
    	if(requestCode==0&&resultCode==Activity.RESULT_OK){
    		int result = data.getIntExtra("CHOOSE", -1);
    		if(result!=-1){
    			//����ָ����Ƶ
    			vv.setVideoPath(playList.get(result).path);
    			position = result;
    		}   		
    		return ;
    	}
		super.onActivityResult(requestCode, resultCode, data);
	}
    //�ı䲥�Ž������¼�ID
	private final static int PROGRESS_CHANGED = 0;
	//���ؿ������¼�ID
    private final static int HIDE_CONTROLER = 1;
    //��Ϣ������
    Handler myHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			
			switch(msg.what){
				//�������ı�
				case PROGRESS_CHANGED:
					//�������ƶ���ָ��λ��
					int i = vv.getCurrentPosition();
					seekBar.setProgress(i);
					//��ʾ�Ѳ���ʱ��
					i/=1000;
					int minute = i/60;
					int hour = minute/60;
					int second = i%60;
					minute %= 60;
					playedTextView.setText(String.format("%02d:%02d:%02d", hour,minute,second));					
					sendEmptyMessage(PROGRESS_CHANGED);
					break;
				case HIDE_CONTROLER:
					//���ؿ�����
					hideController();
					break;
			}			
			super.handleMessage(msg);
		}	
    };
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		//��������¼�
		boolean result = mGestureDetector.onTouchEvent(event);
		return result;
	}
	//��Ӧandroid:configChanges��ָ�����¼�
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		//���»����Ļ�ߴ�
		getScreenSize();
		if(isControllerShow){
			//ȡ���ӳ�����
			cancelDelayHide();
			//���ؿ�����
			hideController();
			//��ʾ������
			showController();
			//�����ӳ����ص���Ϣ
			hideControllerDelay();
		}		
		super.onConfigurationChanged(newConfig);
	}
	@Override
	protected void onPause() {
		//���浱ǰ�Ĳ���ʱ��
		playedTime = vv.getCurrentPosition();
		//��ͣ����
		vv.pause();
		//�ı䰴ť
		bn3.setImageResource(R.drawable.play);
		super.onPause();   
	}
	@Override
	protected void onResume() {
		//�ָ���֮ǰ���ŵ�λ�ÿ�ʼ����
		vv.seekTo(playedTime);
		vv.start();  
		if(vv.getVideoHeight()!=0){
			bn3.setImageResource(R.drawable.pause);
			hideControllerDelay();
		}		
		super.onResume();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		//�رտ�����
		if(controler.isShowing()){
			controler.dismiss();
		}
		//�ر��������ƽ���
		if(mSoundWindow.isShowing()){
			mSoundWindow.dismiss();
		}
		//�Ƴ���Ϣ�����е���Ϣ
		myHandler.removeMessages(PROGRESS_CHANGED);
		myHandler.removeMessages(HIDE_CONTROLER);
		//�Ƴ������б�
		playList.clear();		
		super.onDestroy();
	}     
	//ȡ����Ļ�ĳߴ�
	private void getScreenSize()
	{
		Display display = getWindowManager().getDefaultDisplay();
        screenHeight = display.getHeight();
        screenWidth = display.getWidth();
        controlHeight = screenHeight/4;
	}
	//���ؿ��ƽ���
	private void hideController(){
		//������Ƶ������
		if(controler.isShowing()){
			controler.update(0,0,0, 0);
			isControllerShow = false;
		}
		//�����������ƽ���
		if(mSoundWindow.isShowing()){
			mSoundWindow.dismiss();
			isSoundShow = false;
		}
	}
	//�ӳٷ������ؿ��ƽ������Ϣ
	private void hideControllerDelay(){
		myHandler.sendEmptyMessageDelayed(HIDE_CONTROLER, TIME);
	}
	//��ʾ���ƽ���
	private void showController(){
		controler.update(0,0,screenWidth, controlHeight);		
		isControllerShow = true;
	}
	//�Ƴ���Ϣ�����д��������ؿ���������Ϣ
	private void cancelDelayHide(){
		myHandler.removeMessages(HIDE_CONTROLER);
	}
	//��Ļ״̬��־
    private final static int SCREEN_FULL = 0;
    private final static int SCREEN_DEFAULT = 1;
    //������ʾ����
    private void setVideoScale(int flag){   	
    	vv.getLayoutParams();   	
    	switch(flag){
    	//ȫ����ʾ
		case SCREEN_FULL:
			vv.setVideoScale(screenWidth, screenHeight);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			
			break;
		//Ĭ����ʾ	
		case SCREEN_DEFAULT:
			//��õ�ǰ���Ŵ��ڵĸ߿�
			int videoWidth = vv.getVideoWidth();
			int videoHeight = vv.getVideoHeight();
			//��õ�ǰ��Ļ�ĸ߿�
			int mWidth = screenWidth;
			int mHeight = screenHeight - 25;
			//�������
			if (videoWidth > 0 && videoHeight > 0) {
	            if ( videoWidth * mHeight  > mWidth * videoHeight ) {
	            	mHeight = mWidth * videoHeight / videoWidth;
	            } else if ( videoWidth * mHeight  < mWidth * videoHeight ) {
	            	mWidth = mHeight * videoWidth / videoHeight;
	            }
	        }
			//������Ƶ��ʾ�ĳߴ�
			vv.setVideoScale(mWidth, mHeight);

			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			
			break;
    	}
    }
    //���ݵ�ǰ������ֵ������ť��ʾ��͸����
    private int findAlphaFromSound(){
    	if(mAudioManager!=null){
    		int alpha = currentVolume * (0xCC-0x55) / maxVolume + 0x55;
    		return alpha;
    	}else{
    		return 0xCC;
    	}
    }
    //��������
    private void updateVolume(int index){
    	if(mAudioManager!=null){
    		if(isSilent){
    			//����
    			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
    		}else{
    			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
    		}
    		currentVolume = index;
    		//������������͸����
    		bn5.setAlpha(findAlphaFromSound());
    	}
    }
    //�����Ƶ�ļ��б�
    private void getVideoFile(final LinkedList<MovieInfo> list,File file){
    	//ȡ��������������Ƶ�ļ�
    	file.listFiles(new FileFilter(){
			@Override
			public boolean accept(File file) {
				// TODO Auto-generated method stub
				String name = file.getName();
				int i = name.indexOf('.');
				if(i != -1){
					name = name.substring(i);
					//���ļ�����չ��Ϊmp4����3gp
					if(name.equalsIgnoreCase(".mp4")||name.equalsIgnoreCase(".3gp")){
						//�÷�����������Ƶ�ļ���Ϣ���浽list��
						MovieInfo mi = new MovieInfo();
						//����ļ���
						mi.displayName = file.getName();
						//�����Ƶ�ļ���·��
						mi.path = file.getAbsolutePath();
						list.add(mi);
						return true;
					}
				}else if(file.isDirectory()){
					getVideoFile(list, file);
				}
				return false;
			}
    	});
    }
}