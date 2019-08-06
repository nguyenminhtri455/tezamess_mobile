package com.example.appchat.views.home.tabmessage.newmember;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appchat.R;
import com.example.appchat.customview.CustomToast;
import com.example.appchat.customview.ProgressBarDialog;
import com.example.appchat.objectclass.Contact;
import com.example.appchat.objectclass.Member;
import com.example.appchat.objectclass.Room;
import com.example.appchat.viewmodel.ChatViewModel;
import com.example.appchat.viewmodel.ChatViewModelFactory;
import com.example.appchat.views.home.tabmessage.newmember.adapter.ContactChooseNewMemberAdapter;
import com.example.appchat.views.home.tabmessage.newmember.adapter.ContactHintNewMemberAdapter;
import com.example.appchat.views.home.tabmessage.newroom.NewRoomActivity;
import com.example.appchat.websocket.WebSocket;
import com.example.appchat.widget.connection.CheckConnection;
import com.example.appchat.widget.validate.Validator;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class NewMemberActivity extends AppCompatActivity {
    public static final int RESPONSE_NEW_MEMBER = 100;
    private Toolbar toolbar;
    private SearchView searchView;
    private TextView txtChooseContact, txtNotifyNotContact;
    private RecyclerView recyclerViewHintContact, recyclerViewChooseContact;
    private ContactHintNewMemberAdapter contactHintNewMemberAdapter;
    private ContactChooseNewMemberAdapter contactChooseNewMemberAdapter;
    private List<Contact> listContactHint, listContactChoose, tempListContact;
    private List<String> listPhone;
    private ChatViewModel chatViewModel;
    private CompositeDisposable compositeDisposable;
    private Member admin;
    private ProgressBarDialog progressBarDialog;

    private Room room;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_member);
        chatViewModel = ViewModelProviders.of(this, ChatViewModelFactory.getInstance(this)).get(ChatViewModel.class);
        compositeDisposable = new CompositeDisposable();
        admin = Member.getInstance(this);
        progressBarDialog = new ProgressBarDialog();
        room = (Room) getIntent().getSerializableExtra("room");
        initViews();
        initActionBar();
        initEvent();

        tempListContact = new ArrayList<>();
        listContactChoose = new ArrayList<>();
        initRecyclerViewContactChoose();

        listContactHint = chatViewModel.getContacts(ChatViewModel.CONTACTS_FRIEND);
        initRecyclerViewContactHint(listContactHint);
        listPhone = new ArrayList<>();
        for (Contact c : listContactHint) {
            listPhone.add(c.getPhone());
        }

        chatViewModel.flagRoom.observe(this, t -> {
            switch (t) {
                case ChatViewModel.FLAG_INVITE_MEMBER_INTO_ROOM:
                    progressBarDialog.dismiss();
                    List<Room> rooms = chatViewModel.getRooms(ChatViewModel.MANY_ROOM);
                    if (rooms.size() > 0) {
                        Room room = rooms.get(0);
                        Intent intent = new Intent();
                        intent.putExtra("room", room);
                        setResult(RESPONSE_NEW_MEMBER, intent);
                        finish();
                    }
                    chatViewModel.setFlagRoom(ChatViewModel.FLAG_DEFAULT);
                    break;
            }
        });

        chatViewModel.flagContact.observe(this, t -> {
            switch (t) {
                case ChatViewModel.FLAG_FIND_MEMBER:
                    if (NewRoomActivity.tempContact != null) {
                        if (listContactHint.contains(NewRoomActivity.tempContact)) {
                            NewRoomActivity.tempContact.setmRelationship(1);
                            NewRoomActivity.tempContact.setmStatusAddFriend(0);
                        }
                        tempListContact.clear();
                        tempListContact.add(NewRoomActivity.tempContact);
                        initRecyclerViewContactHint(tempListContact);
                    } else {
                        txtNotifyNotContact.setVisibility(View.VISIBLE);
                    }
                    chatViewModel.setFlagContact(ChatViewModel.FLAG_DEFAULT);
                    break;
            }
        });

    }

    private void initEvent() {
//        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //dung dinh dang dien thoai
//                if (Validator.checkValidatePhoneNumber(s)) {
//                    // khac so dien thoai cua admin
//                    if (!admin.getPhone().equals(s)) {
//                        //co ket noi internet
//                        if (CheckConnection.haveNetworkConnection(NewMemberActivity.this)) {
//                            //co ket noi toi websocket
//                            if (WebSocket.stompClient != null && WebSocket.stompClient.isConnected()) {
//                                Map<String, Object> map = new HashMap<>();
//                                map.put("id", admin.getId());
//                                map.put("phone", s);
//                                map.put("caller", "newmember");
//                                Disposable subscribe = WebSocket.stompClient
//                                        .send("/chat/find.user", new Gson().toJson(map))
//                                        .subscribeOn(Schedulers.io())
//                                        .observeOn(AndroidSchedulers.mainThread())
//                                        .subscribe(
//                                                () -> {
//                                                    Log.d("BBBBB", "sucess");
//                                                },
//                                                onError -> Log.d("BBBBB", onError.getMessage())
//                                        );
//                                compositeDisposable.add(subscribe);
//                            }
//                        } else {
//                            CustomToast.makeText(NewMemberActivity.this, getResources().getString(R.string.notification_noconnection), Toast.LENGTH_SHORT).show();
//                        }
//                    } else {
//                        txtNotifyNotContact.setVisibility(View.VISIBLE);
//                    }
//                } else {
//                    contactHintNewMemberAdapter.getFilter().filter(s);
//                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
//                txtNotifyNotContact.setVisibility(View.GONE);
//                if (s.isEmpty() && tempListContact.size() > 0) {
//                    NewRoomActivity.tempContact = null;
//                    initRecyclerViewContactHint(listContactHint);
//                } else {
//                contactHintNewMemberAdapter.getFilter().filter(s);
//                }
                //-------------------------
                txtNotifyNotContact.setVisibility(View.GONE);
                //neu khong nhap gi ca thi khoi tao ve trang thai ban dau
                if (s.isEmpty()) {
                    tempListContact.clear();
                    NewRoomActivity.tempContact = null;
                    initRecyclerViewContactHint(listContactHint);
                } else {
                    //dung dinh dang dien thoai
                    if (Validator.checkValidatePhoneNumber(s)) {
                        // neu so dien thoai la ban be
                        int i = listPhone.indexOf(s);
                        if (i != -1) {
                            tempListContact.clear();
                            tempListContact.add(listContactHint.get(i));
                            initRecyclerViewContactHint(tempListContact);
                        } else {   // neu so dien thoai khong la ban be
                            // neu so dien thoai dang ton tai trong danh sach tam
                            if (tempListContact.size() > 0 && tempListContact.get(0).getPhone().equals(s)) {
                                initRecyclerViewContactHint(tempListContact);
                            } else {// neu so dien thoai khong ton tai trong danh sach tam
                                // khac so dien thoai cua admin
                                if (!admin.getPhone().equals(s)) {
                                    //co ket noi internet
                                    if (CheckConnection.haveNetworkConnection(NewMemberActivity.this)) {
                                        //co ket noi toi websocket
                                        if (WebSocket.stompClient != null) {
                                            if (WebSocket.stompClient.isConnected()) {
                                                Map<String, Object> map = new HashMap<>();
                                                map.put("id", admin.getId());
                                                map.put("phone", s);
                                                map.put("caller", "newroom");
                                                Disposable subscribe = WebSocket.stompClient
                                                        .send("/chat/find.user", new Gson().toJson(map))
                                                        .subscribeOn(Schedulers.io())
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(
                                                                () -> {
                                                                    Log.d("BBBBB", "sucess");
                                                                },
                                                                onError -> Log.d("BBBBB", onError.getMessage())
                                                        );
                                                compositeDisposable.add(subscribe);
                                            } else {
                                                CustomToast.makeText(NewMemberActivity.this, getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            CustomToast.makeText(NewMemberActivity.this, getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        CustomToast.makeText(NewMemberActivity.this, getResources().getString(R.string.notification_noconnection), Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    txtNotifyNotContact.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    } else {
                        contactHintNewMemberAdapter.getFilter().filter(s);
                    }
                }
                return false;
            }
        });
    }

    private void initRecyclerViewContactChoose() {
        contactChooseNewMemberAdapter = new ContactChooseNewMemberAdapter(this, listContactChoose);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewChooseContact.setLayoutManager(linearLayoutManager);
        recyclerViewChooseContact.setAdapter(contactChooseNewMemberAdapter);
    }

    private void initRecyclerViewContactHint(List<Contact> contacts) {
        contactHintNewMemberAdapter = new ContactHintNewMemberAdapter(this, contacts, room.getContacts(), listContactChoose);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerViewHintContact.setLayoutManager(linearLayoutManager);
        recyclerViewHintContact.setAdapter(contactHintNewMemberAdapter);
    }

    public void addContactChoose(Contact contact) {
        txtChooseContact.setVisibility(View.INVISIBLE);
        listContactChoose.add(contact);
        contactChooseNewMemberAdapter.notifyItemInserted(listContactChoose.size() - 1);
        toolbar.setSubtitle(getResources().getString(R.string.selected) + " " + contactChooseNewMemberAdapter.getItemCount());
    }

    public void removeContactChoose(Contact contact) {
        int i = listContactChoose.indexOf(contact);
        if (i != -1) {
            listContactChoose.remove(i);
            contactChooseNewMemberAdapter.notifyItemRemoved(i);
        }
        if (listContactChoose.size() <= 0) {
            txtChooseContact.setVisibility(View.VISIBLE);
            toolbar.setSubtitle("");
        } else {
            toolbar.setSubtitle(getResources().getString(R.string.selected) + " " + contactChooseNewMemberAdapter.getItemCount());
        }
    }

    public void updateContactsHint(Contact contact) {
        int i = listContactHint.indexOf(contact);
        int i2 = tempListContact.indexOf(contact);
        if (i != -1) {
            contactHintNewMemberAdapter.notifyItemChanged(i);
        }
        if (i2 != -1) {
            contactHintNewMemberAdapter.notifyItemChanged(i2);
        }
    }

    private void initActionBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_while_24dp);
        toolbar.setNavigationOnClickListener(t -> {
            onBackPressed();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_invite_member, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (CheckConnection.haveNetworkConnection(this)) {
            if (WebSocket.stompClient != null) {
                if (WebSocket.stompClient.isConnected()) {
                    if (listContactChoose.size() > 0) {
                        progressBarDialog.show(getSupportFragmentManager(), "dialog");
                        List<Integer> ids = new ArrayList<>();
                        for (Contact c : listContactChoose) {
                            ids.add(c.getId());
                        }

                        Map<String, Object> map = new HashMap<>();
                        map.put("idRoom", room.getId());
                        map.put("ids", ids);
                        Disposable subscribe = WebSocket.stompClient.send("/chat/invite/member", new Gson().toJson(map))
                                .subscribeOn(Schedulers.io())
                                .subscribe(() -> Log.d("BBBBB", "success"),
                                        onError -> onError.getMessage());
                        compositeDisposable.add(subscribe);
                    }
                } else {
                    CustomToast.makeText(this
                            , getResources().getString(R.string.server_error)
                            , Toast.LENGTH_SHORT).show();
                }
            } else {
                CustomToast.makeText(this
                        , getResources().getString(R.string.server_error)
                        , Toast.LENGTH_SHORT).show();
            }
        } else {
            CustomToast.makeText(this
                    , getResources().getString(R.string.notification_noconnection)
                    , Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_invite_member);
        searchView = findViewById(R.id.searchview_contact);
        txtNotifyNotContact = findViewById(R.id.textview_notify);
        txtChooseContact = findViewById(R.id.textview_choose_contact);
        recyclerViewChooseContact = findViewById(R.id.recyclerview_choose_contact);
        recyclerViewHintContact = findViewById(R.id.recyclerview_hint_contacts);
    }

    @Override
    protected void onDestroy() {
        NewRoomActivity.tempContact = null;
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
        super.onDestroy();
    }
}
