package com.example.mapplane;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PointF;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateMap extends Fragment implements MqttCallback {
    private CoordinatePlaneView coordinatePlaneView;
    float xmap= 0;
    float ymap= 0;
    String maps_name;
    MqttClient client = null;
    float x = 0;
    float y = 0;
    String topics;

    private EditText editText, topic;
    public CreateMap() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_create_map, container, false);
        // Get a reference to the CoordinatePlaneView
        coordinatePlaneView = view.findViewById(R.id.coordinate_plane_view);
        editText = view.findViewById(R.id.maps_name);
        topic = view.findViewById(R.id.topic_name);
        topic.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (text.contains(" ")) {
                    text = text.replaceAll(" ", "");
                    topic.setText(text);
                    topic.setSelection(text.length());
                }
            }
        });
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
        Button back = view.findViewById(R.id.back_button);
        back.setOnClickListener(new View.OnClickListener() {
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
        Button connect = view.findViewById(R.id.connect_button);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the previous fragment

                topics = topic.getText().toString();
                connectmqtt(topics);

            }
        });
        return view;
    }

    public void connectmqtt(String topics){
        // Inflate the layout for this fragment
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            //Background work here
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
                Log.d("MQTT", "Error"+e);
                e.printStackTrace();
            }
            handler.post(() -> {
                //UI Thread work here
            });
        });
    }

        public void generateDatapoint() {
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
                    coordinatePlaneView.addDataPoint(screenPoint.x, screenPoint.y, true);
                    coordinatePlaneView.invalidate();
                    //UI Thread work here
                });
            });
        }


    public void saveData() {
        List<PointF> dataPoints = coordinatePlaneView.getDataPoints();
        if (!dataPoints.isEmpty()) {
            maps_name = editText.getText().toString();
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                try {
                    DataPointDbHelper dbHelper = new DataPointDbHelper(getContext());
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.beginTransaction();
                    ContentValues mapsValues = new ContentValues();
                    mapsValues.put(DataPointDbHelper.MapEntry.COLUMN_NAME_MAP_NAME, maps_name);
                    mapsValues.put(DataPointDbHelper.MapEntry.COLUMN_NAME_TOPIC_COORDINATE, topics);
                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    String user_id = sharedPreferences.getString("user_id", null); // Retrieve the JSON string
                    mapsValues.put(DataPointDbHelper.MapEntry.COLUMN_NAME_USER_ID, user_id);
                    long mapsId = db.insert(DataPointDbHelper.MapEntry.TABLE_NAME, null, mapsValues);
                    try {
                        for (PointF dataPoint : dataPoints) {
                            ContentValues values = new ContentValues();
                            values.put(DataPointDbHelper.DataPointEntry.COLUMN_NAME_MAP_ID, mapsId);
                            values.put(DataPointDbHelper.DataPointEntry.COLUMN_NAME_X, dataPoint.x);
                            values.put(DataPointDbHelper.DataPointEntry.COLUMN_NAME_Y, dataPoint.y);
                            db.insert(DataPointDbHelper.DataPointEntry.TABLE_NAME, null, values);
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
                List<PointF> dataPoints1 = new ArrayList<>();
                // Update the UI on the main thread
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "Data saved successfully", Toast.LENGTH_SHORT).show();
                        coordinatePlaneView.setDataPoints(dataPoints1);
                        coordinatePlaneView.invalidate();
                        editText.setText("");
                        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                        fragmentManager.popBackStack();
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
            });
        }else{
            Toast.makeText(getContext(), "No Datapoint", Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteData() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new DeleteAllDataRunnable());
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
        if (coordinatePlaneView != null) {
            // Inflate the layout for this fragment
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());
            executor.execute(() -> {
                //Background work here
                PointF screenPoint = coordinatePlaneView.mapToScreen(x, y);
                xmap = screenPoint.x;
                ymap = screenPoint.y;
                Log.d("MQTT", "x: " + x);
                // Add current position to coordinate plane view
                coordinatePlaneView.addDataPoint(screenPoint.x, screenPoint.y, false);
                coordinatePlaneView.invalidate();
                handler.post(() -> {
                    //UI Thread work here
                });
            });
        }
    }
    @Override
    public void connectionLost(Throwable cause) {
    connectmqtt(topics);
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
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

}