package com.example.mapplane;

import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.Arrays;

public class CustomAdapter extends BaseAdapter {

    private String[] name;
    SQLiteDatabase sqLiteDatabase;
    private int[] id;

    public CustomAdapter(String[] name,int[] id) {
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
        ImageView delete;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_layout, parent, false);
        }
        GeoFenceDbHelper geoFenceDbHelper = new GeoFenceDbHelper(convertView.getContext());

        TextView nameTextView = convertView.findViewById(R.id.tv1);
        nameTextView.setText(name[position]);


        delete = convertView.findViewById(R.id.delete_data);
        View finalConvertView = convertView;
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sqLiteDatabase = geoFenceDbHelper.getReadableDatabase();
                Toast.makeText(finalConvertView.getContext(), "id:"+id[position], Toast.LENGTH_SHORT).show();

                long record = sqLiteDatabase.delete(GeoFenceDbHelper.GeofenceEntry.TABLE_NAME, GeoFenceDbHelper.GeofenceEntry._ID + "=?",new String[]{String.valueOf(id[position])});
                if (record != -1){
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
                notifyDataSetChanged(); // Refresh the view
            }
        });

        return convertView;
    }
}
