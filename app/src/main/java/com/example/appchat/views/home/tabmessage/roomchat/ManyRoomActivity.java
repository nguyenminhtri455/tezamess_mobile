package com.example.appchat.views.home.tabmessage.roomchat;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.asksira.bsimagepicker.BSImagePicker;
import com.asksira.bsimagepicker.Utils;
import com.bumptech.glide.Glide;
import com.example.appchat.R;
import com.example.appchat.customview.CustomToast;
import com.example.appchat.database.TableMessage;
import com.example.appchat.objectclass.Avatar;
import com.example.appchat.objectclass.ChatMessage;
import com.example.appchat.objectclass.Contact;
import com.example.appchat.objectclass.Member;
import com.example.appchat.objectclass.Room;
import com.example.appchat.viewmodel.ChatViewModel;
import com.example.appchat.viewmodel.ChatViewModelFactory;
import com.example.appchat.views.home.tabmessage.editroom.EditRoomActivity;
import com.example.appchat.views.home.tabmessage.newmember.NewMemberActivity;
import com.example.appchat.views.home.tabmessage.roomchat.adapter.DetailStatusMessageAdapter;
import com.example.appchat.views.home.tabmessage.roomchat.adapter.ManyChatAdapter;
import com.example.appchat.websocket.WebSocket;
import com.example.appchat.widget.connection.CheckConnection;
import com.example.appchat.widget.image.ScaleBitmap;
import com.example.appchat.widget.mapjson.MappedMessageToJson;
import com.example.appchat.widget.retrofit.DataClient;
import com.example.appchat.widget.retrofit.RetrofitClient;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Retrofit;

