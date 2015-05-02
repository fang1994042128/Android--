package guo.supermario.shooting;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class AirPlane {
	/**存活状态**/
	public static final int ENEMY_ALIVE_STATE = 0;
    /**死亡状态**/
   public static final int ENEMY_DEATH_STATE = 1;
   /**XY坐标 **/
   public int m_posX = 0;
   public int m_posY = 0;
   /** 死亡的动画 **/
   private Animation mAnimation1 = null;   
   /** 行走的动画 **/
   private Animation mAnimation0 = null;
   /**是否更新绘制**/
   boolean mFacus = false;
   /**状态**/
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
		    //当状态为死亡并且死亡动画播放完毕 不在绘制敌人
		    if(mAnimState == ENEMY_DEATH_STATE) {
				if(mAnimation1.mIsend) {
				    mFacus = false; 
				    mState = ENEMY_DEATH_STATE;
				}
		    }
		}
	}
	


}
