package com.example.stockertest;


import java.io.File;

import  com.github.mikephil.charting.listener.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent.Callback;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

public class MainActivity extends ListActivity implements OnClickListener,Callback,OnChartGestureListener{
	private EditText symbolText;
	private Button addButton;
	Context mContext;
	DataHandler mDataHandler;
	QuoteAdapter quoteAdapter;
	
	private Button cancelButton;
	//ɾ����ť
	private Button deleteButton;
	private Button  detailButton;
	//�Ի���
	private Dialog dialog = null;
	//��Ʊ��ϸ��Ϣ
	private TextView currentTextView,noTextView, openTextView, closeTextView, dayLowTextView, dayHighTextView;
	//��K��ͼ
	private ImageView chartView;
	int currentSelectedIndex;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext=this;
        getListView().setEmptyView(findViewById(R.id.empty));
        File mFile =new File("/data/data/com.example.stockertest/files/symbols.txt");
		if(mFile.exists())
		{
			Log.e("guojs","file exist");
		}else{
			try {
				//�½���Ź�Ʊ������ļ�
				FileOutputStream outputStream=openFileOutput("symbols.txt",MODE_PRIVATE);
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Log.e("guojs","file no exist");
		}
		mDataHandler=new DataHandler(mContext);
        symbolText=(EditText)findViewById(R.id.stock_symbols);
        addButton=(Button)findViewById(R.id.add_symbols_button);
        quoteAdapter = new QuoteAdapter(this,mContext,mDataHandler);
        this.setListAdapter(quoteAdapter);
        addButton.setOnClickListener(this);
    }
    
    protected void onResume(){
		super.onResume();
		if(quoteAdapter != null)
		{
			//��ʼ���½���
			quoteAdapter.startRefresh();
		}
	}
	//���治�ɼ�ʱ��ֹͣ����
	protected void onStop(){
		super.onStop();
		//ֹͣ���½���
		quoteAdapter.stopRefresh();
	}
    
    

	@Override
	public void onClick(View v) {
		if(v == addButton){
			addSymbol();
		}else if(v == cancelButton){
			//�رնԻ���
			dialog.dismiss();
		} else if(v== deleteButton){
			//ɾ����ǰ��Ʊ
			quoteAdapter.removeQuoteAtIndex(currentSelectedIndex);			
			dialog.dismiss();
		}else if(v == detailButton){
			dialog.dismiss();
			Intent i = new Intent(this, LineChartActivity1.class);
	        startActivity(i);
		}else if(v.getParent() instanceof RelativeLayout){
			RelativeLayout rl = (RelativeLayout)v.getParent();
			this.onListItemClick(getListView(), rl, rl.getId()-33, rl.getId());
		} else if(v instanceof RelativeLayout){
			this.onListItemClick(getListView(), v, v.getId()-33, v.getId());
		}
		
		
	}
	
