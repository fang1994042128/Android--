package com.guo.videoplayer;

import java.util.LinkedList;
import com.guo.videoplayer.VideoPlayerActivity.MovieInfo;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
//��Ƶѡ�����
public class VideoChooseActivity extends Activity{
	//��Ƶ�����б�
	private LinkedList<MovieInfo> mLinkedList;
	private LayoutInflater mInflater;
	View root;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog);
		//ȡ����Ƶ�����б�
		mLinkedList = VideoPlayerActivity.playList;
		mInflater = getLayoutInflater();
		//���ذ���
		ImageButton iButton = (ImageButton) findViewById(R.id.cancel);
		iButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				//�رյ�ǰ����
				VideoChooseActivity.this.finish();
			}			
		});
		//ȡ�ò����б����
		ListView myListView = (ListView) findViewById(R.id.list);
		//����������
		myListView.setAdapter(new BaseAdapter(){
			//ȡ������
			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return mLinkedList.size();
			}
			//ȡ��Ԫ��
			@Override
			public Object getItem(int arg0) {
				// TODO Auto-generated method stub
				return arg0;
			}
			//ȡ��Ԫ��id
			@Override
			public long getItemId(int arg0) {
				// TODO Auto-generated method stub
				return arg0;
			}
			//ȡ����ͼ
			@Override
			public View getView(int arg0, View convertView, ViewGroup arg2) {
				// TODO Auto-generated method stub
				if(convertView==null){
					convertView = mInflater.inflate(R.layout.list, null);
				}
				//��ʾ��Ƶ�ļ�����
				TextView text = (TextView) convertView.findViewById(R.id.text);
				text.setText(mLinkedList.get(arg0).displayName);				
				return convertView;   
			}			
		});
		//���ð���������
		myListView.setOnItemClickListener(new OnItemClickListener(){
			//����������
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				//����intent�������棬��֪�����ŵ���Ƶ�ļ���Ϣ
				Intent intent = new Intent();
				intent.putExtra("CHOOSE", arg2);
				VideoChooseActivity.this.setResult(Activity.RESULT_OK, intent);
				VideoChooseActivity.this.finish();
				}
		});
	}
}