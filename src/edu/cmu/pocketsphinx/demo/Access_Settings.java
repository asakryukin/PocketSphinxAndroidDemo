package edu.cmu.pocketsphinx.demo;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class Access_Settings extends Activity implements OnClickListener {
	private Access_Manager access;
	private ListView list;
	private Cursor c;
	private Button btn_cancel,btn_ok;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.access_list);
		access=new Access_Manager(getApplicationContext(), Environment.getExternalStorageDirectory() + "/CHIBI/faces.txt");
		
		btn_cancel=(Button) findViewById(R.id.access_list_cancel);
		btn_ok=(Button) findViewById(R.id.access_list_ok);
		list=(ListView) findViewById(R.id.access_list_list);
		
		btn_cancel.setOnClickListener(this);
		btn_ok.setOnClickListener(this);
		
		c=access.getAll();
		
		String[] from={"name"};
		int[] to={R.id.acces_item_name};
		ChibiCursorAdapter adapter=new ChibiCursorAdapter(getApplicationContext(), R.layout.access_item, c, from, to);	
		list.setAdapter(adapter);
		
		
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.access_list_cancel:
			finish();
			break;
			
		case R.id.access_list_ok:
			change_settings();
			finish();
			break;
		}
	}
	
	private void change_settings(){
		for(int i=0; i<list.getCount();i++){
			View v=list.getChildAt(i);
			int c1=0,c2=0,c3=0;
			
			if (((CheckBox) v.findViewById(R.id.access_item_task1)).isChecked()){
				c1=1;
			}
			if (((CheckBox) v.findViewById(R.id.access_item_task2)).isChecked()){
				c2=1;
			}
			if (((CheckBox) v.findViewById(R.id.access_item_task3)).isChecked()){
				c3=1;
			}
			
			access.Update(((TextView) v.findViewById(R.id.acces_item_name)).getText().toString(), c1, c2, c3);
		}
	}

	private class ChibiCursorAdapter extends SimpleCursorAdapter{

		Cursor cur;
		public ChibiCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);
			// TODO Auto-generated constructor stub
			cur=c;
		}
		/*
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			cur.moveToPosition(position);
			
			CheckBox cb1,cb2,cb3;
			
			cb1=(CheckBox) convertView.findViewById(R.id.access_item_task1);
			cb2=(CheckBox) convertView.findViewById(R.id.access_item_task2);
			cb3=(CheckBox) convertView.findViewById(R.id.access_item_task3);
			
			if (cur.getInt(cur.getColumnIndex("task_1"))==1){
				cb1.setChecked(true);
			}else {
				cb1.setChecked(false);
			}
			
			if (cur.getInt(cur.getColumnIndex("task_2"))==1){
				cb2.setChecked(true);
			}else {
				cb2.setChecked(false);
			}
			
			if (cur.getInt(cur.getColumnIndex("task_3"))==1){
				cb3.setChecked(true);
			}else {
				cb3.setChecked(false);
			}
			
			
			return super.getView(position, convertView, parent);
			
		}*/
		
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// TODO Auto-generated method stub
			CheckBox cb1,cb2,cb3;
			cb1=(CheckBox) view.findViewById(R.id.access_item_task1);
			cb2=(CheckBox) view.findViewById(R.id.access_item_task2);
			cb3=(CheckBox) view.findViewById(R.id.access_item_task3);
			if (cursor.getInt(cursor.getColumnIndex("task_1"))==1){
				cb1.setChecked(true);
			}else {
				cb1.setChecked(false);
			}
			
			if (cursor.getInt(cursor.getColumnIndex("task_2"))==1){
				cb2.setChecked(true);
			}else {
				cb2.setChecked(false);
			}
			
			if (cursor.getInt(cursor.getColumnIndex("task_3"))==1){
				cb3.setChecked(true);
			}else {
				cb3.setChecked(false);
			}
			super.bindView(view, context, cursor);
			
			
		}
	}
	
}
