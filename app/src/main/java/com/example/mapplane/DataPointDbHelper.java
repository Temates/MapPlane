package com.example.mapplane;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.PointF;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

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

    public List<PointF> getAllDataPoints() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<PointF> dataPoints = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT * FROM " + DataPointEntry.TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") float x = cursor.getFloat(cursor.getColumnIndex(DataPointEntry.COLUMN_NAME_X));
                @SuppressLint("Range") float y = cursor.getFloat(cursor.getColumnIndex(DataPointEntry.COLUMN_NAME_Y));
                PointF dataPoint = new PointF(x, y);
                dataPoints.add(dataPoint);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return dataPoints;
    }

    public DataPointDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void deleteAllDataPoints() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DataPointEntry.TABLE_NAME, null, null);
        db.close();
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
