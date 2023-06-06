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

    // Table map
    public static class MapEntry implements BaseColumns {
        public static final String TABLE_NAME = "maps";
        public static final String COLUMN_NAME_USER_ID = "user_id";
        public static final String COLUMN_NAME_MAP_NAME = "map_name";
        public static final String COLUMN_NAME_TOPIC_COORDINATE = "topic_coordinate";
    }
    public static class DataPointEntry implements BaseColumns {
        public static final String TABLE_NAME = "data_points";
        public static final String COLUMN_NAME_X = "x";
        public static final String COLUMN_NAME_Y = "y";
        public static final String COLUMN_NAME_MAP_ID = "map_id";
    }
    private static final String SQL_CREATE_MAPS_TABLE =
            "CREATE TABLE " + MapEntry.TABLE_NAME + " (" +
                    MapEntry._ID + " INTEGER PRIMARY KEY," +
                    MapEntry.COLUMN_NAME_MAP_NAME + " TEXT," +
                    MapEntry.COLUMN_NAME_TOPIC_COORDINATE + " TEXT," +
                    MapEntry.COLUMN_NAME_USER_ID + " INTEGER)";
    private static final String SQL_CREATE_DATA_POINTS_TABLE =
            "CREATE TABLE " + DataPointEntry.TABLE_NAME + " (" +
                    DataPointEntry._ID + " INTEGER PRIMARY KEY," +
                    DataPointEntry.COLUMN_NAME_X + " REAL," +
                    DataPointEntry.COLUMN_NAME_Y + " REAL," +
                    DataPointEntry.COLUMN_NAME_MAP_ID + " INTEGER)";

    private static final String SQL_DELETE_MAPS_TABLE =
            "DROP TABLE IF EXISTS " + MapEntry.TABLE_NAME;

    private static final String SQL_DELETE_DATA_POINTS_TABLE =
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
        db.execSQL(SQL_CREATE_MAPS_TABLE);
        db.execSQL(SQL_CREATE_DATA_POINTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_MAPS_TABLE);
        db.execSQL(SQL_DELETE_DATA_POINTS_TABLE);
        onCreate(db);
    }
    public List<PointF> getSpecificDataPoints(String datapointId) {
        List<PointF> dataPoints = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {
                DataPointEntry.COLUMN_NAME_X,
                DataPointEntry.COLUMN_NAME_Y
        };
        String selection = DataPointEntry.COLUMN_NAME_MAP_ID + " = ?";
        String[] selectionArgs = {datapointId};
        Cursor cursor = db.query(
                DataPointEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        while (cursor.moveToNext()) {
            float x = cursor.getFloat(cursor.getColumnIndexOrThrow(DataPointEntry.COLUMN_NAME_X));
            float y = cursor.getFloat(cursor.getColumnIndexOrThrow(DataPointEntry.COLUMN_NAME_Y));
            dataPoints.add(new PointF(x, y));
        }
        cursor.close();
        return dataPoints;
    }
    public String getTopics(String mapId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {MapEntry.COLUMN_NAME_TOPIC_COORDINATE};
        String selection = DataPointDbHelper.MapEntry._ID + " = ?";
        String[] selectionArgs = {mapId};
        Cursor cursor = db.query(
                DataPointDbHelper.MapEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        String topicX = null;
        if (cursor.moveToFirst()) {
            topicX = cursor.getString(cursor.getColumnIndexOrThrow(MapEntry.COLUMN_NAME_TOPIC_COORDINATE));
        }
        cursor.close();
        return topicX;
    }
    public List<String> getAllMapIds() {
        List<String> mapIds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {MapEntry._ID};

        Cursor cursor = db.query(
                MapEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {
            String mapId = cursor.getString(cursor.getColumnIndexOrThrow(MapEntry._ID));
            mapIds.add(mapId);
        }
        cursor.close();

        return mapIds;
    }

    public String getMapNameById(String mapId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {MapEntry.COLUMN_NAME_MAP_NAME};
        String selection = MapEntry._ID + " = ?";
        String[] selectionArgs = {mapId};
        Cursor cursor = db.query(
                MapEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        String mapName = null;
        if (cursor.moveToFirst()) {
            mapName = cursor.getString(cursor.getColumnIndexOrThrow(MapEntry.COLUMN_NAME_MAP_NAME));
        }
        cursor.close();
        return mapName;
    }

}
