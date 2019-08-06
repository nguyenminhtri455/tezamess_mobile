package com.example.appchat.websocket;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.appchat.R;
import com.example.appchat.database.TableContact;
import com.example.appchat.database.TableMessage;
import com.example.appchat.database.TableParticipation;
import com.example.appchat.database.TableRoom;
import com.example.appchat.database.TableStatus;
import com.example.appchat.objectclass.ChatMessage;
import com.example.appchat.objectclass.Contact;
import com.example.appchat.objectclass.Member;
import com.example.appchat.objectclass.Room;
import com.example.appchat.objectclass.Status;
import com.example.appchat.viewmodel.ChatViewModel;
import com.example.appchat.viewmodel.ChatViewModelFactory;
import com.example.appchat.views.home.HomeActivity;
import com.example.appchat.views.home.tabmessage.newroom.NewRoomActivity;
import com.example.appchat.views.home.tabmessage.roomchat.ManyRoomActivity;
import com.example.appchat.views.welcome.WelcomActivity;
import com.example.appchat.widget.connection.CheckConnection;
import com.example.appchat.widget.mapjson.MappedMessageToJson;
import com.example.appchat.widget.notification.NotificationHelper;
import com.example.appchat.widget.retrofit.RetrofitClient;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class WebSocket {
    private volatile int count = 0;
    public static final String LOGIN = "login";

    public static final String PASSCODE = "passcode";
    //status message
    public static final String SENT = "Sent";
    public static final String RECEIVED = "Received";
    public static final String SEEN = "Seen";
    public static final String ONLINE = "Online";
    public static final String OFFLINE = "Offline";

    //type message
    public static final String CHAT = "Chat";
    public static final String IMAGE = "Image";
    public static final String NOTIFY = "Notify";
    public static final String RESPONSE = "Response";

    private Map<Integer, Disposable> map = new HashMap<>();
    private ChatViewModel chatViewModel;
    public static CompositeDisposable compositeDisposable;

    private AppCompatActivity appCompatActivity;
    private Context context;

    public static StompClient stompClient;
    public static WebSocket webSocket;
    private Member admin;

    private TableContact tableContact;
    private TableRoom tableRoom;
    private TableMessage tableMessage;
    private TableParticipation tableParticipation;
    private TableStatus tableStatus;
    private Gson gson = new Gson();

    private WebSocket(AppCompatActivity appCompatActivity) {
        this.appCompatActivity = appCompatActivity;
        context = appCompatActivity.getApplicationContext();
        admin = Member.getInstance(context);
        tableContact = TableContact.getInstance(context);
        tableRoom = TableRoom.getInstance(context);
        tableMessage = TableMessage.getInstance(context);
        tableParticipation = TableParticipation.getInstance(context);
        tableStatus = TableStatus.getInstance(context);

        compositeDisposable = new CompositeDisposable();
        chatViewModel = ViewModelProviders.of(appCompatActivity, ChatViewModelFactory.getInstance(context)).get(ChatViewModel.class);

        if (!CheckConnection.haveNetworkConnection(context)) {
            count = 2;
            initRoomAndFriend();
        }
    }

    private void initRoomAndFriend() {
        if (count == 2) {
            //lay danh sach conversation
            Disposable subscribe = Completable.create(o -> {
                List<Room> conversation = tableRoom.getConversation();
                chatViewModel.addRooms(conversation, ChatViewModel.ALL_ROOM);
                o.onComplete();
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> chatViewModel.setFlagRoom(ChatViewModel.FLAG_INIT_CONVERSATION));
            compositeDisposable.add(subscribe);

            //lay danh sach many room
            Disposable subscribe1 = Completable.create(o -> {
                List<Room> conversation = tableRoom.getManyRoom();
                chatViewModel.addRooms(conversation, ChatViewModel.MANY_ROOM);
                o.onComplete();
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> chatViewModel.setFlagRoom(ChatViewModel.FLAG_INIT_CONVERSATION));
            compositeDisposable.add(subscribe1);

            //lay danh sach tat ca nhung contact la ban be trong cache
            Disposable subscribe2 = Completable.create(o -> {
                List<Contact> contactsFriend = tableContact.getContactsFriend();
                chatViewModel.addContacts(contactsFriend, ChatViewModel.CONTACTS_FRIEND);
                o.onComplete();
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> chatViewModel.setFlagContact(ChatViewModel.FLAG_INIT_CONTACT_FRIEND));
            compositeDisposable.add(subscribe2);
        }
    }

    public static WebSocket getInstance(AppCompatActivity appCompatActivity) {
        if (webSocket == null) {
            webSocket = new WebSocket(appCompatActivity);
        }
        return webSocket;
    }

    public void webSocketConnect() {
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, RetrofitClient.webSocketUrl);
        Disposable subscribe = stompClient.lifecycle().subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            Log.d("BBBBB", "Stomp connection opened");
                            initRooms();
                            break;
                        case ERROR:
                            Log.e("BBBBB", "Error cannot connect to server");
                            initRoomAndFriend();
                            break;
                        case CLOSED:
                            Log.d("BBBBB", "Stomp connection closed");
                            destroy();
                            break;
                        case FAILED_SERVER_HEARTBEAT:
                            Log.e("BBBBB", "Stomp failed server heartbeat");
                            break;
                    }
                },
                onError -> onError.getMessage());
        compositeDisposable.add(subscribe);
        stompClient.connect();
    }

    private void notifyOnlineToFriend(Contact contact) {
        // gui thong bao online den ban be
        Gson gson = new Gson();
        Map<String, Object> map = new HashMap<>();
        map.put("createdate", new Date().getTime());
        map.put("body", ChatMessage.StatusMessage.Online.name());
        map.put("friend", contact.getId());
        map.put("user", admin.getId());
        map.put("type", ChatMessage.TypeMessage.Notify.name());
        map.put("status", -1);
        Disposable subscribe2 = WebSocket.stompClient.send("/chat/notifyOnline/" + contact.getId()
                , gson.toJson(map))
                .subscribeOn(Schedulers.io())
                .subscribe(
                        () -> {
                            Log.d("BBBBB", "sucess");
                        },
                        onError -> Log.d("BBBBB", onError.getMessage())
                );
        compositeDisposable.add(subscribe2);
    }

    private void notifyOnlineToRoom(Room room) {
        // gui thong bao online den cac phong co tham gia
        ChatMessage chatMessage = new ChatMessage(new Date().getTime()
                , ChatMessage.StatusMessage.Online.name()
                , room.getId()
                , admin.getId()
        );
        chatMessage.setTypeMessage(ChatMessage.TypeMessage.Notify);
        chatMessage.setStatus(ChatMessage.StatusMessage.Online);
        Disposable subscribe2 = stompClient.send("/chat/chat.joinRoom/" + room.getId()
                , MappedMessageToJson.mapToOnlineOrOffLine(chatMessage))
                .subscribeOn(Schedulers.io())
                .subscribe(
                        () -> {
                            Log.d("BBBBB", "sucess");
                        },
                        onError -> Log.d("BBBBB", onError.getMessage())
                );
        compositeDisposable.add(subscribe2);
    }

    private void notifyConnectWebSocket() {
        // gui thong bao connect websocket
        Map<String, Object> map = new HashMap<>();
        map.put("user", admin.getId());
        if (WelcomActivity.flagLogin) {
            map.put("login", true);
        } else {
            map.put("login", false);
        }
        Disposable subscribe2 = stompClient.send("/chat/notifyConnect"
                , gson.toJson(map))
                .subscribeOn(Schedulers.io())
                .subscribe(
                        () -> {
                            Log.d("BBBBB", "sucess");
                        },
                        onError -> Log.d("BBBBB", onError.getMessage())
                );
        compositeDisposable.add(subscribe2);
    }

    public void destroy() {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
        if (stompClient != null) {
            stompClient.disconnect();
        }
        map.clear();
        count = 0;
        stompClient = null;
        webSocket = null;
    }

    public void initRooms() {
        Disposable subscribe = stompClient.topic("/room/user/" + admin.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(t -> {
                    Log.d("BBBBB", t.getPayload() + " room/user");
                    JSONObject jsonObject = new JSONObject(t.getPayload());
                    switch (jsonObject.getInt("status")) {
                        case -1:
                            handlerNotifyFriendOnline(jsonObject);
                            break;
                        case 8:
                            handlerCreateRoom(jsonObject.getJSONObject("data"));
                            break;
                        case 9:
                            handlerAddfriendRequest(jsonObject.getJSONArray("data"));
                            break;
                        case 10:
                            break;
                        case 11:
                            handlerMessageUnread(jsonObject.getJSONArray("data"));
                            break;
                        case 12:
                            handlerFindUser(jsonObject.getJSONObject("data"));
                            break;
                        case 13:
                            handlerLoadMoreMessage(jsonObject.getJSONArray("data"));
                            break;
//                        case 14:
//                            handlerFindRoom(jsonObject.getJSONObject("data"));
//                            break;
                        case 15:
                            handlerAgreeAddfriend(new JSONObject(jsonObject.getString("data")));
                            break;
                        case 16:
                            handlerDisagreeAddfriend(new JSONObject(jsonObject.getString("data")));
                            break;
                        case 17:
                            handlerRequestAddfriendOffline(jsonObject.getJSONArray("data"));
                            break;
                        case 18:
                            handlerResponseAddfriendOffline(jsonObject.getJSONArray("data"));
                            break;
                        case 19:
                            handlerResponseDisAgreeAddfriendOffline(jsonObject.getJSONArray("data"));
                            break;
                        case 21:
                            handlerCreateRoomAndFriend(jsonObject.getJSONObject("data"));
                            break;
                        case 23:
                            handlerInviteMember(jsonObject.getJSONObject("data"));
                            break;
                        case 24:
                            handlerLeaveRoom(jsonObject.getJSONObject("data"));
                            break;
                        case 25:
                            handlerUnFriend(jsonObject.getString("data"));
                            break;
                        case 26:
                            handlerUpdateRoom(jsonObject.getJSONObject("data"));
                            break;
                        case 27:
                            handlerFinUserHintNewRoom(jsonObject.getJSONObject("data"));
                            break;
                        case 28:
                            handlerCancelRequestAddFriend(jsonObject.getString("data"));
                            break;
                        case 29:
                            handlerCheckDetailStatusMessage(jsonObject.getJSONObject("data"));
                            break;
                        case 30:
                            handlerPostStatus(jsonObject.getJSONObject("data"));
                            break;
                        case 31:
                            handlerGetStatuses(jsonObject.getJSONArray("data"));
                            break;
                        case 32:
                            handlerLoadMoreStatuses(jsonObject.getJSONArray("data"));
                            break;
                    }
                });
        compositeDisposable.add(subscribe);

        //subcribe room
        Disposable subscribe2 = Observable.defer(() -> Observable.just(tableRoom.getRooms()))
                .subscribeOn(Schedulers.io())
                .subscribe(t -> {
                    if (t.size() > 0) {
                        for (Room room : t) {
                            subcribeRoom(room);
                        }
                    }
                    //thong bao ket noi websocket
                    notifyConnectWebSocket();
                });
        compositeDisposable.add(subscribe2);
    }

    private void handlerLoadMoreStatuses(JSONArray jsonArray) {
        if (jsonArray.length() > 0) {
            Disposable subscribe = Completable.create(o -> {
                List<Status> list = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject data = jsonArray.getJSONObject(i);
                    Status status = new Status();
                    int id = data.getInt("id");
                    long createdate = data.getLong("createdate");
                    String body = data.getString("body");

                    if (data.has("images")) {
                        JSONArray images = data.getJSONArray("images");
                        for (int j = 0; j < images.length(); j++) {
                            String url = images.getString(j);
                            status.getUrlImages().add(url);
                        }
                    }
                    status.setId(id);
                    status.setBody(body);
                    status.setCreatedate(createdate);

                    JSONObject user = data.getJSONObject("user");
                    Contact contact = new Contact();
                    contact.setId(user.getInt("id"));
                    contact.setPhone(user.getString("phone"));
                    contact.setName(user.getString("name"));
                    contact.setUrlavatar(user.getString("urlavatar"));

                    status.setUserid(contact);
//                    int userid = data.getInt("userid");
//                    if (userid == Member.getInstance(context).getId()) {
//                        Contact contact = new Contact();
//                        contact.setId(Member.getInstance(context).getId());
//                        contact.setName(Member.getInstance(context).getName());
//                        contact.setUrlavatar(Member.getInstance(context).getUrlavatar());
//                        contact.setPhone(Member.getInstance(context).getPhone());
//                        status.setUserid(contact);
//                    } else {
//                        Contact contact = tableContact.getContact(userid);
//                        status.setUserid(contact);
//                    }
                    list.add(status);
                }
                chatViewModel.addLoadMoreStatuses(list, ChatViewModel.STATUS_ALL);
                o.onComplete();
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> chatViewModel.setFlagStatus(ChatViewModel.FLAG_LOADMORE_STATUSES));
            compositeDisposable.add(subscribe);
        } else {
            chatViewModel.setFlagStatus(ChatViewModel.FLAG_LOADMORE_STATUSES);
        }
    }

    private void handlerGetStatuses(JSONArray jsonArray) {
        if (jsonArray.length() > 0) {
            Disposable subscribe = Completable.create(o -> {
                List<Status> list = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject data = jsonArray.getJSONObject(i);
                    Status status = new Status();
                    int id = data.getInt("id");
                    long createdate = data.getLong("createdate");
                    String body = data.getString("body");

                    if (data.has("images")) {
                        JSONArray images = data.getJSONArray("images");
                        for (int j = 0; j < images.length(); j++) {
                            String url = images.getString(j);
                            status.getUrlImages().add(url);
                        }
                    }
                    status.setId(id);
                    status.setBody(body);
                    status.setCreatedate(createdate);

                    JSONObject user = data.getJSONObject("user");
                    Contact contact = new Contact();
                    contact.setId(user.getInt("id"));
                    contact.setPhone(user.getString("phone"));
                    contact.setName(user.getString("name"));
                    contact.setUrlavatar(user.getString("urlavatar"));

                    status.setId(id);
                    status.setBody(body);
                    status.setCreatedate(createdate);
                    status.setUserid(contact);
//                    int userid = data.getInt("userid");
//                    if (userid == Member.getInstance(context).getId()) {
//                        Contact contact = new Contact();
//                        contact.setId(Member.getInstance(context).getId());
//                        contact.setName(Member.getInstance(context).getName());
//                        contact.setUrlavatar(Member.getInstance(context).getUrlavatar());
//                        contact.setPhone(Member.getInstance(context).getPhone());
//                        status.setUserid(contact);
//                    } else {
//                        Contact contact = tableContact.getContact(userid);
//                        status.setUserid(contact);
//                    }
                    list.add(status);
                }
//                chatViewModel.getStatuses(ChatViewModel.STATUS_ALL).clear();
//                chatViewModel.addStatuses(list, ChatViewModel.STATUS_ALL);
                list.removeAll(chatViewModel.getStatuses(ChatViewModel.STATUS_ALL));
                if (list.size() > 0) {
                    chatViewModel.addStatuses(list, ChatViewModel.STATUS_ALL);
                }
                o.onComplete();
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> chatViewModel.setFlagStatus(ChatViewModel.FLAG_GET_STATUSES));
            compositeDisposable.add(subscribe);
        } else {
            chatViewModel.setFlagStatus(ChatViewModel.FLAG_GET_STATUSES);
        }
    }

    private void handlerPostStatus(JSONObject data) {
        Status status = new Status();
        Disposable subscribe = Completable.create(o -> {
            int id = data.getInt("id");
            long createdate = data.getLong("createdate");
            String body = data.getString("body");

            if (data.has("images")) {
                JSONArray images = data.getJSONArray("images");
                for (int i = 0; i < images.length(); i++) {
                    String url = images.getString(i);
                    status.getUrlImages().add(url);
                }
            }
            status.setId(id);
            status.setBody(body);
            status.setCreatedate(createdate);

            JSONObject user = data.getJSONObject("user");
            Contact contact = new Contact();
            contact.setId(user.getInt("id"));
            contact.setPhone(user.getString("phone"));
            contact.setName(user.getString("name"));
            contact.setUrlavatar(user.getString("urlavatar"));
            status.setUserid(contact);
            tableStatus.saveStatus(status);
//            int userid = data.getInt("userid");
//            if (userid == Member.getInstance(context).getId()) {
//                Contact contact = new Contact();
//                contact.setId(Member.getInstance(context).getId());
//                contact.setName(Member.getInstance(context).getName());
//                contact.setUrlavatar(Member.getInstance(context).getUrlavatar());
//                contact.setPhone(Member.getInstance(context).getPhone());
//                status.setUserid(contact);
//                tableStatus.saveStatus(status);
//            } else {
//                Contact contact = tableContact.getContact(userid);
//                status.setUserid(contact);
//            }
            chatViewModel.addStatus(status, ChatViewModel.STATUS_ALL);
            o.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    if (status.getUserid().getId() != Member.getInstance(context).getId()) {
                        NotificationHelper.showNotCancel(context, "Tezamess", status.getUserid().getName()
                                + " " + context.getResources().getString(R.string.notify_status));
                        ((HomeActivity) appCompatActivity).showNotifiPostStatus(true);
                    }
                    chatViewModel.setFlagStatus(ChatViewModel.FLAG_POST_STATUS);
                });
        compositeDisposable.add(subscribe);
    }

    private void handlerCheckDetailStatusMessage(JSONObject data) {
        ManyRoomActivity.contactsReceived.clear();
        ManyRoomActivity.contactsSeen.clear();
        if (data.length() > 0) {
            Disposable subscribe = Completable.create(o -> {
                if (!data.isNull("received")) {
                    JSONArray received = data.getJSONArray("received");
                    for (int i = 0; i < received.length(); i++) {
                        JSONObject jsonObject = received.getJSONObject(i);
                        int id = jsonObject.getInt("id");
                        String name = jsonObject.getString("name");
                        String phone = jsonObject.getString("phone");
                        String avatar = jsonObject.getString("urlavatar");

                        Contact contact = new Contact();
                        contact.setId(id);
                        contact.setName(name);
                        contact.setPhone(phone);
                        contact.setUrlavatar(avatar);

                        ManyRoomActivity.contactsReceived.add(contact);
                    }
                }

                if (!data.isNull("seen")) {
                    JSONArray seen = data.getJSONArray("seen");
                    for (int i = 0; i < seen.length(); i++) {
                        JSONObject jsonObject = seen.getJSONObject(i);
                        int id = jsonObject.getInt("id");
                        String name = jsonObject.getString("name");
                        String phone = jsonObject.getString("phone");
                        String avatar = jsonObject.getString("urlavatar");

                        Contact contact = new Contact();
                        contact.setId(id);
                        contact.setName(name);
                        contact.setPhone(phone);
                        contact.setUrlavatar(avatar);

                        ManyRoomActivity.contactsSeen.add(contact);
                    }
                }

                o.onComplete();
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            () -> chatViewModel.setFlagMessage(ChatViewModel.FLAG_CHECK_DETAIL_STATUS_MESSAGE)
                    );
            compositeDisposable.add(subscribe);
        } else {
            ManyRoomActivity.contactsReceived.clear();
            ManyRoomActivity.contactsSeen.clear();
            chatViewModel.setFlagMessage(ChatViewModel.FLAG_CHECK_DETAIL_STATUS_MESSAGE);
        }
    }

    private void handlerCancelRequestAddFriend(String data) {
        try {
            JSONObject jsonObject = new JSONObject(data);
            int idRequest = jsonObject.getInt("idRequest");
            int idFriend = jsonObject.getInt("idFriend");

            Disposable subscribe = Completable.create(o -> {
                Contact contact = null;
                if (idRequest == admin.getId()) {
                    contact = tableContact.updateRelationship(idFriend, -1, 1);
                    chatViewModel.saveOrUpdateContact(contact, ChatViewModel.CONTACTS_NOT_FRIEND);
                }
                if (idFriend == admin.getId()) {
                    contact = tableContact.updateRelationship(idRequest, -1, 1);
                    chatViewModel.removeContact(contact, ChatViewModel.CONTACTS_INVITED_ADD_FRIEND);
                }

                o.onComplete();
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                        chatViewModel.setFlagContact(ChatViewModel.FLAG_CANCEL_REQUEST_ADDFRIEND);
                    });
            compositeDisposable.add(subscribe);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void handlerFinUserHintNewRoom(JSONObject data) {
        if (data.length() > 0) {
            Disposable subscribe = Completable.create(o -> {
                try {
                    int id = data.getInt("id");
                    String name = data.getString("name");
                    String phone = data.getString("phone");
                    String photo = data.getString("urlavatar");
                    long lastActive = data.getLong("lastactive");
                    long birthday = data.getLong("birthday");
                    boolean gender = data.getBoolean("gender");

                    Contact contact = new Contact();
                    contact.setId(id);
                    contact.setName(name);
                    contact.setPhone(phone);
                    contact.setUrlavatar(photo);
                    contact.setLastactive(lastActive);
                    contact.setBirthday(new Date(birthday));
                    contact.setGender(gender);

                    NewRoomActivity.tempContact = contact;

                    o.onComplete();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> chatViewModel.setFlagContact(ChatViewModel.FLAG_FIND_MEMBER));
            compositeDisposable.add(subscribe);

        } else {
            NewRoomActivity.tempContact = null;
            chatViewModel.setFlagContact(ChatViewModel.FLAG_FIND_MEMBER);
        }

    }

    private void handlerUpdateRoom(JSONObject data) {
        Disposable subscribe = Completable.create(o -> {
            try {
                int updater = data.getInt("sender");
                Room room = new Room();
                room.setId(data.getInt("id"));
                room.setName(data.getString("name"));
                room.setCreator(data.getInt("creator"));
                room.setType(data.getString("type"));
                room.setUrlAvatar(data.getString("avatar"));
                Room oldRoom = tableRoom.getRoomAndContacts(room.getId());
                tableRoom.updateRoom(room);

                List<ChatMessage> messages = new ArrayList<>();
                if (!oldRoom.getName().equals(room.getName())) {
                    oldRoom.setName(room.getName());
                    ChatMessage chatMessage = new ChatMessage(
                            -1
                            , new Date().getTime()
                            , context.getResources().getString(R.string.change_name_room)
                            , room.getId()
                            , updater
                            , ChatMessage.StatusMessage.Received
                            , ChatMessage.TypeMessage.Notify);
                    messages.add(chatMessage);
                }

                if (!oldRoom.getUrlAvatar().equals(room.getUrlAvatar())) {
                    oldRoom.setUrlAvatar(room.getUrlAvatar());
                    ChatMessage chatMessage = new ChatMessage(
                            -1
                            , new Date().getTime()
                            , context.getResources().getString(R.string.change_avatar_room)
                            , room.getId()
                            , updater
                            , ChatMessage.StatusMessage.Received
                            , ChatMessage.TypeMessage.Notify);
                    messages.add(chatMessage);
                }

                tableMessage.addChatMessages(messages);
                oldRoom.setLastChatMessage(messages.get(messages.size() - 1));
                //luu lai tong so luong tin nhan da tao
                chatViewModel.addPostQuantityUnreadMessages(messages.size());
                Room roomInViewModel = chatViewModel.getRoom(oldRoom.getId(), ChatViewModel.ALL_ROOM);
                if (roomInViewModel != null) {
                    oldRoom.setQuantityUnreadMessage(roomInViewModel.getQuantityUnreadMessage() + messages.size());
                }
                chatViewModel.addChatMessagesNotCheck(messages, room.getId());
                chatViewModel.saveOrUpdateRoom(oldRoom, ChatViewModel.ALL_ROOM);
                Room cloneRoom = new Room();
                cloneRoom.cloneRoom(oldRoom);
                chatViewModel.saveOrUpdateRoom(cloneRoom, ChatViewModel.MANY_ROOM);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            o.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    chatViewModel.setFlagRoom(ChatViewModel.FLAG_UPDATE_ROOM);
                });
        compositeDisposable.add(subscribe);
    }

    private void handlerUnFriend(String data) {
        try {
            JSONObject jsonObject = new JSONObject(data);
            int idRequest = jsonObject.getInt("idRequest");
            int idFriend = jsonObject.getInt("idFriend");

            Disposable subscribe = Completable.create(o -> {
                Contact contact = null;
                if (idRequest == admin.getId()) {
                    contact = tableContact.updateRelationship(idFriend, -1, 1);
                }
                if (idFriend == admin.getId()) {
                    contact = tableContact.updateRelationship(idRequest, -1, 1);
                }
                chatViewModel.addContact(contact, ChatViewModel.CONTACTS_NOT_FRIEND);
                chatViewModel.removeContact(contact, ChatViewModel.CONTACTS_FRIENDS_ONLINE);
                chatViewModel.removeContact(contact, ChatViewModel.CONTACTS_FRIEND);

                o.onComplete();
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                        chatViewModel.setFlagContact(ChatViewModel.FLAG_UNFRIEND);
                    });
            compositeDisposable.add(subscribe);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handlerLeaveRoom(JSONObject data) {
        Disposable subscribe = Completable.create(o -> {
            try {
                Room room = new Room();
                room.setId(data.getInt("id"));
                room.setName(data.getString("name"));
                room.setCreator(data.getInt("creator"));
                room.setType(data.getString("type"));
                room.setUrlAvatar(data.getString("avatar"));

                List<Contact> members = new ArrayList<>();
                JSONArray jsonMembers = data.getJSONArray("users");
                for (int j = 0; j < jsonMembers.length(); j++) {
                    JSONObject json = jsonMembers.getJSONObject(j);
                    int id = json.getInt("id");
                    String name = json.getString("name");
                    String phone = json.getString("phone");
                    String photo = json.getString("urlavatar");
                    long lastActive = json.getLong("lastactive");

                    Contact contact = new Contact();
                    contact.setId(id);
                    contact.setName(name);
                    contact.setPhone(phone);
                    contact.setUrlavatar(photo);
                    contact.setmRoomId(room.getId());
                    contact.setLastactive(lastActive);
                    members.add(contact);
                }

                if (!members.contains(new Contact(admin.getId()))) {

                    Disposable disposable = map.get(room.getId());
                    compositeDisposable.remove(disposable);
                    map.remove(room.getId());

                    tableRoom.deleteRoom(room.getId());
                    tableParticipation.deleteParticipations(room.getId());
                    tableMessage.deleteChatMessages(room.getId());
                    chatViewModel.removeChatMessages(room.getId());
                    chatViewModel.removeRoom(room, ChatViewModel.ALL_ROOM);
                    chatViewModel.removeRoom(room, ChatViewModel.MANY_ROOM);
                    chatViewModel.setPostFlagRoom(ChatViewModel.FLAG_REMOVE_CONVERSATION);
                } else {
                    room.setContacts(members);
                    room.setMembers(members.size());

                    //cap nhat lai room
                    tableRoom.updateRoom(room);

                    Room roomLocal = tableRoom.getRoomAndContacts(room.getId());
                    //danh sach user dang luu trong local
                    List<Contact> localContact = roomLocal.getContacts();

                    //danh sach nhung user khong con trong nhom
                    localContact.removeAll(members);

                    //xoa nhung user khong con trong nhom
                    tableParticipation.removeContacts(localContact);

                    //tao danh sach tin nhan nhung user da roi khoi nhom
                    List<ChatMessage> messages = new ArrayList<>();
                    for (Contact contact : localContact) {
                        ChatMessage chatMessage = new ChatMessage(
                                -1
                                , new Date().getTime()
                                , context.getResources().getString(R.string.left)
                                , contact.getmRoomId()
                                , contact.getId()
                                , ChatMessage.StatusMessage.Received
                                , ChatMessage.TypeMessage.Notify);
                        messages.add(chatMessage);
                    }
                    //luu tin nhan roi khoi nhom
                    tableMessage.addChatMessages(messages);
                    chatViewModel.addChatMessagesNotCheck(messages, room.getId());
                    //luu lai tong so luong tin nhan da tao
                    chatViewModel.addPostQuantityUnreadMessages(messages.size());
                    Room roomInViewModel = chatViewModel.getRoom(room.getId(), ChatViewModel.ALL_ROOM);
                    if (roomInViewModel != null) {
                        room.setQuantityUnreadMessage(roomInViewModel.getQuantityUnreadMessage() + messages.size());
                    }
                    room.setLastChatMessage(messages.get(messages.size() - 1));
                    chatViewModel.saveOrUpdateRoom(room, ChatViewModel.ALL_ROOM);
                    Room cloneRoom = new Room();
                    cloneRoom.cloneRoom(room);
                    chatViewModel.saveOrUpdateRoom(cloneRoom, ChatViewModel.MANY_ROOM);
                    chatViewModel.setPostFlagRoom(ChatViewModel.FLAG_LEAVE_ROOM);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).subscribeOn(Schedulers.io())
                .subscribe();
        compositeDisposable.add(subscribe);
    }

    private void handlerInviteMember(JSONObject data) {
        Disposable subscribe = Completable.create(o -> {
            try {
                Room room = new Room();
                room.setId(data.getInt("id"));
                room.setName(data.getString("name"));
                room.setCreator(data.getInt("creator"));
                room.setType(data.getString("type"));
                room.setUrlAvatar(data.getString("avatar"));

                List<Contact> members = new ArrayList<>();
                JSONArray jsonMembers = data.getJSONArray("users");
                for (int j = 0; j < jsonMembers.length(); j++) {
                    JSONObject json = jsonMembers.getJSONObject(j);
                    int id = json.getInt("id");
                    String name = json.getString("name");
                    String phone = json.getString("phone");
                    String photo = json.getString("urlavatar");
                    long lastActive = json.getLong("lastactive");

                    Contact contact = new Contact();
                    contact.setId(id);
                    contact.setName(name);
                    contact.setPhone(phone);
                    contact.setUrlavatar(photo);
                    contact.setLastactive(lastActive);
                    members.add(contact);
                }
                room.setContacts(members);
                room.setMembers(members.size());

                Room roomLocal = tableRoom.getRoomAndContacts(room.getId());
                //chua luu phong trong local
                if (roomLocal.getId() != room.getId()) {
                    ChatMessage chatMessage = new ChatMessage(
                            -1
                            , new Date().getTime()
                            , context.getResources().getString(R.string.joined)
                            , room.getId()
                            , admin.getId()
                            , ChatMessage.StatusMessage.Received
                            , ChatMessage.TypeMessage.Notify);
                    room.setLastChatMessage(chatMessage);
                    room.setQuantityUnreadMessage(1);
                    tableRoom.addRoom(room);
                    tableParticipation.addContacts(room.getContacts(), room.getId());
                    tableContact.saveOrUpdateContact(room.getContacts());
                    tableMessage.addChatMessages(Arrays.asList(chatMessage));

                    chatViewModel.saveOrUpdateRoom(room, ChatViewModel.ALL_ROOM);
                    Room cloneRoom = new Room();
                    cloneRoom.cloneRoom(room);
                    chatViewModel.saveOrUpdateRoom(cloneRoom, ChatViewModel.MANY_ROOM);
                    chatViewModel.addPostQuantityUnreadMessages(1);
                    chatViewModel.setPostFlagRoom(ChatViewModel.FLAG_UPDATE_CONVERSATION);

                    subcribeRoom(room);
                    notifyOnlineToRoom(room);

                    NotificationHelper.showNotCancel(context
                            , "Tezamess"
                            , context.getResources().getString(R.string.you)
                                    + context.getResources().getString(R.string.joined)
                                    + " \"" + room.getName() + "\"");


                } else { // da co trong local (chi them thanh vien moi)
                    room.getContacts().removeAll(roomLocal.getContacts());
                    tableParticipation.addContacts(room.getContacts(), room.getId());
                    tableContact.saveOrUpdateContact(room.getContacts());

                    List<ChatMessage> messages = new ArrayList<>();
                    for (Contact c : room.getContacts()) {
                        ChatMessage chatMessage = new ChatMessage(
                                -1
                                , new Date().getTime()
                                , context.getResources().getString(R.string.joined)
                                , room.getId()
                                , c.getId()
                                , ChatMessage.StatusMessage.Seen
                                , ChatMessage.TypeMessage.Notify);
                        messages.add(chatMessage);
                    }
                    tableMessage.addChatMessages(messages);
                    //luu lai tong so luong tin nhan da tao
                    chatViewModel.addPostQuantityUnreadMessages(messages.size());
                    Room roomInViewModel = chatViewModel.getRoom(room.getId(), ChatViewModel.ALL_ROOM);
                    if (roomInViewModel != null) {
                        room.setQuantityUnreadMessage(roomInViewModel.getQuantityUnreadMessage() + messages.size());
                    }
                    room.setLastChatMessage(messages.get(messages.size() - 1));
                    room.getContacts().addAll(roomLocal.getContacts());
                    chatViewModel.saveOrUpdateRoom(room, ChatViewModel.ALL_ROOM);
                    Room cloneRoom = new Room();
                    cloneRoom.cloneRoom(room);
                    chatViewModel.saveOrUpdateRoom(cloneRoom, ChatViewModel.MANY_ROOM);
                    chatViewModel.addChatMessagesNotCheck(messages, room.getId());
                    chatViewModel.setPostFlagRoom(ChatViewModel.FLAG_INVITE_MEMBER_INTO_ROOM);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).subscribeOn(Schedulers.io())
                .subscribe();
        compositeDisposable.add(subscribe);

    }

    private void handlerCreateRoomAndFriend(JSONObject data) {
        Disposable subscribe = Completable.create(o -> {
            try {
                JSONArray jsonRooms = data.getJSONArray("rooms");
                if (jsonRooms.length() > 0) {
                    List<Room> doubleRooms = new ArrayList<>();
                    List<Room> manyRooms = new ArrayList<>();

                    for (int i = 0; i < jsonRooms.length(); i++) {
                        JSONObject jsonRoom = jsonRooms.getJSONObject(i);
                        Room room = new Room();
                        room.setId(jsonRoom.getInt("id"));
                        room.setName(jsonRoom.getString("name"));
                        room.setCreator(jsonRoom.getInt("creator"));
                        room.setType(jsonRoom.getString("type"));
                        room.setUrlAvatar(jsonRoom.getString("avatar"));
                        room.setMembers(jsonRoom.getInt("size"));

                        List<Contact> members = new ArrayList<>();
                        JSONArray jsonMembers = jsonRoom.getJSONArray("members");
                        for (int j = 0; j < jsonMembers.length(); j++) {
                            JSONObject json = jsonMembers.getJSONObject(j);
                            int id = json.getInt("id");
                            switch (room.getType()) {
                                case "D":
                                    if (id == admin.getId())
                                        continue;
                                    break;
                                case "G":
                                    break;
                            }
                            String name = json.getString("name");
                            String phone = json.getString("phone");
                            String photo = json.getString("urlavatar");
                            long lastActive = json.getLong("lastactive");
                            Contact contact = new Contact();
                            contact.setId(id);
                            contact.setName(name);
                            contact.setPhone(phone);
                            contact.setUrlavatar(photo);
                            contact.setLastactive(lastActive);
                            contact.setmRoomId(room.getId());
                            members.add(contact);
                        }
                        room.setContacts(members);

                        switch (room.getType()) {
                            case "D":
                                doubleRooms.add(room);
                                break;
                            case "G":
                                manyRooms.add(room);
                                break;
                        }
                    }

                    //xu ly double room
                    List<Room> doubleRoomsNotContact = tableRoom.getDoubleRoomsNotContact();
                    doubleRooms.removeAll(doubleRoomsNotContact);
                    if (doubleRooms.size() > 0) {
                        tableRoom.addRoomsAndContact(doubleRooms);
                        for (Room room : doubleRooms) {
                            subcribeRoom(room);
                            notifyOnlineToRoom(room);
                        }
                    }

                    //---------------------------------------------------
                    //xu ly many room
                    List<Room> manyRoomsAndContacts = tableRoom.getManyRoomsAndContacts();
                    //danh sach nhung user moi cua tat ca cac phong
                    List<Contact> totalNewContact = new ArrayList<>();
                    //danh sach nhung user da roi khoi nhom chat cua tat ca cac phong
                    List<Contact> totalOldContact = new ArrayList<>();

                    List<ChatMessage> messages = new ArrayList<>();


                    for (Room room : manyRooms) {
                        int index = manyRoomsAndContacts.indexOf(room);
                        if (index != -1) { // tim thay room da luu trong local
                            Room roomLocal = manyRoomsAndContacts.get(index);
                            if (!room.getName().equals(roomLocal.getName()) ||
                                    !room.getUrlAvatar().equals(roomLocal.getUrlAvatar())) {
                                tableRoom.updateRoom(room);
                            }
                            //danh sach user thuc te trong phong
                            List<Contact> newContact = room.getContacts();
                            //danh sach user dang luu trong local
                            List<Contact> localContact = roomLocal.getContacts();
                            //sao chep lai danh sach dang luu trong local
                            List<Contact> tempLocalContact = new ArrayList<>(localContact);

                            //lay user chung giua danh sach thuc te va danh sach dang luu trong local
                            localContact.retainAll(newContact);

                            //xoa user chung de lay nhung user da roi khoi nhom
                            tempLocalContact.removeAll(localContact);
                            totalOldContact.addAll(tempLocalContact);

                            //xoa user chung de lay nhung user moi tham gia vao nhom
                            newContact.removeAll(localContact);
                            totalNewContact.addAll(newContact);
                        } else { // room moi tao chua luu trong local
                            tableRoom.addRoom(room);
                            tableParticipation.addContacts(room.getContacts());
                            for (Contact c : room.getContacts()) {
                                if (c.getId() != room.getCreator()) {
                                    ChatMessage chatMessage = new ChatMessage(
                                            -1
                                            , new Date().getTime()
                                            , context.getResources().getString(R.string.joined)
                                            , c.getmRoomId()
                                            , c.getId()
                                            , ChatMessage.StatusMessage.Received
                                            , ChatMessage.TypeMessage.Notify);
                                    messages.add(chatMessage);
                                }
                                c.setmRoomId(-1);
                            }
                            tableContact.saveOrUpdateContact(room.getContacts());
                            //tao danh sach tin nhan nhung user moi tham gia vao nhom
                            subcribeRoom(room);
                            notifyOnlineToRoom(room);
                            if (room.getCreator() != admin.getId()) {
                                NotificationHelper.showNotCancel(context
                                        , "Tezamess"
                                        , context.getResources().getString(R.string.you)
                                                + context.getResources().getString(R.string.joined)
                                                + " \"" + room.getName() + "\"");
                            }

                        }
                    }

                    //tao danh sach tin nhan nhung user da roi khoi nhom
                    for (Contact contact : totalOldContact) {
                        if (contact.getId() != admin.getId()) {
                            ChatMessage chatMessage = new ChatMessage(
                                    -1
                                    , new Date().getTime()
                                    , context.getResources().getString(R.string.left)
                                    , contact.getmRoomId()
                                    , contact.getId()
                                    , ChatMessage.StatusMessage.Received
                                    , ChatMessage.TypeMessage.Notify);
                            messages.add(chatMessage);
                        }
                    }
                    //tao danh sach tin nhan nhung user moi tham gia vao nhom
                    for (Contact contact : totalNewContact) {
                        if (contact.getId() != admin.getId()) {
                            ChatMessage chatMessage = new ChatMessage(
                                    -1
                                    , new Date().getTime()
                                    , context.getResources().getString(R.string.joined)
                                    , contact.getmRoomId()
                                    , contact.getId()
                                    , ChatMessage.StatusMessage.Received
                                    , ChatMessage.TypeMessage.Notify);
                            messages.add(chatMessage);
                        }
                    }
                    //luu lai tong so luong tin nhan da tao
                    chatViewModel.addPostQuantityUnreadMessages(messages.size());
                    tableParticipation.addContacts(totalNewContact);
                    tableParticipation.removeContacts(totalOldContact);
                    tableMessage.addChatMessages(messages);

                    for (Contact c : totalNewContact) {
                        c.setmRoomId(-1);
                    }
                    tableContact.saveOrUpdateContact(totalNewContact);


                    doubleRooms.clear();
                    manyRooms.clear();
                    totalNewContact.clear();
                    totalOldContact.clear();
                    manyRoomsAndContacts.clear();
                    messages.clear();
                }
//-------------------------------------------------------------------------
                JSONArray jsonFriends = data.getJSONArray("friends");
                if (jsonFriends.length() > 0) {
                    // danh sach ban be trong local
                    List<Contact> friends = tableContact.getContactsFriend();
                    // sao chep danh sach ban be trong local
                    List<Contact> tempFriends = new ArrayList<>(friends);
                    // danh sach ban be tren server
                    List<Contact> contactsFriend = new ArrayList<>();
                    for (int i = 0; i < jsonFriends.length(); i++) {
                        JSONObject object = jsonFriends.getJSONObject(i);
                        int idContact = object.getInt("id");
                        String name = object.getString("name");
                        String phone = object.getString("phone");
                        String photo = object.getString("urlavatar");
                        long lastActive = object.getLong("lastactive");

                        Contact contact = new Contact();
                        contact.setName(name);
                        contact.setPhone(phone);
                        contact.setUrlavatar(photo);
                        contact.setId(idContact);
                        contact.setmRoomId(-1);
                        contact.setmRelationship(1);
                        contact.setmStatusAddFriend(0);
                        contact.setLastactive(lastActive);
                        contactsFriend.add(contact);
                    }
                    //luu ban be moi va cap nhat lai danh sach ban be cu
                    tableContact.saveOrUpdateContact(contactsFriend);

                    //danh sach ban be chung giua local va server
                    friends.retainAll(contactsFriend);
                    //danh sach nhung nguoi khong con la ban be
                    tempFriends.removeAll(friends);

                    //thay doi trang thai ban be cu (xoa ban be)
                    for (Contact c : tempFriends) {
                        c.setmRelationship(-1);
                        c.setmStatusAddFriend(1);
                    }
                    if (tempFriends.size() > 0) {
                        tableContact.updateRelationship(tempFriends);
                    }
                    tempFriends.clear();
                    friends.clear();
                    contactsFriend.clear();

                }
                o.onComplete();

            } catch (JSONException e) {
                initRoomAndFriend();
                e.printStackTrace();
            }

        }).subscribeOn(Schedulers.io())
                .subscribe(() -> {
                    count++;
                    initRoomAndFriend();
                });

        compositeDisposable.add(subscribe);

    }

    private void handlerResponseDisAgreeAddfriendOffline(JSONArray data) {
        Disposable subscribe = Completable.create(o -> {
            try {
                List<Contact> contactsResponse = new ArrayList<>();
                int size = data.length();
                for (int i = 0; i < size; i++) {
                    Contact contact = new Contact();
                    JSONObject object = data.getJSONObject(i);
                    int idContact = object.getInt("id");
                    String name = object.getString("name");
                    String phone = object.getString("phone");
                    String urlavatar = object.getString("urlavatar");
                    long lastactive = object.getLong("lastactive");
                    contact.setId(idContact);
                    contact.setName(name);
                    contact.setPhone(phone);
                    contact.setUrlavatar(urlavatar);
                    contact.setLastactive(lastactive);
                    contact.setmStatusAddFriend(1);
                    contact.setmRelationship(-1);
                    contactsResponse.add(contact);
                }

                if (contactsResponse.size() > 0) {
                    tableContact.updateRelationship(contactsResponse);

                    //gui tin nhan da nhan duoc response addfriend
                    Map<String, Object> map = new HashMap<>();
                    map.put("idRequest", admin.getId());
                    map.put("status", -1);
                    Disposable success = stompClient
                            .send("/chat/response/receivedaddfriend", gson.toJson(map))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    () -> Log.d("BBBBB", "success"),
                                    onError -> Log.d("BBBBB", onError.getMessage())
                            );
                    compositeDisposable.add(success);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).subscribeOn(Schedulers.io())
                .subscribe();
        compositeDisposable.add(subscribe);

    }

    private void handlerResponseAddfriendOffline(JSONArray data) {
        try {
            List<Contact> contactsResponse = new ArrayList<>();
            int size = data.length();
            for (int i = 0; i < size; i++) {
                Contact contact = new Contact();
                JSONObject object = data.getJSONObject(i);
                int idContact = object.getInt("id");
                String name = object.getString("name");
                String phone = object.getString("phone");
                String urlavatar = object.getString("urlavatar");
                long lastactive = object.getLong("lastactive");
                contact.setId(idContact);
                contact.setName(name);
                contact.setPhone(phone);
                contact.setUrlavatar(urlavatar);
                contact.setLastactive(lastactive);
                contact.setmStatusAddFriend(0);
                contact.setmRelationship(1);
                contactsResponse.add(contact);
            }

            if (contactsResponse.size() > 0) {
                Disposable subscribe = Completable.create(t -> {
                    tableContact.saveOrUpdateContact(contactsResponse);
                    t.onComplete();
                }).subscribeOn(Schedulers.io())
                        .subscribe(() -> {
                            for (Contact c : contactsResponse) {
                                NotificationHelper.showNotCancel(context, "Tezamess"
                                        , context.getResources().getString(R.string.you) + " " +
                                                context.getResources().getString(R.string.and) + " " +
                                                c.getName() + " " +
                                                context.getResources().getString(R.string.title_agree_add_friend));
                            }
                            chatViewModel.addContacts(contactsResponse, ChatViewModel.CONTACTS_FRIEND);
                            chatViewModel.setPostFlagContact(ChatViewModel.FLAG_AGREE_ADDFRIEND);
                        });
                compositeDisposable.add(subscribe);


                //gui tin nhan da nhan duoc response addfriend
                Map<String, Object> map = new HashMap<>();
                map.put("idRequest", admin.getId());
                map.put("status", 1);
                Disposable success = stompClient
                        .send("/chat/response/receivedaddfriend", gson.toJson(map))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> Log.d("BBBBB", "success"),
                                onError -> Log.d("BBBBB", onError.getMessage())
                        );
                compositeDisposable.add(success);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handlerRequestAddfriendOffline(JSONArray data) {
        try {
            List<Contact> contactsRequest = new ArrayList<>();
            int size = data.length();
            for (int i = 0; i < size; i++) {
                Contact contact = new Contact();
                JSONObject object = data.getJSONObject(i);
                int idContact = object.getInt("id");
                String name = object.getString("name");
                String phone = object.getString("phone");
                String urlavatar = object.getString("urlavatar");
                long lastactive = object.getLong("lastactive");
                contact.setId(idContact);
                contact.setName(name);
                contact.setPhone(phone);
                contact.setUrlavatar(urlavatar);
                contact.setLastactive(lastactive);
                contact.setmStatusAddFriend(3);
                contactsRequest.add(contact);
            }
            if (contactsRequest.size() > 0) {
                Disposable subscribe = Completable.create(t -> {
                    tableContact.saveOrUpdateContact(contactsRequest);
                    t.onComplete();
                }).subscribeOn(Schedulers.io())
                        .subscribe(() -> {
                            chatViewModel.addContacts(contactsRequest, ChatViewModel.CONTACTS_INVITED_ADD_FRIEND);
                            chatViewModel.setPostFlagContact(ChatViewModel.FLAG_INVITED_ADDFRIEND_CONTACT);
                        });
                compositeDisposable.add(subscribe);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handlerDisagreeAddfriend(JSONObject jsonObject) {
        try {
            Contact contact = new Contact();
            // id friend dong y ket ban voi admin
            int idRequest = jsonObject.getInt("idRequest");
            // id admin
            int idFriend = jsonObject.getInt("idFriend");
            if (admin.getId() == idFriend) { // nhan duoc tin nhan dong y ket ban tu ban be minh yeu cau
                Log.d("BBBBB", " da nhan duoc tin khong dong y ket ban");
                contact.setId(idRequest);
                //cap nhat lai trang thai chua ban dau cua contact (trang thai chua gui yeu cau ket ban) trong cache
                Disposable subscribe = Observable.defer(() ->
                        Observable.just(tableContact.updateStatusAddfriend(idRequest, 1)))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(t -> {
                            // xoa ban be trong danh sach contact_not_friend
                            chatViewModel.updateContact(t, ChatViewModel.CONTACTS_NOT_FRIEND);
                            // cap nhat lai giao dien
                            chatViewModel.setFlagContact(ChatViewModel.FLAG_DISAGREE_ADDFRIEND);
                        });
                compositeDisposable.add(subscribe);

                //gui tin nhan da nhan duoc response disagree addfriend
                Map<String, Object> map = new HashMap<>();
                map.put("idRequest", admin.getId());
                map.put("status", -1);
                Disposable success = stompClient
                        .send("/chat/response/receivedaddfriend", gson.toJson(map))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> Log.d("BBBBB", "success"),
                                onError -> Log.d("BBBBB", onError.getMessage())
                        );
                compositeDisposable.add(success);
            }

            if (admin.getId() == idRequest) { // tin nhan dong y ket ban do chinh minh gui di
                Log.d("BBBBB", " da nhan duoc tin khong dong y ket ban do admin gui di");
                contact.setId(idFriend);
                //cap nhat lai trang thai da ket ban trong cache
                Disposable subscribe = Observable.defer(() ->
                        Observable.just(tableContact.updateStatusAddfriend(idFriend, 1)))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(t -> {
                            // xoa ban be trong danh sach contact_not_friend
                            chatViewModel.removeContact(t, ChatViewModel.CONTACTS_INVITED_ADD_FRIEND);
                            // cap nhat lai giao dien
                            chatViewModel.setFlagContact(ChatViewModel.FLAG_DISAGREE_ADDFRIEND);
                        });
                compositeDisposable.add(subscribe);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handlerAgreeAddfriend(JSONObject jsonObject) {
        try {
            Contact contact = new Contact();
            // id friend dong y ket ban voi admin
            int idRequest = jsonObject.getInt("idRequest");
            // id admin
            int idFriend = jsonObject.getInt("idFriend");
            if (admin.getId() == idFriend) { // nhan duoc tin nhan dong y ket ban tu ban be minh yeu cau
                Log.d("BBBBB", " da nhan duoc tin dong y");
                contact.setId(idRequest);
                //cap nhat lai trang thai da ket ban trong cache
                Disposable subscribe = Observable.defer(() ->
                        Observable.just(tableContact.updateRelationship(idRequest, 1, 0)))
                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(t -> {
                            // xoa ban be trong danh sach contact_not_friend
                            chatViewModel.removeContact(t, ChatViewModel.CONTACTS_NOT_FRIEND);
                            //them ban be vao danh sach ban be
                            chatViewModel.addContact(t, ChatViewModel.CONTACTS_FRIEND);
                            // cap nhat lai giao dien
                            chatViewModel.setPostFlagContact(ChatViewModel.FLAG_AGREE_ADDFRIEND);
                            notifyOnlineToFriend(t);
                            NotificationHelper.show(context, "Tezamess"
                                    , context.getResources().getString(R.string.you) + " " +
                                            context.getResources().getString(R.string.and) + " " +
                                            t.getName() + " " +
                                            context.getResources().getString(R.string.title_agree_add_friend));
                        });
                compositeDisposable.add(subscribe);

                //gui tin nhan da nhan duoc response addfriend
                Map<String, Object> map = new HashMap<>();
                map.put("idRequest", admin.getId());
                map.put("status", 1);
                Disposable success = stompClient
                        .send("/chat/response/receivedaddfriend", gson.toJson(map))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> Log.d("BBBBB", "success"),
                                onError -> Log.d("BBBBB", onError.getMessage())
                        );
                compositeDisposable.add(success);


            }

            if (admin.getId() == idRequest) { // tin nhan dong y ket ban do chinh minh gui di
                Log.d("BBBBB", " da nhan duoc tin dong y ket ban do admin gui di");
                contact.setId(idFriend);
                //cap nhat lai trang thai da ket ban trong cache
                Disposable subscribe = Observable.defer(() ->
                        Observable.just(tableContact.updateRelationship(idFriend, 1, 0)))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(t -> {
                            Log.d("BBBBB", "da cap nhat " + t.getmRelationship());
                            // xoa ban be trong danh sach contact_not_friend
                            chatViewModel.removeContact(t, ChatViewModel.CONTACTS_INVITED_ADD_FRIEND);
                            //them ban be vao danh sach ban be
                            chatViewModel.addContact(t, ChatViewModel.CONTACTS_FRIEND);
                            // cap nhat lai giao dien
                            chatViewModel.setFlagContact(ChatViewModel.FLAG_AGREE_ADDFRIEND);
                            notifyOnlineToFriend(t);
                        });
                compositeDisposable.add(subscribe);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

//    private void handlerFindRoom(JSONObject data) {
//        try {
//            int idRoom = data.getInt("id");
//            int creator = data.getInt("creator");
//            String name = data.getString("name");
//            String type = data.getString("type");
//            String urlAvatar = data.getString("avatar");
//
//            JSONArray users = data.getJSONArray("users");
//            int size = users.length();
//
//            Room room = new Room();
//            room.setId(idRoom);
//            room.setCreator(creator);
//            room.setType(type);
//            room.setUrlAvatar(urlAvatar);
//            room.setName(name);
//            room.setMembers(size);
//
//            Disposable subscribe1 = Observable.defer(() -> Observable.just(tableRoom.checkRoomExists(room.getId())))
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(c -> {
//                        if (!c) {
//                            tableRoom.addRoom(room);
//                            subcribeRoom(room);
//                            notifyOnlineToRoom(room);
//
//                            switch (room.getType()) {
//                                case "D": //xu ly phong 2 nguoi
//                                    for (int i = 0; i < size; i++) {
//                                        JSONObject jsonObject = users.getJSONObject(i);
//                                        int idContact = jsonObject.getInt("id");
//
//                                        if (idContact != admin.getId()) {
//                                            String phone = jsonObject.getString("phone");
//                                            String nameContact = jsonObject.getString("name");
//                                            String urlavatar = jsonObject.getString("urlavatar");
//                                            long lastactive = jsonObject.getLong("lastactive");
//
//                                            Contact contact = new Contact();
//                                            contact.setId(idContact);
//                                            contact.setmRoomId(room.getId());
//                                            contact.setName(nameContact);
//                                            contact.setPhone(phone);
//                                            contact.setUrlavatar(urlavatar);
//                                            contact.setLastactive(lastactive);
//
//                                            Disposable subscribe = Observable.defer(() ->
//                                                    Observable.just(tableContact.saveOrUpdateContact(contact)))
//                                                    .subscribeOn(Schedulers.io())
//                                                    .observeOn(AndroidSchedulers.mainThread())
//                                                    .subscribe(t -> {
//                                                        switch (t) {
//                                                            case ChatViewModel.CONTACTS_FRIEND:
//                                                                contact.setmRelationship(ChatViewModel.CONTACTS_FRIEND);
//                                                                chatViewModel.saveOrUpdateContact(contact, ChatViewModel.CONTACTS_FRIEND);
//                                                                break;
//                                                            case ChatViewModel.CONTACTS_NOT_FRIEND:
//                                                                contact.setmRelationship(ChatViewModel.CONTACTS_NOT_FRIEND);
//                                                                chatViewModel.saveOrUpdateContact(contact, ChatViewModel.CONTACTS_NOT_FRIEND);
//                                                                break;
//                                                        }
//                                                        room.setContacts(new ArrayList<>(Arrays.asList(contact)));
//                                                        room.setLastChatMessage(chatViewModel.tempMessage);
//                                                        room.setQuantityUnreadMessage(chatViewModel.quantity);
//                                                        chatViewModel.addRoom(room, ChatViewModel.ALL_ROOM);
////                                                        chatViewModel.setFlagRoom(ChatViewModel.FLAG_ADD_CONVERSATION);
//                                                        chatViewModel.setFlagRoom(ChatViewModel.FLAG_UPDATE_CONVERSATION);
//                                                        if (room.getType().equals("D")) {
//                                                            List<Contact> contacts = room.getContacts();
//                                                            for (Contact c1 : contacts) {
//                                                                if (c1.getId() == chatViewModel.tempMessage.getUser()) {
//                                                                    NotificationHelper.show(context, c1.getName(), chatViewModel.tempMessage.getBody());
//                                                                }
//                                                                return;
//                                                            }
//                                                        }
////                                                        chatViewModel.quantity = 0;
//                                                    });
//                                            compositeDisposable.add(subscribe);
//
//                                        }
//                                    }
//                                    break;
//                                case "G": //xu ly phong nhieu nguoi
//                                    break;
//                            }
//                        }
//                    });
//            compositeDisposable.add(subscribe1);
//
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    private void handlerNotifyFriendOnline(JSONObject jsonObject) {
        try {
            String type = jsonObject.getString("type");
            String status = jsonObject.getString("body");
            int id = jsonObject.getInt("user");
            Contact contact = new Contact();
            contact.setId(id);
            switch (type) {
                case NOTIFY:
                    switch (status) {
                        case ONLINE:
                            // them lien lac vao danh sach online
                            chatViewModel.addContact(contact, ChatViewModel.CONTACTS_ONLINE);

                            // gui lai phan hoi online khi nhan duoc thong bao online
                            Map<String, Object> map = new HashMap<>();
                            map.put("createdate", new Date().getTime());
                            map.put("body", ChatMessage.StatusMessage.Online.name());
                            map.put("friend", id);
                            map.put("user", admin.getId());
                            map.put("type", ChatMessage.TypeMessage.Response.name());
                            map.put("status", -1);
                            Disposable subscribe2 = WebSocket.stompClient.send("/chat/notifyOnline/" + id
                                    , gson.toJson(map))
                                    .subscribeOn(Schedulers.io())
                                    .subscribe(
                                            () -> {
                                                Log.d("BBBBB", "sucess");
                                            },
                                            onError -> Log.d("BBBBB", onError.getMessage())
                                    );

                            compositeDisposable.add(subscribe2);
                            break;
                        case OFFLINE:
                            //luu lai thoi gian offline
                            tableContact.updateLastActiveContact(contact.getId(), new Date().getTime());

                            //xoa contact ra khoi danh sach dang online
                            chatViewModel.removeContact(contact, ChatViewModel.CONTACTS_ONLINE);
                            break;
                    }
                    break;
                case RESPONSE:
                    switch (status) {
                        case ONLINE:
                            // them lien lac vao danh sach online
                            chatViewModel.addContact(contact, ChatViewModel.CONTACTS_ONLINE);
                            break;
                        case OFFLINE:
                            // truong hop nay khong xay ra
                            break;
                    }
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handlerLoadMoreMessage(JSONArray data) {
        try {
            List<ChatMessage> messages = new ArrayList<>();
            int length = data.length();
            for (int i = 0; i < length; i++) {
                JSONObject jsonObject = data.getJSONObject(i);
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setId(jsonObject.getInt("id"));
                chatMessage.setRoom(jsonObject.getInt("room"));
                chatMessage.setUser(jsonObject.getInt("user"));
                chatMessage.setCreatedate(jsonObject.getLong("createdate"));
                chatMessage.setBody(jsonObject.getString("body"));
                String type = jsonObject.getString("type");
                switch (type) {
                    case "Chat":
                        chatMessage.setTypeMessage(ChatMessage.TypeMessage.Chat);
                        break;
                    case "Image":
                        chatMessage.setTypeMessage(ChatMessage.TypeMessage.Image);
                        break;
                    case "File":
                        break;
                }
                messages.add(chatMessage);
            }
            if (messages.size() > 0) {
                chatViewModel.addLoadMoreChatMessages(messages, messages.get(0).getRoom());
                chatViewModel.setFlagMessage(ChatViewModel.FLAG_LOADMORE_MESSAGE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handlerMessageUnread(JSONArray data) {
        try {
            List<ChatMessage> messages = new ArrayList<>();
            int length = data.length();
            for (int i = 0; i < length; i++) {
                JSONObject jsonObject = data.getJSONObject(i);

                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setId(jsonObject.getInt("id"));
                chatMessage.setRoom(jsonObject.getInt("room"));
                chatMessage.setUser(jsonObject.getInt("user"));
                chatMessage.setCreatedate(jsonObject.getLong("createdate"));
                chatMessage.setBody(jsonObject.getString("body"));
                chatMessage.setStatus(ChatMessage.StatusMessage.Received);
                String type = jsonObject.getString("type");
                switch (type) {
                    case "Chat":
                        chatMessage.setTypeMessage(ChatMessage.TypeMessage.Chat);
                        break;
                    case "Image":
                        chatMessage.setTypeMessage(ChatMessage.TypeMessage.Image);
                        break;
                    case "File":
                        break;
                }
                messages.add(chatMessage);
            }
            int size = messages.size();
            if (size > 0) {
                Disposable subscribe = Completable.create(o -> {
                    // luu du lieu vao local
                    List<ChatMessage> l = new ArrayList<>(messages);
                    tableMessage.addChatMessagesAndCheckExist(l);
//                for (ChatMessage c : l) {
//                    chatViewModel.addChatMessageAndLastChatMessage(c, c.getRoom());
//                }
                    chatViewModel.addChatMessageAndLastChatMessages(l);

                    // them so luong tin nhan chua doc de hien thi giao dien
                    chatViewModel.addPostQuantityUnreadMessages(size);
                    //loc tin nhan cuoi cung cua moi phong
                    List<ChatMessage> list = new ArrayList<>();
                    for (int i = size - 1; i >= 0; i--) {
                        if (i == size - 1) {
                            list.add(messages.get(i));
                            continue;
                        }
                        for (int j = i; j <= size - 2; j++) {
                            if (messages.get(i).getRoom() != messages.get(j + 1).getRoom()) {
                                list.add(messages.get(i));
                                break;
                            }
                        }
                    }
                    for (ChatMessage lastMessage : list) {
                        //gui tin nhan phan hoi la da nhan tin nhan cuoi cung
                        Disposable subscribe2 = stompClient
                                .send("/chat/chat.sendMessageResponse/" + lastMessage.getRoom()
                                        , MappedMessageToJson.mapTo8Value(lastMessage, admin.getId()))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        () -> {
                                            Log.d("BBBBB", "sucess");
                                        },
                                        onError -> Log.d("BBBBB", onError.getMessage())
                                );
                        compositeDisposable.add(subscribe2);
                    }
                    o.onComplete();
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            count++;
                            initRoomAndFriend();
                        });
                compositeDisposable.add(subscribe);


            } else {
                count++;
                initRoomAndFriend();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void handlerAddfriendRequest(JSONArray data) {
        try {
            JSONObject objectRequest = data.getJSONObject(0);
            int idRequest = objectRequest.getInt("idRequest");

            JSONObject objectFriend = data.getJSONObject(1);
            int idFriend = objectFriend.getInt("idFriend");

            Contact contact = new Contact();

            if (admin.getId() == idRequest) {
                //gui
                Log.d("BBBBB", "da gui");
                contact.setId(idFriend);
                contact.setName(objectFriend.getString("name"));
                contact.setPhone(objectFriend.getString("phone"));
                contact.setUrlavatar(objectFriend.getString("urlavatar"));
                contact.setmStatusAddFriend(2);

                Disposable subscribe = Observable.defer(() -> Observable.just(tableContact.saveOrUpdateContact(contact)))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(t -> {
                            Log.d("BBBBB", t + " rela");
                            switch (t) {
                                case ChatViewModel.CONTACTS_NOT_FRIEND:
                                    contact.setmRelationship(ChatViewModel.CONTACTS_NOT_FRIEND);
                                    chatViewModel.saveOrUpdateContact(contact, ChatViewModel.CONTACTS_NOT_FRIEND);
                                    chatViewModel.setFlagContact(ChatViewModel.FLAG_INVITE_ADDFRIEND_CONTACT);
                                    break;
                            }
                        });
                compositeDisposable.add(subscribe);
            }

            if (admin.getId() == idFriend) {
                //nhan
                Log.d("BBBBB", "da nhan");
                contact.setId(idRequest);
                contact.setName(objectRequest.getString("name"));
                contact.setPhone(objectRequest.getString("phone"));
                contact.setUrlavatar(objectRequest.getString("urlavatar"));
                contact.setmStatusAddFriend(3);

                Disposable subscribe = Observable.defer(() -> Observable.just(tableContact.saveOrUpdateContact(contact)))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(t -> {
                            contact.setmRelationship(t);
                            chatViewModel.addContact(contact, ChatViewModel.CONTACTS_INVITED_ADD_FRIEND);
                            chatViewModel.setFlagContact(ChatViewModel.FLAG_INVITED_ADDFRIEND_CONTACT);
                            NotificationHelper.show(context
                                    , "Tezamess"
                                    , contact.getName() + " "
                                            + context.getResources().getString(R.string.content_add_friend));
                        });
                compositeDisposable.add(subscribe);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handlerCreateRoom(JSONObject data) {
        try {
            int idRoom = data.getInt("id");
            int creator = data.getInt("creator");
            String name = data.getString("name");
            String type = data.getString("type");
            String urlAvatar = data.getString("avatar");

            JSONArray users = data.getJSONArray("users");
            int size = users.length();

            Room room = new Room();
            room.setId(idRoom);
            room.setCreator(creator);
            room.setType(type);
            room.setName(name);
            room.setUrlAvatar(urlAvatar);
            room.setMembers(size);

            Disposable subscribe1 = Observable.defer(() -> Observable.just(tableRoom.checkRoomExists(room.getId())))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(c -> {
                        if (!c) {
                            tableRoom.addRoom(room);
                            subcribeRoom(room);
                            notifyOnlineToRoom(room);

                            switch (room.getType()) {
                                case "D": //xu ly phong 2 nguoi
                                    for (int i = 0; i < size; i++) {
                                        JSONObject jsonObject = users.getJSONObject(i);
                                        int idContact = jsonObject.getInt("id");

                                        if (idContact != admin.getId()) {
                                            String phone = jsonObject.getString("phone");
                                            String nameContact = jsonObject.getString("name");
                                            String urlavatar = jsonObject.getString("urlavatar");
                                            long lastactive = jsonObject.getLong("lastactive");

                                            Contact contact = new Contact();
                                            contact.setId(idContact);
                                            contact.setmRoomId(room.getId());
                                            contact.setName(nameContact);
                                            contact.setPhone(phone);
                                            contact.setUrlavatar(urlavatar);
                                            contact.setLastactive(lastactive);
                                            Disposable subscribe = Observable.defer(() ->
                                                    Observable.just(tableContact.saveOrUpdateContact(contact)))
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(t -> {
                                                        switch (t) {
                                                            case ChatViewModel.CONTACTS_FRIEND:
                                                                contact.setmRelationship(ChatViewModel.CONTACTS_FRIEND);
                                                                chatViewModel.saveOrUpdateContact(contact, ChatViewModel.CONTACTS_FRIEND);
                                                                break;
                                                            case ChatViewModel.CONTACTS_NOT_FRIEND:
                                                                contact.setmRelationship(ChatViewModel.CONTACTS_NOT_FRIEND);
                                                                chatViewModel.saveOrUpdateContact(contact, ChatViewModel.CONTACTS_NOT_FRIEND);
                                                                break;
                                                        }
                                                        chatViewModel.setFlagContact(ChatViewModel.FLAG_UPDATE_ROOM_CONTACT);
                                                    });
                                            compositeDisposable.add(subscribe);
                                        }
                                    }
                                    break;
                                case "G": //xu ly phong nhieu nguoi
                                    List<Contact> contacts = new ArrayList<>();
                                    for (int i = 0; i < size; i++) {
                                        JSONObject jsonObject = users.getJSONObject(i);
                                        int idContact = jsonObject.getInt("id");
                                        String phone = jsonObject.getString("phone");
                                        String nameContact = jsonObject.getString("name");
                                        String urlavatar = jsonObject.getString("urlavatar");
                                        long lastactive = jsonObject.getLong("lastactive");

                                        Contact contact = new Contact();
                                        contact.setId(idContact);
                                        contact.setName(nameContact);
                                        contact.setPhone(phone);
                                        contact.setUrlavatar(urlavatar);
                                        contact.setLastactive(lastactive);

                                        contacts.add(contact);

                                    }
                                    Disposable disposable = Completable.create(t -> {
                                        tableContact.addContacts(contacts);
                                        tableParticipation.addContacts(contacts, idRoom);
                                        t.onComplete();
                                    }).subscribeOn(Schedulers.io())
                                            .subscribe(() -> {
                                                room.setContacts(contacts);
                                                List<ChatMessage> messages = new ArrayList<>();
                                                for (Contact contact : contacts) {
                                                    if (contact.getId() != room.getCreator()) {
                                                        ChatMessage chatMessage = new ChatMessage(
                                                                -1
                                                                , new Date().getTime()
                                                                , context.getResources().getString(R.string.joined)
                                                                , room.getId()
                                                                , contact.getId()
                                                                , ChatMessage.StatusMessage.Received
                                                                , ChatMessage.TypeMessage.Notify);
                                                        messages.add(chatMessage);
                                                    }
                                                }
                                                room.setLastChatMessage(messages.get(messages.size() - 1));
                                                if (room.getCreator() != admin.getId()) {
                                                    room.setQuantityUnreadMessage(messages.size());
                                                    chatViewModel.addPostQuantityUnreadMessages(messages.size());
                                                    NotificationHelper.showNotCancel(context
                                                            , "Tezamess"
                                                            , context.getResources().getString(R.string.you)
                                                                    + context.getResources().getString(R.string.joined)
                                                                    + " \"" + room.getName() + "\"");
                                                }
                                                tableMessage.addChatMessages(messages);
                                                chatViewModel.addRoom(room, ChatViewModel.ALL_ROOM);
                                                chatViewModel.addRoom(room, ChatViewModel.MANY_ROOM);
                                                chatViewModel.setPostFlagRoom(ChatViewModel.FLAG_UPDATE_CONVERSATION);

                                            });
                                    compositeDisposable.add(disposable);
                                    break;
                            }
                        }
                    });
            compositeDisposable.add(subscribe1);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handlerFindUser(JSONObject data) {
        try {
            int id = data.getInt("id");
            String name = data.getString("name");
            String phone = data.getString("phone");
            String photo = data.getString("urlavatar");
            long lastActive = data.getLong("lastactive");
//            long birthday = data.getLong("birthday");
//            boolean gender = data.getBoolean("gender");

            Contact contact = new Contact();
            contact.setId(id);
            contact.setName(name);
            contact.setPhone(phone);
            contact.setUrlavatar(photo);
            contact.setLastactive(lastActive);
//            contact.setBirthday(new Date(birthday));
//            contact.setGender(gender);

            Disposable subscribe = Observable.defer(() ->
                    Observable.just(tableContact.saveOrUpdateContact(contact)))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(t -> {
                        switch (t) {
                            case ChatViewModel.CONTACTS_FRIEND:
                                contact.setmRelationship(ChatViewModel.CONTACTS_FRIEND);
                                chatViewModel.saveOrUpdateContact(contact, ChatViewModel.CONTACTS_FRIEND);
                                break;
                            case ChatViewModel.CONTACTS_NOT_FRIEND:
                                contact.setmRelationship(ChatViewModel.CONTACTS_NOT_FRIEND);
                                chatViewModel.saveOrUpdateContact(contact, ChatViewModel.CONTACTS_NOT_FRIEND);
                                break;
                        }
                        chatViewModel.setFlagContact(ChatViewModel.FLAG_UPDATE_CONTACT);
                    });
            compositeDisposable.add(subscribe);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void subcribeRoom(Room room) {
        Disposable subscribe1 = stompClient.topic("/room/" + room.getId())
                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(t -> {
                    Log.d("BBBBB", t.getPayload() + " 111111111111111 room" + room.getId());
                    JSONObject jsonObject = new JSONObject(t.getPayload());
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setRoom(jsonObject.getInt("room"));
                    chatMessage.setUser(jsonObject.getInt("user"));
                    chatMessage.setCreatedate(jsonObject.getLong("createdate"));
                    chatMessage.setBody(jsonObject.getString("body"));
                    String typeMessage = jsonObject.getString("type");
                    String statusMessage = jsonObject.getString("status");

                    if (typeMessage.equals(ChatMessage.TypeMessage.Notify.name())) {
                        chatMessage.setTypeMessage(ChatMessage.TypeMessage.Notify);
                    } else if (typeMessage.equals(ChatMessage.TypeMessage.Response.name())) {
                        chatMessage.setTypeMessage(ChatMessage.TypeMessage.Response);
                    } else if (typeMessage.equals(ChatMessage.TypeMessage.Chat.name())) {
                        chatMessage.setTypeMessage(ChatMessage.TypeMessage.Chat);
                    } else if (typeMessage.equals(ChatMessage.TypeMessage.Image.name())) {
                        chatMessage.setTypeMessage(ChatMessage.TypeMessage.Image);
                    }

                    if (statusMessage.equals(ChatMessage.StatusMessage.Sent.name())) {
                        chatMessage.setStatus(ChatMessage.StatusMessage.Sent);
                    } else if (statusMessage.equals(ChatMessage.StatusMessage.Received.name())) {
                        chatMessage.setStatus(ChatMessage.StatusMessage.Received);
                    } else if (statusMessage.equals(ChatMessage.StatusMessage.Seen.name())) {
                        chatMessage.setStatus(ChatMessage.StatusMessage.Seen);
                    } else if (statusMessage.equals(ChatMessage.StatusMessage.Online.name())) {
                        chatMessage.setStatus(ChatMessage.StatusMessage.Online);
                    } else if (statusMessage.equals(ChatMessage.StatusMessage.Offline.name())) {
                        chatMessage.setStatus(ChatMessage.StatusMessage.Offline);
                    }

                    //kiem tra loai tin nhan nhan duoc (chat,notify,response)
                    switch (typeMessage) {
                        case IMAGE:
                        case CHAT:
                            chatMessage.setId(jsonObject.getInt("id"));
                            //kiem tra trang thai tin nhan nhan duoc (sent,received,seen)
                            switch (statusMessage) {
                                case SENT:
                                    //kiem tra tin nhan co phai cua minh gui hay khong
                                    if (chatMessage.getUser() == admin.getId()) { // tin nhan la minh gui
                                        //thong bao da gui tin nhan thanh cong
                                        chatViewModel.flagCheckMessageError = true;
                                        //cap nhat danh sach moi de hien thi giao dien
                                        chatViewModel.updateChatMessage(chatMessage, chatMessage.getRoom());
//
                                        chatViewModel.addChatMessageAndLastChatMessage(chatMessage, chatMessage.getRoom());
                                    } else { // tin nhan khong phai minh gui
                                        //thiet lap tin nhan sang trang thai da nhan
                                        chatMessage.setStatus(ChatMessage.StatusMessage.Received);

                                        //gui lai tin nhan thong bao da nhan duoc tin nhan tu phong chat
                                        Disposable subscribe2 = stompClient
                                                .send("/chat/chat.sendMessageResponse/" + chatMessage.getRoom()
                                                        , MappedMessageToJson.mapTo8Value(chatMessage, admin.getId()))
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(
                                                        () -> {
                                                            Log.d("BBBBB", "sucess");
                                                        },
                                                        onError -> Log.d("BBBBB", onError.getMessage())
                                                );
                                        compositeDisposable.add(subscribe2);


//                                        Contact contact = new Contact();
//                                        contact.setId(chatMessage.getUser());
//                                        contact.setmRoomId(chatMessage.getRoom());
//                                        chatViewModel.addContact(contact, ChatViewModel.CONTACTS_ONLINE);

                                        // them so luong tin nhan chua doc de hien thi giao dien
                                        chatViewModel.addPostQuantityUnreadMessages(1);
                                        //add tin nhan vao viewmodel
                                        chatViewModel.addChatMessageAndLastChatMessage(chatMessage, chatMessage.getRoom());
                                    }
                                    //add tin nhan vao db
                                    tableMessage.addChatMessage(chatMessage);

                                    break;
                                //received va seen la giong nhau (chi can cap nhat lai trang thai tin nhan trong danh sach
                                // va cap nhat giao dien)
                                case RECEIVED:
                                    //cap nhat lai tin nhan trong viewmodel
                                    chatViewModel.updateChatMessage(chatMessage, chatMessage.getRoom());
                                    if (chatMessage.getUser() == admin.getId()) {
                                        //set co thong bao cho roomactivity xu ly
                                        chatViewModel.setPostFlagMessage(ChatViewModel.FLAG_UPDATE_MESSAGE);
                                    }
                                    //cap nhat lai tin nhan trong db
                                    tableMessage.updateStatusChatMessages(chatMessage);
                                    break;
                                case SEEN:
                                    //cap nhat lai tin nhan trong viewmodel
                                    chatViewModel.updateChatMessage(chatMessage, chatMessage.getRoom());
                                    if (chatMessage.getUser() == admin.getId()) {

                                        //set co thong bao cho roomactivity xu ly
                                        chatViewModel.setPostFlagMessage(ChatViewModel.FLAG_UPDATE_MESSAGE);
                                    }
                                    //cap nhat lai tin nhan trong db
                                    tableMessage.updateStatusChatMessages(chatMessage);
                                    break;
                            }
                            break;
                        case NOTIFY:
                            // co 2 loai tin nhan notify( tin nhan thong bao do minh gui va tin nhan thong bao minh nhan
                            // duoc tu nguoi khac)
                            // chi xu ly nhung tin nhan nguoi khac thong bao den cho minh
                            if (chatMessage.getUser() != admin.getId()) {
                                Contact contact = new Contact();
                                contact.setId(chatMessage.getUser());
                                contact.setmRoomId(chatMessage.getRoom());

                                switch (statusMessage) {
                                    case ONLINE:
                                        // them lien lac vao danh sach online
                                        chatViewModel.addContact(contact, ChatViewModel.CONTACTS_ONLINE);
                                        // gui lai phan hoi online khi nhan duoc thong bao online
                                        ChatMessage chat = new ChatMessage(new Date().getTime()
                                                , ChatMessage.TypeMessage.Response.name()
                                                , room.getId()
                                                , admin.getId()
                                        );
                                        chat.setTypeMessage(ChatMessage.TypeMessage.Response);
                                        chat.setStatus(ChatMessage.StatusMessage.Online);
                                        Disposable subscribe2 = stompClient.send("/chat/chat.joinRoom/" + room.getId()
                                                , MappedMessageToJson.mapToOnlineOrOffLine(chat))
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(
                                                        () -> {
                                                            Log.d("BBBBB", "sucess");
                                                        },
                                                        onError -> Log.d("BBBBB", onError.getMessage())
                                                );
                                        compositeDisposable.add(subscribe2);
                                        break;
                                    case OFFLINE:
                                        //luu lai thoi gian offline
                                        tableContact.updateLastActiveContact(contact.getId(), new Date().getTime());

                                        //xoa contact ra khoi danh sach dang online
                                        chatViewModel.removeContact(contact, ChatViewModel.CONTACTS_ONLINE);
                                        break;
                                }
                            }
                            break;
                        case RESPONSE:
                            // co 2 loai tin nhan response( tin nhan phan hoi do minh gui va tin nhan phan hoi minh nhan
                            // duoc tu nguoi khac)
                            // chi xu ly nhung tin nhan nguoi khac phan hoi lai cho minh
                            if (chatMessage.getUser() != admin.getId()) {

                                Contact contact = new Contact();
                                contact.setId(chatMessage.getUser());
                                contact.setmRoomId(chatMessage.getRoom());

                                switch (statusMessage) {
                                    case ONLINE:
                                        // them lien lac vao danh sach online
                                        chatViewModel.addContact(contact, ChatViewModel.CONTACTS_ONLINE);
                                        break;
                                    case OFFLINE:
                                        // truong hop nay khong xay ra
                                        break;
                                }
                            }
                            break;
                    }
                });
        compositeDisposable.add(subscribe1);

        map.put(room.getId(), subscribe1);
    }
}