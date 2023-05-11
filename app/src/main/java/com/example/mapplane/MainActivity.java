package com.example.mapplane;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

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
    BottomNavigationView bottomNavigation;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    float val1=0;
    float val2=0;


    @Override   
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectmqtt();

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







        bottomNavigation = findViewById(R.id.bottom_navigation);
//      menjalankan fragment default
      getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomePage()).commit();

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
                        selectedFragment = new HomePage();
                        selectedFragment.setArguments(bundle);
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

    }


    @Override
    public void connectionLost(Throwable cause) {
        Log.d("MQTT", "connectionLost: " + cause.getMessage());
//        connectmqtt();


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
                if (EditMapFragment.isCalibrated) {
                    // Update the calibration data
                    EditMapFragment.updateCalibrationData(x, y);
                } else {
                    // Update the map
                    EditMapFragment.updateData(x, y);
                }
            }
        }

        if (fragment instanceof AddGeoFragment) {
            AddGeoFragment addgeoFragment = (AddGeoFragment) fragment;
            if (addgeoFragment != null) {
                if (addgeoFragment.isCalibrated) {
                    // Update the calibration data
                    addgeoFragment.updateCalibrationData(x, y);
                } else {
                    // Update the map
                    addgeoFragment.updateData(x, y);
                }
            }



        }



    }


    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

}