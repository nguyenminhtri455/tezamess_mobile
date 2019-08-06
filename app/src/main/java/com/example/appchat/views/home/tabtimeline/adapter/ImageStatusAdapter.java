package com.example.appchat.views.home.tabtimeline.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.appchat.R;

import java.util.List;

public class ImageStatusAdapter extends RecyclerView.Adapter<ImageStatusAdapter.ViewHodel> {

    private Context context;
    private List<String> listUrl;

    public ImageStatusAdapter(Context context, List<String> listUrl) {
        this.context = context;
        this.listUrl = listUrl;
    }

    @NonNull
    @Override
    public ViewHodel onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_item_image_status, viewGroup, false);
        return new ViewHodel(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHodel viewHodel, int i) {
        String url = listUrl.get(i);

        Glide.with(context)
                .load(url)
                .placeholder(R.drawable.image40)
                .error(R.drawable.image40)
                .into(viewHodel.imgStatus);

    }

    @Override
    public int getItemCount() {
        return listUrl.size();
    }

    class ViewHodel extends RecyclerView.ViewHolder {
        private ImageView imgStatus;

        public ViewHodel(@NonNull View itemView) {
            super(itemView);
            imgStatus = itemView.findViewById(R.id.img_status);
        }


    }
}
