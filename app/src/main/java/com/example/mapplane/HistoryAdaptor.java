package com.example.mapplane;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class HistoryAdaptor extends BaseAdapter {
    private String[] time;

    private OnShowButtonClickListener listener;

    public interface OnShowButtonClickListener {
        void onShowButtonClick(int position);
    }



    public HistoryAdaptor(String[] responseString,  OnShowButtonClickListener listener) {
        this.time = responseString;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        if (time == null) {
            return 0;
        }
        return time.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView show;
        Log.d("MQTT", "getView: "+ time);
        if (convertView == null) {
        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_list_layout, parent, false);        }
        show = convertView.findViewById(R.id.show_data);
        TextView idTextView = convertView.findViewById(R.id.tv1);
        idTextView.setText(String.valueOf(position+1));
        TextView timeTextView = convertView.findViewById(R.id.tv2);
        timeTextView.setText(time[position]);
        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onShowButtonClick(position);
                }
            }
        });

        return convertView;
    }
}
