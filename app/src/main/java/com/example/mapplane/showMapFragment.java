package com.example.mapplane;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.Button;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class showMapFragment extends Fragment implements MqttCallback {
    private CoordinatePlaneView coordinatePlaneView;
    private GeofencePlot geofencePlot;
    GeoFence geofenceFragment;
    HistoryFragment historyFragment;
    MqttClient client = null;
    String topics;
    float x;
    float y;

    String datapointId;
    private Context context;
    public static showMapFragment newInstance(String datapointId) {
        showMapFragment fragment = new showMapFragment();
        Bundle args = new Bundle();
        args.putString("datapointId", datapointId);
        fragment.setArguments(args);
        return fragment;
    }
    public showMapFragment() {

        // Required empty public constructor
    }

    public static showMapFragment newInstance(String param1, String param2) {
        showMapFragment fragment = new showMapFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_map, container, false);
        // Inflate the layout for this fragment
        // Get a reference to the CoordinatePlaneView
        coordinatePlaneView = view.findViewById(R.id.coordinate_plane_view);
        geofencePlot = view.findViewById(R.id.geofence_plot);
        if (getArguments() != null) {
            datapointId = getArguments().getString("datapointId");
            displaySavedData(datapointId);
            displayGeofenceData(datapointId);
            connectmqtt(datapointId);
        }
        Button backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the previous fragment
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                fragmentManager.popBackStack();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        Button geofencebtn = view.findViewById(R.id.geofence);
        geofencebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    geofenceFragment(datapointId);
            }
        });
        Button historybtn = view.findViewById(R.id.history);
        historybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    historyFragment(datapointId);

            }
        });
        Button clearbtn = view.findViewById(R.id.clear);
        clearbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<PointF> dataPoints = new ArrayList<>();
                coordinatePlaneView.setHistoryPoint(dataPoints);
                coordinatePlaneView.invalidate();

            }
        });

        return view;
    }


    private void geofenceFragment(String datapointId) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        geofenceFragment = geofenceFragment.newInstance(datapointId);
        fragmentTransaction.replace(R.id.fragment_container, geofenceFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
    private void historyFragment(String datapointId) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        historyFragment = historyFragment.newInstance(datapointId);
        fragmentTransaction.replace(R.id.fragment_container, historyFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }



    public void displaySavedData(String datapointId) {
        if (datapointId != null) {
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
                    coordinatePlaneView.invalidate();
                });
            });
        }
    }

    public void displayGeofenceData(String datapointId){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            GeoFenceDbHelper geoFenceDbHelper = new GeoFenceDbHelper(getContext());
            ArrayList<String> names = geoFenceDbHelper.getGeofenceNames(datapointId);
            List<List<PointF>> geofencePointsList = geoFenceDbHelper.getAllGeofencePointsByMapId(datapointId);
            JSONArray mainArray = new JSONArray();
            for (int i = 0; i < names.size(); i++) {
                String geofenceName = names.get(i);
                List<PointF> geofencePoints = geofencePointsList.get(i);
                JSONArray pointsArray = new JSONArray();
                for (PointF point : geofencePoints) {
                    JSONObject pointObject = new JSONObject();
                    try {
                        pointObject.put("x", point.x);
                        pointObject.put("y", point.y);
                        pointsArray.put(pointObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
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


    public void updateData(float x, float y){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            PointF screenPoint = coordinatePlaneView.mapToScreen(x, y);
                coordinatePlaneView.addHistoryPoint(screenPoint.x,screenPoint.y);
            handler.post(() -> {
                coordinatePlaneView.invalidate();
            });
        });
    }

    public void connectmqtt(String datapointId){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            DataPointDbHelper dbHelper = new DataPointDbHelper(getContext());
            topics = dbHelper.getTopics(datapointId);
            dbHelper.close();
            String clientId = MqttClient.generateClientId();
            try {
                client = new MqttClient("tcp://broker.hivemq.com:1883", clientId, new MemoryPersistence());
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