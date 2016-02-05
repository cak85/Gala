package de.cak85.gala.applications.database;

import android.provider.BaseColumns;

/**
 * Created by ckuster on 05.02.2016.
 */
public class CategoryContract {

	private static final String TEXT_TYPE = " TEXT";
	private static final String INTEGER_TYPE = " INTEGER";
	private static final String COMMA_SEP = ",";

	private CategoryContract(){}

	public static abstract class CategoryEntry implements BaseColumns {
		public static final String TABLE_NAME = "categories";
		public static final String COLUMN_PACKAGE_NAME = "package_name";
		public static final String COLUMN_IS_GAME = "is_game";

		public static final String SQL_CREATE_ENTRIES =
				"CREATE TABLE " + TABLE_NAME + " (" + COLUMN_PACKAGE_NAME + TEXT_TYPE
						+ " PRIMARY KEY" + COMMA_SEP
						+ COLUMN_IS_GAME + INTEGER_TYPE +
						" )";

		public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;
	}

	public static abstract class DescriptionEntry implements BaseColumns {
		public static final String TABLE_NAME = "descriptions";
		public static final String COLUMN_PACKAGE_NAME = "package_name";
		public static final String COLUMN_DESCRIPTION = "description";

		public static final String SQL_CREATE_ENTRIES =
				"CREATE TABLE " + TABLE_NAME + " (" + COLUMN_PACKAGE_NAME + TEXT_TYPE
						+ " PRIMARY KEY" + COMMA_SEP
						+ COLUMN_DESCRIPTION + TEXT_TYPE +
						" )";

		public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;
	}

}
