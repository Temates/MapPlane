package com.example.mapplane;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class GeoFence extends Fragment {

    GeoFenceDbHelper geoDB;
    SQLiteDatabase sqLiteDatabase;
    String name[];
    int id[];
    AddGeoFragment addGeoFragment;
    String datapointId;
    showMapFragment showMapFragment;

    public static GeoFence newInstance(String datapointId) {
        GeoFence fragment = new GeoFence();
        Bundle args = new Bundle();
        args.putString("datapointId", datapointId);
        fragment.setArguments(args);
        return fragment;
    }
    public GeoFence() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        geoDB = new GeoFenceDbHelper(getContext());
        View view = inflater.inflate(R.layout.fragment_geo_fence,container,false);
        Button addGeo = view.findViewById(R.id.addgeofence);
        if (getArguments() != null) {
            datapointId = getArguments().getString("datapointId");
        }
        addGeo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    addGeofence(datapointId);
            }
        });
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
        displaylist(view);
        return view;
    }

    public void displaylist(View view) {
        try {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
        @Override
        public void run() {
            sqLiteDatabase = geoDB.getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("select * from geofence where Maps_id=?",new String[]{datapointId});
            if (cursor.getCount()>0){
                id = new int[cursor.getCount()];
                name = new String[cursor.getCount()];
                int i = 0;
                while (cursor.moveToNext()){
                    id[i] = cursor.getInt(0);
                    name[i] = cursor.getString(1);
                    i++;
                }
                cursor.close();
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dis(view);
                }
            });
        }
        });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dis(View view){
        CustomAdapter adapter = new CustomAdapter(name,id);
        ListView listView = view.findViewById(R.id.lv1);
        listView.setAdapter(adapter);
    }

    private void addGeofence(String datapointId) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        addGeoFragment = addGeoFragment.newInstance(datapointId);
        fragmentTransaction.replace(R.id.fragment_container, addGeoFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        geoDB.close();
    }


}