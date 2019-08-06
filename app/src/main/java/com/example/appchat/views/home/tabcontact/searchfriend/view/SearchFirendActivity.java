package com.example.appchat.views.home.tabcontact.searchfriend.view;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appchat.R;
import com.example.appchat.customview.CustomToast;
import com.example.appchat.customview.ProgressBarDialog;
import com.example.appchat.database.TableContact;
import com.example.appchat.objectclass.Contact;
import com.example.appchat.objectclass.Member;
import com.example.appchat.presenters.tabcontact.searchfriend.PresenterSearchFriend;
import com.example.appchat.viewmodel.ChatViewModel;
import com.example.appchat.viewmodel.ChatViewModelFactory;
import com.example.appchat.views.home.tabcontact.searchfriend.adapter.ContactAdapter;
import com.example.appchat.views.home.tabcontact.searchfriend.adapter.RequestAdapter;
import com.example.appchat.views.profileuser.ProfileUserActivity;
import com.example.appchat.websocket.WebSocket;
import com.example.appchat.widget.connection.CheckConnection;
import com.example.appchat.widget.validate.FormatPhone;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class SearchFirendActivity extends AppCompatActivity implements IViewSeachFriend {

    private final int REQUEST_CODE_PHONE = 0;
    private EditText edtSearch;
    private Button btnSearch;
    private Toolbar tlbSearch;
    private RecyclerView lstContact;
    private RecyclerView lstRequest;
    private ProgressBarDialog progressBarDialog;
    private TextView txtInvitedFriend;
    private View divider_two;

    private String phoneNumber;
    private ContactAdapter mAdapterContact;
    private RequestAdapter mAdapterRequest;
    private PresenterSearchFriend presenterSearchFriend;
    private Member admin;

    private List<String> list = new ArrayList<>();
    public CompositeDisposable compositeDisposable;
    private ChatViewModel chatViewModel;
    private TableContact tableContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_firend);
        chatViewModel = ViewModelProviders.of(this, ChatViewModelFactory.getInstance(getApplicationContext())).get(ChatViewModel.class);
        presenterSearchFriend = new PresenterSearchFriend(this, this);
        tableContact = TableContact.getInstance(this);
        admin = Member.getInstance(this);
        compositeDisposable = new CompositeDisposable();
        initView();
        handleEvent();
        initToolbar();


        chatViewModel.flagContact.observe(this, t -> {
            switch (t) {
                case ChatViewModel.FLAG_INVITE_ADDFRIEND_CONTACT:
                    showProgressBar(false);
                    mAdapterContact.notifyItems();
                    chatViewModel.setFlagContact(ChatViewModel.FLAG_DEFAULT);
                    break;
                case ChatViewModel.FLAG_INVITED_ADDFRIEND_CONTACT:
                    mAdapterRequest.notifyDataSetChanged();
                    showLayoutFriendRequest();
                    if (mAdapterContact != null) {
                        mAdapterContact.removeContactsRequest(chatViewModel.getContacts(ChatViewModel.CONTACTS_INVITED_ADD_FRIEND));
                    }
                    chatViewModel.setFlagContact(ChatViewModel.FLAG_DEFAULT);
                    break;
                case ChatViewModel.FLAG_AGREE_ADDFRIEND:
                    if (mAdapterContact != null) {
                        mAdapterContact.notifyDataSetChanged();
                    }
                    if (mAdapterRequest != null) {
                        mAdapterRequest.notifyDataSetChanged();
                        showLayoutFriendRequest();
                    }
                    chatViewModel.setFlagContact(ChatViewModel.FLAG_DEFAULT);
                    break;
                case ChatViewModel.FLAG_DISAGREE_ADDFRIEND:
                    if (mAdapterContact != null) {
                        mAdapterContact.notifyDataSetChanged();
                    }
                    if (mAdapterRequest != null) {
                        mAdapterRequest.notifyDataSetChanged();
                        showLayoutFriendRequest();
                    }
                    chatViewModel.setFlagContact(ChatViewModel.FLAG_DEFAULT);
                    break;
                case ChatViewModel.FLAG_CANCEL_REQUEST_ADDFRIEND:
                    showProgressBar(false);
                    if (mAdapterContact != null) {
                        mAdapterContact.notifyDataSetChanged();
                    }
                    if (mAdapterRequest != null) {
                        mAdapterRequest.notifyDataSetChanged();
                        showLayoutFriendRequest();
                    }
                    chatViewModel.setFlagContact(ChatViewModel.FLAG_DEFAULT);
                    break;
            }
        });

        initRecyclerViewContactRequest();
    }

    private void showLayoutFriendRequest() {
        if (mAdapterRequest.getItemCount() > 0) {
            txtInvitedFriend.setVisibility(View.VISIBLE);
            divider_two.setVisibility(View.VISIBLE);
        } else {
            txtInvitedFriend.setVisibility(View.GONE);
            divider_two.setVisibility(View.GONE);
        }
    }

    private void initRecyclerViewNotFriend() {
        List<Contact> contactsNotFriend = chatViewModel.getContacts(ChatViewModel.CONTACTS_NOT_FRIEND);
        contactsNotFriend.removeAll(chatViewModel.getContacts(ChatViewModel.CONTACTS_INVITED_ADD_FRIEND));
        if (contactsNotFriend.size() > 0) {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            lstContact.setLayoutManager(linearLayoutManager);
            mAdapterContact = new ContactAdapter(chatViewModel.getContacts(ChatViewModel.CONTACTS_NOT_FRIEND), this);
            lstContact.setAdapter(mAdapterContact);
            getPhoneInDevice();
        } else {
            Disposable disposable = Observable.defer(() -> Observable.fromIterable(tableContact.getContactsNotFriend()))
                    .toSortedList((o1, o2) -> o2.getmStatusAddFriend() - o1.getmStatusAddFriend())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(t -> {
                        t.removeAll(chatViewModel.getContacts(ChatViewModel.CONTACTS_INVITED_ADD_FRIEND));
                        chatViewModel.addContacts(t, ChatViewModel.CONTACTS_NOT_FRIEND);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
                        lstContact.setLayoutManager(linearLayoutManager);
                        mAdapterContact = new ContactAdapter(chatViewModel.getContacts(ChatViewModel.CONTACTS_NOT_FRIEND), this);
                        lstContact.setAdapter(mAdapterContact);
                        getPhoneInDevice();
                    });
            compositeDisposable.add(disposable);
        }
    }

    private void initRecyclerViewContactRequest() {
        if (chatViewModel.getContacts(ChatViewModel.CONTACTS_INVITED_ADD_FRIEND).size() > 0) {
            txtInvitedFriend.setVisibility(View.VISIBLE);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            lstRequest.setLayoutManager(linearLayoutManager);
            mAdapterRequest = new RequestAdapter(chatViewModel.getContacts(ChatViewModel.CONTACTS_INVITED_ADD_FRIEND), this);
            lstRequest.setAdapter(mAdapterRequest);
            initRecyclerViewNotFriend();
        } else {
            Disposable disposable = Observable.defer(() -> Observable.just(tableContact.getContactsRequest()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(t -> {
                        chatViewModel.addContacts(t, ChatViewModel.CONTACTS_INVITED_ADD_FRIEND);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                        lstRequest.setLayoutManager(linearLayoutManager);
                        mAdapterRequest = new RequestAdapter(chatViewModel.getContacts(ChatViewModel.CONTACTS_INVITED_ADD_FRIEND), this);
                        lstRequest.setAdapter(mAdapterRequest);
                        if (t.size() > 0) {
                            txtInvitedFriend.setVisibility(View.VISIBLE);
                            divider_two.setVisibility(View.VISIBLE);
                        } else {
                            txtInvitedFriend.setVisibility(View.GONE);
                            divider_two.setVisibility(View.GONE);
                        }
                        initRecyclerViewNotFriend();
                    });
            compositeDisposable.add(disposable);
        }
    }

    private void getPhoneInDevice() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS},
                    REQUEST_CODE_PHONE);
        } else {
            checkPhoneInDevice();
        }
    }

    private void checkPhoneInDevice() {
        if (CheckConnection.haveNetworkConnection(this)) {
            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
            while (phones.moveToNext()) {
                String phone = FormatPhone.format(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                list.add(phone);
            }
            phones.close();
            presenterSearchFriend.getContactsNoFriend(Member.getInstance(this).getToken(this), list);
        }
    }

    private void initToolbar() {
        tlbSearch.setNavigationIcon(R.drawable.ic_arrow_back_while_24dp);
        tlbSearch.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void handleEvent() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (edtSearch.length() > 0) {
                    btnSearch.setEnabled(true);
                } else {
                    btnSearch.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        edtSearch.addTextChangedListener(textWatcher);

        btnSearch.setOnClickListener(v -> {
            if (CheckConnection.haveNetworkConnection(getApplicationContext())) {
                if (WebSocket.stompClient != null) {
                    if (WebSocket.stompClient.isConnected()) {
                        showProgressBar(true);
                        phoneNumber = edtSearch.getText().toString().trim();
                        if (phoneNumber.length() > 0) {
                            presenterSearchFriend.checkValid(phoneNumber);
                        }
                    } else {
                        CustomToast.makeText(getApplicationContext(), getResources().getString(R.string.server_error), Toast.LENGTH_SHORT);
                    }
                } else {
                    CustomToast.makeText(getApplicationContext(), getResources().getString(R.string.server_error), Toast.LENGTH_SHORT);
                }
            } else {
                CheckConnection.showToast_short(getApplicationContext(), getResources().getString(R.string.notification_noconnection));
            }
        });
    }

    private void initView() {
        divider_two = findViewById(R.id.divider_two);
        edtSearch = findViewById(R.id.edittext_search);
        btnSearch = findViewById(R.id.button_search);
        tlbSearch = findViewById(R.id.toolbar_searchfriend);
        lstContact = findViewById(R.id.recyclerview_notfriend);
        lstRequest = findViewById(R.id.recyclerView_request);
        txtInvitedFriend = findViewById(R.id.textview_invited_friend);
    }


    @Override
    public void dataError(String error) {
        showProgressBar(false);
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void searchSucessUser(Contact contact) {
        showProgressBar(false);
        Intent intent = new Intent(SearchFirendActivity.this, ProfileUserActivity.class);
        intent.putExtra("source", "Search");
        intent.putExtra("contact", contact);
        startActivity(intent);
    }

    @Override
    public void searchSucessMember() {
        showProgressBar(false);
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void searchFail(String fail) {
        showProgressBar(false);
        Toast.makeText(this, fail, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void connectError(String messgae) {
        showProgressBar(false);
        Toast.makeText(this, messgae, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void loadContact(List<Contact> contacts) {
        contacts.removeAll(chatViewModel.getContacts(ChatViewModel.CONTACTS_INVITED_ADD_FRIEND));
        contacts.removeAll(chatViewModel.getContacts(ChatViewModel.CONTACTS_NOT_FRIEND));
        contacts.remove(new Contact(admin.getId()));
        mAdapterContact.updateContactNotFriend(contacts);
    }


    public void showProgressBar(boolean visibility) {
        if (progressBarDialog == null) {
            progressBarDialog = new ProgressBarDialog();
        }
        if (visibility) {
            progressBarDialog.show(getSupportFragmentManager(), "dialog");
        } else {
            if (progressBarDialog.isAdded()) {
                progressBarDialog.dismiss();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_PHONE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPhoneInDevice();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapterContact != null) {
            mAdapterContact.notifyDataSetChanged();
        }
        if (mAdapterRequest != null) {
            mAdapterRequest.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }

        super.onDestroy();
    }
}
