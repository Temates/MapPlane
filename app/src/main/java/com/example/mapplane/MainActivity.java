package com.example.mapplane;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements MqttCallback {
    CoordinatePlaneView coordinatePlaneView;
    GeoFenceDbHelper geoDB;
    SQLiteDatabase sqLiteDatabase;
    MqttClient client = null;
    BottomNavigationView bottomNavigation;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    float val1=0;
    float val2=0;
    String name[];
    int id[];
    List<String> geofenceNames;
    List<String> geofenceIds;
    private boolean isInsideGeofence = false; // Flag to track geofence status

    @Override   
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        coordinatePlaneView = findViewById(R.id.coordinate_plane_view);


        //google auth for getting data
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(this,gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        String Name = account.getDisplayName();
        String Email = account.getEmail();
        Bundle bundle = new Bundle();
        bundle.putString("email", Email); // replace "key" and "value" with your own data
        bundle.putString("name", Name); // replace "key" and "value" with your own data
        connectmqtt();

        geoDB = new GeoFenceDbHelper(this);
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                // Perform long-running operation in background thread
                // getting geofence data into array list to loop
                sqLiteDatabase = geoDB.getReadableDatabase();
                Cursor cursor = sqLiteDatabase.rawQuery("select * from geofence",null);
                geofenceIds = new ArrayList<>();
                geofenceNames = new ArrayList<>();
                if (cursor.getCount()>0){
                    id = new int[cursor.getCount()];
                    name = new String[cursor.getCount()];
                    int i = 0;
                    while (cursor.moveToNext()){
                        id[i] = cursor.getInt(0);
                        name[i] = cursor.getString(1);
                        geofenceIds.add(String.valueOf(id[i]));
                        geofenceNames.add(name[i]);
                        i++;
                    }

                    cursor.close();


                }
                // Update UI on the main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Update UI elements with new data
                        // ...
                        switchToHomePageFragment(geofenceIds);
                    }
                });
            }
        });

        bottomNavigation = findViewById(R.id.bottom_navigation);
