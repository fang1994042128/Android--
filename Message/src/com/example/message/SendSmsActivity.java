package com.example.message;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SendSmsActivity extends Activity {
	EditText ednum;
	EditText edbody;
	Button send;
	BroadcastReceiver send_sms;
	BroadcastReceiver delivered_sms;
	
	String SENT_SMS_ACTION  ="SENT_SMS_ACTION";
	String DELIVERED_SMS_ACTION ="DELIVERED_SMS_ACTION";
	String getnum;
	String getmsg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.write);
		
		ednum=(EditText)findViewById(R.id.ednum);
		edbody=(EditText)findViewById(R.id.edbody);
		send=(Button)findViewById(R.id.send);
		send.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getnum=ednum.getText().toString();
				getmsg=edbody.getText().toString();
				SendMassage sendmsg =new SendMassage(SendSmsActivity.this, getmsg, getnum);
				sendmsg.sendmsg(send_sms, delivered_sms);
				ContentValues values =new ContentValues();
				values.put("date",System.currentTimeMillis());
				values.put("read", 0);
				values.put("type", 2);
				values.put("address", getnum);
				values.put("body", getmsg);
				getContentResolver().insert(Uri.parse("content://sms/sent"), values);
				edbody.setText("");
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
	}
	
	private  void back(){
		Intent intent =new Intent(SendSmsActivity.this,MainActivity.class);
		SendSmsActivity.this.startActivity(intent);
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(send_sms);
		unregisterReceiver(delivered_sms);
		super.onDestroy();
	}
	
	
	
	

}
