package com.example.mapplane;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PointF;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

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

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

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


public class AddGeoFragment extends Fragment implements MqttCallback {
    private CoordinatePlaneView coordinatePlaneView;
    private GeoFenceView geoFenceView;
    MqttClient client = null;
    String topics;
    float x = 0;
    float y = 0;
    float xmap= 0;
    float ymap= 0;
    private EditText editText;
    PointF tempPointF;
    String location_name;
    String datapointId;
    private List<PointF> currentPositionPoints = new ArrayList<>();
    showMapFragment showMapFragment;

    public static AddGeoFragment newInstance(String datapointId) {
        AddGeoFragment fragment = new AddGeoFragment();
        Bundle args = new Bundle();
        args.putString("datapointId", datapointId);
        fragment.setArguments(args);
        return fragment;
    }

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
        if (getArguments() != null) {
            datapointId = getArguments().getString("datapointId");
            connectmqtt(datapointId);
            Log.d("MQTT", "datapoint: "+datapointId);
        }
        // Get a reference to the CoordinatePlaneView
        coordinatePlaneView = view.findViewById(R.id.coordinate_plane_view);
        geoFenceView = view.findViewById(R.id.geofence_view);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                    displaySavedData(datapointId);
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
                saveData(datapointId);
            }
        });

        // Set up the "Delete Data" button
        Button clear = view.findViewById(R.id.clear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteData();
            }
        });// Set up the "Back" button
        Button back = view.findViewById(R.id.back_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the previous fragment
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                showMapFragment = showMapFragment.newInstance(datapointId);
                fragmentManager.popBackStack();
            }
        });
        return view;
    }

    public void generateDatapoint() {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());
            executor.execute(() -> {
                PointF screenPoint = coordinatePlaneView.mapToScreen(x,y);
                xmap = screenPoint.x;
                ymap = screenPoint.y;
                Log.d("MQTT", "x: "+x);
                Log.d("MQTT", "y: "+y);
                handler.post(() -> {
                    geoFenceView.addDataPoint(screenPoint.x, screenPoint.y, true);
                    geoFenceView.invalidate();
                });
            });
    }
    public void displaySavedData(String datapointId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            DataPointDbHelper dbHelper = new DataPointDbHelper(getContext());
            List<PointF> dataPoints = dbHelper.getSpecificDataPoints(datapointId);
            dbHelper.close();
            handler.post(() -> {
                for (PointF dataPoint : dataPoints) {
                    coordinatePlaneView.addDataPoint(dataPoint.x, dataPoint.y, true);
                }
                for (PointF point : currentPositionPoints) {
                    coordinatePlaneView.addDataPoint(point.x, point.y, false);
                }
                coordinatePlaneView.invalidate();
            });
        });
    }
    public void saveData(String datapointId) {
        location_name = editText.getText().toString();
        if (!TextUtils.isEmpty(location_name))
        {
            if (tempPointF != null) {
                List<PointF> GeoFencedataPoints = geoFenceView.getDataPoints();
                GeoFenceDbHelper geofenceContract = new GeoFenceDbHelper(getContext());
                SQLiteDatabase db = geofenceContract.getWritableDatabase();
                ContentValues geofenceValues = new ContentValues();
                geofenceValues.put(GeoFenceDbHelper.GeofenceEntry.COLUMN_NAME_NAME, location_name);
                geofenceValues.put(GeoFenceDbHelper.GeofenceEntry.COLUMN_NAME_MAPS_ID, datapointId);
                long geofenceId = db.insert(GeoFenceDbHelper.GeofenceEntry.TABLE_NAME, null, geofenceValues);
                List<ContentValues> contentValuesList = new ArrayList<>();
                for (PointF point : GeoFencedataPoints) {
                    ContentValues values = new ContentValues();
                    values.put(GeoFenceDbHelper.GeofenceDataEntry.COLUMN_NAME_GEOFENCE_ID, geofenceId);
                    values.put(GeoFenceDbHelper.GeofenceDataEntry.COLUMN_NAME_X, point.x);
                    values.put(GeoFenceDbHelper.GeofenceDataEntry.COLUMN_NAME_Y, point.y);
                    contentValuesList.add(values);
                    db.insert(GeoFenceDbHelper.GeofenceDataEntry.TABLE_NAME, null, values);
                }
                GeoFencedataPoints.clear();
                db.close();
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



    public void updateData(float x, float y){
        // Inflate the layout for this fragment
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            PointF screenPoint = coordinatePlaneView.mapToScreen(x,y);
            xmap = screenPoint.x;
            ymap = screenPoint.y;
            handler.post(() -> {
            coordinatePlaneView.addDataPoint(screenPoint.x, screenPoint.y, false);
            coordinatePlaneView.invalidate();
                //UI Thread work here
            });
        });
    }

    public void connectmqtt(String datapointId){
        // Inflate the layout for this fragment
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            //Background work here
            DataPointDbHelper dbHelper = new DataPointDbHelper(getContext());
            topics = dbHelper.getTopics(datapointId);
            dbHelper.close();
            String clientId = MqttClient.generateClientId();
            Log.d("MQTT", "Paho: "+clientId);
            try {
                client = new MqttClient("tcp://broker.hivemq.com:1883", clientId, new MemoryPersistence());
//                client = new MqttClient("tcp://192.168.185.12:1883", clientId, new MemoryPersistence());
                client.setCallback(this);
                client.connect();
                client.subscribe(topics, 0);
                Log.d("MQTT", "subs");
                Thread.sleep(5000);
            } catch (MqttException | InterruptedException e) {
                Log.d("MQTT", "Error");
                e.printStackTrace();
            }
            handler.post(() -> {
                //UI Thread work here
            });
        });
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        if ( topic.equals(topics)) {
            String payload = new String(message.getPayload());
            try {
                JSONObject json = new JSONObject(payload);
                x = (float) json.getDouble("x");
                y = (float) json.getDouble("y");
                updateData(x, y);
            } catch (NumberFormatException e) {}
        }
    }
    @Override
    public void connectionLost(Throwable cause) {
        connectmqtt(datapointId);
    }



    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}