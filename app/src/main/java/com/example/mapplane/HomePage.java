package com.example.mapplane;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomePage extends Fragment {
    private CoordinatePlaneView coordinatePlaneView;
    private GeofencePlot geofencePlot;
    GoogleSignInClient gsc;
    GoogleSignInOptions gso;

    FirebaseAuth firebaseAuth;

    private List<PointF> currentPositionPoints = new ArrayList<>();
    private Context context;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }
    public void setGoogleSignInClient(GoogleSignInClient googleSignInClient) {
        this.gsc = googleSignInClient;
    }


    public HomePage(Context context) {
        this.context = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_home_page, container, false);
        // Get a reference to the CoordinatePlaneView
        coordinatePlaneView = view.findViewById(R.id.coordinate_plane_view);
//        geoFenceView = view.findViewById(R.id.geofence_view);
        geofencePlot = view.findViewById(R.id.geofence_plot);
        Button logout = view.findViewById(R.id.logout);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        gsc = GoogleSignIn.getClient(getActivity(), gso);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignOut();
            }
        });

        displaySavedData();



        // Inflate the layout for this fragment
        return view;
    }

    private void SignOut() {
        FirebaseAuth.getInstance().signOut();
        gsc.signOut().addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // Google Sign-In account cache cleared
                // Proceed with signing in a new user
                startActivity(new Intent(getActivity(), Auth.class));
                getActivity().finish();
            }
        });

    }

    public void displaySavedData() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            DataPointDbHelper dbHelper = new DataPointDbHelper(getContext());
            List<PointF> dataPoints = dbHelper.getAllDataPoints();
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


    public void displayGeofenceData(List<String> geofenceIds){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            GeoFenceDbHelper geoFenceDbHelper = new GeoFenceDbHelper(context);
            ArrayList<String> names = geoFenceDbHelper.getGeofenceNames(); // Retrieve geofence names from the database
            handler.post(() -> {
                JSONArray mainArray = new JSONArray();
                for (String geofenceId : geofenceIds) {
                    List<PointF> geofencePointsFromDb = geoFenceDbHelper.getAllGeofencePoints(geofenceId);
                    JSONArray pointsArray = new JSONArray();
                    for (PointF point : geofencePointsFromDb) {
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
                        geofenceObject.put("points", pointsArray);
                        mainArray.put(geofenceObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                geoFenceDbHelper.close();

                geofencePlot.setGeofenceData(mainArray.toString());
                geofencePlot.setGeofenceNames(names);
                geofencePlot.invalidate();
            });
        });
    }


    public void updateData(float x, float y){
        // Inflate the layout for this fragment
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            //Background work here
            PointF screenPoint = coordinatePlaneView.mapToScreen(x, y);
//            Log.d("MQTT", "x: "+x);
//            Log.d("MQTT", "x: "+y);
            // Add current position to coordinate plane view
            coordinatePlaneView.addDataPoint(screenPoint.x, screenPoint.y, false);
            coordinatePlaneView.invalidate();
            handler.post(() -> {
                //UI Thread work here
            });
        });
    }

}