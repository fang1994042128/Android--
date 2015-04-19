package guo.supermario.shooting;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class Enemy {
    /**���˴��״̬**/
   public static final int ENEMY_ALIVE_STATE = 0;
    /**��������״̬**/
   public static final int ENEMY_DEATH_STATE = 1;  
    /**�������ߵ�Y���ٶ�**/
    static final int ENEMY_STEP_Y = 5;  
    /**�ӵ�ͼƬ�Ŀ��**/
    static final int BULLET_WIDTH = 40;    
    /** �ӵ���XY���� **/
    public int m_posX = 0;
    public int m_posY = 0; 
    /** �������ߵĶ��� **/
    private Animation mAnimation0 = null;
    /** ���������Ķ��� **/
    private Animation mAnimation1 = null;   
    /**���Ŷ���״̬**/
    public int mAnimState = 0;
    
    /**�Ƿ���»��Ƶ���**/
    boolean mFacus = false; 
    /**����״̬**/
    int mState =0;
    Context mContext = null;
    public Enemy(Context context, Bitmap[] frameBitmap,Bitmap[] deadBitmap) {
	mContext = context;
	mAnimation0 = new Animation(mContext, frameBitmap, true);
	mAnimation1 = new Animation(mContext, deadBitmap, false);
    }    
    /**��ʼ������**/
    public void init(int x, int y) {
	m_posX = x;
	m_posY = y;
	mFacus = true;
	mAnimState = ENEMY_ALIVE_STATE;
	mState = ENEMY_ALIVE_STATE;
	mAnimation0.reset();
	mAnimation1.reset();
    }   
    /**���Ƶ��˶���**/
    public void DrawEnemy(Canvas Canvas, Paint paint) {
	if (mFacus) {
	    if(mAnimState == ENEMY_ALIVE_STATE) {
		 mAnimation0.DrawAnimation(Canvas, paint, m_posX, m_posY);
	    }else if(mAnimState == ENEMY_DEATH_STATE) {
		mAnimation1.DrawAnimation(Canvas, paint, m_posX, m_posY);
	    }
	   
	}
    }
    /**���µ���״̬**/
    public void UpdateEnemy() {
	if (mFacus) {
	    m_posY += ENEMY_STEP_Y;
	    //������״̬Ϊ����������������������� ���ڻ��Ƶ���
	    if(mAnimState == ENEMY_DEATH_STATE) {
			if(mAnimation1.mIsend) {
			    mFacus = false; 
			    mState = ENEMY_DEATH_STATE;
			}
	    }
	}
    }
}