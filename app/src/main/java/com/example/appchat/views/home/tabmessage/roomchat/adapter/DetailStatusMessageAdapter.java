package com.example.appchat.views.home.tabmessage.roomchat.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.appchat.R;
import com.example.appchat.customview.CircleImage;
import com.example.appchat.objectclass.Contact;
import com.squareup.picasso.Picasso;

import java.util.List;

public class DetailStatusMessageAdapter extends RecyclerView.Adapter<DetailStatusMessageAdapter.ViewHodel> {

    private List<Contact> contacts;
    private Context context;


    public DetailStatusMessageAdapter(List<Contact> contacts, Context context) {
        this.contacts = contacts;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHodel onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_item_detail_message, viewGroup, false);
        ViewHodel viewHodel = new ViewHodel(view);
        return viewHodel;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHodel viewHodel, int i) {
        Contact contact = contacts.get(i);
        viewHodel.txtName.setText(contact.getName());
        Picasso.get()
                .load(contact.getUrlavatar())
                .placeholder(R.drawable.account_icon)
                .error(R.drawable.account_icon)
                .into(viewHodel.imgAvatar);
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    class ViewHodel extends RecyclerView.ViewHolder {
        private TextView txtName;
        private CircleImage imgAvatar;

        public ViewHodel(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.textview_name);
            imgAvatar = itemView.findViewById(R.id.imageview_avatar);
        }
    }
}
