package com.example.mapplane;

import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//
///**
// * A simple {@link Fragment} subclass.
// * Use the {@link HomePage#newInstance} factory method to
// * create an instance of this fragment.
// */
public class HomePage extends Fragment {
    private CoordinatePlaneView coordinatePlaneView;
    private List<PointF> dataPoints;
    MqttClient client = null;
    double sum = 0;
    int count = 0;
    float val1 =0;
    float val2 =0;
    float x = 0;
    float y = 0;
    private List<PointF> currentPositionPoints = new ArrayList<>();
    private float mScale = 50.0f;

    // TODO: Rename parameter arguments, choose names that match
//    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;

    public HomePage() {
        // Required empty public constructor
    }

//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment HomePage.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static HomePage newInstance(String param1, String param2) {
//        HomePage fragment = new HomePage();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_home_page, container, false);
        // Get a reference to the CoordinatePlaneView
        coordinatePlaneView = view.findViewById(R.id.coordinate_plane_view);
        displaySavedData();

        // Inflate the layout for this fragment
        return view;
    }

    private void displaySavedData() {

        GetAllDataAsyncTask getAllDataAsyncTask = new GetAllDataAsyncTask();
        getAllDataAsyncTask.execute();
    }




    //asyntask plot map

    private class GetAllDataAsyncTask extends AsyncTask<Void, Void, List<PointF>> {

        @Override
        protected List<PointF> doInBackground(Void... voids) {
            DataPointDbHelper dbHelper = new DataPointDbHelper(getContext());
            List<PointF> dataPoints = dbHelper.getAllDataPoints();
            dbHelper.close();
            return dataPoints;
        }

        @Override
        protected void onPostExecute(List<PointF> dataPoints) {
            for (PointF dataPoint : dataPoints) {
                coordinatePlaneView.addDataPoint(dataPoint.x, dataPoint.y,true);
            }
            // Add any current position data points to the coordinate plane view
            for (PointF point : currentPositionPoints) {
                coordinatePlaneView.addDataPoint(point.x, point.y, false);
            }
            coordinatePlaneView.invalidate();
        }

    }

    public void updateData(float x, float y){
        // Inflate the layout for this fragment
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            //Background work here
            PointF screenPoint = coordinatePlaneView.mapToScreen(x, y);
            Log.d("MQTT", "x: "+x);
            Log.d("MQTT", "x: "+y);
            // Add current position to coordinate plane view
            coordinatePlaneView.addDataPoint(screenPoint.x, screenPoint.y, false);
            coordinatePlaneView.invalidate();
            handler.post(() -> {



                //UI Thread work here
            });
        });
    }

}