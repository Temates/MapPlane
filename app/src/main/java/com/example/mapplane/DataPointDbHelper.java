package com.example.mapplane;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DataPointDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "DataPoint.db";

    public static class DataPointEntry implements BaseColumns {
        public static final String TABLE_NAME = "data_points";
        public static final String COLUMN_NAME_X = "x";
        public static final String COLUMN_NAME_Y = "y";
    }

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DataPointEntry.TABLE_NAME + " (" +
                    DataPointEntry._ID + " INTEGER PRIMARY KEY," +
                    DataPointEntry.COLUMN_NAME_X + " REAL," +
                    DataPointEntry.COLUMN_NAME_Y + " REAL)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + DataPointEntry.TABLE_NAME;

    public DataPointDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
