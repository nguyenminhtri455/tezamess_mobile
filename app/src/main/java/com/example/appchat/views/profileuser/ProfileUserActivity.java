package com.example.appchat.views.profileuser;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appchat.R;
import com.example.appchat.customview.CircleImage;
import com.example.appchat.customview.CustomToast;
import com.example.appchat.customview.ProgressBarDialog;
import com.example.appchat.database.TableContact;
import com.example.appchat.objectclass.Contact;
import com.example.appchat.objectclass.Member;
import com.example.appchat.viewmodel.ChatViewModel;
import com.example.appchat.viewmodel.ChatViewModelFactory;
import com.example.appchat.views.home.tabmessage.editroom.EditRoomActivity;
import com.example.appchat.views.home.tabmessage.newroom.NewRoomActivity;
import com.example.appchat.views.home.tabmessage.roomchat.DoubleRoomActivity;
import com.example.appchat.websocket.WebSocket;
import com.example.appchat.widget.connection.CheckConnection;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ProfileUserActivity extends AppCompatActivity {

    private TextView txtGender, txtBirthday, txtPhone, txtNotifyError;
    private CircleImage imgUser;
    private Button btnAddFriend;
    private Toolbar tlbUser;
    private LinearLayout linearLayout;
    private FloatingActionButton btnMessage;

    private ChatViewModel chatViewModel;
    private Contact contact;
    private TableContact tableContact;
    private Member admin;
    private CompositeDisposable compositeDisposable;
    private String source = null;
    private ProgressBarDialog progressBarDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_user);
        initView();
        initToolbar();
        admin = Member.getInstance(this);
        tableContact = TableContact.getInstance(this);
        chatViewModel = ViewModelProviders.of(this, ChatViewModelFactory.getInstance(this)).get(ChatViewModel.class);
        compositeDisposable = new CompositeDisposable();
        progressBarDialog = new ProgressBarDialog();

        Intent intent = getIntent();
        source = intent.getStringExtra("source");
        contact = (Contact) intent.getSerializableExtra("contact");
        tlbUser.setTitle(contact.getName());

        if (CheckConnection.haveNetworkConnection(this)) {
            if (WebSocket.stompClient != null) {
                if (WebSocket.stompClient.isConnected()) {
                    showLayout(true);
                    chatViewModel.flagContact.observe(this, t -> {
                        switch (t) {
                            case ChatViewModel.FLAG_INVITE_ADDFRIEND_CONTACT:
                                showProgressBarDialog(false);
                                contact.setmStatusAddFriend(2);
                                btnAddFriend.setText(this.getResources().getString(R.string.cancel));
                                showUICancel();
                                chatViewModel.setFlagContact(ChatViewModel.FLAG_DEFAULT);
                                break;
                            case ChatViewModel.FLAG_INVITED_ADDFRIEND_CONTACT:
                                contact.setmStatusAddFriend(3);
                                btnAddFriend.setText(this.getResources().getString(R.string.agree));
                                showUINotCancel();
                                chatViewModel.setFlagContact(ChatViewModel.FLAG_DEFAULT);
                                break;
                            case ChatViewModel.FLAG_AGREE_ADDFRIEND:
                                showProgressBarDialog(false);
                                contact.setmStatusAddFriend(0);
                                btnAddFriend.setText(this.getResources().getString(R.string.un_friend));
                                showUINotCancel();
                                chatViewModel.setFlagContact(ChatViewModel.FLAG_DEFAULT);
                                break;
                            case ChatViewModel.FLAG_DISAGREE_ADDFRIEND:
                                showProgressBarDialog(false);
                                contact.setmStatusAddFriend(1);
                                btnAddFriend.setText(this.getResources().getString(R.string.add_friend));
                                showUINotCancel();
                                chatViewModel.setFlagContact(ChatViewModel.FLAG_DEFAULT);
                                break;
                            case ChatViewModel.FLAG_UNFRIEND:
                                showProgressBarDialog(false);
                                chatViewModel.setFlagContact(ChatViewModel.FLAG_DEFAULT);
                                contact.setmStatusAddFriend(1);
                                btnAddFriend.setText(this.getResources().getString(R.string.add_friend));
                                showUINotCancel();
                                chatViewModel.setFlagContact(ChatViewModel.FLAG_DEFAULT);
                                break;
                            case ChatViewModel.FLAG_FIND_MEMBER:
                                contact.setBirthday(NewRoomActivity.tempContact.getBirthday());
                                contact.setGender(NewRoomActivity.tempContact.isGender());
                                contact.setName(NewRoomActivity.tempContact.getName());
                                contact.setUrlavatar(NewRoomActivity.tempContact.getUrlavatar());
                                new Handler().postDelayed(() -> {
                                    showProgressBarDialog(false);
                                    handleEvent();
                                    initUI();
                                }, 300);
                                chatViewModel.setFlagContact(ChatViewModel.FLAG_DEFAULT);
                                break;
                            case ChatViewModel.FLAG_CANCEL_REQUEST_ADDFRIEND:
                                showProgressBarDialog(false);
                                contact.setmStatusAddFriend(1);
                                btnAddFriend.setText(this.getResources().getString(R.string.add_friend));
                                showUINotCancel();
                                chatViewModel.setFlagContact(ChatViewModel.FLAG_DEFAULT);
                                break;
                        }
                    });

                    if (contact.getBirthday() == null) {
                        if (WebSocket.stompClient != null && WebSocket.stompClient.isConnected()) {
                            progressBarDialog.show(getSupportFragmentManager(), "dialog");
                            Map<String, Object> map = new HashMap<>();
                            map.put("id", admin.getId());
                            map.put("phone", this.contact.getPhone());
                            map.put("caller", "profile");
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
                        }
                    } else {
                        handleEvent();
                    }
                } else {
                    txtNotifyError.setText(getResources().getString(R.string.server_error));
                    showLayout(false);
                }
            } else {
                txtNotifyError.setText(getResources().getString(R.string.server_error));
                showLayout(false);
            }
        } else {
            txtNotifyError.setText(getResources().getString(R.string.notification_noconnection));
            showLayout(false);
        }

    }

    private void showUICancel() {
        if (Build.VERSION.SDK_INT >= 23) {
            btnAddFriend.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.unfriend), null, null, null);
            btnAddFriend.setTextColor(ContextCompat.getColor(this, R.color.colorBlack));
            btnAddFriend.setBackground(ContextCompat.getDrawable(this, R.drawable.custom_backgroud_button_cancel));
        } else {
            btnAddFriend.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.unfriend), null, null, null);
            btnAddFriend.setTextColor(getResources().getColor(R.color.colorBlack));
            btnAddFriend.setBackground(getResources().getDrawable(R.drawable.custom_backgroud_button_cancel));
        }
    }

    private void showUINotCancel() {
        Drawable[] compoundDrawables = btnAddFriend.getCompoundDrawables();
        if (Build.VERSION.SDK_INT >= 23) {
            btnAddFriend.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_person_add_while_24dp)
                    , compoundDrawables[1], compoundDrawables[2], compoundDrawables[3]);
            btnAddFriend.setTextColor(ContextCompat.getColor(this, R.color.colorWhile));
            btnAddFriend.setBackground(ContextCompat.getDrawable(this, R.drawable.custom_backgroud_button_yes));
        } else {
            btnAddFriend.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_person_add_while_24dp)
                    , compoundDrawables[1], compoundDrawables[2], compoundDrawables[3]);
            btnAddFriend.setTextColor(getResources().getColor(R.color.colorWhile));
            btnAddFriend.setBackground(getResources().getDrawable(R.drawable.custom_backgroud_button_yes));
        }
    }

    private void initUI() {
        Disposable subscribe = Observable.defer(() -> Observable.just(tableContact.getContact(contact.getId())))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(t -> {
                    if (t.getId() != 0) {
                        contact.setmRelationship(t.getmRelationship());
                        contact.setmStatusAddFriend(t.getmStatusAddFriend());
                    }
                    setUser(contact);
                    if (contact.getmRelationship() == 1) {
                        btnAddFriend.setText(this.getResources().getString(R.string.un_friend));
                    } else {
                        switch (contact.getmStatusAddFriend()) {
                            case 1:
                                btnAddFriend.setText(this.getResources().getString(R.string.add_friend));
                                showUINotCancel();
                                break;
                            case 2:
                                btnAddFriend.setText(this.getResources().getString(R.string.cancel));
                                showUICancel();
                                break;
                            case 3:
                                btnAddFriend.setText(this.getResources().getString(R.string.agree));
                                showUINotCancel();
                                break;
                        }
                    }
                });
        compositeDisposable.add(subscribe);
    }

    private void showProgressBarDialog(boolean visibility) {
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

    private void handleEvent() {
        btnAddFriend.setOnClickListener(t -> {
            if (CheckConnection.haveNetworkConnection(this)) {
                if (WebSocket.stompClient != null) {
                    if (WebSocket.stompClient.isConnected()) {
                        switch (contact.getmStatusAddFriend()) {
                            case 0:
                                showDiagLogUnFriend();
                                break;
                            case 1:
                                Map<String, Object> map = new HashMap<>();
                                map.put("idRequest", Member.getInstance(this).getId());
                                map.put("idFriend", contact.getId());
                                Disposable success = WebSocket.stompClient
                                        .send("/chat/addfriend", new Gson().toJson(map))
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(
                                                () -> {
                                                    Log.d("BBBBB", "success");
                                                    showProgressBarDialog(true);
                                                },
                                                onError -> Log.d("BBBBB", onError.getMessage())
                                        );
                                compositeDisposable.add(success);
                                break;
                            case 2:
                                Map<String, Object> mapCancel = new HashMap<>();
                                mapCancel.put("idRequest", Member.getInstance(this).getId());
                                mapCancel.put("idFriend", contact.getId());
                                Disposable disposable = WebSocket.stompClient
                                        .send("/chat/cancel/request.addfriend", new Gson().toJson(mapCancel))
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(
                                                () -> {
                                                    Log.d("BBBBB", "success");
                                                    showProgressBarDialog(true);
                                                },
                                                onError -> Log.d("BBBBB", onError.getMessage())
                                        );
                                compositeDisposable.add(disposable);
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
                                                () -> {
                                                    Log.d("BBBBB", "success");
                                                    showProgressBarDialog(true);
                                                },
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

        btnMessage.setOnClickListener(t -> {
            switch (source) {
                case "DoubleRoomActivity":
                    finish();
                    break;
                case "Search":
                    Intent intent = new Intent(ProfileUserActivity.this, DoubleRoomActivity.class);
                    intent.putExtra("contact", contact);
                    intent.putExtra("source", "search");
                    startActivity(intent);
                    break;
                case "EditRoomActivity":
                    Intent intent1 = new Intent(ProfileUserActivity.this, DoubleRoomActivity.class);
                    intent1.putExtra("contact", contact);
                    startActivity(intent1);
                    setResult(EditRoomActivity.REQUEST_PROFILE_USER);
                    finish();
                    break;
            }
        });


    }

    private void showDiagLogUnFriend() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_unfriend);
        builder.setMessage(getResources().getString(R.string.confirm_unfriend) + " " + contact.getName() + " ?");
        builder.setPositiveButton(R.string.title_yes, (dialog, which) -> {
            progressBarDialog.show(getSupportFragmentManager(), "dialog");
            Map<String, Object> mapUnFriend = new HashMap<>();
            mapUnFriend.put("idRequest", Member.getInstance(this).getId());
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

    private void showLayout(boolean visibility) {
        if (visibility) {
            txtNotifyError.setVisibility(View.GONE);
            linearLayout.setVisibility(View.VISIBLE);
        } else {
            txtNotifyError.setVisibility(View.VISIBLE);
            linearLayout.setVisibility(View.GONE);
        }
    }

    private void initToolbar() {
        tlbUser.setNavigationIcon(R.drawable.ic_arrow_back_while_24dp);
        tlbUser.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setUser(Contact contact) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        txtBirthday.setText(simpleDateFormat.format(contact.getBirthday()));
        tlbUser.setTitle(contact.getName());
        txtPhone.setText(contact.getPhone());
        Picasso.get().load(contact.getUrlavatar())
                .placeholder(R.drawable.account_icon)
                .error(R.drawable.account_icon)
                .into(imgUser);

        if (!contact.isGender()) {
            txtGender.setText("Female");
        } else {
            txtGender.setText("Male");
        }
    }

    private void initView() {
        txtGender = findViewById(R.id.textview_gender_user);
        txtBirthday = findViewById(R.id.textview_birthday_user);
        txtPhone = findViewById(R.id.textview_phone_user);
        imgUser = findViewById(R.id.image_user);
        btnMessage = findViewById(R.id.button_message_user);
        btnAddFriend = findViewById(R.id.button_addfriend_user);
        tlbUser = findViewById(R.id.toolbar_user);
        txtNotifyError = findViewById(R.id.textview_notify_error);
        linearLayout = findViewById(R.id.layout_infomation);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (CheckConnection.haveNetworkConnection(this)) {
            if (WebSocket.stompClient != null) {
                if (WebSocket.stompClient.isConnected()) {
                    showLayout(true);
                    if (!progressBarDialog.isAdded()) {
                        initUI();
                    }
                } else {
                    txtNotifyError.setText(getResources().getString(R.string.server_error));
                    showLayout(false);
                }
            } else {
                txtNotifyError.setText(getResources().getString(R.string.server_error));
                showLayout(false);
            }
        } else {
            txtNotifyError.setText(getResources().getString(R.string.notification_noconnection));
            showLayout(false);
        }

    }

    @Override
    protected void onDestroy() {
        NewRoomActivity.tempContact = null;
        super.onDestroy();
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
    }
}
