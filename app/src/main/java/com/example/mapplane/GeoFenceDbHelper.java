    package com.example.mapplane;

    import android.content.Context;
    import android.database.Cursor;
    import android.database.sqlite.SQLiteDatabase;
    import android.database.sqlite.SQLiteOpenHelper;
    import android.graphics.PointF;
    import android.provider.BaseColumns;

    import java.util.ArrayList;
    import java.util.List;

    public class GeoFenceDbHelper extends SQLiteOpenHelper {

        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_NAME = "geofence.db";

        // Define the geofence table and data table SQL queries
        private static final String SQL_CREATE_GEOFENCE_TABLE =
                "CREATE TABLE " + GeofenceEntry.TABLE_NAME + " (" +
                        GeofenceEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        GeofenceEntry.COLUMN_NAME_NAME + " TEXT)";

        private static final String SQL_CREATE_GEOFENCE_DATA_TABLE =
                "CREATE TABLE " + GeofenceDataEntry.TABLE_NAME + " (" +
                        GeofenceDataEntry._ID + " INTEGER PRIMARY KEY," +
                        GeofenceDataEntry.COLUMN_NAME_GEOFENCE_ID + " INTEGER," +
                        GeofenceDataEntry.COLUMN_NAME_X + " REAL," +
                        GeofenceDataEntry.COLUMN_NAME_Y + " REAL," +
                        "FOREIGN KEY (" + GeofenceDataEntry.COLUMN_NAME_GEOFENCE_ID + ") REFERENCES " +
                        GeofenceEntry.TABLE_NAME + "(" + GeofenceEntry._ID + "))";

        private static final String SQL_DELETE_GEOFENCE_TABLE =
                "DROP TABLE IF EXISTS " + GeofenceEntry.TABLE_NAME;

        private static final String SQL_DELETE_GEOFENCE_DATA_TABLE =
                "DROP TABLE IF EXISTS " + GeofenceDataEntry.TABLE_NAME;

        // Define the geofence table and data table schema
        public final class GeofenceEntry implements BaseColumns {
            public static final String TABLE_NAME = "geofence";
            public static final String COLUMN_NAME_NAME = "name";
        }

        public final class GeofenceDataEntry implements BaseColumns {
            public static final String TABLE_NAME = "geofence_data";
            public static final String COLUMN_NAME_GEOFENCE_ID = "geofence_id";
            public static final String COLUMN_NAME_X = "x";
            public static final String COLUMN_NAME_Y = "y";
        }

        public GeoFenceDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_GEOFENCE_TABLE);
            db.execSQL(SQL_CREATE_GEOFENCE_DATA_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_GEOFENCE_TABLE);
            db.execSQL(SQL_DELETE_GEOFENCE_DATA_TABLE);
            onCreate(db);
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);

        }
        public ArrayList<String> getGeofenceNames(){
            ArrayList<String> names = new ArrayList<>();
            SQLiteDatabase db = getReadableDatabase();
            String[] projection = {GeoFenceDbHelper.GeofenceEntry.COLUMN_NAME_NAME};
            Cursor cursor = db.query(
                    GeoFenceDbHelper.GeofenceEntry.TABLE_NAME,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(GeoFenceDbHelper.GeofenceEntry.COLUMN_NAME_NAME));
                names.add(name);
            }
            cursor.close();
            return names;

        }
        public List<PointF> getAllGeofencePoints(String geofenceId) {
            List<PointF> dataPoints = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();
            String[] projection = {
                    GeoFenceDbHelper.GeofenceDataEntry.COLUMN_NAME_X,
                    GeoFenceDbHelper.GeofenceDataEntry.COLUMN_NAME_Y
            };
            String selection = GeoFenceDbHelper.GeofenceDataEntry.COLUMN_NAME_GEOFENCE_ID + " = ?";
            String[] selectionArgs = {geofenceId};
            Cursor cursor = db.query(
                    GeoFenceDbHelper.GeofenceDataEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );
            while (cursor.moveToNext()) {
                float x = cursor.getFloat(cursor.getColumnIndexOrThrow(GeoFenceDbHelper.GeofenceDataEntry.COLUMN_NAME_X));
                float y = cursor.getFloat(cursor.getColumnIndexOrThrow(GeoFenceDbHelper.GeofenceDataEntry.COLUMN_NAME_Y));
                dataPoints.add(new PointF(x, y));
            }
            cursor.close();
            return dataPoints;
        }
    }
