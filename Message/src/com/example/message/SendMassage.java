package com.example.message;

import java.util.ArrayList;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

public class SendMassage {
	Context context;
	String msg;
	String mobile;
	
	public SendMassage(Context context, String msg, String mobile) {
		this.context = context;
		this.msg = msg;
		this.mobile = mobile;
	}
	
	public void sendmsg(BroadcastReceiver send_sms , BroadcastReceiver delivered_sms){
		String SENT_SMS_ACTION  ="SENT_SMS_ACTION";
		String DELIVERED_SMS_ACTION ="DELIVERED_SMS_ACTION"; 
		
		Intent sentIntent =new Intent(SENT_SMS_ACTION);
		PendingIntent sentPI=PendingIntent.getBroadcast(context, 0, sentIntent,0);
		Intent deliveredIntent =new Intent(DELIVERED_SMS_ACTION);
		PendingIntent deliverPI =PendingIntent.getBroadcast(context, 0, deliveredIntent,0);
		SmsManager smsManager =SmsManager.getDefault();
		ArrayList<String> texts=smsManager.divideMessage(msg);
		for(String text:texts){
			smsManager.sendTextMessage(mobile, null, text, sentPI, deliverPI);
		}
	}
	
	

}
