package com.example.mapplane;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.fitness.data.DataPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//    /**
// * A simple {@link Fragment} subclass.
// * Use the {@link EditMap#newInstance} factory method to
// * create an instance of this fragment.
// */
public class EditMap extends Fragment {

    private CoordinatePlaneView coordinatePlaneView;
    float x = 0;
    float y = 0;
    float xmap= 0;
    float ymap= 0;
    boolean isCalibrated = false;

    public EditMap() {
        // Required empty public constructor

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_edit_map, container, false);
        // Get a reference to the CoordinatePlaneView
        coordinatePlaneView = view.findViewById(R.id.coordinate_plane_view);
        displaySavedData();

        // Add button to add random data point
        Button calibrate = view.findViewById(R.id.calibrate);
        calibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                generateDatapoint(x,y,true);
            }
        });

        // Set up the "Save Data" button
        Button saveDataButton = view.findViewById(R.id.savedatapoint);
        saveDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                saveData();
            }
        });

        // Set up the "Delete Data" button
        Button clear = view.findViewById(R.id.clear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               deleteData();
            }
        });

        return view;
    }




        public void generateDatapoint(float x, float y, boolean con) {
            isCalibrated = true;
        }


    private void displaySavedData() {

        getAllDataPoints();
    }

    public void saveData() {
        List<PointF> dataPoints = coordinatePlaneView.getDataPoints();

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                DataPointDbHelper dbHelper = new DataPointDbHelper(getContext());
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                db.beginTransaction();
                try {
                    for (PointF dataPoint : dataPoints) {
                        ContentValues values = new ContentValues();
                        values.put(DataPointDbHelper.DataPointEntry.COLUMN_NAME_X, dataPoint.x);
                        values.put(DataPointDbHelper.DataPointEntry.COLUMN_NAME_Y, dataPoint.y);
                        db.insert(DataPointDbHelper.DataPointEntry.TABLE_NAME, null, values);
                        Log.d("data", String.valueOf(dataPoints));
                    }
                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    Log.e("Save Data", "Error saving data: " + e.getMessage());
                } finally {
                    db.endTransaction();
                    dbHelper.close();
                }
            } catch (Exception e) {
                Log.e("Save Data", "Error saving data: " + e.getMessage());
            }

            // Update the UI on the main thread
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                Toast.makeText(getContext(), "Data saved successfully", Toast.LENGTH_SHORT).show();
            });
        });
    }

    public void deleteData() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new DeleteAllDataRunnable());
    }




    private void getAllDataPoints() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                DataPointDbHelper dbHelper = new DataPointDbHelper(getContext());
                List<PointF> dataPoints = dbHelper.getAllDataPoints();
                dbHelper.close();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (PointF dataPoint : dataPoints) {
                            coordinatePlaneView.addDataPoint(dataPoint.x, dataPoint.y, true);
                        }
                        coordinatePlaneView.invalidate();
                    }
                });
            }
        });

        executorService.shutdown();
    }

    public void updateCalibrationData(float x, float y) {
        // Inflate the layout for this fragment
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            //Background work here
            handler.post(() -> {

                PointF screenPoint = coordinatePlaneView.mapToScreen(x,y);
                xmap = screenPoint.x;
                ymap = screenPoint.y;
                Log.d("MQTT", "x: "+x);
                Log.d("MQTT", "x: "+y);
                // Add current position to coordinate plane view
                coordinatePlaneView.addDataPoint(screenPoint.x, screenPoint.y, true);
                coordinatePlaneView.invalidate();
                isCalibrated = false;
                //UI Thread work here
            });
        });
    }


    private class DeleteAllDataRunnable implements Runnable {
        @Override
        public void run() {
            DataPointDbHelper dbHelper = new DataPointDbHelper(getContext());
            dbHelper.deleteAllDataPoints();
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    List<PointF> dataPoints = new ArrayList<>();
                    coordinatePlaneView.setDataPoints(dataPoints);
                    coordinatePlaneView.invalidate();
                    Toast.makeText(getContext(), "All data points deleted.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    public void updateData(float x, float y){
        // Inflate the layout for this fragment
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            //Background work here
            handler.post(() -> {

                PointF screenPoint = coordinatePlaneView.mapToScreen(x,y);
                xmap = screenPoint.x;
                ymap = screenPoint.y;
                Log.d("MQTT", "x: "+x);
                Log.d("MQTT", "x: "+y);
                // Add current position to coordinate plane view
                coordinatePlaneView.addDataPoint(screenPoint.x, screenPoint.y, false);
                coordinatePlaneView.invalidate();
                //UI Thread work here
            });
        });
    }

}