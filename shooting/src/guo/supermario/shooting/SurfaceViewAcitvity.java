package guo.supermario.shooting;

import java.io.InputStream;
import java.util.Random;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;
public class SurfaceViewAcitvity extends Activity{
    AnimView mAnimView = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	// ȫ����ʾ����
	requestWindowFeature(Window.FEATURE_NO_TITLE);
	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		WindowManager.LayoutParams.FLAG_FULLSCREEN);
	// ��ȡ��Ļ���
	Display display = getWindowManager().getDefaultDisplay();
	
	// ��ʾ�Զ������ϷView
	mAnimView = new AnimView(this,display.getWidth(), display.getHeight());
	setContentView(mAnimView);
    }

    public boolean onTouchEvent(MotionEvent event) {
		// ��ô���������
		int x = (int) event.getX();
		int y = (int) event.getY();
		switch (event.getAction()) {
		// ������Ļʱ��
		case MotionEvent.ACTION_DOWN:
		    mAnimView.UpdateTouchEvent(x, y,true);
		    break;
		// �������ƶ�ʱ��
		case MotionEvent.ACTION_MOVE:
		    break;
		// ��ֹ����ʱ��
		case MotionEvent.ACTION_UP:
		    mAnimView.UpdateTouchEvent(x, y,false);
		    break;
		}
		return false;
    }	
    public class AnimView extends SurfaceView implements Callback,Runnable{          
        /**��Ļ�Ŀ��**/
	private int mScreenWidth = 0;
	private int mScreenHeight = 0;
	private int BgHeight = 0;		
	/**��Ϸ���˵�״̬**/
	private  static final int STATE_GAME = 0;
	
	/**��Ϸ״̬**/
	private int mState = STATE_GAME; 
	
	Paint mPaint = null;	
	
	/**��Ϸ������Դ ����ͼƬ�����л�����Ļ��������**/
	private Bitmap mBitMenuBG = null;
	
	/**��¼���ű���ͼƬʱʱ���µ�Y����**/
	private int mBitposY0 =0;
	private int mBitposY1 =0;	
	
	/**�ɻ�����֡��**/
	final static int PLAN_ANIM_COUNT = 6;
	
	/**�ӵ�����֡��**/
	final static int BULLET_ANIM_COUNT = 4;

	/**�ӵ����������**/
	final static int BULLET_POOL_COUNT = 15 ;
	
	/**�ɻ��ƶ�����**/
	final static int PLAN_STEP = 10;
	
	/**û��500���뷢��һ���ӵ�**/
	final static int PLAN_TIME = 500;
	
	/**�ӵ�ͼƬ����ƫ����������**/
	final static int BULLET_UP_OFFSET = 40;
	/**�ӵ�ͼƬ����ƫ����������**/
	final static int BULLET_LEFT_OFFSET = 5;
	
	/**���˶��������**/
	final static int ENEMY_POOL_COUNT = 5 ;
	
	/**�������߶���֡��**/
	final static int ENEMY_ALIVE_COUNT = 1 ;
	/**������������֡��**/
	final static int ENEMY_DEATH_COUNT = 6 ;
	
	/**���˷ɻ�ƫ����**/
	final static int ENEMY_POS_OFF = 65 ;	
	
	/**��Ϸ���߳�**/
	private Thread mThread = null;
	/**�߳�ѭ����־**/
	private boolean mIsRunning = false;
	
	private SurfaceHolder mSurfaceHolder = null;
	private Canvas mCanvas = null;
	
	private Context mContext = null;
	
	
	/**�ɻ�����**/
	public Animation mAircraft =null;
	/**�ɻ�����Ļ�е�����**/
	public int mAirPosX = 0;
	public int mAirPosY = 0;

	/**������**/
	Enemy mEnemy[] = null;		
	
	/**�ӵ���**/
	Bullet mBuilet[] = null;
	Bitmap mBitbullet[] = null;	
	
	/**��ʼ�������ӵ�ID����**/
	public int mSendId = 0;	
	
	/**��һ���ӵ������ʱ��**/
	public Long mSendTime = 0L;
	/**��ָ����Ļ����������**/
	public int mTouchPosX = 0;
	public int mTouchPosY = 0;	
	
	/**��־��ָ����Ļ������**/
	public boolean mTouching = false;
	
	/**
	 * ���췽��
	 * 
	 * @param context
	 */
	public AnimView(Context context,int screenWidth, int screenHeight) {
	    super(context);
	    mContext = context;
	    mPaint = new Paint();
	    mScreenWidth = screenWidth;
	    mScreenHeight = screenHeight;
	    /**��ȡmSurfaceHolder**/
	    mSurfaceHolder = getHolder();
	    mSurfaceHolder.addCallback(this);
	    setFocusable(true);
	    init();
	    setGameState(STATE_GAME);
	}

	private void init() {
	    /**��Ϸ����**/
	    mBitMenuBG = ReadBitMap(mContext,R.drawable.map);	   

	    /**�������Ƿɻ���������**/
	    mAircraft = new Animation(mContext,new int[] {R.drawable.plan_0,R.drawable.plan_1,R.drawable.plan_2,R.drawable.plan_3,R.drawable.plan_4,R.drawable.plan_5},true);
	    
	    /**��һ��ͼƬ����Ļ0�㣬�ڶ���ͼƬ�ڵ�һ��ͼƬ�Ϸ�**/
	    mBitposY0 = 0;
	    mBitposY1 = - mBitMenuBG.getHeight();
	    BgHeight = mBitMenuBG.getHeight();	    
	    Log.e("guojs","ScreenHeight"+mScreenHeight);
	    
	    /**��ʼ���ɻ�������**/
	    mAirPosX = 150;
	    mAirPosY = 400;
	
	    /**����������߶�����1֡**/
	    Bitmap []bitmap0 = new Bitmap[ENEMY_ALIVE_COUNT];
	    bitmap0[0] = ReadBitMap(mContext,R.drawable.enemy);
	    /**������������**/
	    Bitmap []bitmap1 = new Bitmap[ENEMY_DEATH_COUNT];
	    for(int i =0; i< ENEMY_DEATH_COUNT; i++) {
	    	bitmap1[i] = ReadBitMap(mContext,R.drawable.bomb_enemy_0 + i); 
	    }
	   
	    /**�������˶���**/
	    mEnemy = new Enemy[ENEMY_POOL_COUNT];	   
	    for(int i =0; i< ENEMY_POOL_COUNT; i++) {
			mEnemy[i] = new Enemy(mContext,bitmap0,bitmap1);
			mEnemy[i].init(i * ENEMY_POS_OFF, 0);
	    }
	    	    	    
	    /**�����ӵ������**/
	    mBuilet = new Bullet[BULLET_POOL_COUNT];
	    mBitbullet = new Bitmap[BULLET_ANIM_COUNT];
	    for(int i=0; i<BULLET_ANIM_COUNT;i++) {
	    	mBitbullet[i] = ReadBitMap(mContext,R.drawable.bullet);
	    }
	    for (int i =0; i< BULLET_POOL_COUNT;i++) {
	    	mBuilet[i] = new Bullet(mContext,mBitbullet);
	    }
	    mSendTime = System.currentTimeMillis();
	}
	//������Ϸ��״̬
	private void setGameState(int newState) {
	    mState =  newState;
	}

	/**
	 * ��ȡ������Դ��ͼƬ
	 * 
	 * @param context
	 * @param resId
	 * @return
	 */
	public Bitmap ReadBitMap(Context context, int resId) {
	    BitmapFactory.Options opt = new BitmapFactory.Options();
	    opt.inPreferredConfig = Bitmap.Config.RGB_565;
	    opt.inPurgeable = true;
	    opt.inInputShareable = true;
	    // ��ȡ��ԴͼƬ
	    InputStream is = context.getResources().openRawResource(resId);
	    return BitmapFactory.decodeStream(is, null, opt);
	} 
	protected void Draw() {
	    switch (mState) {
	    case STATE_GAME:
			renderBg();
			updateBg();
			break;
	    }
	}
	/** ������Ϸ��ͼ **/
	public void renderBg() {
	    
	    mCanvas.drawBitmap(mBitMenuBG, 0, mBitposY0, mPaint);
	    mCanvas.drawBitmap(mBitMenuBG, 0, mBitposY1, mPaint);
	    /**���Ʒɻ�����**/
	    mAircraft.DrawAnimation(mCanvas, mPaint, mAirPosX, mAirPosY);	   	    
	    
	    /**�����ӵ�����*/
	    for (int i =0; i < BULLET_POOL_COUNT; i++) {
	       mBuilet[i].DrawBullet(mCanvas, mPaint);
	    }
	    
	    /**���Ƶ��˶���**/
	    for(int i =0; i< ENEMY_POOL_COUNT; i++) {
			mEnemy[i].DrawEnemy(mCanvas, mPaint);
	    }	    	    	    
	}
	private void updateBg() {
	    /** ���³����Ĳ���**/
	    mBitposY0 += 10;
	    mBitposY1 += 10;
	    if (mBitposY0 == BgHeight) {
	    	mBitposY0 = - BgHeight;
	    }
	    if (mBitposY1 == BgHeight) {
	    	mBitposY1 = - BgHeight;
	    }

	    /** ��ָ������Ļ���·ɻ����� **/
	    if (mTouching) {

		if (mAirPosX < mTouchPosX) {
		    mAirPosX += PLAN_STEP;
		} else {
		    mAirPosX -= PLAN_STEP;
		}
		if (mAirPosY < mTouchPosY) {
		    mAirPosY += PLAN_STEP;
		} else {
		    mAirPosY -= PLAN_STEP;
		}

		if (Math.abs(mAirPosX - mTouchPosX) <= PLAN_STEP) {
		    mAirPosX = mTouchPosX;
		}
		if (Math.abs(mAirPosY - mTouchPosY) <= PLAN_STEP) {
		    mAirPosY = mTouchPosY;
		}
	    }
	    /** �����ӵ����� **/
	    for (int i = 0; i < BULLET_POOL_COUNT; i++) {
		/** �ӵ����������¸�ֵ**/
	    	mBuilet[i].UpdateBullet();
		
	    }
	    /**���Ƶ��˶���**/
	    for(int i =0; i< ENEMY_POOL_COUNT; i++) {
	    	mEnemy[i].UpdateEnemy();
		/**�л����� ���� �л�������Ļ��δ������������**/
		if(mEnemy[i].mState == Enemy.ENEMY_DEATH_STATE || mEnemy[i].m_posY >=mScreenHeight) {
		    mEnemy[i].init(UtilRandom(0,ENEMY_POOL_COUNT) *ENEMY_POS_OFF, 0);
		}
		
	    }	    
	    
	    /**����ʱ���ʼ��Ϊ������ӵ�**/
	    if (mSendId < BULLET_POOL_COUNT) {
		long now = System.currentTimeMillis();
		if (now - mSendTime >= PLAN_TIME) {
		    mBuilet[mSendId].init(mAirPosX - BULLET_LEFT_OFFSET, mAirPosY - BULLET_UP_OFFSET);
		    mSendTime = now;
		    mSendId++;
		}
	    }else {
		mSendId = 0;
	    }

	    //�����ӵ�����˵���ײ
	    Collision();
	    
	}
	/** 
	    * ����һ������� 
	    * @param botton 
	    * @param top 
	    * @return 
	    */  
	private int UtilRandom(int botton, int top) {
	    return ((Math.abs(new Random().nextInt()) % (top - botton)) + botton);  
	    }
	public void Collision() {
	    //�����ӵ��������ײ
	    for (int i = 0; i < BULLET_POOL_COUNT; i++) {
		for (int j = 0; j < ENEMY_POOL_COUNT; j++) {
		   if(mBuilet[i].m_posX >= mEnemy[j].m_posX && (mBuilet[i].m_posX <= mEnemy[j].m_posX + 20)
		    	 &&mBuilet[i].m_posY >= mEnemy[j].m_posY && (mBuilet[i].m_posY<=mEnemy[j].m_posY + 20)  
		   
		   ) {
		        mEnemy[j].mAnimState = Enemy.ENEMY_DEATH_STATE;
		   }
		}

	    }
	}	
	
	public void UpdateTouchEvent(int x, int y, boolean touching) {
	    // �������ⰴť���²��Ų�ͬ����Ч
	    switch (mState) {
	    case STATE_GAME:
		mTouching = touching;
		mTouchPosX = x;
		mTouchPosY = y;
		break;
	    }
	}
     
	@Override
	public void run() {
	    while (mIsRunning) {
		//����������̰߳�ȫ��
		synchronized (mSurfaceHolder) {
		    /**�õ���ǰ���� Ȼ������**/
		    mCanvas =mSurfaceHolder.lockCanvas();  
		    Draw();
		    /**���ƽ����������ʾ����Ļ��**/
		    mSurfaceHolder.unlockCanvasAndPost(mCanvas);
		}
		try {
		    Thread.sleep(100);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	}
	//��SurfaceView��������߿����ı�ʱ����
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2,
		int arg3) {
	    // surfaceView�Ĵ�С�����ı��ʱ��
	    
	}
	//��surfaceView������ʱ����
	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
	    /**������Ϸ���߳�**/
	    mIsRunning = true;
	    mThread = new Thread(this);
	    mThread.start();
	}
	// surfaceView����ʱ����
	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {	 
	    mIsRunning = false;
	}
    }
}