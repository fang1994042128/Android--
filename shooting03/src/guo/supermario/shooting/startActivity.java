package guo.supermario.shooting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
public class startActivity extends Activity {
    Context mContext = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mContext = this;      
		/**¿ªÊ¼**/
        ImageButton botton0 = (ImageButton)findViewById(R.id.button0);
        botton0.setOnClickListener(new OnClickListener() {
		    @Override
		    public void onClick(View arg0) {
			 Intent intent = new Intent(mContext,SurfaceViewAcitvity.class); 
			 startActivity(intent);
		    }
        });
        
    }
}
