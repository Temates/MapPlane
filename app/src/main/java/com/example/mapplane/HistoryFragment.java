package com.example.mapplane;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.PointF;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryFragment extends Fragment implements HistoryAdaptor.OnShowButtonClickListener {
    private CoordinatePlaneView coordinatePlaneView;
    private GeofencePlot geofencePlot;
    private List<PointF> currentPositionPoints = new ArrayList<>();
    private Context context;
    String sTime, eTime, Date;
    String datapointId;
    showMapFragment showMapFragment;
    String responseString = new String();






    public HistoryFragment() {
        // Required empty public constructor
    }

    public static HistoryFragment newInstance(String datapointId) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putString("datapointId", datapointId);
        fragment.setArguments(args);
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
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        coordinatePlaneView = view.findViewById(R.id.coordinate_plane_view);
        geofencePlot = view.findViewById(R.id.geofence_plot);
        if (getArguments() != null) {
            datapointId = getArguments().getString("datapointId");
            Log.d("MQTT", "datapoint: "+datapointId);
        }
        Button startimepicker = view.findViewById(R.id.starttime);
        startimepicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        getContext(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                sTime = String.format(Locale.getDefault(), "%02d:%02d:00", hourOfDay, minute);
                                startimepicker.setText(sTime);
                            }
                        },
                        0, 0, true
                );
                timePickerDialog.show();
            }
        });

        Button endtimepicker = view.findViewById(R.id.endtime);
        endtimepicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        getContext(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                eTime = String.format(Locale.getDefault(), "%02d:%02d:00", hourOfDay, minute);
                                endtimepicker.setText(eTime);
                            }
                        },
                        0, 0, true
                );
                timePickerDialog.show();
            }
        });
        Button datePickerButton = view.findViewById(R.id.date);
        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                Date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year,monthOfYear + 1 ,dayOfMonth );
                                datePickerButton.setText(Date);
                            }
                        },
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH),
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                );
                datePickerDialog.show();
            }
        });
        if (getArguments() != null) {
            String datapointId = getArguments().getString("datapointId");
            displaySavedData(datapointId);
            displayGeofenceData(datapointId);
        }
        Button back = view.findViewById(R.id.back_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the previous fragment
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                showMapFragment = showMapFragment.newInstance(datapointId);
                Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.remove(currentFragment); // Remove the current showMapFragment
                fragmentTransaction.replace(R.id.fragment_container, showMapFragment); // Replace with the new instance
                fragmentTransaction.commit();
            }
        });
        Button getdata = view.findViewById(R.id.start);
        getdata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(!Date.isEmpty() && !sTime.isEmpty() && !eTime.isEmpty()){
                    callApi(view);


//                }
            }
        });

        return view;
    }
    private void callApi(View view) {
        try {
            // Construct the request URL
            String urlString = "http://192.168.185.12:8000/history";
            URL url = new URL(urlString);
            // Create the connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            // Retrieve the user_id from SharedPreferences
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            String token = sharedPreferences.getString("token", null); // Retrieve the JSON string
            JSONObject requestBody = new JSONObject();
            requestBody.put("datetimestart", Date + " " + sTime);
            requestBody.put("datetimeend", Date + " " + eTime);
            requestBody.put("token", token);
            String requestBodyString = requestBody.toString();
            // Execute the network operation on a background thread
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                try {
                    // Write the request body to the connection
                    OutputStream outputStream = connection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    writer.write(requestBodyString);
                    writer.flush();
                    writer.close();
                    outputStream.close();

                    // Get the response from the connection
                    int responseCode = connection.getResponseCode();
                    InputStream inputStream;
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        inputStream = connection.getInputStream();
                    } else {
                        inputStream = connection.getErrorStream();
                    }
                    // Read the response from the input stream
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder responseBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                    reader.close();
                    responseString = responseBuilder.toString();
                    if (responseString.equals("token invalid")){
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.apply();
                        Toast.makeText(getActivity(), "Sesi telah habis silahkan melakukan Login kembali!", Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                        Intent intent = new Intent(getActivity(),Auth.class);
                        startActivity(intent);
                    }
                    // Add a debug statement to check the response
                    Log.d("History", "Response: " + responseString);
                    getActivity().runOnUiThread(() -> {
                        // Update UI or perform actions on the main thread
                        if(responseString != null && !responseString.isEmpty()) {
                            displaylist(view);
                            displayAllUserLocation(parseResponseJson(responseString));
                        }
                    });
                    // Close the connection
                    connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                    getActivity().runOnUiThread(() -> {
                        // Show a toast or perform actions on the main thread
//                        Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
//            Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
        }
//        responseString = "[ { \"datetime\": \"Sun, 28 May 2023 08:02:06 GMT\", \"x\": 5.00841, \"y\": 3.61537 }, { \"datetime\": \"Sun, 28 May 2023 08:07:07 GMT\", \"x\": 4.23861, \"y\": 0.0311318 }, { \"datetime\": \"Sun, 28 May 2023 08:12:07 GMT\", \"x\": 2.91463, \"y\": 5.86138 }, { \"datetime\": \"Sun, 28 May 2023 08:17:07 GMT\", \"x\": 1.93774, \"y\": 5.10751 }, { \"datetime\": \"Sun, 28 May 2023 08:22:07 GMT\", \"x\": 4.91448, \"y\": 1.85806 }, { \"datetime\": \"Sun, 28 May 2023 08:27:07 GMT\", \"x\": 4.5551, \"y\": 4.54656 } ]";


    }

    private ArrayList<PointF> parseResponseJson(String responseString) {
        ArrayList<PointF> dataPoints = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(responseString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                float x = (float) jsonObject.getDouble("x");
                float y = (float) jsonObject.getDouble("y");
                PointF point = new PointF(x, y);
                dataPoints.add(point);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("MQTT", String.valueOf(dataPoints));
        return dataPoints;
    }
    public String[] extractDatetimeValues(String responseString) {
        try {
            JSONArray jsonArray = new JSONArray(responseString);
            String[] datetimeArray = new String[jsonArray.length()];

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String datetime = jsonObject.getString("datetime");
                datetimeArray[i] = datetime;
            }
            return datetimeArray;
        } catch (JSONException e) {
            e.printStackTrace();
            return new String[0];
        }
    }

    public void displayUserLocation(ArrayList<PointF> datapoints, int position){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            handler.post(() -> {
                if (position >= 0 && position < datapoints.size()) {
                    Log.d("MQTT", "displayUserLocation: "+position);
                    PointF dataPoint = datapoints.get(position);
                    PointF screenPoint = coordinatePlaneView.mapToScreen(dataPoint.x, dataPoint.y);
                    coordinatePlaneView.addDataPoint(screenPoint.x, screenPoint.y,false);
                    coordinatePlaneView.invalidate();
                }
            });
        });
    }

    public void displayGeofenceData(String datapointId){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            GeoFenceDbHelper geoFenceDbHelper = new GeoFenceDbHelper(getContext());
            ArrayList<String> names = geoFenceDbHelper.getGeofenceNames(datapointId); // Retrieve geofence names from the database
            // Retrieve geofence points for the specific map
            List<List<PointF>> geofencePointsList = geoFenceDbHelper.getAllGeofencePointsByMapId(datapointId);
            // Create a JSONArray to hold the main array of geofences
            JSONArray mainArray = new JSONArray();
            for (int i = 0; i < names.size(); i++) {
                String geofenceName = names.get(i);
                List<PointF> geofencePoints = geofencePointsList.get(i);
                // Create a JSONArray to hold the points of the current geofence
                JSONArray pointsArray = new JSONArray();
                for (PointF point : geofencePoints) {
                    // Create a JSONObject for each point
                    JSONObject pointObject = new JSONObject();
                    try {
                        pointObject.put("x", point.x);
                        pointObject.put("y", point.y);
                        pointsArray.put(pointObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                // Create a JSONObject for the current geofence
                JSONObject geofenceObject = new JSONObject();
                try {
                    geofenceObject.put("name", geofenceName);
                    geofenceObject.put("points", pointsArray);
                    mainArray.put(geofenceObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            geoFenceDbHelper.close();
            handler.post(() -> {
                geofencePlot.setGeofenceData(mainArray.toString());
                geofencePlot.setGeofenceNames(names);
                geofencePlot.invalidate();
            });
        });
    }

    public void displaySavedData(String datapointId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            DataPointDbHelper dbHelper = new DataPointDbHelper(getContext());
            List<PointF> dataPoints = dbHelper.getSpecificDataPoints(datapointId);
            Log.d("MQTT", "ini debug"+String.valueOf(dataPoints));
            dbHelper.close();
            handler.post(() -> {
                for (PointF dataPoint : dataPoints) {
                    coordinatePlaneView.addDataPoint(dataPoint.x, dataPoint.y, true);
                }
                coordinatePlaneView.invalidate();
            });
        });
    }
    public void displaylist(View view) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                //Background work here
                String[] datetimeArray = extractDatetimeValues(responseString);
                dis(datetimeArray);
            }
        });
    }
    public void dis(String[] datetimeArray){
        HistoryAdaptor adapter = new HistoryAdaptor(datetimeArray,this::onShowButtonClick);
        View view = getView();
        if (view != null) {
            ListView listView = view.findViewById(R.id.lv1);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listView.setAdapter(adapter);
                }
            });
        }
    }
    @Override
    public void onShowButtonClick(int position) {
        displayUserLocation(parseResponseJson(responseString),position);
    }

    public void displayAllUserLocation(ArrayList<PointF> datapoints){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            for (int i = 0; i < datapoints.size(); i++) {
                final int currentPosition = i;
                handler.postDelayed(() -> {
                    if (currentPosition >= 0 && currentPosition < datapoints.size()) {
                        PointF dataPoint = datapoints.get(currentPosition);
                        PointF screenPoint = coordinatePlaneView.mapToScreen(dataPoint.x, dataPoint.y);
                        coordinatePlaneView.addHistoryPoint(screenPoint.x, screenPoint.y);
                        coordinatePlaneView.invalidate();
                    }}, currentPosition * 1000);}});
    }
}