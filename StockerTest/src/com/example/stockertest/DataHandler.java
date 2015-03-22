package com.example.stockertest;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.widget.Toast;

public class DataHandler implements Runnable{
	private static final String TAG="DataHandler";
	private static final String QUERY_URL="http://hq.sinajs.cn/list=";
	private static final String SYMBOL_FILE_NAME="symbols.txt";
	private int BUF_SIZE=16384;
	private ArrayList<String> stocks=new ArrayList<String>();
	private ArrayList<StockInfo> stockInfo=new ArrayList<StockInfo>();
	ArrayList<String> newStocks ;
	private static final String QUERY_IMG ="http://image.sinajs.cn/newchart/daily/n/";
	
	//各个股票信息在数组中的索引
		private final int NAME=0;
		private final int OPENING_PRICE=1;
		private final int CLOSING_PRICE=2;
		private final int CURRENT_PRICE=3;
		private final int MAX_PRICE=4;
		private final int MIN_PRICE=5;
		Context context;
		
		public  DataHandler(Context mContext){
			context=mContext;
		}
		
		public void addSymbolToFile(String symbol) {
			for(int i=0;i<stocks.size();i++){
				if(symbol.equals(stocks.get(i))){
					return ;
				}
			}
			newStocks = new ArrayList<String>();
			newStocks.add(symbol);
			if((newStocks.size()>0) && (newStocks !=null)){
				(new Thread(this)).start();
			}
			
		}
		
		protected void __addQuotes(ArrayList<String> stockSymbols){
			if(stockSymbols != null && stockSymbols.size() > 0){
				int index, count = stockSymbols.size();
				//取得http客户端实例
				HttpClient req = new DefaultHttpClient();
				//用于存放网址
				StringBuffer buf = new StringBuffer();
				//构建网址
				buf.append(QUERY_URL);
				buf.append(stockSymbols.get(0));
				//如果股票数超过1
				for(index = 1; index < count; index++){
					buf.append(",");	
					buf.append(stockSymbols.get(index));
				}
				try {
					//采用get方式获取网页数据
					HttpGet httpGet = new HttpGet(buf.toString());
					HttpResponse response = req.execute(httpGet);
					InputStream iStream = response.getEntity().getContent();
					//解析网页数据，若解析成功则添加股票
					if(parseQuotesFromStream(iStream))
					{
						for(index = 0; index < count; index++){
							stocks.add(stockSymbols.get(index));
						}
						savePortfolio();
					}

				} catch (IOException e){
					Log.e(TAG, e.getMessage());
				}
			}
		}
		
		public Bitmap getChartForSymbol(String symbol){
			try {	
				try {
					//构建股票K线图网址
					StringBuilder sb = new StringBuilder(QUERY_IMG);
					sb = sb.append(symbol+".gif");
					HttpClient req = new DefaultHttpClient();
					HttpGet httpGet = new HttpGet(sb.toString());
					//访问执行网址
					HttpResponse response = req.execute(httpGet);
					InputStream iStream;
					BitmapDrawable bitMap;
					//取得网址返回的内容
					iStream = response.getEntity().getContent();
					//将返回的内容解析成图片
					bitMap = new BitmapDrawable(iStream);
					iStream.close();
					iStream = null;
					return bitMap.getBitmap();
				} catch ( IOException iox ){
					Log.d(TAG, iox.getMessage());	
				}
			} catch (Exception e) {
				Log.d(TAG, e.getMessage());
			}
			return null;
		}
		
		private boolean parseQuotesFromStream(InputStream aStream){
			boolean flag=false;
			if(aStream != null){
				//读取数据
				BufferedInputStream iStream = new BufferedInputStream(aStream);
				InputStreamReader iReader=null;
				try {
					//使用GBK方式解析数据
					iReader = new InputStreamReader(iStream,"GBK");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				StringBuffer strBuf = new StringBuffer();
				char buf[] = new char[BUF_SIZE];
				try {
					int charsRead;
					//将数据读取到StringBuffer中
					while((charsRead = iReader.read(buf, 0, buf.length)) != -1){
						strBuf.append(buf, 0, charsRead);
					}
					//匹配股票数据
					Pattern pattern=Pattern.compile("str_(.+)=\"(.+)\"");
					Matcher matcher=pattern.matcher(strBuf);
					while(matcher.find()){      
						//股票信息为第二个括号中对应的内容
			    		String result=matcher.group(2);
			    		String[] data=result.split(",");
			    		StockInfo mStockInfo=new StockInfo();
			    		//存储股票的代码
			    		mStockInfo.setNo(matcher.group(1));
			    		//存储股票名字
			    		mStockInfo.setName(data[NAME]);
			    		//存储股票今日开盘价
			    		mStockInfo.setOpening_price(data[OPENING_PRICE]);
			    		//存储股票昨日收盘价
			    		mStockInfo.setClosing_price(data[CLOSING_PRICE]);
			    		//存储股票当前价格
			    		mStockInfo.setCurrent_price(Double.parseDouble(data[CURRENT_PRICE])+0.01*(int)(10*Math.random())+"");
			    		//存储股票今日最高价格
			    		mStockInfo.setMax_price(data[MAX_PRICE]);
			    		//存储股票今日最低价格
			    		mStockInfo.setMin_price(data[MIN_PRICE]);		
			    		//将数据存储到数组中
			    		stockInfo.add(mStockInfo);
System.out.println(mStockInfo.getName());
			    		flag=true;
			    		break;
			    	}
				}catch(IOException iox){
					Log.e(TAG, iox.getMessage());
				}
			}
			return flag;
		}

		private void savePortfolio() {
			if(stocks.size() > 0){
				FileOutputStream outStream = null;
				OutputStreamWriter oWriter;
				try {
					//打开文件
					outStream = context.openFileOutput(SYMBOL_FILE_NAME, Context.MODE_PRIVATE);
					oWriter = new OutputStreamWriter(outStream); 
					StringBuffer buf = new StringBuffer();
					int index, count = stocks.size();
					//构建字符串写入文件中
					buf.append(stocks.get(0));
					for(index = 1; index < count; index++){
						buf.append(",");
						buf.append(stocks.get(index));
					}
					String outStr = buf.toString();
					oWriter.write(outStr, 0, outStr.length());
					oWriter.close();
					outStream.close();
				} catch(IOException e){
					Log.e(TAG, e.getMessage());
				}
			}
			
		}
		
		public  StockInfo getQuoteForIndex(int index){
			return stockInfo.get(index);
		}
		
		public  int stocksSize(){
			if(stocks != null)
				return stocks.size();
			return 0;
		}

		@Override
		public void run() {
			__addQuotes(newStocks);
			
		}
		
		public  void removeQuoteByIndex(int index){
			stocks.remove(index);
			//保存当前股票
			savePortfolio();
		}
		

}
