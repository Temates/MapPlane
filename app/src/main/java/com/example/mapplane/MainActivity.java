package com.example.mapplane;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.snatik.polygon.Point;
import com.snatik.polygon.Polygon;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements MqttCallback {
    CoordinatePlaneView coordinatePlaneView;
    GeoFenceDbHelper geoDB;
    MqttClient client = null;
    BottomNavigationView bottomNavigation;
    int i = 0;
    private boolean[] geofenceConditions;
    private boolean[] insideAnyGeofence;
    private boolean[] isInsideGeofence;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        coordinatePlaneView = findViewById(R.id.coordinate_plane_view);
        connectmqtt();
        geoDB = new GeoFenceDbHelper(this);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        HomePage homePage = new HomePage();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homePage).commit();
        List<String> mapIds;
        DataPointDbHelper dataPointDbHelper = new DataPointDbHelper(this);
        mapIds = dataPointDbHelper.getAllMapIds();
        geofenceConditions = new boolean[mapIds.size()];
        isInsideGeofence = new boolean[mapIds.size()];
        insideAnyGeofence = new boolean[mapIds.size()];
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                switch (item.getItemId()){
                    case R.id.home_page:
                        selectedFragment = homePage;
                        break;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                return true;
            }
        });
    }
    private void showNotification(String title, String message) {
        // Create an intent for the MainActivity (or any other activity you want to open when the notification is clicked)
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        // Create a notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_id")
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_baseline_notification_icon)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSound(soundUri); // Set the notification sound
        // Show the notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("channel_id", "Channel Name", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String channelId = "Your_channel_id";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }
        notificationManager.notify(0, builder.build());
    }
    private void checkCoordinateInsideGeofences(float x, float y, String mapId) {
        // Retrieve geofence data from the database based on mapId
        List<Polygon> polygons = getGeofencePolygonsFromDatabase(mapId);
        List<String> mapIds;
        DataPointDbHelper dataPointDbHelper = new DataPointDbHelper(this);
        mapIds = dataPointDbHelper.getAllMapIds();
        String mapName = dataPointDbHelper.getMapNameById(mapId);
        // Create a Point object for the coordinate
        Point point = new Point(x, y);
        // Check if the coordinate is inside any of the geofences
        int mapIdIndex = mapIds.indexOf(mapId); // Find the index of the mapId in the mapIds list
        insideAnyGeofence[mapIdIndex] = false;
        int geofenceIndex = 0;
        for (int i = 0; i < polygons.size(); i++) {
            Polygon polygon = polygons.get(i);
            if (polygon.contains(point)) {
                insideAnyGeofence[mapIdIndex] = true;
                geofenceIndex = i;
                break;
            }
        }
        List<String> geofenceNames = geoDB.getGeofenceNamesByMapId(mapId);
        // Check geofence status and show appropriate notification
        if (insideAnyGeofence[mapIdIndex] && !isInsideGeofence[mapIdIndex]) {
            // The coordinate entered the geofence
            isInsideGeofence[mapIdIndex] = true;
            String geofenceName = geofenceNames.get(geofenceIndex);
            showNotification("Geofence Entered", "Peta: " + mapName + " || Geofence: " + geofenceName);
            String message = "Lansia sedang berada di:" + geofenceName;
            // Perform any actions you need when the coordinate enters a geofence
        } else if (!insideAnyGeofence[mapIdIndex] && isInsideGeofence[mapIdIndex]) {
            // The coordinate left the geofence
            isInsideGeofence[mapIdIndex] = false;
            // showNotification("Geofence Exited", "You leave the geofence");
            // Perform any actions you need when the coordinate exits a geofence
        }
    }


    private List<Polygon> getGeofencePolygonsFromDatabase(String mapId) {
        List<Polygon> polygons = new ArrayList<>();
        List<List<PointF>> geofencePointsFromDb = geoDB.getAllGeofencePointsByMapId(mapId);

        if (geofencePointsFromDb == null || geofencePointsFromDb.isEmpty()) {
            return polygons;
        }

        for (List<PointF> points : geofencePointsFromDb) {
            if (points.size() < 3) {
                continue;
            }

            Polygon.Builder builder = new Polygon.Builder();
            for (PointF point : points) {
                builder.addVertex(new Point(point.x, point.y));
            }
            Polygon polygon = builder.build();
            polygons.add(polygon);
        }

        return polygons;
    }

    public void connectmqtt(){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            DataPointDbHelper dataPointDbHelper = new DataPointDbHelper(this);
            List<String> mapIds = dataPointDbHelper.getAllMapIds();
            String clientId = MqttClient.generateClientId();
            try {
                client = new MqttClient("tcp://broker.hivemq.com:1883", clientId, new MemoryPersistence());
                client.setCallback(this);
                client.connect();
                for (String mapId : mapIds) {
                    String topics = dataPointDbHelper.getTopics(mapId);
                    if (topics != null) {
                        client.subscribe(topics, 0);
                    }
                }
                Log.d("MQTT", "Subscriptions successful");
            } catch (MqttException e) {
                Log.d("MQTT", "Error: " + e.getMessage());
                e.printStackTrace();
            }
            handler.post(() -> {
            });
        });
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        List<String> mapIds ;
        synchronized (this) {
            DataPointDbHelper dataPointDbHelper = new DataPointDbHelper(this);
            mapIds = dataPointDbHelper.getAllMapIds();
        }

        ExecutorService executor = Executors.newFixedThreadPool(mapIds.size());

        for (String mapId : mapIds) {
            final String currentMapId = mapId;
            executor.execute(() -> {
                DataPointDbHelper dataPointDbHelper = new DataPointDbHelper(MainActivity.this);
                float x = 0;
                float y = 0;
                String topics = dataPointDbHelper.getTopics(currentMapId); // Use the local variable

                if (topics != null && topics.equals(topic)) {
                    String payload = new String(message.getPayload());
                    try {
                        JSONObject json = new JSONObject(payload);
                        x = (float) json.getDouble("x");
                        y = (float) json.getDouble("y");
                        Log.d("MQTT", "message" + mapId+" :"+x);
                    } catch (NumberFormatException | JSONException e) {
                        // Handle parsing error if needed
                    }
                }
                PointF screenPoint = coordinatePlaneView.mapToScreen(x, y);

                checkCoordinateInsideGeofences(screenPoint.x, screenPoint.y, mapId); // Use the local variable
                dataPointDbHelper.close();
            });
        }

        executor.shutdown();
    }


    @Override
    public void connectionLost(Throwable cause) {
        Log.d("MQTT", "connectionLost: " + cause.getMessage());
        connectmqtt();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

}