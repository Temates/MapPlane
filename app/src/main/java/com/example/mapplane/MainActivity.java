package com.example.mapplane;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;



import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements MqttCallback {

    MqttClient client = null;
    double sum = 0;
    int count = 0;
    private CoordinatePlaneView coordinatePlaneView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        coordinatePlaneView = findViewById(R.id.coordinate_plane_view);


        // Add button to add random data point
        Button calibrate = findViewById(R.id.calibrate);
        calibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateRandomData();
            }
        });

        Button saveDataButton = findViewById(R.id.savedatapoint);
        saveDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<PointF> dataPoints = coordinatePlaneView.getDataPoints();
                for (PointF dataPoint : dataPoints) {
                    insertDataPoint(dataPoint.x, dataPoint.y);
                }
                Toast.makeText(MainActivity.this, "Data saved!", Toast.LENGTH_SHORT).show();
            }
        });



//
//        String clientId = MqttClient.generateClientId();
//
//        try {
//            client = new MqttClient("tcp://172.22.48.108:1883", clientId, new MemoryPersistence());
//            client.setCallback(this);
//            client.connect();
//            client.subscribe("esp32/tes", 2);
//            Log.d("MQTT", "subs");
//            Thread.sleep(5000);
//
//
//        } catch (MqttException | InterruptedException e) {
//            Log.d("MQTT", "Error");
//            e.printStackTrace();
//        }
    }
    private void generateRandomData() {
        // Generate random data points
        Random random = new Random();
        float x = random.nextFloat() * coordinatePlaneView.getWidth();
        float y = random.nextFloat() * coordinatePlaneView.getHeight();
        coordinatePlaneView.addDataPoint(x, y);

    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.d("MQTT", "connectionLost: " + cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
//        Log.d("MQTT", topic);
        String payload = new String(message.getPayload());
//        Log.d("MQTT", payload);
        if ( topic.equals("esp32/tes")) {
            double value = Double.parseDouble(new String(message.getPayload()));
            sum += value;
            count++;
//            Log.d("MQTT", payload);
            if (count == 10) {
                double average = sum / count;
                Log.d("MQTT", "Average value: " + average);

            }
        }

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}