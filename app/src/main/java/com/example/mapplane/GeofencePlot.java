package com.example.mapplane;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GeofencePlot extends GeoFenceView{
    private List<List<PointF>> geofenceData;
    private List<String> geofenceNames;
    private Paint geofencePaint;
    private List<List<PointF>> geofencePoints;
    private int[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.MAGENTA};

    public GeofencePlot(Context context) {
        super(context);
        init();
    }

    public GeofencePlot(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GeofencePlot(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        geofencePoints = new ArrayList<>();
        geofencePaint = new Paint();
        geofencePaint.setStyle(Paint.Style.FILL);
        geofencePaint.setColor(Color.parseColor("#8000FF00")); // Semi-transparent green
    }

    public void setGeofenceData(String jsonData) {
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            geofencePoints.clear();
//            Log.d("MQTT", String.valueOf(jsonData));/

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                JSONArray polygonArray = jsonObject.getJSONArray("points");

                List<PointF> polygonPoints = new ArrayList<>();
                for (int j = 0; j < polygonArray.length(); j++) {
                    JSONObject pointObject = polygonArray.getJSONObject(j);
                    float x = (float) pointObject.getDouble("x");
                    float y = (float) pointObject.getDouble("y");
                    polygonPoints.add(new PointF(x, y));
                }

                geofencePoints.add(polygonPoints);
            }

            invalidate();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        for (int i = 0; i < geofencePoints.size(); i++) {
            List<PointF> polygonPoints = geofencePoints.get(i);

            // Draw the geofence polygon if it has at least 3 points
            if (polygonPoints.size() >= 3) {
                Path geofencePath = new Path();
                geofencePath.moveTo(polygonPoints.get(0).x, polygonPoints.get(0).y);
                for (int j = 1; j < polygonPoints.size(); j++) {
                    geofencePath.lineTo(polygonPoints.get(j).x, polygonPoints.get(j).y);
                }
                geofencePath.close();

                // Use a different color for each polygon
                // Generate a random color with semi-transparency
                int alpha = 120; // Adjust the alpha value as desired
                int red = (int) (Math.random() * 256);
                int green = (int) (Math.random() * 256);
                int blue = (int) (Math.random() * 256);
                int color = Color.argb(alpha, red, green, blue);
                geofencePaint.setColor(color);

                // Draw the filled geofence polygon
                canvas.drawPath(geofencePath, geofencePaint);

                // Draw the geofence name inside the geofence
                if (geofenceNames != null && i < geofenceNames.size()) {
                    String geofenceName = geofenceNames.get(i);
                    PointF centerPoint = calculatePolygonCenter(polygonPoints);

                    Paint textPaint = new Paint();
                    textPaint.setColor(Color.BLACK);
                    textPaint.setTextSize(30);
                    textPaint.setTextAlign(Paint.Align.CENTER);
                    // Adjust the y-coordinate to center the text vertically
                    Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
                    float yOffset = (fontMetrics.descent + fontMetrics.ascent) / 2;

                    canvas.drawText(geofenceName, centerPoint.x, centerPoint.y - yOffset, textPaint);
                }
            }
        }
    }

    private PointF calculatePolygonCenter(List<PointF> polygonPoints) {
        float sumX = 0;
        float sumY = 0;
        int numPoints = polygonPoints.size();
        for (int i = 0; i < numPoints; i++) {
            PointF point = polygonPoints.get(i);
            sumX += point.x;
            sumY += point.y;
        }
        return new PointF(sumX / numPoints, sumY / numPoints);
    }

    public void setGeofenceNames(List<String> geofenceNames) {
        this.geofenceNames = geofenceNames;
        invalidate();
    }

    public void clearGeofenceData() {
        geofencePoints.clear();
        invalidate();
    }



}