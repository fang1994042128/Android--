package com.example.message;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Replymsg extends Activity{
	
	private TextView phonnum;
	private TextView phontime;
	private TextView phonmsg;
	private EditText sendcontent;
	private Button replybt;
	private Intent intent;
	
	BroadcastReceiver send_sms;
	BroadcastReceiver delivered_sms;
	String SENT_SMS_ACTION  ="SENT_SMS_ACTION";
	String DELIVERED_SMS_ACTION ="DELIVERED_SMS_ACTION";
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reply);
		phonnum=(TextView)findViewById(R.id.renum);
		phontime=(TextView)findViewById(R.id.retime);
		phonmsg=(TextView)findViewById(R.id.recontent);
		sendcontent=(EditText)findViewById(R.id.replybody);
		replybt=(Button)findViewById(R.id.reply);
		replybt.setOnClickListener(new OnClickListener() {
			String getnum;
			String getmsg;
			
			@Override
			public void onClick(View v) {
				getnum=phonnum.getText().toString();
				getmsg=phonmsg.getText().toString();
				SendMassage sendmsg =new SendMassage(Replymsg.this, getmsg, getnum);
				sendmsg.sendmsg(send_sms, delivered_sms);
				ContentValues values =new ContentValues();
				values.put("date",System.currentTimeMillis());
				values.put("read", 0);
				values.put("type", 2);
				values.put("address", getnum);
				values.put("body", getmsg);
				getContentResolver().insert(Uri.parse("content://sms/sent"), values);
				sendcontent.setText("");
				back();			
			}
		});
		send_sms=new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				switch(getResultCode()){
				case Activity.RESULT_OK:Toast.makeText(context,"发送成功", Toast.LENGTH_SHORT).show();break;
				default :Toast.makeText(context, "信息发送失败", Toast.LENGTH_SHORT).show();
				}
			}
		};
		delivered_sms =new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				Toast.makeText(context, "短信已被接收", Toast.LENGTH_SHORT).show();
				
			}
		};	
		registerReceiver(send_sms, new IntentFilter(SENT_SMS_ACTION));
		registerReceiver(delivered_sms, new IntentFilter(DELIVERED_SMS_ACTION));
		getDate();
		
	}
	
	private void getDate(){
		intent =getIntent();
		String listnum=intent.getStringExtra("listnum");
		String listtime=intent.getStringExtra("listtime");
		String listmsg=intent.getStringExtra("listmsg");
		phonmsg.setText(listmsg);
		phonnum.setText(listnum);
		phontime.setText(listtime);
	}
	
	private  void back(){
		Intent intent =new Intent(Replymsg.this,MainActivity.class);
		Replymsg.this.startActivity(intent);
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(send_sms);
		unregisterReceiver(delivered_sms);
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			back();
		}
		return super.onKeyDown(keyCode, event);
	}
	

}
