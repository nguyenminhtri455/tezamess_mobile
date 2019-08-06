package com.example.appchat.views.home.tabmessage.newroom;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appchat.R;
import com.example.appchat.customview.CircleImage;
import com.example.appchat.customview.CustomToast;
import com.example.appchat.customview.ProgressBarDialog;
import com.example.appchat.objectclass.Avatar;
import com.example.appchat.objectclass.Contact;
import com.example.appchat.objectclass.Member;
import com.example.appchat.objectclass.Room;
import com.example.appchat.viewmodel.ChatViewModel;
import com.example.appchat.viewmodel.ChatViewModelFactory;
import com.example.appchat.views.home.tabmessage.newroom.adapter.ContactChooseAdapter;
import com.example.appchat.views.home.tabmessage.newroom.adapter.ContactHintAdapter;
import com.example.appchat.views.home.tabmessage.roomchat.ManyRoomActivity;
import com.example.appchat.websocket.WebSocket;
import com.example.appchat.widget.connection.CheckConnection;
import com.example.appchat.widget.image.ScaleBitmap;
import com.example.appchat.widget.retrofit.DataClient;
import com.example.appchat.widget.retrofit.RetrofitClient;
import com.example.appchat.widget.validate.Validator;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Retrofit;

public class NewRoomActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_AVATAR = 0;
    private Toolbar toolbar;
    private CircleImage imgAvatarRoom;
    private EditText edNameRoom;
    private SearchView searchView;
    private TextView txtChooseContact, txtNotifyNotContact;
    private RecyclerView recyclerViewHintContact, recyclerViewChooseContact;
    private ContactHintAdapter contactHintAdapter;
    private ContactChooseAdapter contactChooseAdapter;
    private List<Contact> listContactHint, listContactChoose, tempListContact;
    private List<String> listPhone;
    private ChatViewModel chatViewModel;
    private CompositeDisposable compositeDisposable;
    private Member admin;
    private ProgressBarDialog progressBarDialog;
    private String realPath;
    public static Contact tempContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_room);
        chatViewModel = ViewModelProviders.of(this, ChatViewModelFactory.getInstance(this)).get(ChatViewModel.class);
        compositeDisposable = new CompositeDisposable();
        admin = Member.getInstance(this);
        progressBarDialog = new ProgressBarDialog();

        initViews();
        initActionBar();
        initEvent();

        listening();

        tempListContact = new ArrayList<>();
        listContactChoose = new ArrayList<>();
        initRecyclerViewContactChoose();

        listContactHint = chatViewModel.getContacts(ChatViewModel.CONTACTS_FRIEND);
        initRecyclerViewContactHint(listContactHint);
        listPhone = new ArrayList<>();
        for (Contact c : listContactHint) {
            listPhone.add(c.getPhone());
        }
    }

    private void listening() {
        chatViewModel.flagRoom.observe(this, t -> {
            switch (t) {
                case ChatViewModel.FLAG_UPDATE_CONVERSATION:
                    List<Room> rooms = chatViewModel.getRooms(ChatViewModel.MANY_ROOM);
                    if (rooms.size() > 0) {
                        Room room = rooms.get(0);
                        if (room.getCreator() == admin.getId()) {
                            progressBarDialog.dismiss();
                            Intent intent = new Intent(NewRoomActivity.this, ManyRoomActivity.class);
                            intent.putExtra("room", room);
                            startActivity(intent);
                            finish();
                        }
                    }
                    chatViewModel.setFlagRoom(ChatViewModel.FLAG_DEFAULT);
                    break;
            }
        });

        chatViewModel.flagContact.observe(this, t -> {
            switch (t) {
                case ChatViewModel.FLAG_FIND_MEMBER:
                    if (tempContact != null) {
                        if (listContactHint.contains(tempContact)) {
                            tempContact.setmRelationship(1);
                            tempContact.setmStatusAddFriend(0);
                        }
                        tempListContact.clear();
                        tempListContact.add(tempContact);
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
        imgAvatarRoom.setOnClickListener(t -> {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(NewRoomActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_AVATAR);
            } else {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_AVATAR);
            }
        });

//        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //dung dinh dang dien thoai
//                if (Validator.checkValidatePhoneNumber(s)) {
//                    // neu da co trong danh sach goi y
//                    int i = listPhone.indexOf(s);
//                    if (i != -1) {
//                        contactHintAdapter.getFilter().filter(s);
//                    } else {
//                        // khac so dien thoai cua admin
//                        if (!admin.getPhone().equals(s)) {
//                            //co ket noi internet
//                            if (CheckConnection.haveNetworkConnection(NewRoomActivity.this)) {
//                                //co ket noi toi websocket
//                                if (WebSocket.stompClient != null && WebSocket.stompClient.isConnected()) {
//                                    Map<String, Object> map = new HashMap<>();
//                                    map.put("id", admin.getId());
//                                    map.put("phone", s);
//                                    map.put("caller", "newroom");
//                                    Disposable subscribe = WebSocket.stompClient
//                                            .send("/chat/find.user", new Gson().toJson(map))
//                                            .subscribeOn(Schedulers.io())
//                                            .observeOn(AndroidSchedulers.mainThread())
//                                            .subscribe(
//                                                    () -> {
//                                                        Log.d("BBBBB", "sucess");
//                                                    },
//                                                    onError -> Log.d("BBBBB", onError.getMessage())
//                                            );
//                                    compositeDisposable.add(subscribe);
//                                }
//                            } else {
//                                CustomToast.makeText(NewRoomActivity.this, getResources().getString(R.string.notification_noconnection), Toast.LENGTH_SHORT).show();
//                            }
//                        } else {
//                            txtNotifyNotContact.setVisibility(View.VISIBLE);
//                        }
//                    }
//                } else {
//                    contactHintAdapter.getFilter().filter(s);
//                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
//                txtNotifyNotContact.setVisibility(View.GONE);
//                if (s.isEmpty() && tempListContact.size() > 0) {
//                    tempContact = null;
//                    initRecyclerViewContactHint(listContactHint);
//                } else {
//                    contactHintAdapter.getFilter().filter(s);
//                }
                //---------------------------
                txtNotifyNotContact.setVisibility(View.GONE);
                //neu khong nhap gi ca thi khoi tao ve trang thai ban dau
                if (s.isEmpty()) {
                    tempListContact.clear();
                    tempContact = null;
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
                                    if (CheckConnection.haveNetworkConnection(NewRoomActivity.this)) {
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
                                                CustomToast.makeText(NewRoomActivity.this, getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            CustomToast.makeText(NewRoomActivity.this, getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        CustomToast.makeText(NewRoomActivity.this, getResources().getString(R.string.notification_noconnection), Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    txtNotifyNotContact.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    } else {
                        contactHintAdapter.getFilter().filter(s);
                    }
                }
                return false;
            }
        });
    }

    private void initRecyclerViewContactChoose() {
        contactChooseAdapter = new ContactChooseAdapter(this, listContactChoose);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewChooseContact.setLayoutManager(linearLayoutManager);
        recyclerViewChooseContact.setAdapter(contactChooseAdapter);
    }

    private void initRecyclerViewContactHint(List<Contact> contacts) {
        contactHintAdapter = new ContactHintAdapter(this, contacts, listContactChoose);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerViewHintContact.setLayoutManager(linearLayoutManager);
        recyclerViewHintContact.setAdapter(contactHintAdapter);
    }

    public void addContactChoose(Contact contact) {
        txtChooseContact.setVisibility(View.INVISIBLE);
        listContactChoose.add(contact);
        contactChooseAdapter.notifyItemInserted(listContactChoose.size() - 1);
        toolbar.setSubtitle(getResources().getString(R.string.selected) + " " + contactChooseAdapter.getItemCount());
    }

    public void removeContactChoose(Contact contact) {
        int i = listContactChoose.indexOf(contact);
        if (i != -1) {
            listContactChoose.remove(i);
            contactChooseAdapter.notifyItemRemoved(i);
        }

        if (listContactChoose.size() <= 0) {
            txtChooseContact.setVisibility(View.VISIBLE);
            toolbar.setSubtitle("");
        } else {
            toolbar.setSubtitle(getResources().getString(R.string.selected) + " " + contactChooseAdapter.getItemCount());
        }
    }

    public void updateContactsHint(Contact contact) {
        int i = listContactHint.indexOf(contact);
        int i2 = tempListContact.indexOf(contact);
        if (i != -1) {
            contactHintAdapter.notifyItemChanged(i);
        }
        if (i2 != -1) {
            contactHintAdapter.notifyItemChanged(i2);
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
        getMenuInflater().inflate(R.menu.menu_create_room, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private Retrofit retrofit = RetrofitClient.getRetrofit();
    private DataClient dataClient = retrofit.create(DataClient.class);
    private Call<String> call;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String roomName = edNameRoom.getText().toString().trim();
        if (roomName.isEmpty()) {
            CustomToast.makeText(this, getResources().getString(R.string.hint_name_room), Toast.LENGTH_SHORT).show();
        } else if (contactChooseAdapter.getItemCount() == 0) {
            CustomToast.makeText(this, getResources().getString(R.string.choose_member), Toast.LENGTH_SHORT).show();
        } else {
            if (CheckConnection.haveNetworkConnection(this)) {
                if (WebSocket.stompClient != null) {
                    if (WebSocket.stompClient.isConnected()) {
                        Disposable subscribe = Completable.create(o -> {
                            List<Integer> listId = new ArrayList<>();
                            for (Contact c : listContactChoose) {
                                listId.add(c.getId());
                            }
                            Map<String, Object> map = new HashMap();
                            map.put("name", roomName);
                            map.put("creator", admin.getId());
                            map.put("ids", listId);
                            if (realPath != null) {
                                Bitmap bitmap = ((BitmapDrawable) imgAvatarRoom.getDrawable()).getBitmap();
                                Avatar avatar = ScaleBitmap.encodeBase64Avatar(NewRoomActivity.this,
                                        bitmap, realPath);
                                JSONObject mapAvatar = new JSONObject();
                                mapAvatar.put("valueBase64", avatar.getValueBase64());
                                mapAvatar.put("name", avatar.getName());
                                map.put("avatar", mapAvatar.toString());
                            }
                            call = dataClient.createRoom(admin.getToken(NewRoomActivity.this), new Gson().toJson(map));

                            RetrofitClient.excute(call, s -> {
                                Log.d("BBBBB", "sucees!!!!");
                            });
                            progressBarDialog.show(getSupportFragmentManager(), "dialog");
                        }).subscribeOn(Schedulers.io())
                                .subscribe();
                        compositeDisposable.add(subscribe);
                    } else {
                        CustomToast.makeText(this, getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    CustomToast.makeText(this, getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                }
            } else {
                CustomToast.makeText(this, getResources().getString(R.string.notification_noconnection), Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_newroom);
        imgAvatarRoom = findViewById(R.id.imageview_avatar_room);
        edNameRoom = findViewById(R.id.edittext_name_room);
        searchView = findViewById(R.id.searchview_contact);
        txtChooseContact = findViewById(R.id.textview_choose_contact);
        txtNotifyNotContact = findViewById(R.id.textview_notify);
        recyclerViewChooseContact = findViewById(R.id.recyclerview_choose_contact);
        recyclerViewHintContact = findViewById(R.id.recyclerview_hint_contacts);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
        tempContact = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_AVATAR:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    realPath = ScaleBitmap.getRealPathFromURI(uri, this);
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
//            Log.d("BBB","truoc khi scale" + bitmap.getByteCount());
                        Bitmap scaleAfterBitmap = ScaleBitmap.scaleBitmap(bitmap, 1024 * 1024);
//            Log.d("BBB","sau khi scale" + scaleAfter.getByteCount());

                        if (bitmap != scaleAfterBitmap) {
                            bitmap.recycle();
                        }
                        imgAvatarRoom.setImageBitmap(scaleAfterBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }
}
