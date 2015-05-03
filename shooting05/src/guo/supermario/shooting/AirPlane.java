package guo.supermario.shooting;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class AirPlane {
	/**���״̬**/
	public static final int ENEMY_ALIVE_STATE = 0;
    /**����״̬**/
   public static final int ENEMY_DEATH_STATE = 1;
   /**XY���� **/
   public int m_posX = 0;
   public int m_posY = 0;
   /** �����Ķ��� **/
   private Animation mAnimation1 = null;   
   /** ���ߵĶ��� **/
   private Animation mAnimation0 = null;
   /**�Ƿ���»���**/
   boolean mFacus = false;
   /**״̬**/
   int mState =0;
   Context mContext = null;
   public int mAnimState = 0;
   
   
   public AirPlane(Context context, int[] frameBitmap,Bitmap[] deadBitmap) {
		mContext = context;
		mAnimation0 = new Animation(mContext, frameBitmap, true);
		mAnimation1 = new Animation(mContext, deadBitmap, false);
    }    
   
   
   public void init(int x,int y){
	   m_posX = x;
	   m_posY = y;
	   mFacus = true;
	   mAnimState = ENEMY_ALIVE_STATE;
	   mState = ENEMY_ALIVE_STATE;
	   mAnimation0.reset();
	   mAnimation1.reset();
	   
   }

	public void DrawAirPlane(Canvas Canvas, Paint paint) {
		if (mFacus) {
		    if(mAnimState == ENEMY_ALIVE_STATE) {
			 mAnimation0.DrawAnimation(Canvas, paint, m_posX, m_posY);
		    }else if(mAnimState == ENEMY_DEATH_STATE) {
			mAnimation1.DrawAnimation(Canvas, paint, m_posX, m_posY);
		    }
		   
		}
	
	}
	
	public void Update() {
		if (mFacus) {
		    //��״̬Ϊ����������������������� ���ڻ��Ƶ���
		    if(mAnimState == ENEMY_DEATH_STATE) {
				if(mAnimation1.mIsend) {
				    mFacus = false; 
				    mState = ENEMY_DEATH_STATE;
				}
		    }
		}
	}
	


}