	private void addSymbol() {
		String symbolsStr = symbolText.getText().toString();
		symbolText.setText("");
		quoteAdapter.addSymbolsToFile(symbolsStr);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		StockInfo quote = (StockInfo)quoteAdapter.getItem(position);
		//ȡ�õ�ǰλ�õ����
		currentSelectedIndex=position;
		if(dialog == null){
			dialog = new Dialog(mContext);
			dialog.setContentView(R.layout.quote_detail);
			//ɾ����ť
			deleteButton = (Button) dialog.findViewById(R.id.delete);
			//����ɾ����ť������
			deleteButton.setOnClickListener(this);
			//����ȫͼ��ť
			detailButton =(Button)dialog.findViewById(R.id.detail);
			//����ȫͼ��ť������
			detailButton.setOnClickListener(this);
			//���������水ť
			cancelButton = (Button) dialog.findViewById(R.id.close);
			//���÷��ذ�ť������
			cancelButton.setOnClickListener(this);
			//��ǰ��Ʊ�۸�
			currentTextView = (TextView) dialog.findViewById(R.id.current);
			//��ǰ��Ʊ����
			noTextView = (TextView) dialog.findViewById(R.id.no);
			//�������̼�
			openTextView = (TextView) dialog.findViewById(R.id.opening_price);
			//�������̼�
			closeTextView = (TextView) dialog.findViewById(R.id.closing_price);
			//������ͼ�
			dayLowTextView = (TextView) dialog.findViewById(R.id.day_low);
			//������߼�
			dayHighTextView = (TextView) dialog.findViewById(R.id.day_high);
			//��ƱK��ͼ
			chartView = (ImageView)dialog.findViewById(R.id.chart_view);
		}
		//���öԻ������
		dialog.setTitle(quote.getName());
		//���ù�Ʊ��ǰ�۸�
		double current=Double.parseDouble(quote.getCurrent_price());
		double closing_price=Double.parseDouble(quote.getClosing_price());
		//������λС��
		DecimalFormat df=new DecimalFormat("#0.00"); 
		String percent=df.format(((current-closing_price)*100/closing_price))+"%";
		//����Ʊ�۸񳬹��������̼�
		if(current > closing_price)
		{
			//����������ɫΪ��ɫ
			currentTextView.setTextColor(0xffEE3B3B);			
		}
		else 
		{
			//����������ɫΪ��ɫ
			currentTextView.setTextColor(0xff2e8b57);
		}
		mDataHandler.getSymbol(quote.no);
System.out.println(quote.no);
		//����TextView����
		currentTextView.setText(df.format(current)+"  ("+percent+")");
		openTextView.setText(quote.opening_price);
		closeTextView.setText(quote.closing_price);
		dayLowTextView.setText(quote.min_price);
		dayHighTextView.setText(quote.max_price);
		noTextView.setText(quote.no);
		//����K��ͼ
		chartView.setImageBitmap(mDataHandler.bitmap);
		dialog.show();
	}

	public class QuoteAdapter extends BaseAdapter implements  ListAdapter,Runnable{
		private static final int DISPLAY_COUNT = 10;
		public DataHandler dataHandler;
		//ǿ�Ƹ��±�־
		private boolean forceUpdate = false;
		//����������
		Context context;
		//����Activityʵ��
		MainActivity stocker;
		LayoutInflater inflater;

		QuoteRefreshTask quoteRefreshTask = null;
		int progressInterval;
		//��Ϣ������
		Handler messageHandler = new Handler();


		public QuoteAdapter(MainActivity aController, Context mContext,DataHandler mdataHandler) {
			//���浱ǰ�������ĺ�Activityʵ��
			context = mContext;
			stocker = aController;
			dataHandler = mdataHandler;
		}
		
		public void addSymbolsToFile(String symbol){
			forceUpdate=true;
			dataHandler.addSymbolToFile(symbol);
			messageHandler.post(this);
			Toast.makeText(mContext,"���ӹ�Ʊ�ɹ�",Toast.LENGTH_LONG).show();
		}
		@Override
		public int getCount() {
			return dataHandler.stocksSize();
		}

