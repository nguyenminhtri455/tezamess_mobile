package com.example.appchat.views.home.tabcontact;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.appchat.R;
import com.example.appchat.database.TableContact;
import com.example.appchat.objectclass.Contact;
import com.example.appchat.objectclass.Member;
import com.example.appchat.viewmodel.ChatViewModel;
import com.example.appchat.viewmodel.ChatViewModelFactory;
import com.example.appchat.views.home.HomeActivity;
import com.example.appchat.views.home.tabcontact.adapter.FriendAdapter;
import com.example.appchat.views.home.tabcontact.adapter.OnlineAdapter;
import com.example.appchat.views.home.tabcontact.searchfriend.view.SearchFirendActivity;
import com.example.appchat.views.home.tabsetting.profile.ProfileActivity;

import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ContactFragment extends Fragment {

    private final int REQUEST_SEARCH = 0;
    private HomeActivity homeActivity;
    private RecyclerView recyclerViewFriendOnline, recyclerViewFriend;
    private TextView txtLayoutNotContact, txtLayoutOnline, txtAmountNotification;
    private ProgressBar progressBar;
    private View divider;
    private FriendAdapter friendAdapter;
    private OnlineAdapter onlineAdapter;

    private Member admin;
    private CompositeDisposable compositeDisposable;
    private ChatViewModel chatViewModel;
    private TableContact tableContact;
    private List<Contact> contactsFriend;
    private List<Contact> contactsOnlineFriend;
    private Handler handler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeActivity = (HomeActivity) getActivity();
        compositeDisposable = new CompositeDisposable();
        admin = Member.getInstance(getContext());
        chatViewModel = ViewModelProviders.of(homeActivity, ChatViewModelFactory.getInstance(getContext())).get(ChatViewModel.class);
        tableContact = TableContact.getInstance(getContext());
        handler = new Handler();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        initView(view);
        setHasOptionsMenu(true);


        // khoi tao danh sach ban be
        contactsFriend = chatViewModel.getContacts(ChatViewModel.CONTACTS_FRIEND);
        initRecyclerViewFriend(contactsFriend);

        //  khoi tao danh sach ban be dang online
        contactsOnlineFriend = chatViewModel.getContacts(ChatViewModel.CONTACTS_FRIENDS_ONLINE);
        initRecyclerViewOnlineFriend(contactsOnlineFriend);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        chatViewModel.flagContact.observe(homeActivity, t -> {
            switch (t) {
                case ChatViewModel.FLAG_INIT_CONTACT_FRIEND:
                    if (friendAdapter == null) {
                        initRecyclerViewFriend(contactsFriend);
                    } else {
                        friendAdapter.notifyDataSetChanged();
                    }
                    showLayoutNotContact();
                    chatViewModel.setFlagContact(ChatViewModel.FLAG_DEFAULT);
                    break;
                case ChatViewModel.FLAG_INVITED_ADDFRIEND_CONTACT:
                    if (chatViewModel.getContacts(ChatViewModel.CONTACTS_INVITED_ADD_FRIEND).size() > 0) {
                        showNotifyAddFriend(true, chatViewModel.getContacts(ChatViewModel.CONTACTS_INVITED_ADD_FRIEND).size());
                    } else {
                        showNotifyAddFriend(false, 0);
                    }
                    chatViewModel.setFlagContact(ChatViewModel.FLAG_DEFAULT);
                    break;
                case ChatViewModel.FLAG_NOTIFY_ONLINE_OFFLINE_CONTACT:
                    onlineAdapter.notifyDataSetChanged();
                    if(friendAdapter != null){
                        friendAdapter.notifyDataSetChanged();
                    }
                    showLayoutFriendOnline();
                    chatViewModel.setFlagContact(ChatViewModel.FLAG_DEFAULT);
                    break;
                case ChatViewModel.FLAG_AGREE_ADDFRIEND:
                    Collections.sort(contactsFriend, (t1, t2) -> Character.compare(Character.toLowerCase(t1.getName().charAt(0)),
                            Character.toLowerCase(t2.getName().charAt(0))));
                    if(friendAdapter != null){
                        friendAdapter.notifyDataSetChanged();
                    }
                    showLayoutNotContact();
                    chatViewModel.setFlagContact(ChatViewModel.FLAG_DEFAULT);
                    break;
                case ChatViewModel.FLAG_UNFRIEND:
                    onlineAdapter.notifyDataSetChanged();
                    friendAdapter.notifyDataSetChanged();
                    showLayoutFriendOnline();
                    showLayoutNotContact();
                    chatViewModel.setFlagContact(ChatViewModel.FLAG_DEFAULT);
                    break;
                case ChatViewModel.FLAG_CANCEL_REQUEST_ADDFRIEND:
                    if (chatViewModel.getContacts(ChatViewModel.CONTACTS_INVITED_ADD_FRIEND).size() > 0) {
                        homeActivity.showNotifyContact(true);
                        showNotifyAddFriend(true, chatViewModel.getContacts(ChatViewModel.CONTACTS_INVITED_ADD_FRIEND).size());
                    } else {
                        homeActivity.showNotifyContact(false);
                        showNotifyAddFriend(false, 0);
                    }
                    chatViewModel.setFlagContact(ChatViewModel.FLAG_DEFAULT);
                    break;
            }
        });
    }

    private void initRecyclerViewFriend(List<Contact> contacts) {
        homeActivity.threadPoolExecutor.execute(() -> {
            handler.postDelayed(() -> {
                progressBar.setVisibility(View.GONE);
                Collections.sort(contacts, (t1, t2) -> Character.compare(Character.toLowerCase(t1.getName().charAt(0)),
                        Character.toLowerCase(t2.getName().charAt(0))));
                friendAdapter = new FriendAdapter(contacts, getContext());
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                recyclerViewFriend.setLayoutManager(linearLayoutManager);
                recyclerViewFriend.setAdapter(friendAdapter);
            }, 500);
        });
    }

    private void initRecyclerViewOnlineFriend(List<Contact> contacts) {
//        homeActivity.threadPoolExecutor.execute(() -> {
//            handler.postDelayed(() -> {
                onlineAdapter = new OnlineAdapter(contacts, getContext());
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                recyclerViewFriendOnline.setLayoutManager(linearLayoutManager);
                recyclerViewFriendOnline.setAdapter(onlineAdapter);
//            }, 500);
//        });
    }

    private void initView(View view) {
        recyclerViewFriendOnline = view.findViewById(R.id.recyclerview_friend_online);
        recyclerViewFriend = view.findViewById(R.id.recyclerview_friend);
        txtLayoutNotContact = view.findViewById(R.id.textview_notify);
        txtLayoutOnline = view.findViewById(R.id.textview_online);
        divider = view.findViewById(R.id.divider_online_friend);
        progressBar = view.findViewById(R.id.progressBar);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_addfriend, menu);

        MenuItem menuItem = menu.findItem(R.id.menu_add);
        View menuItemActionView = menuItem.getActionView();
        txtAmountNotification = menuItemActionView.findViewById(R.id.textview_amount_notification);
        menuItemActionView.setOnClickListener(v -> startActivityForResult(new Intent(homeActivity, SearchFirendActivity.class), REQUEST_SEARCH));

        if (chatViewModel.getContacts(ChatViewModel.CONTACTS_INVITED_ADD_FRIEND).size() > 0) {
            showNotifyAddFriend(true, chatViewModel.getContacts(ChatViewModel.CONTACTS_INVITED_ADD_FRIEND).size());
        } else {
            Disposable subscribe = Observable.defer(() -> Observable.just(tableContact.getContactsRequest()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(t -> {
                        if (t.size() > 0) {
                            chatViewModel.addContacts(t, ChatViewModel.CONTACTS_INVITED_ADD_FRIEND);
                            showNotifyAddFriend(true, chatViewModel.getContacts(ChatViewModel.CONTACTS_INVITED_ADD_FRIEND).size());
                        } else {
                            txtAmountNotification.setVisibility(View.GONE);
                            showNotifyAddFriend(false, 0);
                        }
                    });
            compositeDisposable.add(subscribe);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_SEARCH:
                if (resultCode == getActivity().RESULT_OK) {
                    Intent intent = new Intent(getContext(), ProfileActivity.class);
                    startActivity(intent);
                }
                break;
        }
    }

    public void showNotifyAddFriend(boolean visibility, int count) {
        if (txtAmountNotification != null) {
            if (visibility) {
                txtAmountNotification.setText(String.valueOf(count));
                txtAmountNotification.setVisibility(View.VISIBLE);
            } else {
                txtAmountNotification.setVisibility(View.GONE);
            }
        }
        homeActivity.showNotifyContact(visibility);
    }

    private void showLayoutFriendOnline() {
        if (contactsOnlineFriend.size() > 0) {
            txtLayoutOnline.setVisibility(View.VISIBLE);
            divider.setVisibility(View.VISIBLE);
        } else {
            divider.setVisibility(View.GONE);
            txtLayoutOnline.setVisibility(View.GONE);
        }
    }

    private void  showLayoutNotContact() {
        if (contactsFriend.size() > 0) {
            txtLayoutNotContact.setVisibility(View.GONE);
        } else {
            txtLayoutNotContact.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        showLayoutNotContact();
        showLayoutFriendOnline();
        if (onlineAdapter != null) {
            new Handler().postDelayed(() -> {
                if (contactsOnlineFriend.size() > 1) {
                    Collections.shuffle(contactsOnlineFriend);
                    onlineAdapter.notifyDataSetChanged();
                }
            }, 1000);
            onlineAdapter.notifyDataSetChanged();
        }

        if (friendAdapter != null && contactsFriend.size() > 1) {
            Collections.sort(contactsFriend, (t1, t2) -> Character.compare(Character.toLowerCase(t1.getName().charAt(0)),
                    Character.toLowerCase(t2.getName().charAt(0))));
            friendAdapter.notifyDataSetChanged();
        }

        if (txtAmountNotification != null) {
            if (chatViewModel.getContacts(ChatViewModel.CONTACTS_INVITED_ADD_FRIEND).size() > 0) {
                homeActivity.showNotifyContact(true);
                showNotifyAddFriend(true, chatViewModel.getContacts(ChatViewModel.CONTACTS_INVITED_ADD_FRIEND).size());
            } else {
                homeActivity.showNotifyContact(false);
                showNotifyAddFriend(false, 0);
            }
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
        super.onDestroy();
    }
}
