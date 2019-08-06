package com.example.appchat.views.home.tabsetting.language;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.appchat.R;

public class LanguageAdapter extends ArrayAdapter {

    Context context;
    int resource;
    String[] lans;

    public LanguageAdapter(Context context, int resource,String[] lans) {
        super(context, resource);
        this.context = context;
        this.resource = resource;
        this.lans = lans;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Object getItem(int position) {
        return lans[position];
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(resource, parent, false);


        ImageView imgLan = view.findViewById(R.id.imageview_language);
        TextView tvLan = view.findViewById(R.id.textview_language);
        tvLan.setText(lans[position]);
        if(position == 0){
            imgLan.setImageResource(R.drawable.icon_english);
        }else{
            imgLan.setImageResource(R.drawable.icon_vietnamese);
        }


        return view;
    }
}
