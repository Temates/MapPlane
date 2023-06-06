package com.example.mapplane;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CoordinatePlaneView extends View {

    private List<PointF> dataPoints = new ArrayList<>();
    private List<PointF> currentPositionPoints = new ArrayList<>();
    public CoordinatePlaneView(Context context) {
        super(context);
    }
    private List<PointF> HistoryPoints = new ArrayList<>();
    float mScale = 50.0f;
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

        Paint currentdataPaint = new Paint();
        currentdataPaint.setColor(Color.GRAY);
        currentdataPaint.setStrokeWidth(10);

        // Create a gradient color from white to red
        int[] colors = {Color.WHITE, Color.RED};
        float[] positions = {0.0f, 1.0f};
        Shader shader = new LinearGradient(0, 0, 0, canvas.getHeight(), colors, positions, Shader.TileMode.CLAMP);


        Paint HistoryPaint = new Paint();
        HistoryPaint.setShader(shader);
        HistoryPaint.setStrokeWidth(7);



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
        // Draw the additional points with a normal line
        if (!HistoryPoints.isEmpty()) {
            PointF lastPoint = HistoryPoints.get(HistoryPoints.size() - 1);
            for (int i = 0; i < HistoryPoints.size() - 1; i++) {
                PointF point = HistoryPoints.get(i);
                PointF nextPoint = HistoryPoints.get(i + 1);
                canvas.drawLine(point.x, point.y, nextPoint.x, nextPoint.y, HistoryPaint);
            }
            // Draw a circle for the latest history point
            float circleRadius = 5;
            canvas.drawCircle(lastPoint.x, lastPoint.y, circleRadius, HistoryPaint);
        }

        for (PointF point : currentPositionPoints) {
            canvas.drawPoint(point.x, point.y, currentdataPaint);
            canvas.drawCircle(point.x, point.y, 10,currentdataPaint);
        }


    }
    private void drawCoordinatePlane(Canvas canvas) {
        // Set the color of the x and y axes
        Paint axisPaint = new Paint();
        axisPaint.setColor(Color.BLACK);
        axisPaint.setStrokeWidth(2);

        // Draw the x and y axes
        int width = getWidth();
        int height = getHeight();
        canvas.drawLine(0, 0, width, 0, axisPaint); // Top edge line
        canvas.drawLine(width, 0, width, height, axisPaint); // Right edge line
        canvas.drawLine(width, height, 0, height, axisPaint); // Bottom edge line
        canvas.drawLine(0, height, 0, 0, axisPaint); // Left edge line
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
    public PointF mapToScreen(float x, float y) {
        float screenX = getWidth() / 2 + x * mScale;
        float screenY = getHeight() / 2 - y * mScale;
        return new PointF(screenX, screenY);
    }
    public List<PointF> getDataPoints() {
        return dataPoints;
    }
    public void setDataPoints(List<PointF> dataPoints) {
        this.dataPoints = dataPoints;
    }
    public void addHistoryPoint(float x, float y) {
        HistoryPoints.add(new PointF(x, y));
        invalidate();
    }
    public void setHistoryPoint(List<PointF> historyPoint) {
        this.HistoryPoints = historyPoint;
        invalidate();
    }
}
