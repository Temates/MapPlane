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

    public CoordinatePlaneView(Context context) {
        super(context);
    }

    public CoordinatePlaneView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        dataPoints = new ArrayList<>();


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

//        for (PointF point : dataPoints) {
//            canvas.drawPoint(point.x, point.y, dataPaint);
//
//
//        }
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
        canvas.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2, axisPaint);
        canvas.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight(), axisPaint);

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
    public void addDataPoint(float x, float y) {
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        dataPoints.add(new PointF(x, y));
        invalidate();
    }
    public List<PointF> getDataPoints() {
        return dataPoints;
    }
    public void setDataPoints(List<PointF> dataPoints) {
        this.dataPoints = dataPoints;
    }
}
