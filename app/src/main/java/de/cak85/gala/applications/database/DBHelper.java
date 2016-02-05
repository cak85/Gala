package de.cak85.gala.applications.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ckuster on 05.02.2016.
 */
public class DBHelper extends SQLiteOpenHelper {

	private static final String NULL = "null";

	private static DBHelper mInstance = null;

	// If you change the database schema, you must increment the database version.
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "Categories.db";

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public static DBHelper getInstance(Context ctx) {
		/**
		 * use the application context as suggested by CommonsWare.
		 * this will ensure that you dont accidentally leak an Activitys
		 * context (see this article for more information:
		 * http://android-developers.blogspot.nl/2009/01/avoiding-memory-leaks.html)
		 */
		if (mInstance == null) {
			mInstance = new DBHelper(ctx.getApplicationContext());
		}
		return mInstance;
	}


	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CategoryContract.CategoryEntry.SQL_CREATE_ENTRIES);
		db.execSQL(CategoryContract.DescriptionEntry.SQL_CREATE_ENTRIES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(CategoryContract.CategoryEntry.SQL_DELETE_ENTRIES);
		db.execSQL(CategoryContract.DescriptionEntry.SQL_DELETE_ENTRIES);
		onCreate(db);
	}

	public Map<String, Boolean> getCategories() {
		SQLiteDatabase database = getReadableDatabase();
		Cursor cursor = database.query(
				CategoryContract.CategoryEntry.TABLE_NAME,
				new String[]{
						CategoryContract.CategoryEntry.COLUMN_PACKAGE_NAME,
						CategoryContract.CategoryEntry.COLUMN_IS_GAME},
				null,
				null,
				null,
				null,
				null
		);
		Map<String, Boolean> categories = new HashMap<>();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			categories.put(cursor.getString(0), cursor.getInt(1) == 1);
			cursor.moveToNext();
		}
		cursor.close();
		return categories;
	}

	public void saveCategory(String packageName, boolean isGame) {
		ContentValues values = new ContentValues();
		values.put(CategoryContract.CategoryEntry.COLUMN_PACKAGE_NAME, packageName);
		values.put(CategoryContract.CategoryEntry.COLUMN_IS_GAME, isGame ? 1 : 0);

		getWritableDatabase().insert(CategoryContract.CategoryEntry.TABLE_NAME, NULL, values);
	}

	public String getDescription(String packageName) {
		SQLiteDatabase database = getReadableDatabase();
		Cursor cursor = database.query(
				CategoryContract.DescriptionEntry.TABLE_NAME,
				new String[]{CategoryContract.DescriptionEntry.COLUMN_DESCRIPTION},
				CategoryContract.DescriptionEntry.COLUMN_PACKAGE_NAME + " = '" + packageName + "'",
				null,
				null,
				null,
				null
		);
		cursor.moveToFirst();
		String description = null;
		try {
			description = cursor.getString(0);
		} catch (CursorIndexOutOfBoundsException e) {
		} finally {
			cursor.close();
		}
		return description;
	}

	public void saveDescription(String packageName, String description) {
		ContentValues values = new ContentValues();
		values.put(CategoryContract.DescriptionEntry.COLUMN_PACKAGE_NAME, packageName);
		values.put(CategoryContract.DescriptionEntry.COLUMN_DESCRIPTION, description);

		getWritableDatabase().insert(CategoryContract.DescriptionEntry.TABLE_NAME, NULL, values);
	}
}
