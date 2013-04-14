package il.ac.huji.todolist;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;



public class TodoDAL {
	Context context;
	private SQLiteDatabase db;
	public TodoDAL(Context context) { 
		this.context=context;
		TasksDatabaseHelper helper = new TasksDatabaseHelper(context); 
		db = helper.getWritableDatabase();
		Parse.initialize(context,  context.getString(R.string.parseApplication), context.getString(R.string.clientKey)); 
		ParseUser.enableAutomaticUser();
	}

	public boolean insert(ITodoItem todoItem) { 
		try {
			ContentValues taskData = new ContentValues();
			ParseObject parseObject = new ParseObject("todo");
			long dbTime=0;
			taskData.put("title", todoItem.getTitle());
			parseObject.put("title", todoItem.getTitle());
			if (todoItem.getDueDate()!=null)
			{
				dbTime=todoItem.getDueDate().getTime(); //ask regarding the -1 issues
				taskData.put("due", dbTime);
				parseObject.put("due", dbTime);
			}else
			{
				taskData.putNull("due");
			}
			db.insert("todo", null, taskData);
			parseObject.save();
		} catch (ParseException e) {
			return false;
		}
		return true; 
	}
	public boolean update(ITodoItem todoItem) {  
		try {
			ParseQuery query = new ParseQuery("todo");
			query.whereEqualTo("title", todoItem.getTitle());
			List<ParseObject> objects=query.find();
			if (objects.size()<1)
				return false;
			ContentValues args = new ContentValues();
			long updateDue=0;
			if (todoItem.getDueDate()!=null)
			{
				updateDue=todoItem.getDueDate().getTime();
				args.put("due", updateDue);
				db.update("todo", args, "title" + "='" + todoItem.getTitle()+"'", null);	
				for (ParseObject object : objects) {
					object.put("due", updateDue);
					object.save();
				}
			}
			else
			{
				for (ParseObject object : objects) {
					object.remove("due");
					object.save();
					args.putNull("due");
					db.update("todo", args, "title" + "='" + todoItem.getTitle()+"'", null);	
				}
			}
			
			
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	public boolean delete(ITodoItem todoItem) { 

		try {
			db.delete("todo", "title" + "='"+todoItem.getTitle()+"'",null);
			ParseQuery query = new ParseQuery("todo");
			query.whereEqualTo("title", todoItem.getTitle());
			List<ParseObject> objects=query.find();//TODO check if deleted more than needed!
			if (objects.size()<1)
				return false;
			for (ParseObject object : objects) {
				try {
					object.delete();

				} catch (ParseException e) {
					e.printStackTrace();
					throw new RuntimeException();
				}
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public List<ITodoItem> all() { 

		List<ITodoItem> items=new ArrayList<ITodoItem>();

		try {
			items = new ArrayList<ITodoItem>();
			String selectQuery = "SELECT  * FROM " + "todo";
			Cursor cursor = db.rawQuery(selectQuery, null);

			// looping through all rows and adding to list
			if (cursor.moveToFirst()) {
				do {
					Task t = new Task(cursor.getString(1),  new Date(cursor.getLong(2))); 
					items.add(t);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			//
		}
		return items; //check if there is a problem with the "final"	  
	}
}