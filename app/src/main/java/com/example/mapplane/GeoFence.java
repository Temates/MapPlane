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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class GeoFence extends Fragment {
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    GeoFenceDbHelper geoDB;

    ListView listView;

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
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());


        //pull refresh
        View view = inflater.inflate(R.layout.fragment_geo_fence,container,false);
        final SwipeRefreshLayout pullToRefresh = view.findViewById(R.id.pullToRefresh);
        Button addGeo = view.findViewById(R.id.addgeofence);
        addGeo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addGeofence(view);
            }
        });
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {

                        //Background work here
                        Bundle bundle = getArguments();
                        if (bundle != null) {
                            String email = bundle.getString("email");
                            String name = bundle.getString("name");

                            // use the data here
                            try {
//                                Reqtable(email,name);
                                ArrayList<String> names = geoDB.getGeofenceNames();
                                CustomAdapter adapter = new CustomAdapter(names);
                                ListView listView = view.findViewById(R.id.lv1);
                                listView.setAdapter(adapter);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                //UI Thread work here
                            }
                        });
                    }
                });
            }
        });

        return view;
    }


    private void addGeofence(View view) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment addgeoFragment = new AddGeoFragment();
        fragmentTransaction.replace(R.id.fragment_container, addgeoFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

//    private void Reqtable(String email, String name) throws Exception {
//
//        String url = "http://192.168.0.9:3000/list_user/";
//        URL obj = new URL(url);
//        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
//
//        con.setRequestMethod("GET");
//        con.setRequestProperty("User-Agent", "Mozilla/5.0");
//        int responseCode = con.getResponseCode();
//        con.connect();
//        BufferedReader in = new BufferedReader(
//                new InputStreamReader(con.getInputStream())
//        );
//        String input;
//        StringBuilder response = new StringBuilder();
//        while ((input=in.readLine())!=null){
//            response.append(input);
//        }
//        in.close();
//        Log.d("data", response.toString());
//        JSONObject jObj = new JSONObject(response.toString());
//        //JSONObject jAttend = jObj.getJSONObject("attend");
//        JSONArray myArray = jObj.getJSONArray("checkin");
//
//        //  JSONArray myArray = new JSONArray(response.toString());
//        ArrayList<String> resultin = new ArrayList<>();
//        if (myArray.length() == 0) {
//            resultin.add("Check In:");
//            resultin.add("\n");
//        }
//        else {
//            resultin.add("Check In:");
//            for (int i = 0; i < myArray.length(); i++) {
//                JSONObject arrObj = myArray.getJSONObject(i);
//                System.out.println("DATA : " + myArray);
//                //String No_attend = arrObj.getString("No");
//                String Date_in = arrObj.getString("Date");
//                String Time_in = arrObj.getString("Time");
//                //String Data = "No. Absen: "+No_attend+"\n"+"Tanggal: "+Date_attend+ "\n"+ "Jam: " + Time_attend;
//                String Data = "Tanggal: " + Date_in + "\n" + "Jam: " + Time_in;
//                resultin.add(Data);
//                //            result.add(arrObj.getString("Date_attend"));
//                //            result.add(arrObj.getString("Time_attend"));
//                //            result.add(arrObj.getString("No"));
////            result.add(arrObj.getString("Date_attend"));
////            result.add(arrObj.getString("Time_attend"));
//                //result.add(arrObj.getString("title"));
//            }
//
//        }
//
//        ArrayList<String> resultout = new ArrayList<>();
//        JSONArray Arr = jObj.getJSONArray("checkout");
//        if (Arr.length() == 0) {
//            resultout.add("Check Out:");
//            resultout.add("\n");
//        }else{
//            System.out.println("DATA : " + Arr);
//            resultout.add("Check Out:");
//            for (int i = 0; i < Arr.length(); i++) {
//                JSONObject arrObje = Arr.getJSONObject(i);
//                System.out.println("DATA : " + Arr);
//                //String No_salary = arrObje.getString("No");
//                String Date_out = arrObje.getString("Date");
//                String Time_out = arrObje.getString("Time");
//                //String Data = "No. Gaji: "+No_salary+"\n"+"Tanggal: "+Date_salary+ "\n"+ "Jam: " + Time_salary;
//                String Data = "Tanggal: " + Date_out + "\n" + "Jam: " + Time_out;
//                resultout.add(Data);
////            result.add(arrObj.getString("Date_attend"));
////            result.add(arrObj.getString("Time_attend"));
////            result.add(arrObj.getString("No"));
////            result.add(arrObj.getString("Date_attend"));
////            result.add(arrObj.getString("Time_attend"));
//                //result.add(arrObj.getString("title"));
//
//            }
//        }
//
//        //System.out.println(result_salary);
//        MyData = resultin.toArray(new String[0]);
//        otherData = resultout.toArray(new String[0]);
//
//        runOnUiThread(new Runnable() {
//
//            @Override
//            public void run() {
//
//                CustomAdapter cAdapter =
//                        new CustomAdapter(getApplicationContext(),MyData);
//                CustomAdapter bAdapter =
//                        new CustomAdapter(getApplicationContext(),otherData);
//                mylv.setAdapter(cAdapter);
//                mylv1.setAdapter(bAdapter);
//            }
//        });
//
////        mylv = (ListView) findViewById(R.id.lv1);
////        CustomAdapter cAdapter =
////                new CustomAdapter(getApplicationContext(),MyData);
////        mylv.setAdapter(cAdapter);
//
////        return MyData + otherData;
//    }
}