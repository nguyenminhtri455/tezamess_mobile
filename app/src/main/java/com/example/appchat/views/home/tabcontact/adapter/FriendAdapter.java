package com.example.appchat.views.home.tabcontact.adapter;

import android.app.Application;
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

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {
    private List<Contact> mListCotact;
    private Context mContext;

    private Application application;

    public FriendAdapter(List<Contact> mListCotact, Context mContext) {
        this.mListCotact = mListCotact;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.custom_item_contact, null, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Contact contact = mListCotact.get(i);
        Character alphaCurrent = mListCotact.get(i).getName().toUpperCase().charAt(0);
        if (i - 1 == -1) {
            viewHolder.divider.setVisibility(View.GONE);
            viewHolder.txtAlphabet.setText(alphaCurrent.toString());
            viewHolder.txtAlphabet.setVisibility(View.VISIBLE);
        } else {
            Character alphaBefore = mListCotact.get(i - 1).getName().toUpperCase().charAt(0);
            if (alphaBefore.equals(alphaCurrent)) {
                viewHolder.txtAlphabet.setVisibility(View.INVISIBLE);
                viewHolder.divider.setVisibility(View.GONE);
            } else {
                viewHolder.txtAlphabet.setText(alphaCurrent.toString());
                viewHolder.txtAlphabet.setVisibility(View.VISIBLE);
                viewHolder.divider.setVisibility(View.VISIBLE);
            }
        }
        viewHolder.txtName.setText(mListCotact.get(i).getName());
        Picasso.get()
                .load(contact.getUrlavatar())
                .placeholder(R.drawable.account_icon)
                .error(R.drawable.account_icon)
                .into(viewHolder.imgAvatar);

        viewHolder.itemView.setOnClickListener(t -> {
            Intent intent = new Intent(mContext, DoubleRoomActivity.class);
            intent.putExtra("contact", contact);
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mListCotact.size();
    }

    public void addItem(Contact contact) {
        mListCotact.add(contact);
        notifyItemInserted(mListCotact.size() - 1);
    }

    public void removeItem(int index) {
        mListCotact.remove(index);
        notifyItemRemoved(index);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImage imgAvatar;
        TextView txtName, txtAlphabet;
        View divider;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            divider = itemView.findViewById(R.id.divider);
            imgAvatar = itemView.findViewById(R.id.imageview_avatar);
            txtName = itemView.findViewById(R.id.textview_name);
            txtAlphabet = itemView.findViewById(R.id.alpha_bet);
        }
    }
}
