package guo.supermario.shooting;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class Bullet {
	public  boolean isgood=true;
    /**�ӵ���X���ٶ�**/
    static final int BULLET_STEP_X = 3;   
    /**�ӵ���Y���ٶ�**/
    static final int BULLET_STEP_Y = 15;     
    /**�ӵ�ͼƬ�Ŀ��**/
    static final int BULLET_WIDTH = 40;   
    /** �ӵ���XY���� **/
    public int m_posX = 0;
    public int m_posY = 0;   
    /** �ӵ��Ķ��� **/
    private Animation mAnimation = null;    
    /**�Ƿ���»����ӵ�**/
    boolean mFacus = false;   
    Context mContext = null;
    boolean isalive=true;
    
    
    
    public Bullet(Context context, Bitmap[] frameBitmap,boolean _isgood) {
		mContext = context;
		isgood=_isgood;
		mAnimation = new Animation(mContext, frameBitmap, true);
    } 
    
    /**��ʼ������**/
    public void init(int x, int y) {
		m_posX = x;
		m_posY = y;
		mFacus = true;
    }    
    /**�����ӵ�**/
    public void DrawBullet(Canvas Canvas, Paint paint) {
		if (mFacus) {
		    mAnimation.DrawAnimation(Canvas, paint, m_posX, m_posY);
		}
    }
    /**�����ӵ��������**/
    public void UpdateBullet() {
		if (mFacus && isgood) {
		    m_posY -= BULLET_STEP_Y;
		}
		else if(mFacus && !isgood){
			m_posY +=BULLET_STEP_Y; 
		}
    }   
}