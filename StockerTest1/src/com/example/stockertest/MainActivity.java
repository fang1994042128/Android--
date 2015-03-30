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
	//删除按钮
	private Button deleteButton;
	private Button  detailButton;
	//对话框
	private Dialog dialog = null;
	//股票详细信息
	private TextView currentTextView,noTextView, openTextView, closeTextView, dayLowTextView, dayHighTextView;
	//日K线图
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
				//新建存放股票代码的文件
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
			//开始更新界面
			quoteAdapter.startRefresh();
		}
	}
	//界面不可见时，停止更新
	protected void onStop(){
		super.onStop();
		//停止更新界面
		quoteAdapter.stopRefresh();
	}
    
    

	@Override
	public void onClick(View v) {
		if(v == addButton){
			addSymbol();
		}else if(v == cancelButton){
			//关闭对话框
			dialog.dismiss();
		} else if(v== deleteButton){
			//删除当前股票
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
		//取得当前位置的序号
		currentSelectedIndex=position;
		if(dialog == null){
			dialog = new Dialog(mContext);
			dialog.setContentView(R.layout.quote_detail);
			//删除按钮
			deleteButton = (Button) dialog.findViewById(R.id.delete);
			//设置删除按钮监听器
			deleteButton.setOnClickListener(this);
			//设置全图按钮
			detailButton =(Button)dialog.findViewById(R.id.detail);
			//设置全图按钮监听器
			detailButton.setOnClickListener(this);
			//返回主界面按钮
			cancelButton = (Button) dialog.findViewById(R.id.close);
			//设置返回按钮监听器
			cancelButton.setOnClickListener(this);
			//当前股票价格
			currentTextView = (TextView) dialog.findViewById(R.id.current);
			//当前股票编码
			noTextView = (TextView) dialog.findViewById(R.id.no);
			//昨日收盘价
			openTextView = (TextView) dialog.findViewById(R.id.opening_price);
			//今日收盘价
			closeTextView = (TextView) dialog.findViewById(R.id.closing_price);
			//今日最低价
			dayLowTextView = (TextView) dialog.findViewById(R.id.day_low);
			//今日最高价
			dayHighTextView = (TextView) dialog.findViewById(R.id.day_high);
			//股票K线图
			chartView = (ImageView)dialog.findViewById(R.id.chart_view);
		}
		//设置对话框标题
		dialog.setTitle(quote.getName());
		//设置股票当前价格
		double current=Double.parseDouble(quote.getCurrent_price());
		double closing_price=Double.parseDouble(quote.getClosing_price());
		//保留两位小数
		DecimalFormat df=new DecimalFormat("#0.00"); 
		String percent=df.format(((current-closing_price)*100/closing_price))+"%";
		//若股票价格超过昨日收盘价
		if(current > closing_price)
		{
			//设置字体颜色为红色
			currentTextView.setTextColor(0xffEE3B3B);			
		}
		else 
		{
			//设置字体颜色为绿色
			currentTextView.setTextColor(0xff2e8b57);
		}
		mDataHandler.getSymbol(quote.no);
System.out.println(quote.no);
		//设置TextView内容
		currentTextView.setText(df.format(current)+"  ("+percent+")");
		openTextView.setText(quote.opening_price);
		closeTextView.setText(quote.closing_price);
		dayLowTextView.setText(quote.min_price);
		dayHighTextView.setText(quote.max_price);
		noTextView.setText(quote.no);
		//设置K线图
		chartView.setImageBitmap(mDataHandler.bitmap);
		dialog.show();
	}

	public class QuoteAdapter extends BaseAdapter implements  ListAdapter,Runnable{
		private static final int DISPLAY_COUNT = 10;
		public DataHandler dataHandler;
		//强制更新标志
		private boolean forceUpdate = false;
		//保存上下文
		Context context;
		//保存Activity实例
		MainActivity stocker;
		LayoutInflater inflater;

		QuoteRefreshTask quoteRefreshTask = null;
		int progressInterval;
		//消息处理器
		Handler messageHandler = new Handler();


		public QuoteAdapter(MainActivity aController, Context mContext,DataHandler mdataHandler) {
			//保存当前的上下文和Activity实例
			context = mContext;
			stocker = aController;
			dataHandler = mdataHandler;
		}
		
		public void addSymbolsToFile(String symbol){
			forceUpdate=true;
			dataHandler.addSymbolToFile(symbol);
			messageHandler.post(this);
			Toast.makeText(mContext,"增加股票成功",Toast.LENGTH_LONG).show();
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
				field.setText("没有添加任何股票");
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
			//设置股票的代码
			field.setText(quote.getNo());
			field.setClickable(true);
			field.setOnClickListener(stocker);

			//股票名字
			field = (TextView)cellLayout.findViewById(R.id.name);
			field.setClickable(true);
			field.setOnClickListener(stocker);
			field.setText(quote.getName());
			
			field = (TextView)cellLayout.findViewById(R.id.current);
			//设置股票当前价格
			double current=Double.parseDouble(quote.getCurrent_price());
			double closing_price=Double.parseDouble(quote.getClosing_price());
			//保留两位小数
			DecimalFormat df=new DecimalFormat("#0.00"); 
			String percent=df.format(((current-closing_price)*100/closing_price))+"%";
			field.setText(df.format(current));
			field.setClickable(true);
			field.setOnClickListener(stocker);		
			
			field = (TextView)cellLayout.findViewById(R.id.percent);
			//若股票价格超过昨日收盘价
			if(current > closing_price)
			{
				//设置字体颜色为红色
				field.setTextColor(0xffEE3B3B);			
			}
			else 
			{
				//设置字体颜色为绿色
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
				//通知数据更改
				this.notifyDataSetChanged();
			}
			
		}
		public void removeQuoteAtIndex(int index){
			forceUpdate = true;
			mDataHandler.removeQuoteByIndex(index);
			messageHandler.post(this);
			Toast.makeText(mContext,"删除股票成功",Toast.LENGTH_LONG).show();
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
		//开始更新股票
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
			//取消定时器
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
