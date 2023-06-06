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
                        GeofenceEntry.COLUMN_NAME_NAME + " TEXT," +
                        GeofenceEntry.COLUMN_NAME_MAPS_ID + " INTEGER)";

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
            public static final String COLUMN_NAME_MAPS_ID = "Maps_id";
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
        public ArrayList<String> getGeofenceNames(String mapsId){
            ArrayList<String> names = new ArrayList<>();
            SQLiteDatabase db = getReadableDatabase();
            String[] projection = {GeofenceEntry.COLUMN_NAME_NAME};
            String selection = GeofenceEntry.COLUMN_NAME_MAPS_ID + " = ?";
            String[] selectionArgs = {mapsId};
            Cursor cursor = db.query(
                    GeofenceEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(GeofenceEntry.COLUMN_NAME_NAME));
                names.add(name);
            }
            cursor.close();
            return names;
        }


        public List<List<PointF>> getAllGeofencePointsByMapId(String mapsId) {
            List<List<PointF>> geofenceList = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();

            // Query the geofence table to get the geofence IDs for the given mapsId
            String[] geofenceProjection = {GeofenceEntry._ID};
            String geofenceSelection = GeofenceEntry.COLUMN_NAME_MAPS_ID + " = ?";
            String[] geofenceSelectionArgs = {mapsId};
            Cursor geofenceCursor = db.query(
                    GeofenceEntry.TABLE_NAME,
                    geofenceProjection,
                    geofenceSelection,
                    geofenceSelectionArgs,
                    null,
                    null,
                    null
            );

            while (geofenceCursor.moveToNext()) {
                int geofenceId = geofenceCursor.getInt(geofenceCursor.getColumnIndexOrThrow(GeofenceEntry._ID));

                // Query the geofence data table to get the points for each geofence
                String[] pointsProjection = {
                        GeofenceDataEntry.COLUMN_NAME_X,
                        GeofenceDataEntry.COLUMN_NAME_Y
                };
                String pointsSelection = GeofenceDataEntry.COLUMN_NAME_GEOFENCE_ID + " = ?";
                String[] pointsSelectionArgs = {String.valueOf(geofenceId)};
                Cursor pointsCursor = db.query(
                        GeofenceDataEntry.TABLE_NAME,
                        pointsProjection,
                        pointsSelection,
                        pointsSelectionArgs,
                        null,
                        null,
                        null
                );

                List<PointF> dataPoints = new ArrayList<>();
                while (pointsCursor.moveToNext()) {
                    float x = pointsCursor.getFloat(pointsCursor.getColumnIndexOrThrow(GeofenceDataEntry.COLUMN_NAME_X));
                    float y = pointsCursor.getFloat(pointsCursor.getColumnIndexOrThrow(GeofenceDataEntry.COLUMN_NAME_Y));
                    dataPoints.add(new PointF(x, y));
                }
                pointsCursor.close();

                geofenceList.add(dataPoints);
            }
            geofenceCursor.close();

            return geofenceList;
        }
        public ArrayList<String> getGeofenceNamesByMapId(String mapsId) {
            ArrayList<String> names = new ArrayList<>();
            SQLiteDatabase db = getReadableDatabase();
            String[] projection = {GeofenceEntry.COLUMN_NAME_NAME};
            String selection = GeofenceEntry.COLUMN_NAME_MAPS_ID + " = ?";
            String[] selectionArgs = {mapsId};
            Cursor cursor = db.query(
                    GeofenceEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(GeofenceEntry.COLUMN_NAME_NAME));
                names.add(name);
            }
            cursor.close();
            return names;
        }

        public List<PointF> getGeofencepointswithMapId(String mapsId) {
            List<PointF> geofencePoints = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();

            // Query the geofence table to get the geofence IDs for the given mapsId
            String[] geofenceProjection = {GeofenceEntry._ID};
            String geofenceSelection = GeofenceEntry.COLUMN_NAME_MAPS_ID + " = ?";
            String[] geofenceSelectionArgs = {mapsId};
            Cursor geofenceCursor = db.query(
                    GeofenceEntry.TABLE_NAME,
                    geofenceProjection,
                    geofenceSelection,
                    geofenceSelectionArgs,
                    null,
                    null,
                    null
            );

            while (geofenceCursor.moveToNext()) {
                int geofenceId = geofenceCursor.getInt(geofenceCursor.getColumnIndexOrThrow(GeofenceEntry._ID));

                // Query the geofence data table to get the points for each geofence
                String[] pointsProjection = {
                        GeofenceDataEntry.COLUMN_NAME_X,
                        GeofenceDataEntry.COLUMN_NAME_Y
                };
                String pointsSelection = GeofenceDataEntry.COLUMN_NAME_GEOFENCE_ID + " = ?";
                String[] pointsSelectionArgs = {String.valueOf(geofenceId)};
                Cursor pointsCursor = db.query(
                        GeofenceDataEntry.TABLE_NAME,
                        pointsProjection,
                        pointsSelection,
                        pointsSelectionArgs,
                        null,
                        null,
                        null
                );

                while (pointsCursor.moveToNext()) {
                    float x = pointsCursor.getFloat(pointsCursor.getColumnIndexOrThrow(GeofenceDataEntry.COLUMN_NAME_X));
                    float y = pointsCursor.getFloat(pointsCursor.getColumnIndexOrThrow(GeofenceDataEntry.COLUMN_NAME_Y));
                    geofencePoints.add(new PointF(x, y));
                }
                pointsCursor.close();
            }
            geofenceCursor.close();

            return geofencePoints;
        }



    }
