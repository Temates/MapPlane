package com.example.mapplane;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {

    private ArrayList<String> names;

    public CustomAdapter(ArrayList<String> names) {
        this.names = names;
    }


    @Override
    public int getCount() {
        return names.size();
    }

    @Override
    public Object getItem(int position) {
        return names.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position ;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_layout, parent, false);
        }

        TextView nameTextView = convertView.findViewById(R.id.tv1);
        String name = (String) getItem(position);
        nameTextView.setText(name);
        return convertView;
    }
}
