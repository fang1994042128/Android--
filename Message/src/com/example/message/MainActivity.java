package com.example.message;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.support.v4.app.NavUtils;

public class MainActivity extends Activity {
	private Button sendnmsg;
	
	private ListView msglist;
	
	final String[] string =new String[]{"imag","listnum","listmsg","listtime","listtype"};
	final int[] ID =new int[]{R.id.imag,R.id.listnum,R.id.listmsg,R.id.listtime,R.id.type};
	
	int iv =R.drawable.people;
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        sendnmsg=(Button)findViewById(R.id.newsms);
        msglist=(ListView)findViewById(R.id.msg_list);
System.out.println("运行了1");
        
        sendnmsg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent =new Intent(MainActivity.this,SendSmsActivity.class);
				MainActivity.this.startActivity(intent);
				MainActivity.this.finish();
				
			}
		});
        SimpleAdapter adapter =new SimpleAdapter(MainActivity.this,getSmsInPhone(),R.layout.listview,string,ID);
        adapter.notifyDataSetChanged();
        msglist.setAdapter(adapter);
        msglist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent intent =new Intent(MainActivity.this,Replymsg.class);
				Map< String, Object> getlist =getSmsInPhone().get(arg2);
				intent.putExtra("listnum",(String)getlist.get("listnum"));
				intent.putExtra("listtime", (String)getlist.get("listtime"));
				intent.putExtra("listmsg", (String)getlist.get("listmsg"));
				intent.putExtra("listtype",(String)getlist.get("listtype"));
				MainActivity.this.startActivity(intent);
				MainActivity.this.finish();
			}
		});
        
        
    }
    
    public List<Map<String, Object>>  getSmsInPhone(){
    	final String SMS_URI_ALL ="content://sms/";
    	List<Map<String, Object>> contents = new ArrayList<Map<String, Object>>();
    	try{
    		Uri uri =Uri.parse(SMS_URI_ALL);
    		String[] projection =new String[]{"_id","address","person","body","date","type"};
    		Cursor cur =getContentResolver().query(uri, projection, null, null, "date desc");
System.out.println("运行了2");
    		while(cur.moveToNext()){
System.out.println("运行了3");
    			Map<String, Object>map =new HashMap<String, Object>();
    			int index_Address=cur.getColumnIndex("address");
System.out.println(index_Address);
    			int index_Person_=cur.getColumnIndex("person");
    			int index_Body =cur.getColumnIndex("body");
    			int index_Date =cur.getColumnIndex("date");
    			int index_Type =cur.getColumnIndex("type");
    			String strAddress =cur.getString(index_Address);
System.out.println(strAddress);
    			cur.getInt(index_Person_);
    			String strbody =cur.getString(index_Body);
    			long longDate =cur.getLong(index_Date);
    			int type =cur.getInt(index_Type);
    			SimpleDateFormat dateFormat =new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    			Date d =new Date(longDate);
    			String strDate=dateFormat.format(d);
    			map.put("imag", iv);
    			map.put("listnum",strAddress);
    			map.put("listmsg", strbody);
    			map.put("listtime", strDate);
    			if(type==1){
    				map.put("listtype", "收");
    			}else{
    				map.put("listtype", "发");
    			}
    			contents.add(map);
    		}
    		if(!cur.isClosed()){
    			cur.close();
    			cur=null;
    		}
    	}catch (SQLiteException e) {
    		Log.d("SQLiteException in getSmsInPhone", e.getMessage());
		}
    	return contents;	
        }

 

    
}
