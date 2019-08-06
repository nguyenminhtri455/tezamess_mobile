package com.example.appchat.viewmodel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;

import com.example.appchat.R;
import com.example.appchat.database.TableContact;
import com.example.appchat.database.TableMessage;
import com.example.appchat.database.TableRoom;
import com.example.appchat.objectclass.ChatMessage;
import com.example.appchat.objectclass.Contact;
import com.example.appchat.objectclass.Member;
import com.example.appchat.objectclass.Room;
import com.example.appchat.objectclass.Status;
import com.example.appchat.websocket.WebSocket;
import com.example.appchat.widget.notification.NotificationHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ChatViewModel extends ViewModel {

    private Context context;
    private TableContact tableContact;
    private TableRoom tableRoom;
    private TableMessage tableMessage;

    //cac loai danh sach contact
    public static final int CONTACTS_NOT_FRIEND = -1;
    public static final int CONTACTS_FRIEND = 1;
    public static final int CONTACTS_INVITED_ADD_FRIEND = 2;
    public static final int CONTACTS_ONLINE = 3;
    public static final int CONTACTS_FRIENDS_ONLINE = 4;

    //cac loai danh sach room
    //danh sach tat ca cac phong co tin nhan
    public static final int ALL_ROOM = 50;
    public static final int DOUBLE_ROOM = 51;
    public static final int MANY_ROOM = 52;

    //cac loai danh sach status
    public static final int STATUS_OF_ME = 700;
    public static final int STATUS_ALL = 701;


    public static final int FLAG_DEFAULT = -10;

    //flag message theo muc dich
    public static final int FLAG_LOADMORE_MESSAGE = 100;
    public static final int FLAG_UPDATE_MESSAGE = 101;
    public static final int FLAG_CHECK_DETAIL_STATUS_MESSAGE = 102;

    //flag contact theo muc dich
    public static final int FLAG_UPDATE_CONTACT = 200;
    public static final int FLAG_UPDATE_ROOM_CONTACT = 201;
    public static final int FLAG_NOTIFY_ONLINE_OFFLINE_CONTACT = 202;
    public static final int FLAG_INVITE_ADDFRIEND_CONTACT = 210;
    public static final int FLAG_INVITED_ADDFRIEND_CONTACT = 211;
    public static final int FLAG_INIT_CONTACT_FRIEND = 212;
    public static final int FLAG_AGREE_ADDFRIEND = 213;
    public static final int FLAG_DISAGREE_ADDFRIEND = 214;
    public static final int FLAG_UNFRIEND = 215;
    public static final int FLAG_FIND_MEMBER = 216;
    public static final int FLAG_CANCEL_REQUEST_ADDFRIEND = 217;

    //flag room theo muc dich
    public static final int FLAG_INIT_CONVERSATION = 300;
    public static final int FLAG_INIT_MANY_ROOM = 301;
    public static final int FLAG_RESET_UNREAD_CONVERSATION = 302;
    public static final int FLAG_ADD_CONVERSATION = 303;
    public static final int FLAG_REMOVE_CONVERSATION = 304;
    public static final int FLAG_CHANGE_LAST_MESSAGE_CONVERSATION = 305;
    public static final int FLAG_UPDATE_CONVERSATION = 311;
    public static final int FLAG_RESUM_ROOM = 306;
    public static final int FLAG_INVITE_MEMBER_INTO_ROOM = 307;
    public static final int FLAG_INVITED_INTO_ROOM = 308;
    public static final int FLAG_LEAVE_ROOM = 309;
    public static final int FLAG_UPDATE_ROOM = 310;

    //flag status theo muc dich
    public static final int FLAG_POST_STATUS = 400;
    public static final int FLAG_GET_STATUSES = 401;
    public static final int FLAG_LOADMORE_STATUSES = 402;

    public ChatViewModel(Context context) {
        this.context = context;
        tableContact = TableContact.getInstance(context);
        tableRoom = TableRoom.getInstance(context);
        tableMessage = TableMessage.getInstance(context);
        quantity_unread_message.setValue(0);
        setFlagContact(FLAG_DEFAULT);
        setFlagRoom(FLAG_DEFAULT);
        setFlagMessage(FLAG_DEFAULT);
    }

    //danh sach chua tin nhan cua cac phong
    public Map<Integer, List<ChatMessage>> mapMessages = new HashMap<>();

    //danh sach chua contact theo phan loai
    public Map<Integer, List<Contact>> mapContacts = new HashMap<>();

    //danh sach chua cac room theo phan loai
    public Map<Integer, List<Room>> mapRooms = new HashMap<>();

    //danh sach chua status
    public Map<Integer, List<Status>> mapStatus = new HashMap<>();

    ////////////////cac bien can lang nghe
    //flag thong bao co su thay doi danh sach tin nhan
    public MutableLiveData<Integer> flagMessage = new MutableLiveData<>();

    //flag thong bao co su thay doi danh sach contact
    public MutableLiveData<Integer> flagContact = new MutableLiveData<>();

    //flag thong bao co su thay doi danh sach room
    public MutableLiveData<Integer> flagRoom = new MutableLiveData<>();

    //flag thong bao cac su kien khi dang status
    public MutableLiveData<Integer> flagStatus = new MutableLiveData<>();

    //flag thong bao co su thay doi danh sach contact
    public MutableLiveData<Integer> quantity_unread_message = new MutableLiveData<>();

    ////////////////cac bien tam khong can lang nghe
    //message tam
    public ChatMessage tempMessage = new ChatMessage();

    //room tam
    public int tempIdRoom = -1;

    //flag kiem tra roomactivity co ton tai hay khong (-2 la khong ton tai)
    public int flagCheckRoomExist = -2;

    //flag kiem tra tin nhan da gui thanh cong hay khong ? (true la thanh cong, false la that bai)
    public boolean flagCheckMessageError = false;

    //------------------------------------------------
    public int getFlagContact() {
        return flagContact.getValue();
    }

    public void setFlagContact(int flag) {
        flagContact.setValue(flag);
    }

    public void setPostFlagContact(int flag) {
        flagContact.postValue(flag);
    }

    //---------------------
    public int getFlagMessage() {
        return flagMessage.getValue();
    }

    public void setFlagMessage(int flag) {
        flagMessage.setValue(flag);
    }

    public void setPostFlagMessage(int flag) {
        flagMessage.postValue(flag);
    }

    //---------------------
    public int getFlagStatus() {
        return flagStatus.getValue();
    }

    public void setFlagStatus(int flag) {
        flagStatus.setValue(flag);
    }

    public void setPostFlagStatus(int flag) {
        flagStatus.postValue(flag);
    }

    //---------------------
    public int getFlagRoom() {
        return flagRoom.getValue();
    }

    public void setFlagRoom(int flag) {
        flagRoom.setValue(flag);
    }

    public void setPostFlagRoom(int flag) {
        flagRoom.postValue(flag);
    }

    //---------------------
    public int getQuantityUnreadMessages() {
        return quantity_unread_message.getValue();
    }

    public void addQuantityUnreadMessages(int quantity) {
        quantity_unread_message.setValue(quantity_unread_message.getValue() + quantity);
    }

    public void addPostQuantityUnreadMessages(int quantity) {
        quantity_unread_message.postValue(quantity_unread_message.getValue() + quantity);
    }


    public void subtractTotalQuantityUnreadMessages(int quantity) {
        int total = quantity_unread_message.getValue() - quantity;
        if (total <= 0) {
            total = 0;
        }
        quantity_unread_message.setValue(total);
    }

    //-------------------------------------
    //mapMessages
    public List<ChatMessage> getChatMessages(int idRoom) {
        if (!mapMessages.containsKey(idRoom)) {
            mapMessages.put(idRoom, new ArrayList<>());
        }
        return mapMessages.get(idRoom);
    }

    public void addChatMessageAndLastChatMessage(ChatMessage chatMessage, int idRoom) {
        Disposable subscribe = Completable.create(o -> {
            if (!mapMessages.containsKey(idRoom)) {
                //load 20 tin nhan tu cache neu co vao viewmodel
                List<ChatMessage> messages = tableMessage.getChatMessages(idRoom, 0, 20);
                mapMessages.put(idRoom, messages);
                if (!mapMessages.get(idRoom).contains(chatMessage)) {
                    mapMessages.get(idRoom).add(chatMessage);
                }

            } else {
                List<ChatMessage> messages = mapMessages.get(idRoom);
                if (!messages.contains(chatMessage)) {
                    messages.add(chatMessage);
                }

            }
            //gan tin nhan vao tempMessage cho roomactivity xu ly
            tempMessage = chatMessage;

            Room room = getRoom(chatMessage.getRoom(), ALL_ROOM);
            if (room == null) {
                room = tableRoom.getRoomAndContacts(chatMessage.getRoom());
                addRoom(room, ALL_ROOM);
            } else {
                List<Room> rooms = getRooms(ALL_ROOM);
                int index = rooms.indexOf(room);
                if (index != 0) {
                    rooms.remove(index);
                    rooms.add(0, room);
                }
            }

            room.setLastChatMessage(chatMessage);
            if (chatMessage.getUser() != Member.getInstance(context).getId()) {
                room.increaseQuantityUnreadMessage(1);
            }

            if (room.getType().equals("G")) {
                List<Room> manyRooms = getRooms(MANY_ROOM);
                Room manyRoom = getRoom(room.getId(), MANY_ROOM);
                int index = manyRooms.indexOf(manyRoom);
                if (index != -1 && index != 0) {
                    manyRooms.remove(index);
                    manyRooms.add(0, manyRoom);
                }
                if (manyRoom != null) {
                    manyRoom.setLastChatMessage(chatMessage);
                    if (chatMessage.getUser() != Member.getInstance(context).getId()) {
                        manyRoom.increaseQuantityUnreadMessage(1);
                    }
                }
            }

            tempIdRoom = chatMessage.getRoom();
            setPostFlagRoom(FLAG_UPDATE_CONVERSATION);

            if (flagCheckRoomExist != room.getId()) {
                List<Contact> contacts = room.getContacts();
                for (Contact contact : contacts) {
                    if (contact.getId() == chatMessage.getUser()) {
                        switch (room.getType()) {
                            case "D":
                                if (chatMessage.getTypeMessage().equals(ChatMessage.TypeMessage.Chat)) {
                                    NotificationHelper.show(context, contact.getName(), chatMessage.getBody());
                                } else {
                                    NotificationHelper.show(context, contact.getName(), context.getResources().getString(R.string.image));
                                }

                                break;
                            case "G":
                                if (chatMessage.getTypeMessage().equals(ChatMessage.TypeMessage.Chat)) {
                                    NotificationHelper.show(context, room.getName()
                                            , contact.getName() + ": " + chatMessage.getBody());
                                } else {
                                    NotificationHelper.show(context, room.getName()
                                            , contact.getName() + ": " + context.getResources().getString(R.string.image));
                                }
                                break;
                        }
                        break;
                    }
                }
            }
            o.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    //set co thong bao cho roomactivity xu ly
                    setFlagMessage(ChatViewModel.FLAG_UPDATE_MESSAGE);
                });
        WebSocket.compositeDisposable.add(subscribe);
    }

    public void addChatMessageAndLastChatMessages(List<ChatMessage> chatMessages) {
        for (ChatMessage chatMessage : chatMessages) {
            if (!mapMessages.containsKey(chatMessage.getRoom())) {
                //load 20 tin nhan tu cache neu co vao viewmodel
                List<ChatMessage> messages = tableMessage.getChatMessages(chatMessage.getRoom(), 0, 20);
                mapMessages.put(chatMessage.getRoom(), messages);
                if (!mapMessages.get(chatMessage.getRoom()).contains(chatMessage)) {
                    mapMessages.get(chatMessage.getRoom()).add(chatMessage);
                }

            } else {
                List<ChatMessage> messages = mapMessages.get(chatMessage.getRoom());
                if (!messages.contains(chatMessage)) {
                    messages.add(chatMessage);
                }

            }
//                if (mapMessages.containsKey(chatMessage.getRoom())) {
//                    List<ChatMessage> messages = mapMessages.get(chatMessage.getRoom());
//                    if (!messages.contains(chatMessage)) {
//                        messages.add(chatMessage);
//                    }
//                }
            //gan tin nhan vao tempMessage cho roomactivity xu ly
            tempMessage = chatMessage;

            Room room = getRoom(chatMessage.getRoom(), ALL_ROOM);
            if (room == null) {
                room = tableRoom.getRoomAndContacts(chatMessage.getRoom());
                addRoom(room, ALL_ROOM);
            } else {
                List<Room> rooms = getRooms(ALL_ROOM);
                int index = rooms.indexOf(room);
                if (index != 0) {
                    rooms.remove(index);
                    rooms.add(0, room);
                }
            }

            room.setLastChatMessage(chatMessage);
            if (chatMessage.getUser() != Member.getInstance(context).getId()) {
                room.increaseQuantityUnreadMessage(1);
            }

            if (room.getType().equals("G")) {
                List<Room> manyRooms = getRooms(MANY_ROOM);
                Room manyRoom = getRoom(room.getId(), MANY_ROOM);
                int index = manyRooms.indexOf(manyRoom);
                if (index != -1 && index != 0) {
                    manyRooms.remove(index);
                    manyRooms.add(0, manyRoom);
                }
                if (manyRoom != null) {
                    manyRoom.setLastChatMessage(chatMessage);
                    if (chatMessage.getUser() != Member.getInstance(context).getId()) {
                        manyRoom.increaseQuantityUnreadMessage(1);
                    }
                }
            }

            tempIdRoom = chatMessage.getRoom();
            setPostFlagRoom(FLAG_UPDATE_CONVERSATION);

            if (flagCheckRoomExist != room.getId()) {
                List<Contact> contacts = room.getContacts();
                for (Contact contact : contacts) {
                    if (contact.getId() == chatMessage.getUser()) {
                        switch (room.getType()) {
                            case "D":
                                if (chatMessage.getTypeMessage().equals(ChatMessage.TypeMessage.Chat)) {
                                    NotificationHelper.show(context, contact.getName(), chatMessage.getBody());
                                } else {
                                    NotificationHelper.show(context, contact.getName(), context.getResources().getString(R.string.image));
                                }

                                break;
                            case "G":
                                if (chatMessage.getTypeMessage().equals(ChatMessage.TypeMessage.Chat)) {
                                    NotificationHelper.show(context, room.getName()
                                            , contact.getName() + ": " + chatMessage.getBody());
                                } else {
                                    NotificationHelper.show(context, room.getName()
                                            , contact.getName() + ": " + context.getResources().getString(R.string.image));
                                }
                                break;
                        }
                        break;
                    }
                }
            }

        }
    }

    public void addLoadMoreChatMessages(List<ChatMessage> chatMessage, int idRoom) {
        if (!mapMessages.containsKey(idRoom)) {
            mapMessages.put(idRoom, new ArrayList<>());
        }
        mapMessages.get(idRoom).addAll(0, chatMessage);
    }

    public void addChatMessagesNotCheck(List<ChatMessage> chatMessage, int idRoom) {
        if (!mapMessages.containsKey(idRoom)) {
            //load 20 tin nhan tu cache neu co vao viewmodel
            List<ChatMessage> messages = tableMessage.getChatMessages(idRoom, 0, 20);
            mapMessages.put(idRoom, messages);
        }
        getChatMessages(idRoom).addAll(chatMessage);
    }

    public void removeChatMessages(int idRoom) {
        List<ChatMessage> messages = mapMessages.get(idRoom);
        messages.clear();
    }

    public void updateChatMessage(ChatMessage chatMessage, int idRoom) {
        List<ChatMessage> messages = getChatMessages(idRoom);
        int size = messages.size();
        for (int i = size - 1; i >= 0; i--) {
            ChatMessage message = messages.get(i);
            if (message.equals(chatMessage) && !message.getStatus().equals(ChatMessage.StatusMessage.Seen)) {
                mapMessages.get(idRoom).set(i, chatMessage);
                //gan tin nhan vao tempMessage de roomactivy lay ra kiem tra
                tempMessage = chatMessage;
                break;
            } else {
                if (message.getStatus() != null && !message.getStatus().equals(ChatMessage.StatusMessage.Seen)) {
                    message.setStatus(chatMessage.getStatus());
                }
            }
        }
    }

    //-------------------------------------
    //mapContacts
    public List<Contact> getContacts(int keyContact) {
        if (!mapContacts.containsKey(keyContact)) {
            mapContacts.put(keyContact, new ArrayList<>());
        }
        return mapContacts.get(keyContact);
    }

    public Contact getContact(int key, int idContact) {
        List<Contact> contacts = getContacts(key);
        int size = contacts.size();
        for (int i = 0; i < size; i++) {
            if (contacts.get(i).getId() == idContact) {
                return contacts.get(i);
            }
        }
        return null;
    }

    public void addContact(Contact contact, int key) {
        List<Contact> contacts = getContacts(key);
        if (key == CONTACTS_ONLINE) {
            if (!contacts.contains(contact)) {
                contacts.add(contact);
            }
            Contact t = tableContact.getContact(contact.getId());
            if (t.getmRelationship() == 1) {
                List<Contact> contactsFriendOnline = getContacts(CONTACTS_FRIENDS_ONLINE);
                if (!contactsFriendOnline.contains(t)) {
                    contactsFriendOnline.add(t);
                }
            }
            //thong bao cap nhat lai giao dien roomactivity, contactfragment
            setPostFlagContact(ChatViewModel.FLAG_NOTIFY_ONLINE_OFFLINE_CONTACT);
        } else {
            if (contacts.contains(contact)) return;
            contacts.add(contact);
            if (key == CONTACTS_INVITED_ADD_FRIEND) {
                NotificationHelper.showNotCancel(context, "Tezamess", contact.getName() + " " + context.getResources().getString(R.string.content_add_friend));
            }
        }
    }

    public void addContacts(List<Contact> contacts, int key) {
        List<Contact> contacts1 = getContacts(key);
        contacts1.addAll(contacts);
    }

    public void removeContact(Contact contact, int key) {
        List<Contact> contacts = getContacts(key);
        contacts.remove(contact);
        if (key == CONTACTS_ONLINE) {
            Contact contact1 = getContact(CONTACTS_FRIENDS_ONLINE, contact.getId());
            if (contact1 != null) {
                getContacts(CONTACTS_FRIENDS_ONLINE).remove(contact1);
            }
            setPostFlagContact(ChatViewModel.FLAG_NOTIFY_ONLINE_OFFLINE_CONTACT);
        }
    }

    public void updateContact(Contact contact, int key) {
        List<Contact> contacts = getContacts(key);
        int size = contacts.size();
        for (int i = 0; i < size; i++) {
            Contact c = contacts.get(i);
            if (c.equals(contact)) {
                c.setId(contact.getId());
                c.setName(contact.getName());
                c.setLastactive(contact.getLastactive());
                c.setUrlavatar(contact.getUrlavatar());
                c.setmStatusAddFriend(contact.getmStatusAddFriend());
                if (contact.getmRoomId() != -1) {
                    c.setmRoomId(contact.getmRoomId());
                }
                if (contact.getmRelationship() != -1) {
                    c.setmRelationship(contact.getmRelationship());
                }
                break;
            }
        }
    }

    public void saveOrUpdateContact(Contact contact, int key) {
        List<Contact> contacts = getContacts(key);
        int size = contacts.size();
        for (int i = 0; i < size; i++) {
            Contact c = contacts.get(i);
            if (c.equals(contact)) {
                c.setId(contact.getId());
                c.setName(contact.getName());
                c.setLastactive(contact.getLastactive());
                c.setUrlavatar(contact.getUrlavatar());
                c.setBirthday(contact.getBirthday());
                c.setGender(contact.isGender());
                c.setmStatusAddFriend(contact.getmStatusAddFriend());

                if (contact.getmRoomId() != -1) {
                    c.setmRoomId(contact.getmRoomId());
                }
                if (contact.getmRelationship() != -1) {
                    c.setmRelationship(contact.getmRelationship());
                }
                return;
            }
        }
        addContact(contact, key);
    }

    //--------------------------------------
    //mapRooms
    public List<Room> getRooms(int keyRoom) {
        if (mapRooms.get(keyRoom) == null) {
            mapRooms.put(keyRoom, new ArrayList<>());
        }
        return mapRooms.get(keyRoom);
    }


    public Room getRoom(int idRoom, int keyRoom) {
        List<Room> rooms = getRooms(keyRoom);
        for (Room r : rooms) {
            if (r.getId() == idRoom) {
                return r;
            }
        }
        return null;
    }

    public void addRooms(List<Room> list, int keyRoom) {
        List<Room> rooms = getRooms(keyRoom);
        for (Room room : list) {
            if (rooms.contains(room)) {
                int i = rooms.indexOf(room);
                Room room1 = rooms.get(i);
                if (room1.getLastChatMessage().getCreatedate() < room.getLastChatMessage().getCreatedate()) {
                    rooms.set(i, room);
                }
            } else {
                rooms.add(room);
            }
        }
//        rooms.addAll(list);
    }

    public void addRoom(Room room, int keyRoom) {
        List<Room> rooms = getRooms(keyRoom);
        if (!rooms.contains(room)) {
            rooms.add(0, room);
        }
    }

    public void saveOrUpdateRoom(Room room, int keyRoom) {
        List<Room> rooms = getRooms(keyRoom);
        if (rooms.contains(room)) {
            rooms.remove(room);
        }
        rooms.add(0, room);
    }

    public void removeQuantityUnreadMessage(int idRoom) {
        List<Room> rooms = getRooms(ALL_ROOM);
        for (Room r : rooms) {
            if (idRoom == r.getId()) {
                r.setQuantityUnreadMessage(0);
                break;
            }
        }
        List<Room> manyRooms = getRooms(MANY_ROOM);
        for (Room r : manyRooms) {
            if (idRoom == r.getId()) {
                r.setQuantityUnreadMessage(0);
                break;
            }
        }
    }

    public void removeRoom(Room room, int keyRoom) {
        List<Room> rooms = getRooms(keyRoom);
        rooms.remove(room);
    }

    //mapStatus
    public List<Status> getStatuses(int keyStatus) {
        if (!mapStatus.containsKey(keyStatus)) {
            mapStatus.put(keyStatus, new ArrayList<>());
        }
        return mapStatus.get(keyStatus);
    }

    public void addStatus(Status status, int keyStatus) {
        List<Status> statuses = getStatuses(keyStatus);
        statuses.add(0, status);
    }

    public void addStatuses(List<Status> listStatus, int keyStatus) {
        List<Status> statuses = getStatuses(keyStatus);
        statuses.addAll(0, listStatus);
    }

    public void addLoadMoreStatuses(List<Status> listStatus, int keyStatus) {
        List<Status> statuses = getStatuses(keyStatus);
        statuses.addAll(listStatus);
    }

    public void clear() {
        mapContacts.clear();
        mapMessages.clear();
        mapRooms.clear();
        mapStatus.clear();
        flagRoom.setValue(FLAG_DEFAULT);
        flagMessage.setValue(FLAG_DEFAULT);
        flagContact.setValue(FLAG_DEFAULT);
        flagStatus.setValue(FLAG_DEFAULT);
        flagCheckRoomExist = -2;
        flagCheckMessageError = false;
        tempIdRoom = FLAG_DEFAULT;
        tempMessage = null;
    }
}
