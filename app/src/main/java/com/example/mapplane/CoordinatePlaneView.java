package com.example.mapplane;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CoordinatePlaneView extends View {

    private List<PointF> dataPoints;
    private SQLiteDatabase database;
    private List<PointF> currentPositionPoints = new ArrayList<>();
    public CoordinatePlaneView(Context context) {
        super(context);
    }
    float mScale = 25.0f;


    public CoordinatePlaneView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        dataPoints = new ArrayList<>();

//        // Set initial values for x and y at the center of the view
//        int centerX = getWidth() / 2;
//        int centerY = getHeight() / 2;
//        PointF centerPoint = new PointF(centerX, centerY);
//        currentPositionPoints.add(centerPoint);


    }

    public CoordinatePlaneView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawCoordinatePlane(canvas);

        // Draw the data points
        Paint dataPaint = new Paint();
        dataPaint.setColor(Color.RED);
        dataPaint.setStrokeWidth(10);

        Paint currentdataPaint = new Paint();
        currentdataPaint.setColor(Color.GRAY);
        currentdataPaint.setStrokeWidth(10);

        for (PointF point : currentPositionPoints) {
            canvas.drawPoint(point.x, point.y, currentdataPaint);
            canvas.drawCircle(point.x, point.y, 10,currentdataPaint);
        }
        for (int i = 0; i < dataPoints.size(); i++) {
            PointF point = dataPoints.get(i);
            canvas.drawPoint(point.x, point.y, dataPaint);

            // Draw a line connecting this point to the previous one
            if (i > 0) {
                PointF prevPoint = dataPoints.get(i - 1);
                Paint linePaint = new Paint();
                linePaint.setColor(Color.BLUE);
                linePaint.setStrokeWidth(5);
                canvas.drawLine(prevPoint.x, prevPoint.y, point.x, point.y, linePaint);
            }
        }



    }


    private void drawCoordinatePlane(Canvas canvas) {
        // Set the color of the x and y axes
        Paint axisPaint = new Paint();
        axisPaint.setColor(Color.BLACK);

        // Draw the x and y axes
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        canvas.drawLine(centerX, 0, centerX, getHeight(), axisPaint);
        canvas.drawLine(0, centerY, getWidth(), centerY, axisPaint);

        // Set the color of the grid lines
        Paint gridPaint = new Paint();
        gridPaint.setColor(Color.GRAY);
        gridPaint.setStrokeWidth(1);

        // Draw the grid lines
        int step = 50;
        for (int i = step; i < getWidth(); i += step) {
            canvas.drawLine(i, 0, i, getHeight(), gridPaint);
        }
        for (int i = step; i < getHeight(); i += step) {
            canvas.drawLine(0, i, getWidth(), i, gridPaint);
        }
    }

    public void addDataPoint(float x, float y,boolean saveData) {
        PointF point = new PointF(x, y);
        if (saveData) {
            dataPoints.add(point);
        } else {
            currentPositionPoints.clear();
            currentPositionPoints.add(point);
        }
        invalidate();
    }

    public void trackPosition(float x, float y) {
        // Check if the new position is within the bounds of the coordinate plane
        if (x < 0 || x > getWidth() || y < 0 || y > getHeight()) {
            return;
        }

        // Create a new data point to represent the current position
        List<PointF> dataPoints = new ArrayList<>();
        dataPoints.add(new PointF(x, y));

        // Set the new data points and invalidate the view to update the display
        setDataPoints(dataPoints);
        invalidate();
    }

    public List<PointF> getDataPoints() {
        return dataPoints;
    }
    public void setDataPoints(List<PointF> dataPoints) {
        this.dataPoints = dataPoints;
    }

    public PointF mapToScreen(float x, float y) {
        float screenX = getWidth() / 2 + x * mScale;
        float screenY = getHeight() / 2 - y * mScale;
        return new PointF(screenX, screenY);
    }



}