		@Override
		public Object getItem(int arg0) {
			return dataHandler.getQuoteForIndex(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(mDataHandler.stocksSize()==0){
				inflater = LayoutInflater.from(context);
				LinearLayout emptyInflater=(LinearLayout)inflater.inflate(R.layout.empty,null);
				emptyInflater.setMinimumWidth(parent.getWidth());
				TextView field = (TextView)emptyInflater.findViewById(R.id.listViewEmpty);
				field.setText("û������κι�Ʊ");
				return emptyInflater;	
			}
			StockInfo quote;
			inflater = LayoutInflater.from(context);
			RelativeLayout cellLayout = (RelativeLayout)inflater.inflate(R.layout.quote_cell, null);
			cellLayout.setMinimumWidth(parent.getWidth());
			int color;
			stocker.setProgress(progressInterval*(position + 1));
			if(position % 2 > 0)
				color = Color.rgb(48,92,131);
			else 
				color = Color.rgb(119,138,170);
			cellLayout.setBackgroundColor(color);
			quote = dataHandler.getQuoteForIndex(position);
			TextView field = (TextView)cellLayout.findViewById(R.id.symbol);
			//���ù�Ʊ�Ĵ���
			field.setText(quote.getNo());
			field.setClickable(true);
			field.setOnClickListener(stocker);

			//��Ʊ����
			field = (TextView)cellLayout.findViewById(R.id.name);
			field.setClickable(true);
			field.setOnClickListener(stocker);
			field.setText(quote.getName());
			
			field = (TextView)cellLayout.findViewById(R.id.current);
			//���ù�Ʊ��ǰ�۸�
			double current=Double.parseDouble(quote.getCurrent_price());
			double closing_price=Double.parseDouble(quote.getClosing_price());
			//������λС��
			DecimalFormat df=new DecimalFormat("#0.00"); 
			String percent=df.format(((current-closing_price)*100/closing_price))+"%";
			field.setText(df.format(current));
			field.setClickable(true);
			field.setOnClickListener(stocker);		
			
			field = (TextView)cellLayout.findViewById(R.id.percent);
			//����Ʊ�۸񳬹��������̼�
			if(current > closing_price)
			{
				//����������ɫΪ��ɫ
				field.setTextColor(0xffEE3B3B);			
			}
			else 
			{
				//����������ɫΪ��ɫ
				field.setTextColor(0xff2e8b57);
			}
			field.setText(percent);
			cellLayout.setId(position + 33);
			cellLayout.setClickable(true);
			cellLayout.setOnClickListener(stocker);
			return cellLayout;
		}

		@Override
		public void run() {
			if(mDataHandler.stocksSize() >=0){
				if(forceUpdate ){
					forceUpdate = false;
					progressInterval = 10000/DISPLAY_COUNT;
					stocker.setProgressBarVisibility(true);
					stocker.setProgress(progressInterval);
				}
				//֪ͨ���ݸ���
				this.notifyDataSetChanged();
			}
			
		}
		public void removeQuoteAtIndex(int index){
			forceUpdate = true;
			mDataHandler.removeQuoteByIndex(index);
			messageHandler.post(this);
			Toast.makeText(mContext,"ɾ����Ʊ�ɹ�",Toast.LENGTH_LONG).show();
			try {
				Thread.sleep(300);
				mDataHandler.refreshStocks();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		public void stopRefresh(){
			quoteRefreshTask.cancelTimer();
			quoteRefreshTask = null;
		}
		//��ʼ���¹�Ʊ
		public void startRefresh(){
			if(quoteRefreshTask == null)
				quoteRefreshTask = new QuoteRefreshTask(this);
		}
		public class QuoteRefreshTask extends TimerTask {
			QuoteAdapter quoteAdaptor;
			Timer        refreshTimer;
			final static int  TENSECONDS = 10000;
			public QuoteRefreshTask(QuoteAdapter anAdaptor){
				refreshTimer = new Timer("Quote Refresh Timer");
				refreshTimer.schedule(this, TENSECONDS, TENSECONDS);
				quoteAdaptor = anAdaptor;
			}

			public void run(){
				messageHandler.post(quoteAdaptor);
			}

			public void startTimer(){
				if(refreshTimer == null){
					refreshTimer = new Timer("Quote Refresh Timer");
					refreshTimer.schedule(this, TENSECONDS, TENSECONDS);
				}
			}
			//ȡ����ʱ��
			public void cancelTimer(){
				this.cancel();
				refreshTimer.cancel();
				refreshTimer = null;
			}
		}
    	
    }

	@Override
	public void onChartLongPressed(MotionEvent me) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onChartDoubleTapped(MotionEvent me) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onChartSingleTapped(MotionEvent me) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		
	}

    
}
