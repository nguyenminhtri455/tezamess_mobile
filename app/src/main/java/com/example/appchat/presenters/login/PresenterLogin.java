package com.example.appchat.presenters.login;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.util.Log;

import com.example.appchat.R;
import com.example.appchat.database.TableContact;
import com.example.appchat.database.TableMessage;
import com.example.appchat.database.TableParticipation;
import com.example.appchat.database.TableRoom;
import com.example.appchat.model.login.ModelLogin;
import com.example.appchat.objectclass.ChatMessage;
import com.example.appchat.objectclass.Contact;
import com.example.appchat.objectclass.Member;
import com.example.appchat.objectclass.Room;
import com.example.appchat.viewmodel.ChatViewModel;
import com.example.appchat.viewmodel.ChatViewModelFactory;
import com.example.appchat.views.login.IViewLogin;
import com.example.appchat.views.login.LoginActivity;
import com.example.appchat.websocket.WebSocket;
import com.example.appchat.widget.validate.Validator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PresenterLogin implements IPresenterLogin {

    private ModelLogin modelLogin;
    private IViewLogin iViewLogin;
    private Context context;
    private int codeStatus;
    private ChatViewModel chatViewModel;

    public PresenterLogin(IViewLogin viewLogin, Context context) {
        modelLogin = new ModelLogin();
        this.iViewLogin = viewLogin;
        this.context = context;
        chatViewModel = ViewModelProviders.of((LoginActivity) context, ChatViewModelFactory.getInstance(context)).get(ChatViewModel.class);
    }

    public PresenterLogin(Context context) {
        modelLogin = new ModelLogin();
        this.context = context;
    }

    @Override
    public void loginAccount(String userName, String passWord) {
        modelLogin.checkLogin(userName, passWord, s -> {
            Log.d("BBBBB", s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                codeStatus = jsonObject.getInt("status");
                switch (codeStatus) {
                    case 0:
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

                        JSONObject jsonMember = jsonObject.getJSONObject("data");
                        Member admin = Member.getInstance(context);

                        admin.setId(jsonMember.getInt("id"));
                        admin.setPhone(jsonMember.getString("phone"));
                        admin.setName(jsonMember.getString("name"));
                        admin.setPassword(jsonMember.getString("password"));
                        admin.setEmail(jsonMember.getString("email"));
                        admin.setBirthday(simpleDateFormat.parse(jsonMember.getString("birthday")));
                        admin.setGender(jsonMember.getBoolean("gender"));
                        admin.setUrlavatar(jsonMember.getString("urlavatar"));
                        String token = jsonObject.getString("token");
                        admin.saveCache(context, token);

                        List<Contact> contactsSentRequestAddFriend = new ArrayList<>();
                        JSONArray jsonRequest = jsonMember.getJSONArray("request");
                        for (int i = 0; i < jsonRequest.length(); i++) {
                            Contact contact = new Contact();
                            JSONObject object = jsonRequest.getJSONObject(i);
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
                            contact.setmStatusAddFriend(2);
                            contact.setmRelationship(-1);
                            contactsSentRequestAddFriend.add(contact);
                        }

                        List<Room> rooms = new ArrayList<>();
                        List<Contact> contacts = new ArrayList<>();
                        List<Contact> contactsManyRoom = new ArrayList<>();
                        List<ChatMessage> chatMessages = new ArrayList<>();
                        JSONArray jsonRooms = jsonMember.getJSONArray("rooms");

                        for (int i = 0; i < jsonRooms.length(); i++) {
                            JSONObject jsonRoom = jsonRooms.getJSONObject(i);
                            Room room = new Room();
                            room.setId(jsonRoom.getInt("id"));
                            room.setName(jsonRoom.getString("name"));
                            room.setCreator(jsonRoom.getInt("creator"));
                            room.setType(jsonRoom.getString("type"));
                            room.setUrlAvatar(jsonRoom.getString("avatar"));
                            room.setMembers(jsonRoom.getInt("size"));
                            rooms.add(room);

                            JSONArray jsonMembers = jsonRoom.getJSONArray("members");

                            for (int j = 0; j < jsonMembers.length(); j++) {
                                JSONObject json = jsonMembers.getJSONObject(j);
                                int id = json.getInt("id");
                                if (id == admin.getId())
                                    continue;
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
                                contact.setmRelationship(-1);

                                switch (room.getType()) {
                                    case "D":
                                        contact.setmRoomId(room.getId());
                                        if (!contacts.contains(contact)) {
                                            contacts.add(contact);
                                        } else {
                                            contacts.remove(contact);
                                            contacts.add(contact);
                                        }
                                        break;
                                    case "G":
                                        Contact c = new Contact();
                                        c.setId(id);
                                        c.setmRoomId(room.getId());
                                        contactsManyRoom.add(c);
                                        if (!contacts.contains(contact)) {
                                            contacts.add(contact);
                                        }
                                        break;
                                }
                            }

                            if (room.getType().equals("G")) {
                                Contact c = new Contact();
                                c.setId(admin.getId());
                                c.setmRoomId(room.getId());
                                contactsManyRoom.add(c);
                            }

                            Contact admin1 = new Contact();
                            admin1.setId(admin.getId());
                            admin1.setName(admin.getName());
                            admin1.setPhone(admin.getPhone());
                            admin1.setUrlavatar(admin.getUrlavatar());
                            contacts.add(admin1);

                            JSONArray jsonMessage = jsonRoom.getJSONArray("messages");
                            for (int j = 0; j < jsonMessage.length(); j++) {
                                JSONObject json = jsonMessage.getJSONObject(j);
                                int id = json.getInt("id");
                                long createdate = json.getLong("createdate");
                                String body = json.getString("body");
                                int user = json.getInt("user");
                                int mRoom = json.getInt("room");
                                String status = json.getString("status");
                                String type = json.getString("type");
                                ChatMessage chatMessage = new ChatMessage();
                                chatMessage.setId(id);
                                chatMessage.setCreatedate(createdate);
                                chatMessage.setBody(body);
                                chatMessage.setUser(user);
                                chatMessage.setRoom(mRoom);

                                switch (type) {
                                    case WebSocket.CHAT:
                                        chatMessage.setTypeMessage(ChatMessage.TypeMessage.Chat);
                                        break;
                                    case WebSocket.IMAGE:
                                        chatMessage.setTypeMessage(ChatMessage.TypeMessage.Image);
                                        break;
                                }


                                switch (status) {
                                    case WebSocket.SENT:
                                        chatMessage.setStatus(ChatMessage.StatusMessage.Sent);
                                        break;
                                    case WebSocket.RECEIVED:
                                        chatMessage.setStatus(ChatMessage.StatusMessage.Received);
                                        break;
                                    case WebSocket.SEEN:
                                        chatMessage.setStatus(ChatMessage.StatusMessage.Seen);
                                        break;
                                }
                                chatMessages.add(chatMessage);
                            }
                        }


                        Disposable subscribe = Completable.create(o -> {

                            if (contactsSentRequestAddFriend.size() > 0) {
                                TableContact.getInstance(context).saveOrUpdateContact(contactsSentRequestAddFriend);
                            }
                            if (contactsManyRoom.size() > 0) {
                                TableParticipation.getInstance(context).addContacts(contactsManyRoom);
                            }

                            if (chatMessages.size() > 0) {
                                TableMessage tableMessage = TableMessage.getInstance(context);
                                tableMessage.addChatMessages(chatMessages);
                            }

                            if (rooms.size() > 0) {
                                TableRoom.getInstance(context).addRooms(new ArrayList<>(rooms));
                            }

                            o.onComplete();
                        }).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> getContacts(token, admin.getId(), contacts));
                        ((LoginActivity) context).compositeDisposable.add(subscribe);
                        break;
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                        String message = jsonObject.getString("message");
                        iViewLogin.loginFail(message);
                        break;
                }
            } catch (JSONException e) {
                Log.e("BBBBB", e.getMessage());
                if (s.contains("UnknownHostException")) {
                    iViewLogin.connectError(context.getResources().getString(R.string.notification_noconnection));
                } else if (s.contains("SocketTimeoutException")) {
                    iViewLogin.connectError(context.getResources().getString(R.string.timeout_connection));
                } else {
                    Log.e("BBBBB",e.getMessage());
                    iViewLogin.connectError(context.getResources().getString(R.string.server_error));
                }
            } catch (ParseException e) {
                iViewLogin.loginFail(context.getResources().getString(R.string.invalidate_date));
            }
        });
    }

    public void checkValid(String userName, String passWord) {
        if (!Validator.checkValidatePhoneNumber(userName)) {
            iViewLogin.dataError(context.getResources().getString(R.string.invalid_phone_number));
            return;
        }
        if (!Validator.checkValidatePassword(passWord)) {
            iViewLogin.dataError(context.getResources().getString(R.string.invalid_password));
            return;
        }
        loginAccount(userName, passWord);
    }

    @Override
    public void getContacts(String token, int id, List<Contact> mContacts) {
        modelLogin.getContacts(token, id, t -> {
            try {
                Log.d("BBBBB", "getContact : " + t);
                JSONObject jsonObject = new JSONObject(t);
                int status = jsonObject.getInt("status");
                List<Contact> contacts = new ArrayList<>();
                switch (status) {
                    case 0:
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            String name = object.getString("name");
                            String phone = object.getString("phone");
                            String photo = object.getString("urlavatar");
                            int idContact = object.getInt("id");

                            Contact contact = new Contact();
                            contact.setName(name);
                            contact.setPhone(phone);
                            contact.setUrlavatar(photo);
                            contact.setId(idContact);
                            contact.setmRoomId(-1);
                            contact.setmRelationship(1);
                            contact.setmStatusAddFriend(0);
                            contact.setLastactive(0);
                            contacts.add(contact);
                        }
                        TableContact tableContact = TableContact.getInstance(context);
                        if (contacts.size() == 0) {
                            Disposable subscribe = Completable.create(s -> {
                                tableContact.addContacts(mContacts);
                                s.onComplete();
                            }).subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(() ->
                                            iViewLogin.loginSucess(context.getResources().getString(R.string.notification_welcom)
                                                    + Member.getInstance(context).getName()));
                            ((LoginActivity) context).compositeDisposable.add(subscribe);
                            return;
                        }
//                        else {
//                            chatViewModel.addContacts(new ArrayList<>(contacts), ChatViewModel.CONTACTS_FRIEND);
//                        }

                        if (mContacts.size() == 0) {
//                            chatViewModel.addContacts(contacts, ChatViewModel.CONTACTS_FRIEND);
                            Disposable subscribe = Completable.create(s -> {
                                tableContact.addContacts(contacts);
                                s.onComplete();
                            }).subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(() ->
                                            iViewLogin.loginSucess(context.getResources().getString(R.string.notification_welcom)
                                                    + Member.getInstance(context).getName()));
                            ((LoginActivity) context).compositeDisposable.add(subscribe);
                            return;
                        }

                        for (Contact c : mContacts) {
                            if (contacts.contains(c)) {
                                c.setmRelationship(1);
                                c.setmStatusAddFriend(0);
                            }
                        }
                        contacts.removeAll(mContacts);
                        mContacts.addAll(contacts);
                        Disposable subscribe = Completable.create(s -> {
                            tableContact.addContacts(mContacts);
                            s.onComplete();
                        }).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() ->
                                        iViewLogin.loginSucess(context.getResources().getString(R.string.notification_welcom)
                                                + Member.getInstance(context).getName()));
                        ((LoginActivity) context).compositeDisposable.add(subscribe);
                        break;
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                        break;
                }
            } catch (JSONException e) {
                Log.e("PPPPP", e.getMessage());
            }
        });
    }
}
