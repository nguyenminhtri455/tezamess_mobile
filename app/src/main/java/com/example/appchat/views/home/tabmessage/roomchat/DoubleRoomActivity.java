package com.example.appchat.views.home.tabmessage.roomchat;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
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
import com.example.appchat.customview.ProgressBarDialog;
import com.example.appchat.database.TableContact;
import com.example.appchat.database.TableMessage;
import com.example.appchat.objectclass.Avatar;
import com.example.appchat.objectclass.ChatMessage;
import com.example.appchat.objectclass.Contact;
import com.example.appchat.objectclass.Member;
import com.example.appchat.viewmodel.ChatViewModel;
import com.example.appchat.viewmodel.ChatViewModelFactory;
import com.example.appchat.views.home.tabmessage.roomchat.adapter.ChatAdapter;
import com.example.appchat.views.profileuser.ProfileUserActivity;
import com.example.appchat.websocket.WebSocket;
import com.example.appchat.widget.connection.CheckConnection;
import com.example.appchat.widget.image.ScaleBitmap;
import com.example.appchat.widget.mapjson.MappedMessageToJson;
import com.example.appchat.widget.retrofit.DataClient;
import com.example.appchat.widget.retrofit.RetrofitClient;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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

public class DoubleRoomActivity extends AppCompatActivity implements BSImagePicker.OnSingleImageSelectedListener,
        BSImagePicker.OnMultiImageSelectedListener,
        BSImagePicker.ImageLoaderDelegate {

    private Toolbar toolbar;
    private ImageView imgSendMessage, imgPicture;
    private TextView txtAddfriend;
    private LinearLayout txtNotifyNotFriend;
    private RecyclerView recyclerViewChat;
    private EditText edInput;
    private LinearLayout btnScroll, btnScrollNewMessage;
    private ProgressBar progressBar, progressBarCreateRoom;
    private Contact contact;
    private Member admin;
    private ChatAdapter chatAdapter;
    private ChatViewModel chatViewModel;
    private CompositeDisposable compositeDisposable;
    private SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
    private List<ChatMessage> messages;
    private LinearLayoutManager linearLayoutManager;
    private TableMessage tableMessage;
    private TableContact tableContact;
    private ProgressBarDialog progressBarDialog;
    private String source;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_chat);
        admin = Member.getInstance(this);
        compositeDisposable = new CompositeDisposable();
        chatViewModel = ViewModelProviders.of(this, ChatViewModelFactory.getInstance(getApplicationContext())).get(ChatViewModel.class);
        tableMessage = TableMessage.getInstance(this);
        tableContact = TableContact.getInstance(this);
        progressBarDialog = new ProgressBarDialog();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancelAll();
        Intent intent = getIntent();
        contact = (Contact) intent.getSerializableExtra("contact");
        source = intent.getStringExtra("source");
        //danh dau roomactivity dang ton tai
        chatViewModel.flagCheckRoomExist = contact.getmRoomId();
        initViews();
        initActionBar();
        subtractCountUnreadMessage();
        initEvents();
        listening();
    }

    private void initActionBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_while_24dp);
        toolbar.setNavigationOnClickListener(t -> {
            onBackPressed();
        });
    }

    private void listening() {
        chatViewModel.flagContact.observe(this, t -> {
            Contact tempContact = null;
            int relationship = contact.getmRelationship();
            switch (relationship) {
                case ChatViewModel.CONTACTS_FRIEND:
                    tempContact = chatViewModel.getContact(ChatViewModel.CONTACTS_FRIEND, contact.getId());
                    break;
                case ChatViewModel.CONTACTS_NOT_FRIEND:
                    tempContact = chatViewModel.getContact(ChatViewModel.CONTACTS_NOT_FRIEND, contact.getId());
                    break;
            }
            switch (t) {
                case ChatViewModel.FLAG_UPDATE_ROOM_CONTACT:
                    contact.setContact(tempContact);
                    //danh dau roomactivity dang ton tai
                    chatViewModel.flagCheckRoomExist = contact.getmRoomId();
                    initData();
                    progressBarCreateRoom.setVisibility(View.GONE);
                    chatViewModel.setFlagContact(ChatViewModel.FLAG_DEFAULT);
                    break;
                case ChatViewModel.FLAG_UPDATE_CONTACT:
                    contact.setName(tempContact.getName());
                    contact.setUrlavatar(tempContact.getUrlavatar());
                    contact.setLastactive(tempContact.getLastactive());
                    if (contact != null) {
                        toolbar.setTitle(contact.getName());
                        offline(contact.getLastactive());
                    }
                    chatViewModel.setFlagContact(ChatViewModel.FLAG_DEFAULT);
                    break;
                case ChatViewModel.FLAG_NOTIFY_ONLINE_OFFLINE_CONTACT:
                    Contact contactOnline = chatViewModel.getContact(ChatViewModel.CONTACTS_ONLINE, this.contact.getId());
                    if (contactOnline != null) {
                        toolbar.setTitle(this.contact.getName());
                        online();
                    } else {
                        Date date1 = new Date();
                        toolbar.setTitle(this.contact.getName());
                        offline(date1.getTime());
                    }
                    chatViewModel.setFlagContact(ChatViewModel.FLAG_DEFAULT);
                    break;
                case ChatViewModel.FLAG_INVITE_ADDFRIEND_CONTACT:
                    contact.setmStatusAddFriend(2);
                    showLayoutFriend();
                    chatViewModel.setFlagContact(ChatViewModel.FLAG_DEFAULT);
                    break;
                case ChatViewModel.FLAG_INVITED_ADDFRIEND_CONTACT:
                    contact.setmStatusAddFriend(3);
                    showLayoutFriend();
                    chatViewModel.setFlagContact(ChatViewModel.FLAG_DEFAULT);
                    break;
                case ChatViewModel.FLAG_AGREE_ADDFRIEND:
                    contact.setmRelationship(1);
                    contact.setmStatusAddFriend(0);
                    showLayoutFriend();
                    chatViewModel.setFlagContact(ChatViewModel.FLAG_DEFAULT);
                    break;
                case ChatViewModel.FLAG_DISAGREE_ADDFRIEND:
                    contact.setmStatusAddFriend(1);
                    showLayoutFriend();
                    chatViewModel.setFlagContact(ChatViewModel.FLAG_DEFAULT);
                    break;
                case ChatViewModel.FLAG_UNFRIEND:
                    if (progressBarDialog.isAdded()) {
                        progressBarDialog.dismiss();
                    }
                    contact.setmRelationship(-1);
                    contact.setmStatusAddFriend(1);
                    showLayoutFriend();
                    chatViewModel.setFlagContact(ChatViewModel.FLAG_DEFAULT);
                    break;
            }
        });

        chatViewModel.flagMessage.observe(this, t -> {
            switch (t) {
                case ChatViewModel.FLAG_LOADMORE_MESSAGE:
                    int indexItemAfter = messages.size() - sizeBeforeLoad;
                    chatAdapter.notifyItemRangeInserted(0, messages.size() - sizeBeforeLoad);
                    chatAdapter.notifyItemChanged(indexItemAfter);
                    srollable = true;
                    chatAdapter.setLoaded();
                    chatViewModel.setFlagMessage(ChatViewModel.FLAG_DEFAULT);
                    break;
                case ChatViewModel.FLAG_UPDATE_MESSAGE:
                    if (chatAdapter != null && chatViewModel.tempMessage.getRoom() == contact.getmRoomId()) {
                        switch (chatViewModel.tempMessage.getStatus()) {
                            case Sent:
                                //kiem tra tin nhan co phai do minh gui khong
                                if (chatViewModel.tempMessage.getUser() == admin.getId()) {
                                    //neu la tin nhan minh gui thi cap nhat lai giao dien gui thanh cong
                                    chatAdapter.changeStatusMessage(chatViewModel.tempMessage);
                                }
                                break;
                            case Received:
                                //kiem tra tin nhan nhan duoc co phai do minh gui khong
                                if (chatViewModel.tempMessage.getUser() != admin.getId()) {
                                    //neu khong phai tin nhan do minh gui thi cap nhat lai giao dien
                                    chatAdapter.notifyChanged();
                                    //neu nguoi dung hien tai khong scroll tin nhan thi auto scroll den tin nhan cuoi cung
                                    if (statusScroll == 0 && lastVisibleItemPosition == messages.size() - 2) {
                                        recyclerViewChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                                    } else {
                                        btnScrollNewMessage.setVisibility(View.VISIBLE);
                                    }
                                    //gui tin nhan thong bao rang minh da xem tin nhan
                                    chatViewModel.tempMessage.setStatus(ChatMessage.StatusMessage.Seen);
                                    Log.d("BBBBB", "999999999999999");
                                    if (CheckConnection.haveNetworkConnection(this)) {
                                        if (WebSocket.stompClient != null) {
                                            if (WebSocket.stompClient.isConnected()) {
                                                Disposable subscribe = WebSocket.stompClient
                                                        .send("/chat/chat.sendMessageResponse/" + contact.getmRoomId()
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
                                            CustomToast.makeText(this, getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        CustomToast.makeText(this, getResources().getString(R.string.notification_noconnection), Toast.LENGTH_SHORT).show();
                                    }

                                    //tru so luong tin nhan chua doc trong total
                                    chatViewModel.subtractTotalQuantityUnreadMessages(1);

                                    //xoa so luong tin nhan chua doc cua phong hien tai
                                    chatViewModel.removeQuantityUnreadMessage(contact.getmRoomId());
                                    chatViewModel.tempIdRoom = contact.getmRoomId();
                                    chatViewModel.setFlagRoom(ChatViewModel.FLAG_RESET_UNREAD_CONVERSATION);
                                } else {
                                    chatAdapter.changeStatusMessage(chatViewModel.tempMessage);
                                }
                                chatViewModel.setFlagMessage(ChatViewModel.FLAG_DEFAULT);
                                break;
                            case Seen:
                                //kiem tra tin nhan co phai do minh gui khong
                                if (chatViewModel.tempMessage.getUser() == admin.getId()) {
                                    //neu la tin nhan minh gui thi cap nhat lai giao dien gui thanh cong
                                    chatAdapter.changeStatusMessage(chatViewModel.tempMessage);
                                }
                                chatViewModel.setFlagMessage(ChatViewModel.FLAG_DEFAULT);
                                break;
                        }
                    }
                    break;
            }
        });

        chatViewModel.flagRoom.observe(this, t -> {
            switch (t) {
                case ChatViewModel.FLAG_RESUM_ROOM:
                    if (contact.getmRoomId() == -1) {
                        initChat();
                        chatViewModel.setFlagRoom(ChatViewModel.FLAG_DEFAULT);
                    }
                    break;
            }
        });
    }

    private void initChat() {
        if (contact.getmRoomId() != -1) {
            initData();
        } else {
            if (CheckConnection.haveNetworkConnection(this)) {
                progressBarCreateRoom.setVisibility(View.VISIBLE);
                Map<String, Object> map = new HashMap();
                map.put("name", "");
                map.put("creator", admin.getId());
                map.put("ids", Arrays.asList(contact.getId()));
                Disposable subscribe = WebSocket.stompClient
                        .send("/chat/room.create", new Gson().toJson(map))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> Log.d("BBBBB", "tao phong thanh cong"),
                                onError -> Log.d("BBBBB", onError.getMessage())
                        );
                compositeDisposable.add(subscribe);
            } else {
                toolbar.setTitle(contact.getName());
                toolbar.setSubtitle(getResources().getString(R.string.waitting_network));
            }
        }
    }

    private void subtractCountUnreadMessage() {
        Disposable subscribe = Observable.defer(() ->
                Observable.just(tableMessage.getUnreadChatMessagesInRoom(contact.getmRoomId())))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(t -> {
                    //tru so luong tin nhan chua doc cua phong hien tai trong tong nhung tin nhan chua doc
                    chatViewModel.subtractTotalQuantityUnreadMessages(t);

                    //xoa so luong tin nhan chua doc cua phong hien tai trong conversation
                    chatViewModel.removeQuantityUnreadMessage(contact.getmRoomId());
                    chatViewModel.tempIdRoom = contact.getmRoomId();
                    chatViewModel.setFlagRoom(ChatViewModel.FLAG_RESET_UNREAD_CONVERSATION);
                });
        compositeDisposable.add(subscribe);
    }

    //co danh dau chi chay ham initData 1 lan khi bat dau khoi tao activity
    private boolean flagCreated = false;
    //co danh dau co the tiep tuc scroll de tai tin nhan (true la duoc scroll, false la khong duoc scroll)
    private boolean srollable = true;
    private int sizeBeforeLoad;

    private void initData() {
        if (!flagCreated) {
            flagCreated = true;
            showLayoutFriend();
            toolbar.setTitle(contact.getName());
            messages = chatViewModel.getChatMessages(contact.getmRoomId());
            initRecyclerviewChat(messages);

            chatAdapter.setLoadMore(() -> {
                if (srollable) {
                    srollable = false;
                    progressBar.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(() -> {
                        loadMoreMessage(messages.size(), 30);
                    }, 1000);
                }
            });

            if (messages.size() == 0) {
                loadMessage(0, 50);
            } else {
                lastVisibleItemPosition = messages.size() - 1;
                initButtonScroll();
                responseSeenMessage();
                checkStatusMessage();
            }
            checkContactOnlineOrOffline();
        }
    }

    private void showLayoutFriend() {
        if (contact.getmRelationship() == 1) {
            txtNotifyNotFriend.setVisibility(View.GONE);
            if (item != null) {
                item.setVisible(true);
            }

        } else {
            if (item != null) {
                item.setVisible(false);
            }
            txtNotifyNotFriend.setVisibility(View.VISIBLE);
            switch (contact.getmStatusAddFriend()) {
                case 1:
                    txtAddfriend.setText("Add Friend");
                    break;
                case 2:
                    txtAddfriend.setText("Requested");
                    break;
                case 3:
                    txtAddfriend.setText("Agree");
                    break;
            }
        }
    }

    private void initRecyclerviewChat(List<ChatMessage> messages) {
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setStackFromEnd(true);
        recyclerViewChat.setHasFixedSize(true);
        recyclerViewChat.setLayoutManager(linearLayoutManager);

        chatAdapter = new ChatAdapter(recyclerViewChat, this, messages);
        recyclerViewChat.setAdapter(chatAdapter);
    }

    // cac bien va flag khoi tao button scroll
    private int indexFirstOfMessageUnread = 0;
    private int visibleThreshold = 0;
    private boolean flagScroll = false;
    private boolean flagInitButtonScroll = false;
    private int lastVisibleItemPosition = 0;
    private int statusScroll = 0;

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
                lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();
                //khoi tao button scoll
                //newState = 1 la dang scroll, = 0 la khong scroll
                statusScroll = newState;
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
                    if (fisrt <= indexFirstOfMessageUnread && !flagScroll) {
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

    private void loadMessage(int start, int limit) {
        //load tin nhan cua moi phong tu cache neu co vao viewmodel
        Disposable subscribe4 = Observable.just(TableMessage.getInstance(this)
                .getChatMessages(contact.getmRoomId(), start, limit))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(t -> {
                    if (t.size() > 0) {
                        messages.addAll(t);
                        lastVisibleItemPosition = messages.size() - 1;
                        chatAdapter.notifyDataSetChanged();
                        initButtonScroll();
                        responseSeenMessage();
                        checkStatusMessage();
                    }
                });
        compositeDisposable.add(subscribe4);
    }

    private void loadMoreMessage(int start, int limit) {
        Disposable subscribe4 = Observable.just(TableMessage.getInstance(this)
                .getChatMessages(contact.getmRoomId(), start, limit))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(t -> {
                    chatAdapter.setLoaded();
                    progressBar.setVisibility(View.GONE);
                    if (t.size() > 0) {
                        messages.addAll(0, t);
                        chatAdapter.notifyItemRangeInserted(0, t.size());
                        chatAdapter.notifyItemChanged(t.size());
                        srollable = true;
                    } else {
                        if (CheckConnection.haveNetworkConnection(this)) {
                            sizeBeforeLoad = messages.size();
                            Map<String, Object> map = new HashMap<>();
                            map.put("id", admin.getId());
                            map.put("idRoom", contact.getmRoomId());
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
                        }
                    }
                });
        compositeDisposable.add(subscribe4);
    }

    public void checkContactOnlineOrOffline() {
        Contact c = chatViewModel.getContact(ChatViewModel.CONTACTS_ONLINE, this.contact.getId());
        if (c != null) {
            toolbar.setTitle(contact.getName());
            online();
        } else {
            if (CheckConnection.haveNetworkConnection(this)) {
                if (WebSocket.stompClient != null) {
                    if (WebSocket.stompClient.isConnected()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", admin.getId());
                        map.put("phone", this.contact.getPhone());
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
                        toolbar.setTitle(contact.getName());
                        offline(contact.getLastactive());
                    }
                } else {
                    toolbar.setTitle(contact.getName());
                    offline(contact.getLastactive());
                }
            } else {
                toolbar.setTitle(contact.getName());
                offline(contact.getLastactive());
            }
        }
    }

    public void responseSeenMessage() {
        if (CheckConnection.haveNetworkConnection(this)) {
            if (WebSocket.stompClient != null) {
                if (WebSocket.stompClient.isConnected()) {
                    //gui tin nhan phan hoi la da xem tin nhan cuoi cung
                    if (chatAdapter.getItemCount() > 0 && WebSocket.stompClient != null && WebSocket.stompClient.isConnected()) {
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
                                            .send("/chat/chat.sendMessageResponse/" + contact.getmRoomId()
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
                    if (chatAdapter.getItemCount() > 0 && WebSocket.stompClient != null && WebSocket.stompClient.isConnected()) {
                        int size = messages.size();
                        for (int i = size - 1; i >= 0; i--) {
                            ChatMessage chatMessage = messages.get(i);
                            if (chatMessage.getTypeMessage().equals(ChatMessage.TypeMessage.Chat)
                                    || chatMessage.getTypeMessage().equals(ChatMessage.TypeMessage.Image)) {
                                if ((chatMessage.getStatus() == ChatMessage.StatusMessage.Sent
                                        || chatMessage.getStatus() == ChatMessage.StatusMessage.Received) &&
                                        chatMessage.getUser() == admin.getId()) {
                                    Disposable subscribe = WebSocket.stompClient
                                            .send("/chat/check/messages.status/" + contact.getmRoomId()
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

        txtAddfriend.setOnClickListener(t -> {
            if (CheckConnection.haveNetworkConnection(this)) {
                if (WebSocket.stompClient != null) {
                    if (WebSocket.stompClient.isConnected()) {
                        switch (contact.getmStatusAddFriend()) {
                            case 1:
                                Map<String, Object> map = new HashMap<>();
                                map.put("idRequest", Member.getInstance(this).getId());
                                map.put("idFriend", contact.getId());
                                Disposable success = WebSocket.stompClient
                                        .send("/chat/addfriend", new Gson().toJson(map))
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(
                                                () -> Log.d("BBBBB", "success"),
                                                onError -> Log.d("BBBBB", onError.getMessage())
                                        );
                                compositeDisposable.add(success);
                                break;
                            case 2:
                                Toast.makeText(this
                                        , getResources().getString(R.string.sent_addfriend) + " " + contact.getName()
                                        , Toast.LENGTH_SHORT).show();
                                break;
                            case 3:
                                Map<String, Object> mapAgree = new HashMap<>();
                                mapAgree.put("idRequest", admin.getId());
                                mapAgree.put("idFriend", contact.getId());
                                mapAgree.put("status", 1);
                                Disposable succeseAgree = WebSocket.stompClient
                                        .send("/chat/response/addfriend", new Gson().toJson(mapAgree))
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(
                                                () -> Log.d("BBBBB", "success"),
                                                onError -> Log.d("BBBBB", onError.getMessage())
                                        );
                                compositeDisposable.add(succeseAgree);
                                break;
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
            Log.d("BBBBB", contact.getmRoomId() + " idRoom");
            ChatMessage chatMessage = new ChatMessage(new Date().getTime()
                    , edInput.getText().toString()
                    , contact.getmRoomId()
                    , admin.getId()
            );
            chatMessage.setTypeMessage(ChatMessage.TypeMessage.Chat);
            if (WebSocket.stompClient != null) {
                if (contact.getmRoomId() != -1) {

                    if (!WebSocket.stompClient.isConnected()) {
                        CustomToast.makeText(this, "Server dang bao tri :)))))", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    chatMessage.setStatus(ChatMessage.StatusMessage.Sent);
                    Disposable subscribe = WebSocket.stompClient
                            .send("/chat/chat.sendMessage/" + contact.getmRoomId(), MappedMessageToJson.mapTo7Value(chatMessage))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    () -> {
                                        Log.d("BBBBB", "sucess");
                                        chatMessage.setStatus(ChatMessage.StatusMessage.Sending);
                                        chatAdapter.addChatMessages(chatMessage);
                                        recyclerViewChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
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
                            chatAdapter.changeStatusMessage(chatMessage);
                        }
                    }, 10000);
                } else {
                    CustomToast.makeText(this, "Dang tao phong", Toast.LENGTH_SHORT).show();
                }
            } else {
                CustomToast.makeText(this, getResources().getString(R.string.notification_noconnection), Toast.LENGTH_SHORT).show();
                chatMessage.setStatus(ChatMessage.StatusMessage.Error);
                chatMessage.setTypeMessage(ChatMessage.TypeMessage.Chat);
                if (chatAdapter != null) {
                    chatAdapter.addChatMessages(chatMessage);
                } else {
                    initRecyclerviewChat(new ArrayList<>(Arrays.asList(chatMessage)));
                }

                edInput.setText("");
                chatAdapter.notifyItemInserted(chatAdapter.getItemCount() - 1);
                recyclerViewChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
            }
        });
    }

    private MenuItem item;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_friend, menu);
        item = menu.findItem(R.id.menu_unfriend);
        if (contact.getmRelationship() != 1) {
            item.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void showDiagLogUnFriend() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_unfriend);
        builder.setMessage(getResources().getString(R.string.confirm_unfriend) + " " + contact.getName() + " ?");
        builder.setPositiveButton(R.string.title_yes, (dialog, which) -> {
            progressBarDialog.show(getSupportFragmentManager(), "dialog");
            Map<String, Object> mapUnFriend = new HashMap<>();
            mapUnFriend.put("idRequest", admin.getId());
            mapUnFriend.put("idFriend", contact.getId());
            Disposable disposable = WebSocket.stompClient
                    .send("/chat/unfriend", new Gson().toJson(mapUnFriend))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            () -> Log.d("BBBBB", "success"),
                            onError -> Log.d("BBBBB", onError.getMessage())
                    );
            compositeDisposable.add(disposable);
            dialog.dismiss();
        });
        builder.setNegativeButton(R.string.title_no, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_personal:
                if (source != null) {
                    finish();
                } else {
                    Intent intent = new Intent(DoubleRoomActivity.this, ProfileUserActivity.class);
                    intent.putExtra("source", "DoubleRoomActivity");
                    intent.putExtra("contact", contact);
                    startActivity(intent);
                }
                break;
            case R.id.menu_unfriend:
                if (CheckConnection.haveNetworkConnection(this)) {
                    if (WebSocket.stompClient != null) {
                        if (WebSocket.stompClient.isConnected()) {
                            showDiagLogUnFriend();
                        } else {
                            CustomToast.makeText(this, getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        CustomToast.makeText(this, getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    CustomToast.makeText(this, getResources().getString(R.string.notification_noconnection), Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void offline(long time) {
//        if (Build.VERSION.SDK_INT >= 23) {
//            toolbar.setSubtitleTextColor(ContextCompat.getColor(this, R.color.colorWhile));
//        } else {
//            toolbar.setSubtitleTextColor(getResources().getColor(R.color.colorWhile));
//        }
        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        int currentMonth = now.get(Calendar.MONTH);
        int currentDay = now.get(Calendar.DAY_OF_MONTH);
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);

        Date date = new Date(time);
        Calendar now2 = Calendar.getInstance();
        now2.setTime(date);
        int year = now2.get(Calendar.YEAR);
        int month = now2.get(Calendar.MONTH);
        int day = now2.get(Calendar.DAY_OF_MONTH);
        int hour = now2.get(Calendar.HOUR_OF_DAY);
        int minute = now2.get(Calendar.MINUTE);

        if (currentYear == year) {
            if (currentMonth == month) {
                if (currentDay == day) {
                    if (currentHour == hour) {
                        toolbar.setSubtitle(getResources().getString(R.string.offline)
                                + " "
                                + (currentMinute - minute + 1)
                                + " "
                                + getResources().getString(R.string.minute_ago));
                    } else {
                        toolbar.setSubtitle(getResources().getString(R.string.offline)
                                + " "
                                + (currentHour - hour)
                                + " "
                                + getResources().getString(R.string.hours_ago));
                    }
                } else {
                    if (currentDay - day >= 5) {
                        toolbar.setSubtitle(getResources().getString(R.string.offline) + " " + format.format(date));
                    } else {
                        if (currentDay - day == 1) {
                            toolbar.setSubtitle(getResources().getString(R.string.offline)
                                    + " "
                                    + getResources().getString(R.string.yesterday));
                        } else {
                            toolbar.setSubtitle(getResources().getString(R.string.offline)
                                    + " "
                                    + (currentDay - day)
                                    + " "
                                    + getResources().getString(R.string.day_ago));
                        }
                    }
                }
            } else {
                if (currentMonth - month > 1) {
                    toolbar.setSubtitle(getResources().getString(R.string.offline) + " " + format.format(date));
                } else {
                    int dayOfMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH);
                    int distanceDay = dayOfMonth - day + currentDay;
                    toolbar.setSubtitle(getResources().getString(R.string.offline)
                            + " "
                            + distanceDay
                            + " "
                            + getResources().getString(R.string.day_ago));
                }
            }
        } else {
            toolbar.setSubtitle(getResources().getString(R.string.offline) + " " + format.format(date));
        }
    }

    private void online() {
//        if (Build.VERSION.SDK_INT >= 23) {
//            toolbar.setSubtitleTextColor(ContextCompat.getColor(this, R.color.colorGreen));
//        } else {
//            toolbar.setSubtitleTextColor(getResources().getColor(R.color.colorGreen));
//        }

        toolbar.setSubtitle(getResources().getString(R.string.online));
    }

    private void initViews() {
        imgPicture = findViewById(R.id.img_picture);
        toolbar = findViewById(R.id.toolbar_double_room);
        imgSendMessage = findViewById(R.id.img_send_message);
        recyclerViewChat = findViewById(R.id.recyclerview_roomchat);
        edInput = findViewById(R.id.edittext_input_text);
        progressBar = findViewById(R.id.progressBar);
        progressBarCreateRoom = findViewById(R.id.progressbar_create_room);
        btnScroll = findViewById(R.id.btn_scroll_unread);
        btnScrollNewMessage = findViewById(R.id.btn_scroll_new_message);
        txtNotifyNotFriend = findViewById(R.id.textview_notify_notfriend);
        txtAddfriend = findViewById(R.id.textview_addfriend);
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

    @Override
    protected void onResume() {
        super.onResume();
        //kiem tra relationship cua contact trong db
        Observable.defer(() -> Observable.just(tableContact.getContact(contact.getId())))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(t -> {
                    if (t.getId() == contact.getId()) {
                        contact.setName(t.getName());
                        contact.setUrlavatar(t.getUrlavatar());
                        contact.setLastactive(t.getLastactive());
                        contact.setmRelationship(t.getmRelationship());
                        contact.setmStatusAddFriend(t.getmStatusAddFriend());
                        contact.setmRoomId(t.getmRoomId());
                    }
                    showLayoutFriend();
                    initChat();
                });
    }

    @Override
    protected void onDestroy() {
        chatViewModel.flagCheckRoomExist = -1;
        compositeDisposable.dispose();
        super.onDestroy();
    }


    private Retrofit retrofit = RetrofitClient.getRetrofit();
    private DataClient dataClient = retrofit.create(DataClient.class);
    private Call<String> call;
    private Handler handler = new Handler();

    @Override
    public void onMultiImageSelected(List<Uri> uriList, String tag) {
        if (CheckConnection.haveNetworkConnection(this)) {
            Disposable subscribe = Completable.create(o -> {
                try {
                    for (Uri uri : uriList) {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        Bitmap scaleAfterBitmap = ScaleBitmap.scaleBitmap(bitmap, 1024 * 1024);

                        if (bitmap != scaleAfterBitmap) {
                            bitmap.recycle();
                        }
                        Avatar avatar = ScaleBitmap.encodeBase64Avatar(DoubleRoomActivity.this, scaleAfterBitmap, uri.getPath());
                        ChatMessage chatMessage = new ChatMessage(new Date().getTime()
                                , avatar
                                , contact.getmRoomId()
                                , admin.getId()
                                , ChatMessage.TypeMessage.Image
                        );
                        chatMessage.setBody(RetrofitClient.pathImage + avatar.getName());
                        if (WebSocket.stompClient != null) {
                            if (contact.getmRoomId() != -1) {

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
                                        chatAdapter.changeStatusMessage(chatMessage);
                                    }
                                }, 10000);

                                chatMessage.setFile(null);
                                chatMessage.setStatus(ChatMessage.StatusMessage.Sending);
                                chatAdapter.addChatMessages(chatMessage);
                                recyclerViewChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);


                            } else {
                                CustomToast.makeText(this, "Dang tao phong", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            CustomToast.makeText(this, getResources().getString(R.string.notification_noconnection), Toast.LENGTH_SHORT).show();
                            chatMessage.setStatus(ChatMessage.StatusMessage.Error);
                            chatMessage.setTypeMessage(ChatMessage.TypeMessage.Chat);
                            if (chatAdapter != null) {
                                chatAdapter.addChatMessages(chatMessage);
                            } else {
                                initRecyclerviewChat(new ArrayList<>(Arrays.asList(chatMessage)));
                            }

                            chatAdapter.notifyItemInserted(chatAdapter.getItemCount() - 1);
                            recyclerViewChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                        }


                    }

                    o.onComplete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).subscribeOn(Schedulers.io())
                    .subscribe();
            compositeDisposable.add(subscribe);
        }
    }

    @Override
    public void onSingleImageSelected(Uri uri, String tag) {

    }

    @Override
    public void loadImage(File imageFile, ImageView ivImage) {
        Glide.with(DoubleRoomActivity.this).load(imageFile).into(ivImage);
    }
}