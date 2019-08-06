package com.example.appchat.views.home.tabcontact.searchfriend.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appchat.R;
import com.example.appchat.customview.CustomToast;
import com.example.appchat.objectclass.Contact;
import com.example.appchat.objectclass.Member;
import com.example.appchat.views.home.tabcontact.searchfriend.view.SearchFirendActivity;
import com.example.appchat.views.profileuser.ProfileUserActivity;
import com.example.appchat.websocket.WebSocket;
import com.example.appchat.widget.connection.CheckConnection;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHodel> {
    private List<Contact> mListContact;
    private Context context;
    private SearchFirendActivity searchFirendActivity;

    public ContactAdapter(List<Contact> mListContact, Context context) {
        this.mListContact = mListContact;
        this.context = context;
        searchFirendActivity = (SearchFirendActivity) context;
    }

    @NonNull
    @Override
    public ViewHodel onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_item_add_contact, viewGroup, false);
        ViewHodel viewHodel = new ViewHodel(view);
        return viewHodel;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHodel viewHodel, int i) {
        Contact contact = mListContact.get(i);
        viewHodel.txtName.setText(contact.getName());

        Picasso.get()
                .load(mListContact.get(i).getUrlavatar())
                .placeholder(R.drawable.account_icon)
                .error(R.drawable.account_icon)
                .into(viewHodel.imageAvatar);

        switch (contact.getmStatusAddFriend()) {
            case 1:
                viewHodel.btnAddFriend.setText(context.getResources().getString(R.string.add));
                showUINotCancel(viewHodel.btnAddFriend);
                break;
            case 2:
                viewHodel.btnAddFriend.setText(context.getResources().getString(R.string.cancel));
                showUICancel(viewHodel.btnAddFriend);
                break;
            default:
                viewHodel.btnAddFriend.setText(context.getResources().getString(R.string.add));
                showUINotCancel(viewHodel.btnAddFriend);
                break;
        }

        viewHodel.imageAvatar.setOnClickListener(t -> {
            Intent intent = new Intent(searchFirendActivity, ProfileUserActivity.class);
            intent.putExtra("source", "Search");
            intent.putExtra("contact", contact);
            searchFirendActivity.startActivity(intent);
        });


        viewHodel.btnAddFriend.setOnClickListener(t -> {
            switch (contact.getmStatusAddFriend()) {
                case 1:
                    if (CheckConnection.haveNetworkConnection(context)) {
                        if (WebSocket.stompClient != null) {
                            if (WebSocket.stompClient.isConnected()) {
                                Map<String, Object> map = new HashMap<>();
                                map.put("idRequest", Member.getInstance(context).getId());
                                map.put("idFriend", mListContact.get(i).getId());
                                Disposable success = WebSocket.stompClient
                                        .send("/chat/addfriend", new Gson().toJson(map))
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(
                                                () -> {
                                                    Log.d("BBBBB", "success");
                                                    searchFirendActivity.showProgressBar(true);
                                                },
                                                onError -> Log.d("BBBBB", onError.getMessage())
                                        );
                                searchFirendActivity.compositeDisposable.add(success);
                            } else {
                                CustomToast.makeText(context, context.getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            CustomToast.makeText(context, context.getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        CustomToast.makeText(context, context.getResources().getString(R.string.notification_noconnection), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 2:
                    if (CheckConnection.haveNetworkConnection(context)) {
                        if (WebSocket.stompClient != null) {
                            if (WebSocket.stompClient.isConnected()) {
                                Map<String, Object> mapCancel = new HashMap<>();
                                mapCancel.put("idRequest", Member.getInstance(context).getId());
                                mapCancel.put("idFriend", contact.getId());
                                Disposable disposable = WebSocket.stompClient
                                        .send("/chat/cancel/request.addfriend", new Gson().toJson(mapCancel))
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(
                                                () -> {
                                                    Log.d("BBBBB", "success");
                                                    searchFirendActivity.showProgressBar(true);
                                                },
                                                onError -> Log.d("BBBBB", onError.getMessage())
                                        );
                                searchFirendActivity.compositeDisposable.add(disposable);
                            } else {
                                CustomToast.makeText(context, context.getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            CustomToast.makeText(context, context.getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        CustomToast.makeText(context, context.getResources().getString(R.string.notification_noconnection), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        });
    }

    @Override
    public int getItemCount() {

        return mListContact.size();
    }


    class ViewHodel extends RecyclerView.ViewHolder {

        ImageView imageAvatar;
        Button btnAddFriend;
        TextView txtName;

        public ViewHodel(@NonNull View itemView) {
            super(itemView);
            imageAvatar = itemView.findViewById(R.id.imageview_avatar_item_contact);
            btnAddFriend = itemView.findViewById(R.id.btn_add_item_contact);
            txtName = itemView.findViewById(R.id.textview_name_item_contact);
        }
    }

    public void updateContactNotFriend(List<Contact> contacts) {
        mListContact.addAll(contacts);
        notifyDataSetChanged();
    }

    public void notifyItems() {
        Collections.sort(mListContact, (t1, t2) -> t2.getmStatusAddFriend() - t1.getmStatusAddFriend());
        notifyDataSetChanged();
    }

    public void removeContactsRequest(List<Contact> contacts) {
        boolean b = mListContact.removeAll(contacts);
        if (b) {
            notifyDataSetChanged();
        }
    }

    private void showUICancel(Button btnAddFriend) {
        if (Build.VERSION.SDK_INT >= 23) {
            btnAddFriend.setTextColor(ContextCompat.getColor(context, R.color.colorBlack));
            btnAddFriend.setBackground(ContextCompat.getDrawable(context, R.drawable.custom_backgroud_button_cancel));
        } else {
            btnAddFriend.setTextColor(context.getResources().getColor(R.color.colorBlack));
            btnAddFriend.setBackground(context.getResources().getDrawable(R.drawable.custom_backgroud_button_cancel));
        }
    }

    private void showUINotCancel(Button btnAddFriend) {
        if (Build.VERSION.SDK_INT >= 23) {
            btnAddFriend.setTextColor(ContextCompat.getColor(context, R.color.colorWhile));
            btnAddFriend.setBackground(ContextCompat.getDrawable(context, R.drawable.custom_backgroud_button_yes));
        } else {
            btnAddFriend.setTextColor(context.getResources().getColor(R.color.colorWhile));
            btnAddFriend.setBackground(context.getResources().getDrawable(R.drawable.custom_backgroud_button_yes));
        }
    }
}
