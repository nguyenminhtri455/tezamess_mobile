package com.example.appchat.views.home.tabmessage.editroom.adapter;

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
import com.example.appchat.objectclass.Member;
import com.example.appchat.views.home.tabmessage.editroom.EditRoomActivity;
import com.example.appchat.views.home.tabsetting.profile.ProfileActivity;
import com.example.appchat.views.profileuser.ProfileUserActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MembersRoomAdapter extends RecyclerView.Adapter<MembersRoomAdapter.ViewHodel> {

    private Context context;
    private List<Contact> contactsInRoom, contacstOnlineInRoom;
    private int creator;

    public MembersRoomAdapter(Context context, List<Contact> contactsInRoom, int creator, List<Contact> contacstOnlineInRoom) {
        this.context = context;
        this.contactsInRoom = contactsInRoom;
        this.creator = creator;
        this.contacstOnlineInRoom = contacstOnlineInRoom;
    }


    @NonNull
    @Override
    public ViewHodel onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_item_members_room, viewGroup, false);
        return new ViewHodel(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHodel viewHodel, int i) {
        Contact contact = contactsInRoom.get(i);
        Picasso.get()
                .load(contact.getUrlavatar())
                .placeholder(R.drawable.account_icon)
                .error(R.drawable.account_icon)
                .into(viewHodel.imgAvatar);
        viewHodel.txtName.setText(contact.getName());
        if (contacstOnlineInRoom.contains(contact) || contact.getId() == Member.getInstance(context).getId()) {
            viewHodel.txtOnline.setVisibility(View.VISIBLE);
        } else {
            viewHodel.txtOnline.setVisibility(View.GONE);
        }
        if (contact.getId() == creator) {
            viewHodel.imgKey.setVisibility(View.VISIBLE);
        } else {
            viewHodel.imgKey.setVisibility(View.GONE);
        }

        viewHodel.itemView.setOnClickListener(t -> {
            if (contact.getId() == Member.getInstance(context).getId()) {
                Intent intent = new Intent(context, ProfileActivity.class);
                context.startActivity(intent);
            } else {
                Intent intent = new Intent(context, ProfileUserActivity.class);
                intent.putExtra("source", "EditRoomActivity");
                intent.putExtra("contact", contact);
                ((EditRoomActivity) context).startActivityForResult(intent, EditRoomActivity.REQUEST_PROFILE_USER);
            }
        });
    }


    @Override
    public int getItemCount() {
        return contactsInRoom.size();
    }

    public class ViewHodel extends RecyclerView.ViewHolder {

        CircleImage imgAvatar, imgKey;
        TextView txtName, txtOnline;

        public ViewHodel(@NonNull View itemView) {
            super(itemView);
            imgKey = itemView.findViewById(R.id.imageview_key);
            imgAvatar = itemView.findViewById(R.id.imageview_avatar);
            txtName = itemView.findViewById(R.id.textview_name);
            txtOnline = itemView.findViewById(R.id.textview_online);
        }
    }
}
