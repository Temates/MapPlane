package com.example.mapplane;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GeoFenceView extends CoordinatePlaneView {
    private Paint geofencePaint;
    private List<PointF> geofencePoints;
    private List<String> geofenceNames;
    private int[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.MAGENTA};

    public GeoFenceView(Context context) {
        super(context);
        init();
    }
    public void setGeofenceName(List<String> geofenceNames) {
        this.geofenceNames = geofenceNames;
        invalidate();
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
        // Choose a random color from the colors array
        int colorIndex = (int) (Math.random() * colors.length);
        // Draw the points
        Paint pointPaint = new Paint();
        pointPaint.setColor(Color.RED);
        for (PointF point : geofencePoints) {
            canvas.drawCircle(point.x, point.y, 3, pointPaint);
        }

        // Draw the lines between the points
        Paint linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(3);
        for (int i = 0; i < geofencePoints.size() - 1; i++) {
            PointF startPoint = geofencePoints.get(i);
            PointF endPoint = geofencePoints.get(i + 1);
            canvas.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y, linePaint);
        }

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
//        super.addDataPoint(x, y, saveData);
        if (saveData) {
            // Clear the existing geofence points

            // Add the new point
            PointF point = new PointF(x, y);
            geofencePoints.add(point);

            // Redraw the view
            invalidate();
        }
    }
    public List<PointF> getDataPoints() {
        return geofencePoints;
    }
    public void setDataPoints(List<PointF> geofencePoints) {
        this.geofencePoints = geofencePoints;
        invalidate();
    }
    public void setDataPoints(List<PointF> geofencePoints, List<String> geofenceNames) {
        this.geofencePoints = geofencePoints;
        this.geofenceNames = geofenceNames;
        invalidate();
    }
    public void clearDataPoints() {
        geofencePoints.clear();
        invalidate();
    }
}