public class ManyRoomActivity extends AppCompatActivity implements BSImagePicker.OnSingleImageSelectedListener,
        BSImagePicker.OnMultiImageSelectedListener,
        BSImagePicker.ImageLoaderDelegate {

    public static final int REQUEST_NEW_MEMBER = 0;
    public static final int REQUEST_EDIT_ROOM = 1;

    private Toolbar toolbar;
    private ImageView imgSendMessage, imgPicture;
    private RecyclerView recyclerViewChat;
    private EditText edInput;
    private Member admin;
    private LinearLayout btnScroll, btnScrollNewMessage;
    private ProgressBar progressBar;
    private ManyChatAdapter manyChatAdapter;
    private ChatViewModel chatViewModel;
    private CompositeDisposable compositeDisposable;
    private List<ChatMessage> messages;
    private LinearLayoutManager linearLayoutManager;
    private TableMessage tableMessage;
    private boolean srollable = true;
    private int sizeBeforeLoad;
    private Room room;
    private List<Contact> listContactOnline, listContactOnlineInRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_many_room);
        tableMessage = TableMessage.getInstance(this);
        chatViewModel = ViewModelProviders.of(this, ChatViewModelFactory.getInstance(this)).get(ChatViewModel.class);
        compositeDisposable = new CompositeDisposable();
        admin = Member.getInstance(this);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancelAll();
        room = (Room) getIntent().getSerializableExtra("room");
        listContactOnline = chatViewModel.getContacts(ChatViewModel.CONTACTS_ONLINE);
        listContactOnlineInRoom = new ArrayList<>(room.getContacts());
        listContactOnlineInRoom.retainAll(listContactOnline);
        initViews();
        initActionBar();
        initBottomSheet();
        listening();
        initChat();
    }

    private void initChat() {
        initEvents();
        initData();
        subtractCountUnreadMessage();
    }

    private void listening() {
        chatViewModel.flagContact.observe(this, t -> {
            switch (t) {
                case ChatViewModel.FLAG_NOTIFY_ONLINE_OFFLINE_CONTACT:
                    listContactOnlineInRoom.clear();
                    listContactOnlineInRoom.addAll(room.getContacts());
                    listContactOnlineInRoom.retainAll(listContactOnline);
                    toolbar.setSubtitle(room.getMembers() + " members, " + (listContactOnlineInRoom.size() + 1) + " online");
                    chatViewModel.setFlagContact(ChatViewModel.FLAG_DEFAULT);
                    break;
            }
        });

        chatViewModel.flagMessage.observe(this, t -> {
            switch (t) {
                case ChatViewModel.FLAG_LOADMORE_MESSAGE:
                    int indexItemAfter = messages.size() - sizeBeforeLoad;
                    manyChatAdapter.notifyItemRangeInserted(0, messages.size() - sizeBeforeLoad);
                    manyChatAdapter.notifyItemChanged(indexItemAfter);
                    srollable = true;
                    manyChatAdapter.setLoaded();
                    chatViewModel.setFlagMessage(ChatViewModel.FLAG_DEFAULT);
                    break;
                case ChatViewModel.FLAG_UPDATE_MESSAGE:
                    if (manyChatAdapter != null && chatViewModel.tempMessage.getRoom() == room.getId()) {
                        switch (chatViewModel.tempMessage.getStatus()) {
                            case Sent:
                                //kiem tra tin nhan co phai do minh gui khong
                                if (chatViewModel.tempMessage.getUser() == admin.getId()) {
                                    //neu la tin nhan minh gui thi cap nhat lai giao dien gui thanh cong
                                    manyChatAdapter.changeStatusMessage(chatViewModel.tempMessage);
                                }
                                break;
                            case Received:
                                //kiem tra tin nhan nhan duoc co phai do minh gui khong
                                if (chatViewModel.tempMessage.getUser() != admin.getId()) {
                                    //neu khong phai tin nhan do minh gui thi cap nhat lai giao dien
                                    manyChatAdapter.notifyChanged();
                                    //neu nguoi dung hien tai khong scroll tin nhan thi auto scroll den tin nhan cuoi cung
                                    if (statusScroll == 0 && lastVisibleItemPosition == messages.size() - 2) {
                                        recyclerViewChat.smoothScrollToPosition(manyChatAdapter.getItemCount() - 1);
                                    } else {
                                        btnScrollNewMessage.setVisibility(View.VISIBLE);
                                    }
                                    //gui tin nhan thong bao rang minh da xem tin nhan
                                    chatViewModel.tempMessage.setStatus(ChatMessage.StatusMessage.Seen);
                                    Log.d("BBBBB", "999999999999999");
                                    if (CheckConnection.haveNetworkConnection(this)) {
                                        if (WebSocket.stompClient != null && WebSocket.stompClient.isConnected()) {
                                            Disposable subscribe = WebSocket.stompClient
                                                    .send("/chat/chat.sendMessageResponse/" + room.getId()
                                                            , MappedMessageToJson.mapTo8Value(chatViewModel.tempMessage, admin.getId()))
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
                                            CustomToast.makeText(this, getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        CustomToast.makeText(this, getResources().getString(R.string.notification_noconnection), Toast.LENGTH_SHORT).show();
                                    }

                                    //tru so luong tin nhan chua doc trong total
                                    chatViewModel.subtractTotalQuantityUnreadMessages(1);

                                    //xoa so luong tin nhan chua doc cua phong hien tai
                                    chatViewModel.removeQuantityUnreadMessage(room.getId());
                                    chatViewModel.tempIdRoom = room.getId();
                                    chatViewModel.setFlagRoom(ChatViewModel.FLAG_RESET_UNREAD_CONVERSATION);

                                } else {
                                    manyChatAdapter.changeStatusMessage(chatViewModel.tempMessage);
                                }
                                chatViewModel.setFlagMessage(ChatViewModel.FLAG_DEFAULT);
                                break;
                            case Seen:
                                //kiem tra tin nhan co phai do minh gui khong
                                if (chatViewModel.tempMessage.getUser() == admin.getId()) {
                                    //neu la tin nhan minh gui thi cap nhat lai giao dien gui thanh cong
                                    manyChatAdapter.changeStatusMessage(chatViewModel.tempMessage);
                                }
                                chatViewModel.setFlagMessage(ChatViewModel.FLAG_DEFAULT);
                                break;
                        }
                    }
                    break;
                case ChatViewModel.FLAG_CHECK_DETAIL_STATUS_MESSAGE:
                    showUI();
                    if (receivedAdapter == null) {
                        initRecyclerRecevied(contactsReceived);
                    } else {
                        receivedAdapter.notifyDataSetChanged();
                    }
                    if (seenAdapter == null) {
                        initRecyclerSeen(contactsSeen);
                    } else {
                        seenAdapter.notifyDataSetChanged();
                    }
                    chatViewModel.setFlagMessage(ChatViewModel.FLAG_DEFAULT);
                    break;
            }
        });

        chatViewModel.flagRoom.observe(this, t -> {
            switch (t) {
                case ChatViewModel.FLAG_LEAVE_ROOM:
                    room = chatViewModel.getRoom(room.getId(), ChatViewModel.MANY_ROOM);
                    subtractCountUnreadMessage();
                    toolbar.setSubtitle(room.getMembers() + " members");
                    manyChatAdapter.notifyItemInserted(manyChatAdapter.getItemCount() - 1);
                    recyclerViewChat.smoothScrollToPosition(messages.size() - 1);
                    chatViewModel.setFlagRoom(ChatViewModel.FLAG_DEFAULT);
                    break;
                case ChatViewModel.FLAG_UPDATE_ROOM:
                    room = chatViewModel.getRoom(room.getId(), ChatViewModel.MANY_ROOM);
                    subtractCountUnreadMessage();
                    toolbar.setTitle(room.getName());
                    recyclerViewChat.smoothScrollToPosition(messages.size() - 1);
                    chatViewModel.setFlagRoom(ChatViewModel.FLAG_DEFAULT);
                    break;
            }
        });
    }

    private void initActionBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_while_24dp);
        toolbar.setNavigationOnClickListener(t -> {
            onBackPressed();
        });
        toolbar.setTitle(room.getName());
//        toolbar.setSubtitle(room.getMembers() + " members, " + (listContactOnlineInRoom.size() + 1) + " online");
    }

    private void initData() {
        messages = chatViewModel.getChatMessages(room.getId());
        initRecyclerviewChat(messages);

        manyChatAdapter.setLoadMore(() -> {
            if (srollable) {
                srollable = false;
                progressBar.setVisibility(View.VISIBLE);
                new Handler().postDelayed(() -> {
                    loadMoreMessage(messages.size(), 20);
                }, 1000);
            }
        });

        if (messages.size() == 0) {
            loadMessage(0, 20);
        } else {
            lastVisibleItemPosition = messages.size() - 1;
            initButtonScroll();
            responseSeenMessage();
            checkStatusMessage();
        }
    }

    private void initRecyclerviewChat(List<ChatMessage> messages) {
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setStackFromEnd(true);
        recyclerViewChat.setHasFixedSize(true);
        recyclerViewChat.setLayoutManager(linearLayoutManager);

        manyChatAdapter = new ManyChatAdapter(recyclerViewChat, this, messages, room);
        recyclerViewChat.setAdapter(manyChatAdapter);
    }

    private void loadMessage(int start, int limit) {
        //load 50 tin nhan cua moi phong tu cache neu co vao viewmodel
        Disposable subscribe4 = Observable.just(tableMessage.getChatMessages(room.getId(), start, limit))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(t -> {
                    if (t.size() > 0) {
                        messages.addAll(t);
                        manyChatAdapter.notifyDataSetChanged();
                        initButtonScroll();
                        responseSeenMessage();
                        checkStatusMessage();
                    }
                });
        compositeDisposable.add(subscribe4);
    }

    private void loadMoreMessage(int start, int limit) {
        Disposable subscribe4 = Observable.just(TableMessage.getInstance(this)
                .getChatMessages(room.getId(), start, limit))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(t -> {
                    manyChatAdapter.setLoaded();
                    progressBar.setVisibility(View.GONE);
                    if (t.size() > 0) {
                        messages.addAll(0, t);
                        manyChatAdapter.notifyItemRangeInserted(0, t.size());
                        manyChatAdapter.notifyItemChanged(t.size());
                        srollable = true;
                    } else {
                        if (CheckConnection.haveNetworkConnection(this)) {
                            if (WebSocket.stompClient != null && WebSocket.stompClient.isConnected()) {
                                sizeBeforeLoad = messages.size();
                                Map<String, Object> map = new HashMap<>();
                                map.put("id", admin.getId());
                                map.put("idRoom", room.getId());
                                map.put("indexStart", messages.size());
                                map.put("count", limit);
                                Disposable subscribe = WebSocket.stompClient
                                        .send("/chat/loadmore/messages", new Gson().toJson(map))
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
                                CustomToast.makeText(this, getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            CustomToast.makeText(this, getResources().getString(R.string.notification_noconnection), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        compositeDisposable.add(subscribe4);
    }

    // cac bien va flag khoi tao button scroll
    private int indexFirstOfMessageUnread = 0;
    private int visibleThreshold = 0;
    private boolean flagScroll = false;
    private boolean flagInitButtonScroll = false;
    private int statusScroll = 0;
    private int lastVisibleItemPosition = 0;

    private void initButtonScroll() {
        // lay vi tri tin nhan chua doc dau tien trong danh sach
        indexFirstOfMessageUnread = messages.size();
        for (ChatMessage message : messages) {
            if (message.getStatus() != null && message.getStatus().equals(ChatMessage.StatusMessage.Received) && message.getUser() != admin.getId()) {
                indexFirstOfMessageUnread = messages.indexOf(message);
                break;
            }
        }
        // theo doi su kien scoll
        recyclerViewChat.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                statusScroll = newState;
                lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();
                //khoi tao button scoll
                if (newState == 1 && !flagInitButtonScroll) {
                    flagInitButtonScroll = true;
                    visibleThreshold = linearLayoutManager.findLastVisibleItemPosition() - linearLayoutManager.findFirstVisibleItemPosition();
                    //neu so luong tin nhan chua doc lon hon so luong tin nhan co the nhin thay thi hien thi button
                    if ((messages.size() - indexFirstOfMessageUnread > visibleThreshold) && messages.size() > 10) {
                        btnScroll.setVisibility(View.VISIBLE);
                    } else {
                        btnScroll.setVisibility(View.GONE);
                    }
                } else {
                    //neu scoll toi vi tri tin nhan chua doc dau tien thi button scroll an di
                    int fisrt = linearLayoutManager.findFirstVisibleItemPosition();
                    if (fisrt < indexFirstOfMessageUnread && !flagScroll) {
                        flagScroll = true;
                        btnScroll.setVisibility(View.GONE);
                    }
                }
                if (lastVisibleItemPosition == messages.size() - 1) {
                    btnScrollNewMessage.setVisibility(View.GONE);
                }
            }
        });
    }


    private void subtractCountUnreadMessage() {
        tableMessage.updateStatusChatMessagesNotify(room.getId());

        //tru so luong tin nhan chua doc cua phong hien tai trong tong nhung tin nhan chua doc
        chatViewModel.subtractTotalQuantityUnreadMessages(room.getQuantityUnreadMessage());

        //xoa so luong tin nhan chua doc cua phong hien tai trong conversation
        chatViewModel.removeQuantityUnreadMessage(room.getId());
        //set idroom tam de cap nhat lai giao dien
        chatViewModel.tempIdRoom = room.getId();
        chatViewModel.setFlagRoom(ChatViewModel.FLAG_RESET_UNREAD_CONVERSATION);
    }

    private void initEvents() {

        imgPicture.setOnClickListener(t -> {
            showMultiSelectionPicker();
        });

        btnScroll.setOnClickListener(t -> {
            recyclerViewChat.smoothScrollToPosition(indexFirstOfMessageUnread);
            btnScroll.setVisibility(View.GONE);
        });

        btnScrollNewMessage.setOnClickListener(t -> {
            recyclerViewChat.smoothScrollToPosition(messages.size() - 1);
            btnScrollNewMessage.setVisibility(View.GONE);
        });

        edInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (edInput.length() > 0) {
                    imgSendMessage.setVisibility(View.VISIBLE);
                    imgPicture.setVisibility(View.GONE);
                } else {
                    imgSendMessage.setVisibility(View.GONE);
                    imgPicture.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        imgSendMessage.setOnClickListener(t -> {
            ChatMessage chatMessage = new ChatMessage(new Date().getTime()
                    , edInput.getText().toString()
                    , room.getId()
                    , admin.getId()
            );
            chatMessage.setTypeMessage(ChatMessage.TypeMessage.Chat);
            if (WebSocket.stompClient != null) {

                if (!WebSocket.stompClient.isConnected()) {
                    CustomToast.makeText(this, "Server dang bao tri :)))))", Toast.LENGTH_SHORT).show();
                    return;
                }

                chatMessage.setStatus(ChatMessage.StatusMessage.Sent);
                Disposable subscribe = WebSocket.stompClient
                        .send("/chat/chat.sendMessage/" + room.getId(), MappedMessageToJson.mapTo7Value(chatMessage))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    Log.d("BBBBB", "sucess");
                                    chatMessage.setStatus(ChatMessage.StatusMessage.Sending);
                                    manyChatAdapter.addChatMessages(chatMessage);
                                    recyclerViewChat.smoothScrollToPosition(manyChatAdapter.getItemCount() - 1);
                                    edInput.setText("");
                                },
                                onError -> Log.d("BBBBB", onError.getMessage())
                        );
                compositeDisposable.add(subscribe);

                //kiem tra trong vong 10s ko nhan lai tin nhan da gui thi cap nhat tin nhan bi loi gui that bai
                new Handler().postDelayed(() -> {
                    if (!chatViewModel.flagCheckMessageError) {
                        chatMessage.setStatus(ChatMessage.StatusMessage.Error);
                        chatMessage.setTypeMessage(ChatMessage.TypeMessage.Chat);
                        manyChatAdapter.changeStatusMessage(chatMessage);
                    }
                }, 10000);

            } else {
                CustomToast.makeText(this, getResources().getString(R.string.notification_noconnection), Toast.LENGTH_SHORT).show();
                chatMessage.setStatus(ChatMessage.StatusMessage.Error);
                chatMessage.setTypeMessage(ChatMessage.TypeMessage.Chat);
                if (manyChatAdapter != null) {
                    manyChatAdapter.addChatMessages(chatMessage);
                } else {
                    initRecyclerviewChat(new ArrayList<>(Arrays.asList(chatMessage)));
                }

                edInput.setText("");
                manyChatAdapter.notifyItemInserted(manyChatAdapter.getItemCount() - 1);
                recyclerViewChat.smoothScrollToPosition(manyChatAdapter.getItemCount() - 1);
            }
        });
    }

    public void responseSeenMessage() {
        if (CheckConnection.haveNetworkConnection(this)) {
            if (WebSocket.stompClient != null) {
                if (WebSocket.stompClient.isConnected()) {
                    //gui tin nhan phan hoi la da xem tin nhan cuoi cung
                    if (manyChatAdapter.getItemCount() > 0 && WebSocket.stompClient != null && WebSocket.stompClient.isConnected()) {
                        int size = messages.size();
                        for (int i = size - 1; i >= 0; i--) {
                            ChatMessage chatMessage = messages.get(i);
                            if (chatMessage.getTypeMessage().equals(ChatMessage.TypeMessage.Chat)
                                    || chatMessage.getTypeMessage().equals(ChatMessage.TypeMessage.Image)) {
                                if ((chatMessage.getStatus() == ChatMessage.StatusMessage.Sent
                                        || chatMessage.getStatus() == ChatMessage.StatusMessage.Received) &&
                                        chatMessage.getUser() != admin.getId()) {

                                    chatMessage.setStatus(ChatMessage.StatusMessage.Seen);
                                    Disposable subscribe = WebSocket.stompClient
                                            .send("/chat/chat.sendMessageResponse/" + room.getId()
                                                    , MappedMessageToJson.mapTo8Value(chatMessage, admin.getId()))
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(
                                                    () -> {
                                                        Log.d("BBBBB", "sucess");
                                                    },
                                                    onError -> Log.d("BBBBB", onError.getMessage())
                                            );
                                    compositeDisposable.add(subscribe);
                                }
                                return;
                            }
                        }
                    }
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

    public void checkStatusMessage() {
        if (CheckConnection.haveNetworkConnection(this)) {
            if (WebSocket.stompClient != null) {
                if (WebSocket.stompClient.isConnected()) {
                    //gui tin nhan kiem tra trang thai tin nhan da gui
                    if (manyChatAdapter.getItemCount() > 0 && WebSocket.stompClient != null && WebSocket.stompClient.isConnected()) {
                        int size = messages.size();
                        for (int i = size - 1; i >= 0; i--) {
                            ChatMessage chatMessage = messages.get(i);
                            if (chatMessage.getTypeMessage().equals(ChatMessage.TypeMessage.Chat)
                                    || chatMessage.getTypeMessage().equals(ChatMessage.TypeMessage.Image)) {
                                if ((chatMessage.getStatus() == ChatMessage.StatusMessage.Sent
                                        || chatMessage.getStatus() == ChatMessage.StatusMessage.Received) &&
                                        chatMessage.getUser() == admin.getId()) {
                                    Disposable subscribe = WebSocket.stompClient
                                            .send("/chat/check/messages.status/" + room.getId()
                                                    , MappedMessageToJson.mapTo8Value(chatMessage, admin.getId()))
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(
                                                    () -> {
                                                        Log.d("BBBBB", "sucess");
                                                    },
                                                    onError -> Log.d("BBBBB", onError.getMessage())
                                            );
                                    compositeDisposable.add(subscribe);
                                }
                                return;
                            }
                        }
                    }
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

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_many_room);
        recyclerViewChat = findViewById(R.id.recyclerview_many_room);
        edInput = findViewById(R.id.edittext_input_text);
        imgSendMessage = findViewById(R.id.img_send_message);
        imgPicture = findViewById(R.id.img_picture);
        btnScroll = findViewById(R.id.btn_scroll_unread);
        btnScrollNewMessage = findViewById(R.id.btn_scroll_new_message);
        progressBar = findViewById(R.id.progressBar);
        bottomSheet = findViewById(R.id.bottom_sheet_layout);
        recyclerViewReceived = findViewById(R.id.recyclerview_received);
        recyclerViewSeen = findViewById(R.id.recyclerview_seen);
        txtRecevied = findViewById(R.id.textview_received);
        txtSeen = findViewById(R.id.textview_seen);
        txtError = findViewById(R.id.textview_error);
        progressBarBottomSheet = findViewById(R.id.progressbar_bottom_sheet);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_many_room, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_invite_member:
                Intent intent = new Intent(ManyRoomActivity.this, NewMemberActivity.class);
                intent.putExtra("room", room);
                startActivityForResult(intent, REQUEST_NEW_MEMBER);
                break;
            case R.id.item_more_room:
                Intent intentEditRoom = new Intent(ManyRoomActivity.this, EditRoomActivity.class);
                intentEditRoom.putExtra("room", room);
                startActivityForResult(intentEditRoom, REQUEST_EDIT_ROOM);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateContactOnline() {
        listContactOnlineInRoom.clear();
        listContactOnlineInRoom.addAll(room.getContacts());
        listContactOnlineInRoom.retainAll(listContactOnline);
        toolbar.setSubtitle(room.getMembers() + " members, " + (listContactOnlineInRoom.size() + 1) + " online");
    }

    @Override
    protected void onResume() {
        //danh dau room dang duoc hien thi
        chatViewModel.flagCheckRoomExist = room.getId();
        updateContactOnline();
        super.onResume();
    }

    @Override
    protected void onStop() {
        //danh dau room khong duoc hien thi
        chatViewModel.flagCheckRoomExist = -1;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
        if (contactsReceived != null) {
            contactsReceived.clear();
        }
        if (contactsSeen != null) {
            contactsSeen.clear();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_NEW_MEMBER:
                switch (resultCode) {
                    case NewMemberActivity.RESPONSE_NEW_MEMBER:
                        room = (Room) data.getSerializableExtra("room");
                        subtractCountUnreadMessage();
                        toolbar.setSubtitle(room.getMembers() + " members");
                        recyclerViewChat.smoothScrollToPosition(messages.size() - 1);
                        break;
                }
                break;
            case REQUEST_EDIT_ROOM:
                switch (resultCode) {
                    case EditRoomActivity.RESPONSE_LEAVE_ROOM:
                        finish();
                        break;
                    case EditRoomActivity.RESPONSE_UPDATE_ROOM:
                        room = chatViewModel.getRoom(room.getId(), ChatViewModel.MANY_ROOM);
                        subtractCountUnreadMessage();
                        toolbar.setTitle(room.getName());
                        recyclerViewChat.smoothScrollToPosition(messages.size() - 1);
                        break;
                    case EditRoomActivity.RESPONSE_NEW_MEMBER:
                        room = (Room) data.getSerializableExtra("room");
                        subtractCountUnreadMessage();
                        toolbar.setSubtitle(room.getMembers() + " members");
                        recyclerViewChat.smoothScrollToPosition(messages.size() - 1);
                        break;
                    case EditRoomActivity.REQUEST_PROFILE_USER:
                        finish();
                        break;
                }
                break;
        }
    }

    public void showSingleSelectionPicker() {
        BSImagePicker singleSelectionPicker = new BSImagePicker.Builder("com.example.appchat.fileprovider")
                .setMaximumDisplayingImages(24) //Default: Integer.MAX_VALUE. Don't worry about performance :)
                .setSpanCount(3) //Default: 3. This is the number of columns
                .setGridSpacing(Utils.dp2px(2)) //Default: 2dp. Remember to pass in a value in pixel.
                .setPeekHeight(Utils.dp2px(360)) //Default: 360dp. This is the initial height of the dialog.
                .hideCameraTile() //Default: show. Set this if you don't want user to take photo.
                .hideGalleryTile() //Default: show. Set this if you don't want to further let user select from a gallery app. In such case, I suggest you to set maximum     displaying    images to Integer.MAX_VALUE.
                .build();
        singleSelectionPicker.show(getSupportFragmentManager(), "picker");
    }

    public void showMultiSelectionPicker() {
        BSImagePicker multiSelectionPicker = new BSImagePicker.Builder("com.example.appchat.fileprovider")
                .isMultiSelect() //Set this if you want to use multi selection mode.
                .setMinimumMultiSelectCount(1) //Default: 1.
                .setMaximumMultiSelectCount(5) //Default: Integer.MAX_VALUE (i.e. User can select as many images as he/she wants)
                .setMultiSelectBarBgColor(android.R.color.white) //Default: #FFFFFF. You can also set it to a translucent color.
                .setMultiSelectTextColor(R.color.primary_text) //Default: #212121(Dark grey). This is the message in the multi-select bottom bar.
                .setMultiSelectDoneTextColor(R.color.colorAccent) //Default: #388e3c(Green). This is the color of the "Done" TextView.
                .setOverSelectTextColor(R.color.error_text) //Default: #b71c1c. This is the color of the message shown when user tries to select more than maximum select count.
                .disableOverSelectionMessage() //You can also decide not to show this over select message.
                .build();
        multiSelectionPicker.show(getSupportFragmentManager(), "picker");
    }

    private Retrofit retrofit = RetrofitClient.getRetrofit();
    private DataClient dataClient = retrofit.create(DataClient.class);
    private Call<String> call;
    private Handler handler = new Handler();

    @Override
    public void onMultiImageSelected(List<Uri> uriList, String tag) {
        if (CheckConnection.haveNetworkConnection(this)) {
            if (WebSocket.stompClient != null) {
                if (WebSocket.stompClient.isConnected()) {
                    Disposable subscribe = Completable.create(o -> {
                        try {
                            for (Uri uri : uriList) {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                                Bitmap scaleAfterBitmap = ScaleBitmap.scaleBitmap(bitmap, 1024 * 1024);

                                if (bitmap != scaleAfterBitmap) {
                                    bitmap.recycle();
                                }
                                Avatar avatar = ScaleBitmap.encodeBase64Avatar(ManyRoomActivity.this, scaleAfterBitmap, uri.getPath());
                                ChatMessage chatMessage = new ChatMessage(new Date().getTime()
                                        , avatar
                                        , room.getId()
                                        , admin.getId()
                                        , ChatMessage.TypeMessage.Image
                                );
                                chatMessage.setBody(RetrofitClient.pathImage + avatar.getName());
                                if (WebSocket.stompClient != null) {
                                    if (!WebSocket.stompClient.isConnected()) {
                                        CustomToast.makeText(this, "Server dang bao tri :)))))", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    chatMessage.setStatus(ChatMessage.StatusMessage.Sent);

                                    call = dataClient.saveMessage(MappedMessageToJson.mapTo7Value(chatMessage));

                                    RetrofitClient.excute(call, s -> {
                                        Log.d("BBBBB", "sucees!!!!");
                                    });

                                    //kiem tra trong vong 10s ko nhan lai tin nhan da gui thi cap nhat tin nhan bi loi gui that bai
                                    handler.postDelayed(() -> {
                                        if (!chatViewModel.flagCheckMessageError) {
                                            chatMessage.setStatus(ChatMessage.StatusMessage.Error);
                                            chatMessage.setTypeMessage(ChatMessage.TypeMessage.Image);
                                            manyChatAdapter.changeStatusMessage(chatMessage);
                                        }
                                    }, 10000);

                                    chatMessage.setFile(null);
                                    chatMessage.setStatus(ChatMessage.StatusMessage.Sending);
                                    manyChatAdapter.addChatMessages(chatMessage);
                                    recyclerViewChat.smoothScrollToPosition(manyChatAdapter.getItemCount() - 1);
                                } else {
                                    CustomToast.makeText(this, getResources().getString(R.string.notification_noconnection), Toast.LENGTH_SHORT).show();
                                    chatMessage.setStatus(ChatMessage.StatusMessage.Error);
                                    chatMessage.setTypeMessage(ChatMessage.TypeMessage.Chat);
                                    if (manyChatAdapter != null) {
                                        manyChatAdapter.addChatMessages(chatMessage);
                                    } else {
                                        initRecyclerviewChat(new ArrayList<>(Arrays.asList(chatMessage)));
                                    }

                                    manyChatAdapter.notifyItemInserted(manyChatAdapter.getItemCount() - 1);
                                    recyclerViewChat.smoothScrollToPosition(manyChatAdapter.getItemCount() - 1);
                                }
                            }

                            o.onComplete();
                        } catch (IOException e) {
                            e.printStackTrace();
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
    }

    @Override
    public void onSingleImageSelected(Uri uri, String tag) {

    }


    @Override
    public void loadImage(File imageFile, ImageView ivImage) {
        Glide.with(ManyRoomActivity.this).load(imageFile).into(ivImage);
    }

    //-----------------------------------bottomsheet
    @Override
    public void onBackPressed() {
        switch (bottomSheetBehavior.getState()) {
            case BottomSheetBehavior.STATE_EXPANDED:
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                break;
            default:
                super.onBackPressed();

        }
    }

    private CardView bottomSheet;
    private BottomSheetBehavior bottomSheetBehavior;
    private RecyclerView recyclerViewReceived, recyclerViewSeen;
    private DetailStatusMessageAdapter receivedAdapter, seenAdapter;
    private TextView txtRecevied, txtSeen, txtError;
    private ProgressBar progressBarBottomSheet;
    public static List<Contact> contactsReceived, contactsSeen;
    private Gson gson = new Gson();

    private void showUI() {
        progressBarBottomSheet.setVisibility(View.GONE);
        txtRecevied.setText(getResources().getString(R.string.recevied) + " (" + contactsReceived.size() + ")");
        txtSeen.setText(getResources().getString(R.string.seen) + " (" + contactsSeen.size() + ")");
        txtRecevied.setVisibility(View.VISIBLE);
        txtSeen.setVisibility(View.VISIBLE);
    }

    private void showNotifyError(boolean visibility, int error) {
        // error 0:  khong co mang
        // error 1: khong ket noi vs websocket
        if (visibility) {
            switch (error) {
                case 0:
                    txtError.setText(getResources().getString(R.string.notification_noconnection));
                    break;
                case 1:
                    txtError.setText(getResources().getString(R.string.server_error));
                    break;
            }
            txtRecevied.setVisibility(View.GONE);
            txtSeen.setVisibility(View.GONE);
            txtError.setVisibility(View.VISIBLE);
        } else {
            txtRecevied.setVisibility(View.VISIBLE);
            txtSeen.setVisibility(View.VISIBLE);
            txtError.setVisibility(View.GONE);
        }
    }

    private void initBottomSheet() {
        registerForContextMenu(recyclerViewChat);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        contactsReceived = new ArrayList<>();
        contactsSeen = new ArrayList<>();
    }

    private void initRecyclerRecevied(List<Contact> contacts) {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4, LinearLayoutManager.VERTICAL, false);
        recyclerViewReceived.setHasFixedSize(true);
        recyclerViewReceived.setLayoutManager(gridLayoutManager);
        receivedAdapter = new DetailStatusMessageAdapter(contacts, this);
        recyclerViewReceived.setAdapter(receivedAdapter);
    }

    private void initRecyclerSeen(List<Contact> contacts) {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4, LinearLayoutManager.VERTICAL, false);
        recyclerViewSeen.setHasFixedSize(true);
        recyclerViewSeen.setLayoutManager(gridLayoutManager);
        seenAdapter = new DetailStatusMessageAdapter(contacts, this);
        recyclerViewSeen.setAdapter(seenAdapter);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int position = -1;
        try {
            position = manyChatAdapter.getPosition();
        } catch (Exception e) {
            CustomToast.makeText(this, "co loi xay ra !", Toast.LENGTH_SHORT).show();
            return super.onContextItemSelected(item);
        }
        switch (item.getItemId()) {
            case R.id.details_message:
//                DisplayMetrics displaymetrics = new DisplayMetrics();
//                getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
//                int screenHeight = displaymetrics.heightPixels;
//                bottomSheetBehavior.setPeekHeight(screenHeight);
                if (CheckConnection.haveNetworkConnection(this)) {
                    if (WebSocket.webSocket != null) {
                        if (WebSocket.stompClient.isConnected()) {
                            ChatMessage chatMessage = messages.get(position);
                            Map<String, Object> map = new HashMap<>();
                            map.put("id", chatMessage.getId());
                            map.put("room", chatMessage.getRoom());
                            map.put("user", admin.getId());
                            Disposable subscribe = WebSocket.stompClient
                                    .send("/chat/check/detail/messages.status"
                                            , gson.toJson(map))
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
                            showNotifyError(true, 1);
                        }
                    } else {
                        showNotifyError(true, 1);
                    }
                } else {
                    showNotifyError(true, 0);
                }
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                break;
        }
        return super.onContextItemSelected(item);
    }
}
