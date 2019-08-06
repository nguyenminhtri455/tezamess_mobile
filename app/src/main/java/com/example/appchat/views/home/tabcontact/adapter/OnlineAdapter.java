package com.example.appchat.views.home.tabcontact.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.appchat.R;
import com.example.appchat.customview.CircleImage;
import com.example.appchat.objectclass.Contact;
import com.example.appchat.views.home.tabmessage.roomchat.DoubleRoomActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

public class OnlineAdapter extends RecyclerView.Adapter<OnlineAdapter.ViewHolder> {
    private List<Contact> mListCotact;
    private Context mContext;

    public OnlineAdapter(List<Contact> mListCotact, Context mContext) {
        this.mListCotact = mListCotact;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.custom_item_online, null, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.txtName.setText(mListCotact.get(i).getName());
        Picasso.get()
                .load(mListCotact.get(i).getUrlavatar())
                .placeholder(R.drawable.account_icon)
                .error(R.drawable.account_icon)
                .into(viewHolder.imgAvatar);

        viewHolder.itemView.setOnClickListener(t -> {
            Intent intent = new Intent(mContext, DoubleRoomActivity.class);
            intent.putExtra("contact", mListCotact.get(i));
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mListCotact.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImage imgAvatar;
        TextView txtName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imageview_avatar);
            txtName = itemView.findViewById(R.id.textview_name);
        }
    }
}
