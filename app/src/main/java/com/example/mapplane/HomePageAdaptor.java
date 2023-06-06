package com.example.mapplane;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.Arrays;

public class HomePageAdaptor extends BaseAdapter {

    SQLiteDatabase sqLiteDatabase;
    SQLiteDatabase geoDatabase;
    private String[] name;
    private int[] id;
    private FragmentManager fragmentManager; // FragmentManager object
    showMapFragment showMapFragment;

    public HomePageAdaptor(FragmentManager fragmentManager,String[] name, int[] id) {
        this.fragmentManager = fragmentManager;
        this.name = name;
        this.id = id; // initialize the id array
    }

    @Override
    public int getCount() {
        if (name == null) {
            return 0;
        }
        return name.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void updateData(String[] name, int[] id) {
        this.name = name;
        this.id = id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView delete, show;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.homepage_list_layout, parent, false);        }
        DataPointDbHelper dataPointDbHelper = new DataPointDbHelper(convertView.getContext());
        GeoFenceDbHelper geoFenceDbHelper = new GeoFenceDbHelper(convertView.getContext());
        TextView nameTextView = convertView.findViewById(R.id.tv1);
        nameTextView.setText(name[position]);
        delete = convertView.findViewById(R.id.delete_data);
        show = convertView.findViewById(R.id.show_data);
        View finalConvertView = convertView;
        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String datapointId = String.valueOf(id[position]);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                showMapFragment = showMapFragment.newInstance(datapointId);
                fragmentTransaction.replace(R.id.fragment_container, showMapFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sqLiteDatabase = dataPointDbHelper.getReadableDatabase();
                geoDatabase = geoFenceDbHelper.getReadableDatabase();
//                Toast.makeText(finalConvertView.getContext(), "id:" + id[position], Toast.LENGTH_SHORT).show();
                long record = sqLiteDatabase.delete(DataPointDbHelper.MapEntry.TABLE_NAME, DataPointDbHelper.MapEntry._ID + "=?", new String[]{String.valueOf(id[position])});
                if (record != -1) {
                    long delrecord = sqLiteDatabase.delete((DataPointDbHelper.DataPointEntry.TABLE_NAME), DataPointDbHelper.DataPointEntry.COLUMN_NAME_MAP_ID + "=?", new String[]{String.valueOf(id[position])});
                    long delgeorecord = geoDatabase.delete((GeoFenceDbHelper.GeofenceEntry.TABLE_NAME), GeoFenceDbHelper.GeofenceEntry.COLUMN_NAME_MAPS_ID+"=?", new String[]{String.valueOf(id[position])});
                    if (delrecord != -1) {
                        long delgeodatarecord = geoDatabase.delete((GeoFenceDbHelper.GeofenceDataEntry.TABLE_NAME), GeoFenceDbHelper.GeofenceDataEntry.COLUMN_NAME_GEOFENCE_ID+"=?", new String[]{String.valueOf(delgeorecord)});
                        if(delgeodatarecord != -1){
                        Toast.makeText(finalConvertView.getContext(), "Data deleted successfully", Toast.LENGTH_SHORT).show();
                        // Remove the deleted item from your data source
                        ArrayList<String> nameList = new ArrayList<>(Arrays.asList(name));
                        nameList.remove(position);
                        name = nameList.toArray(new String[nameList.size()]);
                        int[] newId = new int[id.length - 1];
                        int j = 0;
                        for (int i = 0; i < id.length; i++) {
                            if (i != position) {
                                newId[j++] = id[i];
                            }
                        }
                        id = newId;
                        // Update the adapter data and notify the change
                        updateData(name, id);
                        notifyDataSetChanged();
                        }
                    } else {
                        Log.d("MQTT", "onClick: Failed to delete data ");
//                        Toast.makeText(finalConvertView.getContext(), "Failed to delete data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("MQTT", "onClick: Failed to delete data ");
//                    Toast.makeText(finalConvertView.getContext(), "Failed to delete data", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return convertView;
    }

}
