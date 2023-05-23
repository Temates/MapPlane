package com.example.mapplane;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PointF;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class AddGeoFragment extends Fragment {
    private CoordinatePlaneView coordinatePlaneView;
    private GeoFenceView geoFenceView;
    float x = 0;
    float y = 0;
    float xmap= 0;
    float ymap= 0;
    private EditText editText;
    PointF tempPointF;
    String location_name;

    public AddGeoFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static AddGeoFragment newInstance(String param1, String param2) {
        AddGeoFragment fragment = new AddGeoFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_geo, container, false);
        // Retrieve the session cookies from SharedPreferences
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String sessionCookiesJson = sharedPreferences.getString("sessionCookies", null); // Retrieve the JSON string
        if (sessionCookiesJson != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<String>>() {}.getType();
            List<String> sessionCookies = gson.fromJson(sessionCookiesJson, type); // Convert the JSON string to List<String>
            // Use the sessionCookies list as needed
        }

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

    public void generateDatapoint() {


        if (getArguments() != null) {
            x = getArguments().getFloat("x");
            y = getArguments().getFloat("y");
            // Inflate the layout for this fragment
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());
            executor.execute(() -> {
                //Background work here

                PointF screenPoint = coordinatePlaneView.mapToScreen(x,y);
                xmap = screenPoint.x;
                ymap = screenPoint.y;
                Log.d("MQTT", "x: "+x);
                Log.d("MQTT", "x: "+y);

                handler.post(() -> {
                    // Add current position to coordinate plane view
                    geoFenceView.addDataPoint(screenPoint.x, screenPoint.y, true);
                    geoFenceView.invalidate();

                    //UI Thread work here
                });
            });
        }


    }




    private void displaySavedData() {
        getAllDataPoints();
    }

    public void saveData() {
        location_name = editText.getText().toString();
        if (!TextUtils.isEmpty(location_name))
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
                List<ContentValues> contentValuesList = new ArrayList<>();
                for (PointF point : GeoFencedataPoints) {
                    ContentValues values = new ContentValues();
                    values.put(GeoFenceDbHelper.GeofenceDataEntry.COLUMN_NAME_GEOFENCE_ID, 1);
                    values.put(GeoFenceDbHelper.GeofenceDataEntry.COLUMN_NAME_X, point.x);
                    values.put(GeoFenceDbHelper.GeofenceDataEntry.COLUMN_NAME_Y, point.y);
                    contentValuesList.add(values);
                    db.insert(GeoFenceDbHelper.GeofenceDataEntry.TABLE_NAME, null, values);
                }
                Log.d("API", String.valueOf(contentValuesList));
                // Use an executor to send the geofence object to the Flask API
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(() -> {
                    try {
//                        String url = "http://192.168.2.6:8000/geofence/send";
                        String url = "http://192.168.4.199:8000/geofence/send";
                        URL apiURL = new URL(url);

                        // Create the connection
                        HttpURLConnection connection = (HttpURLConnection) apiURL.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setRequestProperty("Content-Type", "application/json");

                        connection.setDoOutput(true);

                        // Create the request body with the geofence object as JSON
                        // Retrieve the user_id from SharedPreferences
                        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                        String userIdJson = sharedPreferences.getString("user_id", null); // Retrieve the JSON string


                        Gson gson = new Gson();
                        String dataPointsJson = contentValuesList.toString();
                        JsonObject geofenceObject = new JsonObject();
                        geofenceObject.addProperty("nama_lokasi", location_name);
                        geofenceObject.addProperty("user_id", userIdJson);
                        geofenceObject.addProperty("dataPoints",  dataPointsJson);
                        String requestBody = geofenceObject.toString();
                        Log.d("API", String.valueOf(geofenceObject));

                        // Write the request body to the connection
                        OutputStream outputStream = connection.getOutputStream();
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                        writer.write(requestBody);
                        writer.flush();
                        writer.close();
                        outputStream.close();

                        // Send the request to the Flask API
                        int responseCode = connection.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            // Geofence object sent successfully
                            Log.d("API", "Geofence object sent successfully");
                        } else {
                            // Error sending the geofence object
                            Log.e("API", "Error sending geofence object: " + responseCode);
                        }

                        // Close the connection
                        connection.disconnect();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                // Clear the temporary PointF variable
                GeoFencedataPoints.clear();
                // Close the database
                db.close();

                    // Show a message to the user
                    Toast.makeText(getContext(), "Data saved", Toast.LENGTH_SHORT).show();
                getActivity().getSupportFragmentManager().popBackStack();
                } else {
                    Toast.makeText(getContext(), "No data to save", Toast.LENGTH_SHORT).show();
                }

            Toast.makeText(getContext(), "Geofence berhasil di simpan ", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(getContext(), "Mohon Masukan nama untuk geofence ", Toast.LENGTH_SHORT).show();
        }


        }


    public void deleteData() {
// Inflate the layout for this fragment
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            //Background work here
            List<PointF> tempPointF = new ArrayList<>();
            handler.post(() -> {
                geoFenceView.setDataPoints(tempPointF);
                geoFenceView.invalidate();
                //UI Thread work here
            });
        });

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
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        coordinatePlaneView.invalidate();

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
//            Log.d("MQTT", "x: "+x);
//            Log.d("MQTT", "y: "+y);
            // Add current position to coordinate plane view
            //Background work here
            handler.post(() -> {
            coordinatePlaneView.addDataPoint(screenPoint.x, screenPoint.y, false);
            coordinatePlaneView.invalidate();


                //UI Thread work here
            });
        });
    }

}