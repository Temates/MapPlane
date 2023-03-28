package com.example.mapplane;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

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

//    /**
// * A simple {@link Fragment} subclass.
// * Use the {@link EditMap#newInstance} factory method to
// * create an instance of this fragment.
// */
public class EditMap extends Fragment {

    private CoordinatePlaneView coordinatePlaneView;

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

                generateRandomData();
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

//        // Set up the "Save Data" button
//        Button saveDataButton = view.findViewById(R.id.savedatapoint);
//        saveDataButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                List<PointF> dataPoints = coordinatePlaneView.getDataPoints();
//                DataPointDbHelper dbHelper = new DataPointDbHelper(getContext());
//                SQLiteDatabase db = dbHelper.getWritableDatabase();
//
//                db.beginTransaction();
//                try {
//                    for (PointF dataPoint : dataPoints) {
//                        ContentValues values = new ContentValues();
//                        values.put(DataPointDbHelper.DataPointEntry.COLUMN_NAME_X, dataPoint.x);
//                        values.put(DataPointDbHelper.DataPointEntry.COLUMN_NAME_Y, dataPoint.y);
//
//                        db.insert(DataPointDbHelper.DataPointEntry.TABLE_NAME, null, values);
//                    }
//                    db.setTransactionSuccessful();
//                    Toast.makeText(getContext(), "Data saved successfully", Toast.LENGTH_SHORT).show();
//                } catch (Exception e) {
//                    Log.e("Save Data", "Error saving data: " + e.getMessage());
//                } finally {
//                    db.endTransaction();
//                    dbHelper.close();
//                }
//            }
//        });

        // Set up the "Delete Data" button
//        Button clear = view.findViewById(R.id.clear);
//        clear.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                DataPointDbHelper dbHelper = new DataPointDbHelper(getContext());
//
//                dbHelper.deleteAllDataPoints();
//                List<PointF> dataPoints = dbHelper.getAllDataPoints();
//                coordinatePlaneView.setDataPoints(dataPoints);
//                coordinatePlaneView.invalidate();
////                displaySavedData();
//                Toast.makeText(getContext(), "All data points deleted.", Toast.LENGTH_SHORT).show();
//
//            }
//        });

        return view;
    }




        private void generateRandomData() {
        // Generate random data points
        Random random = new Random();
        float x = random.nextFloat() * coordinatePlaneView.getWidth();
        float y = random.nextFloat() * coordinatePlaneView.getHeight();
        coordinatePlaneView.addDataPoint(x, y);

        }

    private void displaySavedData() {

        GetAllDataAsyncTask getAllDataAsyncTask = new GetAllDataAsyncTask();
        getAllDataAsyncTask.execute();
    }

    public void saveData() {
        SaveDataAsyncTask saveDataAsyncTask = new SaveDataAsyncTask();
        saveDataAsyncTask.execute();
    }

    public void deleteData() {
        DeleteAllDataAsyncTask deleteDataAsyncTask = new DeleteAllDataAsyncTask();
        deleteDataAsyncTask.execute();
    }
    private class SaveDataAsyncTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                List<PointF> dataPoints = coordinatePlaneView.getDataPoints();
                DataPointDbHelper dbHelper = new DataPointDbHelper(getContext());
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                db.beginTransaction();
                try {
                    for (PointF dataPoint : dataPoints) {
                        ContentValues values = new ContentValues();
                        values.put(DataPointDbHelper.DataPointEntry.COLUMN_NAME_X, dataPoint.x);
                        values.put(DataPointDbHelper.DataPointEntry.COLUMN_NAME_Y, dataPoint.y);

                        db.insert(DataPointDbHelper.DataPointEntry.TABLE_NAME, null, values);
                    }
                    db.setTransactionSuccessful();
                    return true;
                } catch (Exception e) {
                    Log.e("Save Data", "Error saving data: " + e.getMessage());
                    return false;
                } finally {
                    db.endTransaction();
                    dbHelper.close();
                }
            } catch (Exception e) {
                Log.e("Save Data", "Error saving data: " + e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Toast.makeText(getContext(), "Data saved successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to save data", Toast.LENGTH_SHORT).show();
            }
        }
    }


//        public void displaySavedData() {
//        DataPointDbHelper dbHelper = new DataPointDbHelper(getContext());
//        List<PointF> dataPoints = dbHelper.getAllDataPoints();
//
//
//        for (PointF dataPoint : dataPoints) {
//            coordinatePlaneView.addDataPoint(dataPoint.x, dataPoint.y);
//            }
//        }

    private class GetAllDataAsyncTask extends AsyncTask<Void, Void, List<PointF>> {

        @Override
        protected List<PointF> doInBackground(Void... voids) {
            DataPointDbHelper dbHelper = new DataPointDbHelper(getContext());
            List<PointF> dataPoints = dbHelper.getAllDataPoints();
            dbHelper.close();
            return dataPoints;
        }

        @Override
        protected void onPostExecute(List<PointF> dataPoints) {
            for (PointF dataPoint : dataPoints) {
                coordinatePlaneView.addDataPoint(dataPoint.x, dataPoint.y);
            }
            coordinatePlaneView.invalidate();
        }
    }
    private class DeleteAllDataAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            DataPointDbHelper dbHelper = new DataPointDbHelper(getContext());
            dbHelper.deleteAllDataPoints();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            List<PointF> dataPoints = new ArrayList<>();
            coordinatePlaneView.setDataPoints(dataPoints);
            coordinatePlaneView.invalidate();
            Toast.makeText(getContext(), "All data points deleted.", Toast.LENGTH_SHORT).show();
        }
    }

}