package edu.cmu.pocketsphinx.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Access_Manager {
	
	private SQLiteDatabase myDB= null;
	private Cursor c;
	private Context context;
	
	
	public Access_Manager(Context c, String mPath) {
		// TODO Auto-generated constructor stub
		context=c;
		Log.d("mylog", "start");
		myDB = c.openOrCreateDatabase("Chibi", 0, null);
		myDB.execSQL("create table if not exists Access ("
		          + "_id integer primary key,"
		          + "name text unique,"
		          + "task_1 integer default 0,"
		          + "task_2 integer default 0,"
		          + "task_3 integer default 0"
		          + ");");
		Log.d("mylog", "databases");
		try
	    {
	        //InputStream instream = context. (mPath); 
			FileInputStream instream = new FileInputStream (new File(mPath));
	        if (instream != null)
	        {
	        	Log.d("mylog", "istream not null");
	            InputStreamReader inputreader = new InputStreamReader(instream); 
	            BufferedReader buffreader = new BufferedReader(inputreader); 
	            String line,line1 = "";
	            try
	            {
	                while ((line = buffreader.readLine()) != null)
	                    {
	                	Log.d("mylog", "line:"+line);
	                	int ic;
	                	String name="";
	                	ic=line.indexOf(",");
	                	name=line.substring(0, ic);
	                	try{
	                	myDB.execSQL("INSERT INTO Access (name) VALUES ('"+name+"')");
	                	Log.d("mylog", name);
	                	}
	                	catch(SQLException e){
	                		Log.d("mylog", e.getMessage());
	                	}
	                	//Toast.makeText(context, name, Toast.LENGTH_LONG).show();
	                    }
	            }catch (Exception e) 
	            {
	            	Log.d("mylog", e.toString());
	                e.printStackTrace();
	            }
	         }
	        instream.close();
	    }
	    catch (Exception e) 
	    {
	    	Log.d("mylog", e.getMessage());
	        String error="";
	        error=e.getMessage();
	        
	    }
		
		
	}
	
	public Cursor getAll(){
		
		c=myDB.rawQuery("SELECT * FROM Access",null);
		
		return c;
	}

	public void Update(String name,int task1,int task2,int task3){
		myDB.execSQL("UPDATE Access SET task_1="+task1+", task_2="+task2+", task_3="+task3+" WHERE name='"+name+"';");
	}
	
	public boolean isAllowed(String name,int task){
		
		c=myDB.rawQuery("SELECT * FROM Access WHERE name='"+name+"'",null);
		
		if (c.getCount()>0){
			int res=0;
			c.moveToFirst();
			switch(task){
			case 1:
				res=c.getInt(c.getColumnIndex("task_1"));
				break;
				
			case 2:
				res=c.getInt(c.getColumnIndex("task_2"));
				break;
				
			case 3:
				res=c.getInt(c.getColumnIndex("task_3"));
				break;
			}
			
			if (res==1)
				return true;
		}
		
		return false;
	}
	
	public String getName(int ind){
		c=myDB.rawQuery("SELECT * FROM Access WHERE _id="+ind+"",null);
		if (c.getCount()>0)
		{
		c.moveToFirst();
		return c.getString(c.getColumnIndex("name"));}
		else return "";
	}
	
	public int getId(String s){
		c=myDB.rawQuery("SELECT * FROM Access WHERE name='"+s+"'",null);
		if (c.getCount()>0)
		{c.moveToFirst();
		return c.getInt(c.getColumnIndex("_id"));}
		else return 0;
	}
}
