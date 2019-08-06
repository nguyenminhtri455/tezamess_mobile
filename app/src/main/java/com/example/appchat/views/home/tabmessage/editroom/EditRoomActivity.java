package com.example.appchat.views.home.tabmessage.editroom;

import android.Manifest;
import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import com.example.appchat.views.home.tabmessage.editroom.adapter.MembersRoomAdapter;
import com.example.appchat.views.home.tabmessage.newmember.NewMemberActivity;
import com.example.appchat.websocket.WebSocket;
import com.example.appchat.widget.connection.CheckConnection;
import com.example.appchat.widget.image.ScaleBitmap;
import com.example.appchat.widget.retrofit.DataClient;
import com.example.appchat.widget.retrofit.RetrofitClient;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Retrofit;

public class EditRoomActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_AVATAR = 0;
    public static final int REQUEST_NEW_MEMBER = 2000;
    public static final int REQUEST_PROFILE_USER = 6000;
    public static final int RESPONSE_NEW_MEMBER = 3000;
    public static final int RESPONSE_LEAVE_ROOM = 4000;
    public static final int RESPONSE_UPDATE_ROOM = 5000;
    private String realPath;
    private Toolbar toolbar;
    private CircleImage imgAvatarRoom;
    private RecyclerView recyclerViewMembers;
    private TextView txtMembers, txtInviteMember, txtLeaveRoom;
    private EditText edRoomName;
    private Room room;
    private Member admin;
    private MembersRoomAdapter membersRoomAdapter;
    private ChatViewModel chatViewModel;
    private List<Contact> contactsInRoom, listContactOnline, listContactOnlineInRoom;
    private CompositeDisposable compositeDisposable;
    private ProgressBarDialog progressBarDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_room);
        chatViewModel = ViewModelProviders.of(this, ChatViewModelFactory.getInstance(this)).get(ChatViewModel.class);
        room = (Room) getIntent().getSerializableExtra("room");
        contactsInRoom = room.getContacts();
        compositeDisposable = new CompositeDisposable();
        progressBarDialog = new ProgressBarDialog();
        admin = Member.getInstance(this);
        listContactOnline = chatViewModel.getContacts(ChatViewModel.CONTACTS_ONLINE);
        listContactOnlineInRoom = new ArrayList<>();
        initViews();
        initActionBar();
        initRecyclerViewMembers();
        initEvents();

        chatViewModel.flagRoom.observe(this, t -> {
            switch (t) {
                case ChatViewModel.FLAG_REMOVE_CONVERSATION:
                    progressBarDialog.dismiss();
                    setResult(RESPONSE_LEAVE_ROOM);
                    chatViewModel.setFlagRoom(ChatViewModel.FLAG_DEFAULT);
                    finish();
                    break;
                case ChatViewModel.FLAG_UPDATE_ROOM:
                    if (progressBarDialog.isAdded()) {
                        progressBarDialog.dismiss();
                        setResult(RESPONSE_UPDATE_ROOM);
                        chatViewModel.setFlagRoom(ChatViewModel.FLAG_DEFAULT);
                        finish();
                    }
                    break;
            }
        });

        chatViewModel.flagContact.observe(this, t -> {
            switch (t) {
                case ChatViewModel.FLAG_NOTIFY_ONLINE_OFFLINE_CONTACT:
                    updateContactOnline();
                    chatViewModel.setFlagContact(ChatViewModel.FLAG_DEFAULT);
                    break;
                case ChatViewModel.FLAG_UPDATE_CONTACT:
                    break;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateContactOnline();
    }

    private void updateContactOnline() {
        listContactOnlineInRoom.clear();
        listContactOnlineInRoom.addAll(contactsInRoom);
        listContactOnlineInRoom.retainAll(listContactOnline);
        membersRoomAdapter.notifyDataSetChanged();
    }

    private void initEvents() {

        imgAvatarRoom.setOnClickListener(t -> {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(EditRoomActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_AVATAR);
            } else {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_AVATAR);
            }
        });

        txtInviteMember.setOnClickListener(t -> {
            Intent intent = new Intent(EditRoomActivity.this, NewMemberActivity.class);
            intent.putExtra("room", room);
            startActivityForResult(intent, REQUEST_NEW_MEMBER);
        });

        txtLeaveRoom.setOnClickListener(t -> {
            if (CheckConnection.haveNetworkConnection(this)) {
                showDiaglog();
            } else {
                CustomToast.makeText(this, getResources().getString(R.string.notification_noconnection), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDiaglog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(R.string.title_leave_room);
        builder.setMessage(R.string.message_leave_room);
        builder.setPositiveButton(R.string.title_yes, (dialog, which) -> {
            if (WebSocket.stompClient != null) {
                if (WebSocket.stompClient.isConnected()) {
                    progressBarDialog.show(getSupportFragmentManager(), "dialog");
                    Map<String, Object> map = new HashMap<>();
                    map.put("idRoom", room.getId());
                    map.put("idUser", admin.getId());
                    Disposable subscribe = WebSocket.stompClient.send("/chat/leave/room", new Gson().toJson(map))
                            .subscribeOn(Schedulers.io())
                            .subscribe(() -> Log.d("BBBBB", "success"),
                                    onError -> onError.getMessage());
                    compositeDisposable.add(subscribe);
                } else {
                    CustomToast.makeText(this, getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                }
            } else {
                CustomToast.makeText(this, getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.title_no, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void initRecyclerViewMembers() {
        membersRoomAdapter = new MembersRoomAdapter(this, contactsInRoom, room.getCreator(), listContactOnlineInRoom);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerViewMembers.addItemDecoration(dividerItemDecoration);
        recyclerViewMembers.setLayoutManager(linearLayoutManager);
        recyclerViewMembers.setAdapter(membersRoomAdapter);
    }

    private void initActionBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_while_24dp);
        toolbar.setNavigationOnClickListener(t -> {
            onBackPressed();
        });
        toolbar.setTitle(room.getName());
        txtMembers.setText("(" + room.getMembers() + ")");
        edRoomName.setText(room.getName());
        if (!room.getUrlAvatar().equals("null")) {
            Glide.with(EditRoomActivity.this)
                    .load(room.getUrlAvatar())
                    .placeholder(R.drawable.group)
                    .error(R.drawable.group)
                    .into(imgAvatarRoom);
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_edit_room);
        imgAvatarRoom = findViewById(R.id.img_avatar);
        recyclerViewMembers = findViewById(R.id.recyclerview_members);
        txtMembers = findViewById(R.id.textview_members);
        txtInviteMember = findViewById(R.id.textview_invite_friend);
        txtLeaveRoom = findViewById(R.id.textview_leave_room);
        edRoomName = findViewById(R.id.edittext_room_name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_room, menu);
        return super.onCreateOptionsMenu(menu);
    }


    private Retrofit retrofit = RetrofitClient.getRetrofit();
    private DataClient dataClient = retrofit.create(DataClient.class);
    private Call<String> call;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (CheckConnection.haveNetworkConnection(this)) {
            if (WebSocket.stompClient != null) {
                if (WebSocket.stompClient.isConnected()) {
                    Disposable subscribe = Completable.create(o -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("idRoom", room.getId());
                        map.put("sender", admin.getId());
                        String newName = edRoomName.getText().toString().trim().toLowerCase();
                        String oldName = room.getName().toLowerCase();
                        if (!newName.equals(oldName)) {
                            map.put("name", edRoomName.getText().toString().trim());
                        }
                        if (realPath != null) {
                            Bitmap bitmap = ((BitmapDrawable) imgAvatarRoom.getDrawable()).getBitmap();
                            Avatar avatar = ScaleBitmap.encodeBase64Avatar(EditRoomActivity.this,
                                    bitmap, realPath);
                            JSONObject mapAvatar = new JSONObject();
                            mapAvatar.put("valueBase64", avatar.getValueBase64());
                            mapAvatar.put("name", avatar.getName());
                            map.put("avatar", mapAvatar.toString());
                        }

                        if (!map.containsKey("name") && !map.containsKey("avatar")) {
                            finish();
                        } else {
                            call = dataClient.updateRoom(admin.getToken(EditRoomActivity.this), new Gson().toJson(map));

                            RetrofitClient.excute(call, s -> {
                                Log.d("BBBBB", "sucees!!!!");
                            });
                            progressBarDialog.show(getSupportFragmentManager(), "dialog");
                        }

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


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_NEW_MEMBER:
                if (resultCode == NewMemberActivity.RESPONSE_NEW_MEMBER) {
                    room = (Room) data.getSerializableExtra("room");
                    Intent intent = new Intent();
                    intent.putExtra("room", room);
                    setResult(RESPONSE_NEW_MEMBER, intent);
                    finish();
                }
                break;
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
            case REQUEST_PROFILE_USER:
                if (resultCode == EditRoomActivity.REQUEST_PROFILE_USER) {
                    setResult(REQUEST_PROFILE_USER);
                    finish();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
    }
}
