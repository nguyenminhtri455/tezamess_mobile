package com.example.appchat.presenters.tabcontact.searchfriend;

import android.content.Context;
import android.util.Log;

import com.example.appchat.R;
import com.example.appchat.database.TableContact;
import com.example.appchat.model.tabcontact.searchfriend.ModelSearchFriend;
import com.example.appchat.objectclass.Contact;
import com.example.appchat.objectclass.Member;
import com.example.appchat.views.home.tabcontact.searchfriend.view.IViewSeachFriend;
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
import io.reactivex.schedulers.Schedulers;

public class PresenterSearchFriend implements IPresenterSearchFriend {
    private ModelSearchFriend modelSearch;
    private IViewSeachFriend iViewSearcch;
    private Context context;

    public PresenterSearchFriend(IViewSeachFriend iViewSearcch, Context context) {
        modelSearch = new ModelSearchFriend();
        this.iViewSearcch = iViewSearcch;
        this.context = context;
    }

    @Override
    public void searchUser(String token, String phoneNumber) {
        modelSearch.searchUser(token, phoneNumber, s -> {
            try {
                Log.d("BBBBB", s);
                JSONObject jsonObject = new JSONObject(s);
                int status = jsonObject.getInt("status");
                switch (status) {
                    case 0:
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

                        JSONObject jsonUser = jsonObject.getJSONObject("data");
                        Contact contact = new Contact();
                        contact.setId(jsonUser.getInt("id"));
                        contact.setPhone(jsonUser.getString("phone"));
                        contact.setName(jsonUser.getString("name"));
                        contact.setBirthday(simpleDateFormat.parse(jsonUser.getString("birthday")));
                        contact.setGender(jsonUser.getBoolean("gender"));
                        contact.setUrlavatar(jsonUser.getString("urlavatar"));

                        iViewSearcch.searchSucessUser(contact);
                        break;
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                        String message = jsonObject.getString("message");
                        iViewSearcch.searchFail(message);
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void checkValid(String phoneNumber) {
        if (!Validator.checkValidatePhoneNumber(phoneNumber)) {
            iViewSearcch.dataError(context.getResources().getString(R.string.invalid_phone_number));
            return;
        }
        Member member = Member.getInstance(context);
        if (phoneNumber.equals(member.getPhone())) {
            iViewSearcch.searchSucessMember();
            return;
        }
        searchUser(member.getToken(context), phoneNumber);
    }

//    @Override
//    public void getContactsNoFriend(String token, List<String> listPhone) {
//        modelSearch.getContactsNoFriend(token, listPhone, t -> {
//            try {
//                JSONObject jsonObject = new JSONObject(t);
//                int status = jsonObject.getInt("status");
//                List<Contact> contacts = new ArrayList<>();
//                switch (status) {
//                    case 0:
//                        JSONArray jsonArray = jsonObject.getJSONArray("data");
//                        for (int i = 0; i < jsonArray.length(); i++) {
//                            JSONObject object = jsonArray.getJSONObject(i);
//                            String name = object.getString("name");
//                            String phone = object.getString("phone");
//                            String photo = object.getString("urlavatar");
//                            int idContact = object.getInt("id");
//                            Contact contact = new Contact();
//                            contact.setName(name);
//                            contact.setPhone(phone);
//                            contact.setUrlavatar(photo);
//                            contact.setId(idContact);
//                            contact.setmRoomId(-1);
//                            contact.setmRelationship(0);
//                            contacts.add(contact);
//                        }
//                        if (contacts.size() > 0) {
//                            TableContact tableContact = TableContact.getInstance(context);
//                            Observable.defer(() ->
//                                    Observable.just(tableContact.getContactsNotFriend()))
//                                    .flatMap(contacts1 -> {
//                                        //xoa het danh sach trong db tu danh sach server tra ve
//                                        contacts.removeAll(contacts1);
//                                        return Observable.just(contacts);
//                                    })
//                                    .subscribeOn(Schedulers.io())
//                                    .observeOn(AndroidSchedulers.mainThread())
//                                    .subscribe(contacts2 -> {
//                                        if(contacts2.size() > 0){
//                                            tableContact.addContacts(contacts2);
//                                            iViewSearcch.loadContact(contacts2);
//                                        }
//                                    });
//                        }
//                        break;
//                    case 1:
//                    case 2:
//                    case 3:
//                    case 4:
//                    case 5:
//                    case 6:
//                    case 7:
//                        break;
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        });
//    }

    @Override
    public void getContactsNoFriend(String token, List<String> listPhone) {
        modelSearch.getContactsNoFriend(token, listPhone, t -> {
            try {
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
                            contact.setmRelationship(-1);
                            contacts.add(contact);
                        }
                        if (contacts.size() > 0) {
                            Completable.create(o -> {
                                TableContact tableContact = TableContact.getInstance(context);
                                tableContact.saveOrUpdateContact(contacts);
                                o.onComplete();
                            }).subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(() -> iViewSearcch.loadContact(contacts));
                        }
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
                e.printStackTrace();
            }
        });
    }
}
