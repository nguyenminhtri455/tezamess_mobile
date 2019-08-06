package com.example.appchat.views.home.tabcontact.searchfriend.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHodel> {
    private List<Contact> mListRequest;
    private Context mContext;
    private SearchFirendActivity searchFirendActivity;

    private Member admin;
    private Gson gson = new Gson();

    public RequestAdapter(List<Contact> mListRequest, Context mContext) {
        this.mListRequest = mListRequest;
        this.mContext = mContext;
        admin = Member.getInstance(mContext);
        searchFirendActivity = (SearchFirendActivity) mContext;
    }

    @NonNull
    @Override
    public ViewHodel onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.custom_item_request, viewGroup, false);
        ViewHodel viewModel = new ViewHodel(view);
        return viewModel;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHodel viewHodel, int i) {
        Contact contact = mListRequest.get(i);
        viewHodel.txtName.setText(contact.getName());
        Picasso.get()
                .load(mListRequest.get(i).getUrlavatar())
                .placeholder(R.drawable.account_icon)
                .error(R.drawable.account_icon)
                .into(viewHodel.imageAvatar);

        viewHodel.itemView.setOnClickListener(t -> {
            Intent intent = new Intent(searchFirendActivity, ProfileUserActivity.class);
            intent.putExtra("source", "Search");
            intent.putExtra("contact", contact);
            searchFirendActivity.startActivity(intent);
        });

        viewHodel.btnAgree.setOnClickListener(t -> {
            if (CheckConnection.haveNetworkConnection(mContext)) {
                if (WebSocket.stompClient != null) {
                    if (WebSocket.stompClient.isConnected()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("idRequest", admin.getId());
                        map.put("idFriend", contact.getId());
                        map.put("status", 1);
                        Disposable success = WebSocket.stompClient
                                .send("/chat/response/addfriend", gson.toJson(map))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        () -> Log.d("BBBBB", "success"),
                                        onError -> Log.d("BBBBB", onError.getMessage())
                                );
                        searchFirendActivity.compositeDisposable.add(success);
                    } else {
                        CustomToast.makeText(mContext, mContext.getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    CustomToast.makeText(mContext, mContext.getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                }
            } else {
                CustomToast.makeText(mContext, mContext.getResources().getString(R.string.notification_noconnection), Toast.LENGTH_SHORT).show();
            }
        });

        viewHodel.btnDelete.setOnClickListener(t -> {
            if (CheckConnection.haveNetworkConnection(mContext)) {
                if (WebSocket.stompClient != null) {
                    if (WebSocket.stompClient.isConnected()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("idRequest", admin.getId());
                        map.put("idFriend", contact.getId());
                        map.put("status", -1);
                        Disposable success = WebSocket.stompClient
                                .send("/chat/response/addfriend", gson.toJson(map))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        () -> Log.d("BBBBB", "success"),
                                        onError -> Log.d("BBBBB", onError.getMessage())
                                );
                        searchFirendActivity.compositeDisposable.add(success);
                    } else {
                        CustomToast.makeText(mContext, mContext.getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    CustomToast.makeText(mContext, mContext.getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                }
            } else {
                CustomToast.makeText(mContext, mContext.getResources().getString(R.string.notification_noconnection), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mListRequest.size();
    }


    class ViewHodel extends RecyclerView.ViewHolder {

        ImageView imageAvatar;
        Button btnAgree;
        Button btnDelete;
        TextView txtName;

        public ViewHodel(@NonNull View itemView) {
            super(itemView);
            imageAvatar = itemView.findViewById(R.id.imageview_avatar_item_request);
            btnAgree = itemView.findViewById(R.id.btn_agree_item_contact);
            btnDelete = itemView.findViewById(R.id.btn_delete_item_contact);
            txtName = itemView.findViewById(R.id.textview_name_item_request);
        }
    }
}
