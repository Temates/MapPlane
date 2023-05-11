package com.example.mapplane;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GeoFenceView extends CoordinatePlaneView {
    private Paint geofencePaint;
    private List<PointF> geofencePoints;

    public GeoFenceView(Context context) {
        super(context);
        init();
    }

    public GeoFenceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GeoFenceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        geofencePaint = new Paint();
        geofencePaint.setStyle(Paint.Style.FILL);
        geofencePaint.setColor(Color.parseColor("#8000FF00")); // Semi-transparent green

        geofencePoints = new ArrayList<>();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Draw the geofence polygon if it has at least 3 points
        if (geofencePoints.size() >= 3) {
            Path geofencePath = new Path();
            geofencePath.moveTo(geofencePoints.get(0).x, geofencePoints.get(0).y);
            for (int i = 1; i < geofencePoints.size(); i++) {
                geofencePath.lineTo(geofencePoints.get(i).x, geofencePoints.get(i).y);
            }
            geofencePath.close();

            canvas.drawPath(geofencePath, geofencePaint);
        }
    }

    @Override
    public void addDataPoint(float x, float y, boolean saveData) {
        super.addDataPoint(x, y, saveData);

        if (saveData) {
//            // Add the point to the geofence polygon
//            geofencePoints.add(new PointF(x, y));
//
//            // Save the geofence polygon to the database
//            GeoFenceDbHelper dbHelper = new GeoFenceDbHelper(getContext());
//            SQLiteDatabase db = dbHelper.getWritableDatabase();
//            ContentValues values = new ContentValues();
//            values.put(GeoFenceDbHelper.GeofenceDataEntry.COLUMN_NAME_X, x);
//            values.put(GeoFenceDbHelper.GeofenceDataEntry.COLUMN_NAME_Y, y);
//            db.insert(GeoFenceDbHelper.GeofenceDataEntry.TABLE_NAME, null, values);
//            db.close();
            PointF point = new PointF(x, y);

            // Redraw the view to update the display
            invalidate();
        }
    }
    public List<PointF> getDataPoints() {
        return geofencePoints;
    }



}
