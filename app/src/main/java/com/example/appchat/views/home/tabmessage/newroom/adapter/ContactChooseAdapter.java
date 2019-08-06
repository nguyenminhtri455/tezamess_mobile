package com.example.appchat.views.home.tabmessage.newroom.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.appchat.R;
import com.example.appchat.customview.CircleImage;
import com.example.appchat.objectclass.Contact;
import com.example.appchat.views.home.tabmessage.newroom.NewRoomActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ContactChooseAdapter extends RecyclerView.Adapter<ContactChooseAdapter.ViewHodel> {

    private Context context;
    private List<Contact> contactsChoose;
    private NewRoomActivity newRoomActivity;

    public ContactChooseAdapter(Context context, List<Contact> contactsChoose) {
        this.context = context;
        this.contactsChoose = contactsChoose;
        newRoomActivity = (NewRoomActivity) context;
    }

    @NonNull
    @Override
    public ViewHodel onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_item_choose_contact, viewGroup, false);
        return new ViewHodel(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHodel viewHodel, int i) {
        Contact contact = contactsChoose.get(i);
        viewHodel.txtName.setText(contact.getName());
        Picasso.get()
                .load(contact.getUrlavatar())
                .placeholder(R.drawable.account_icon)
                .error(R.drawable.account_icon)
                .into(viewHodel.imgAvatar);

        viewHodel.imgRemoveContact.setOnClickListener(t -> {
            newRoomActivity.removeContactChoose(contact);
            newRoomActivity.updateContactsHint(contact);
        });
    }

    @Override
    public int getItemCount() {
        return contactsChoose.size();
    }

    public class ViewHodel extends RecyclerView.ViewHolder {

        CircleImage imgAvatar;
        ImageButton imgRemoveContact;
        TextView txtName;

        public ViewHodel(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imageview_avatar);
            txtName = itemView.findViewById(R.id.textview_name_contact);
            imgRemoveContact = itemView.findViewById(R.id.img_remove_contact);
        }
    }
}
