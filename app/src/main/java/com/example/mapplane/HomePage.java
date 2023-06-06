package com.example.mapplane;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomePage extends Fragment {

    SQLiteDatabase sqLiteDatabase;
    DataPointDbHelper dataPointDbHelper;
    private FragmentManager fragmentManager;
    String name[];
    int id[];

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_page, container, false);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        dataPointDbHelper = new DataPointDbHelper(getContext());
        Button logout = view.findViewById(R.id.logout);
        fragmentManager = requireActivity().getSupportFragmentManager();
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignOut();
            }
        });
        Button crtmap = view.findViewById(R.id.addmap);
        crtmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createMapFragment();
            }
        });

        displaylist(view);
        // Inflate the layout for this fragment
        return view;
    }

    private void createMapFragment() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment createmapFragment= new CreateMap();
        fragmentTransaction.replace(R.id.fragment_container, createmapFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void displaylist(View view) {
       try {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                sqLiteDatabase = dataPointDbHelper.getReadableDatabase();
                Cursor cursor = sqLiteDatabase.rawQuery("select * from maps",null);
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
        HomePageAdaptor adapter = new HomePageAdaptor(fragmentManager,name,id);
        ListView listView = view.findViewById(R.id.lv1);
        listView.setAdapter(adapter);
    }


    private void SignOut() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        try {
            String urlString = "http://192.168.185.12:8000/logout";
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            String token = sharedPreferences.getString("token", null);
            JSONObject requestBody = new JSONObject();
            requestBody.put("token", token);
            String requestBodyString = requestBody.toString();
            Executor executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());
            executor.execute(() -> {
                try {
                    OutputStream outputStream = connection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    writer.write(requestBodyString);
                    writer.flush();
                    writer.close();
                    outputStream.close();
                    int responseCode = connection.getResponseCode();
                    InputStream inputStream;
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        inputStream = connection.getInputStream();
                    } else {
                        inputStream = connection.getErrorStream();
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder responseBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                    reader.close();
                    String responseString = responseBuilder.toString();
                    Toast.makeText(getContext(), "Log Out!", Toast.LENGTH_SHORT).show();
                    Log.d("Auth", "Response: " + responseString);
                    getActivity().runOnUiThread(() -> {
                    });
                    // Close the connection
                    connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                    getActivity().runOnUiThread(() -> {
                    });
                }
                handler.post(() -> {
                    Toast.makeText(getContext(), "Log Out!", Toast.LENGTH_SHORT).show();
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        editor.clear();
        editor.apply();
        startActivity(new Intent(getActivity(), Auth.class));
        getActivity().finish();
    }

}