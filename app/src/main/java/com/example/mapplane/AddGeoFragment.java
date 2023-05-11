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
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddGeoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddGeoFragment extends Fragment {
    private CoordinatePlaneView coordinatePlaneView;
    private GeoFenceView geoFenceView;
    float x = 0;
    float y = 0;
    float xmap= 0;
    float ymap= 0;
    boolean isCalibrated = false;
    private EditText editText;
    PointF tempPointF;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AddGeoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddGeoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddGeoFragment newInstance(String param1, String param2) {
        AddGeoFragment fragment = new AddGeoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_geo, container, false);
        // Get a reference to the CoordinatePlaneView
        coordinatePlaneView = view.findViewById(R.id.coordinate_plane_view);
        geoFenceView = view.findViewById(R.id.geofence_view);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                displaySavedData();
            }
        });
        // Initialize temporary PointF variable
        tempPointF = new PointF();

        editText = view.findViewById(R.id.geofence_name);
        String location_name = editText.getText().toString();
        // Add button to add random data point
        Button calibrate = view.findViewById(R.id.calibrate);
        calibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                generateDatapoint();
            }
        });

        // Set up the "Save Data" button
        Button saveDataButton = view.findViewById(R.id.savedatapoint);
        saveDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                saveData(location_name);
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

    public void generateDatapoint() {
        isCalibrated = true;
    }



    public void updateCalibrationData(float x, float y) {

        // Inflate the layout for this fragment
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            PointF screenPoint = coordinatePlaneView.mapToScreen(x,y);
            Log.d("MQTT", "x: "+x);
            Log.d("MQTT", "x: "+y);
            // Add current position to coordinate plane view
            geoFenceView.addDataPoint(screenPoint.x, screenPoint.y, true);
            geoFenceView.invalidate();
            tempPointF.set(screenPoint.x,screenPoint.y);
            isCalibrated = false;
            //Background work here
            handler.post(() -> {


                //UI Thread work here
            });
        });
    }

    private void displaySavedData() {
        getAllDataPoints();
    }

    public void saveData(String location_name) {
        if (location_name != null )
        {
            if (tempPointF != null) {
                List<PointF> GeoFencedataPoints = geoFenceView.getDataPoints();
                // Create an instance of the GeofenceContract class
                GeoFenceDbHelper geofenceContract = new GeoFenceDbHelper(getContext());
                // Get a writable database
                SQLiteDatabase db = geofenceContract.getWritableDatabase();
                // Iterate over the data points and insert them into the database
                ContentValues geofenceValues = new ContentValues();
                geofenceValues.put(GeoFenceDbHelper.GeofenceEntry.COLUMN_NAME_NAME, location_name);
                long geofenceId = db.insert(GeoFenceDbHelper.GeofenceEntry.TABLE_NAME, null, geofenceValues);
                Log.d("MQTT", "jalan");

                for (PointF point : GeoFencedataPoints) {
                    ContentValues values = new ContentValues();
                    values.put(GeoFenceDbHelper.GeofenceDataEntry.COLUMN_NAME_GEOFENCE_ID, geofenceId);
                    values.put(GeoFenceDbHelper.GeofenceDataEntry.COLUMN_NAME_X, point.x);
                    values.put(GeoFenceDbHelper.GeofenceDataEntry.COLUMN_NAME_Y, point.y);
                    db.insert(GeoFenceDbHelper.GeofenceDataEntry.TABLE_NAME, null, values);

                }
                // Clear the temporary PointF variable
                GeoFencedataPoints.clear();
                // Close the database
                db.close();

                    // Show a message to the user
                    Toast.makeText(getContext(), "Data saved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "No data to save", Toast.LENGTH_SHORT).show();
                }

            Toast.makeText(getContext(), "Geofence berhasil di simpan ", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(getContext(), "Mohon Masukan nama untuk geofence ", Toast.LENGTH_SHORT).show();
        }


        }


    public void deleteData() {
        tempPointF = null;

    }

    //maps
    private void getAllDataPoints() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                DataPointDbHelper dbHelper = new DataPointDbHelper(getContext());
                List<PointF> dataPoints = dbHelper.getAllDataPoints();
                dbHelper.close();
                for (PointF dataPoint : dataPoints) {
                    coordinatePlaneView.addDataPoint(dataPoint.x, dataPoint.y, true);
                }
                coordinatePlaneView.invalidate();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        });

        executorService.shutdown();
    }

    public void updateData(float x, float y){
        // Inflate the layout for this fragment
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            PointF screenPoint = coordinatePlaneView.mapToScreen(x,y);
            xmap = screenPoint.x;
            ymap = screenPoint.y;
            Log.d("MQTT", "x: "+x);
            Log.d("MQTT", "y: "+y);
            // Add current position to coordinate plane view
            coordinatePlaneView.addDataPoint(screenPoint.x, screenPoint.y, false);
            coordinatePlaneView.invalidate();
            //Background work here
            handler.post(() -> {


                //UI Thread work here
            });
        });
    }

}