package com.example.appchat.views.home.tabtimeline.poststatus.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.appchat.R;

import java.util.List;

public class PreviewImageAdapter extends RecyclerView.Adapter<PreviewImageAdapter.ViewHodel> {

    private Context context;
    private List<Uri> listUrisImage;


    public PreviewImageAdapter(Context context, List<Uri> listUrisImage) {
        this.context = context;
        this.listUrisImage = listUrisImage;
    }

    @NonNull
    @Override
    public ViewHodel onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_item_image_status, viewGroup, false);
        return new ViewHodel(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHodel viewHodel, int i) {
        Uri uri = listUrisImage.get(i);
        Glide.with(context)
                .load(uri)
                .error(R.drawable.image40)
                .into(viewHodel.imgContent);

        viewHodel.imgContent.setOnClickListener(t -> {
            listUrisImage.remove(i);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return listUrisImage.size();
    }

    class ViewHodel extends RecyclerView.ViewHolder {
        private ImageView imgContent;

        public ViewHodel(@NonNull View itemView) {
            super(itemView);
            imgContent = itemView.findViewById(R.id.img_status);
        }
    }
}
