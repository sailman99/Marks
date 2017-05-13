package com.trace.marks;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DbAdaptor {
	 
	 
	 private static int count;
	 private static DbAdaptor dbAdaptor = null;
	 private SQLiteDatabase db; 
	
	 
	 
	 private DbAdaptor(Context ctx){
		  DBHelper help = new DBHelper(ctx);
		  db = help.getWritableDatabase();
	 }
	 public SQLiteDatabase getdb(){
		 return db;
	 }
	 public static DbAdaptor getInstance(Context ctx){
		  if(null == dbAdaptor){
			  dbAdaptor = new DbAdaptor(ctx);
		  }  
		  count++;
		  
		  return dbAdaptor;
	 }
	 
	 public void release(){
		 count--;
		 if(count == 0){
		  	db.close();
		  	dbAdaptor = null;
		 }
	 }
}