//      menjalankan fragment default
        HomePage homePage = new HomePage(this);
        homePage.setGoogleSignInClient(gsc);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homePage).commit();

        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                switch (item.getItemId()){
                    case R.id.edit_page:
                        selectedFragment = new EditMap();
                        selectedFragment.setArguments(bundle);
                        break;
                    case R.id.home_page:
                        selectedFragment = homePage;
                        selectedFragment.setArguments(bundle);
                        homePage.setGoogleSignInClient(gsc);


                        Executor executor = Executors.newSingleThreadExecutor();
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                // Perform long-running operation in background thread
                                // getting geofence data into array list to loop
                                sqLiteDatabase = geoDB.getReadableDatabase();
                                Cursor cursor = sqLiteDatabase.rawQuery("select * from geofence",null);
                                List<String> geofenceIds = new ArrayList<>();
                                List<String> geofenceNames = new ArrayList<>();
                                if (cursor.getCount()>0){
                                    id = new int[cursor.getCount()];
                                    name = new String[cursor.getCount()];
                                    int i = 0;
                                    while (cursor.moveToNext()){
                                        id[i] = cursor.getInt(0);
                                        name[i] = cursor.getString(1);
                                        geofenceIds.add(String.valueOf(id[i]));
                                        geofenceNames.add(name[i]);

                                        i++;
                                    }
                                    Log.d("MQTT", String.valueOf(geofenceIds));

                                    cursor.close();


                                }
                                // Update UI on the main thread
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Update UI elements with new data
                                        // ...
                                        switchToHomePageFragment(geofenceIds);
                                    }
                                });
                            }
                        });
                        break;
                    case R.id.geofence_page:
                        selectedFragment = new GeoFence();
                        selectedFragment.setArguments(bundle);
                        break;

                }

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                return true;
            }
        });

    }
    public void connectmqtt(){
        // Inflate the layout for this fragment
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            //Background work here
            String clientId = MqttClient.generateClientId();
//        String clientId = RandomClientIdGenerator.generateRandomClientId();

            try {
                client = new MqttClient("tcp://broker.hivemq.com:1883", clientId, new MemoryPersistence());
//            client = new MqttClient("tcp://192.168.183.12:1883", clientId, new MemoryPersistence());
                client.setCallback(this);
                client.connect();
                client.subscribe("nandox", 0);
                client.subscribe("nandoy", 0);
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

    // Define the method to check if the coordinate is inside any geofence
    private void checkCoordinateInsideGeofences(float x, float y) {
        // Retrieve geofence data from the database
        List<Polygon> polygons = getGeofencePolygonsFromDatabase();

        // Create a Point object for the coordinate
        Point point = new Point(x, y);
        // Check if the coordinate is inside any of the geofences
        boolean insideAnyGeofence = false;
//        // Iterate over the geofences and check if the coordinate is inside any of them
//        for (int i = 0; i < polygons.size(); i++) {
//            Polygon polygon = polygons.get(i);
//            if (polygon.contains(point)) {
//                // The coordinate is inside the geofence
//                String geofenceName = geofenceNames.get(i);
//                Log.d("Geofence", "Coordinate is inside geofence: " + geofenceName);
//                showNotification("Geofence Alert", "Lansia sedang berada di: " + geofenceName);
//                // Perform any actions you need when the coordinate is inside a geofence
//            }
//        }
        int geofenceIndex = 0;
            for (int i = 0; i < polygons.size(); i++) {
                Polygon polygon = polygons.get(i);
                if (polygon.contains(point)) {
                    insideAnyGeofence = true;
                    geofenceIndex = i;
                    break;
                }
            }

            // Check geofence status and show appropriate notification
            if (insideAnyGeofence && !isInsideGeofence) {
                // The coordinate entered the geofence
                isInsideGeofence = true;
                String geofenceName = geofenceNames.get(geofenceIndex);
                showNotification("Geofence Entered", "Lansia sedang berada di: " + geofenceName);
                // Perform any actions you need when the coordinate enters a geofence
            } else if (!insideAnyGeofence && isInsideGeofence) {
                // The coordinate left the geofence
                isInsideGeofence = false;
//                showNotification("Geofence Exited", "You leave the geofence");
                // Perform any actions you need when the coordinate exits a geofence
            }
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

    // Retrieve geofence data from the database and create polygons
    private List<Polygon> getGeofencePolygonsFromDatabase() {
        List<Polygon> polygons = new ArrayList<>();
        GeoFenceDbHelper geoFenceDbHelper = new GeoFenceDbHelper(this);

        for (String geofenceId : geofenceIds) {
            // Retrieve geofence points from the database based on geofenceId
            List<PointF> geofencePointsFromDb = geoFenceDbHelper.getAllGeofencePoints(geofenceId);

            // Create a polygon using the retrieved points
            Polygon.Builder builder = new Polygon.Builder();
            for (PointF point : geofencePointsFromDb) {
                builder.addVertex(new Point(point.x, point.y));
            }
            Polygon polygon = builder.build();
            polygons.add(polygon);
        }
//        Log.d("Geofence", "Coordinate is inside geofence: " + polygons);


        return polygons;
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        if ( topic.equals("nandoy")) {
            String payload2 = new String(message.getPayload());
            try {
                val2 = Float.parseFloat(payload2);
//                Log.d("MQTT", "y: "+val2);
            } catch (NumberFormatException e) {
//                Log.e("MQTT", "Error parsing float value from payload: " + payload2, e);
            }
        }
        if ( topic.equals("nandox")) {
            String payload1 = new String(message.getPayload());
            try {
                val1 = Float.parseFloat(payload1);
//                Log.d("MQTT", "x: "+val1);
            } catch (NumberFormatException e) {
//                Log.e("MQTT", "Error parsing float value from payload: " + payload1, e);
            }
        }

        float x = val1;
        float y = val2;

        // Call the method to check if the coordinate is inside any geofence
        PointF screenPoint = coordinatePlaneView.mapToScreen(x, y);
        checkCoordinateInsideGeofences(screenPoint.x, screenPoint.y);
        // Get a reference to the HomePage fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (fragment instanceof HomePage) {
            HomePage homePageFragment = (HomePage) fragment;
            // Call the updateCoordinates method with the new coordinates
            homePageFragment.updateData(x, y);
        }
        if (fragment instanceof EditMap) {
            EditMap EditMapFragment = (EditMap) fragment;
            if (EditMapFragment != null) {
                    // Update the map
                Bundle bundle = new Bundle();
                bundle.putFloat("x", x);
                bundle.putFloat("y", y);
                EditMapFragment.setArguments(bundle);
                    EditMapFragment.updateData(x, y);

            }
        }

        if (fragment instanceof AddGeoFragment) {
            AddGeoFragment addgeoFragment = (AddGeoFragment) fragment;
            if (addgeoFragment != null) {
                    // Update the map
                Bundle bundle = new Bundle();
                bundle.putFloat("x", x);
                bundle.putFloat("y", y);
                addgeoFragment.setArguments(bundle);
                    addgeoFragment.updateData(x, y);

            }
        }

    }


    @Override
    public void connectionLost(Throwable cause) {
        Log.d("MQTT", "connectionLost: " + cause.getMessage());
        connectmqtt();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
    public void switchToHomePageFragment(List<String> geofenceIds) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        HomePage homePageFragment = new HomePage(this);
            homePageFragment.displayGeofenceData(geofenceIds);
        fragmentTransaction.replace(R.id.fragment_container, homePageFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


}