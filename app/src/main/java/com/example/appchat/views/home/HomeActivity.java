package com.example.appchat.views.home;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.appchat.R;
import com.example.appchat.database.TableContact;
import com.example.appchat.database.TableMessage;
import com.example.appchat.objectclass.Member;
import com.example.appchat.viewmodel.ChatViewModel;
import com.example.appchat.viewmodel.ChatViewModelFactory;
import com.example.appchat.views.home.tabcontact.ContactFragment;
import com.example.appchat.views.home.tabcontact.adapter.ViewPagerAdapter;
import com.example.appchat.views.home.tabmessage.MessageFragment;
import com.example.appchat.views.home.tabroom.RoomFragment;
import com.example.appchat.views.home.tabsetting.SettingFragment;
import com.example.appchat.views.home.tabtimeline.TimelineFragment;
import com.example.appchat.websocket.WebSocket;
import com.example.appchat.widget.connection.CheckConnection;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class HomeActivity extends AppCompatActivity implements IViewHome {

    //index tab
    public static final int MESSAGE_INDEX = 0;
    public static final int CONTACT_INDEX = 1;
    public static final int ROOM_INDEX = 2;
    public static final int TIMELINE_INDEX = 3;
    public static final int SETTING_INDEX = 4;
    public static final String CHANNEL_ID = "com.tezamess.vn";
    public static final String CHANNEL_NAME = "TezamessChannel";
    public static final String CHANNEL_DESCRIPTIONE = "Tezamessdescription";
    public static int CHANGE_LANGUAGE = 0;

    private BottomNavigationView bottomNavigationView;
    private ViewPager viewPager;
    private ViewPagerAdapter pagerAdapter;
    public TextView txtNotifyMessage, txtNotifyContact, txtNotifyPostStatus;
    private Toolbar tlbHome;
    private ChatViewModel chatViewModel;

    private ListenInternetReceiver listenInternetReceiver;
    private WebSocket webSocket;
    private Member admin;
    private TableMessage tableMessage;
    private TableContact tableContact;
    private CompositeDisposable compositeDisposable;
    public ThreadPoolExecutor threadPoolExecutor;
    public ArrayBlockingQueue<Runnable> queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        chatViewModel = ViewModelProviders.of(this, ChatViewModelFactory.getInstance(getApplicationContext())).get(ChatViewModel.class);
        compositeDisposable = new CompositeDisposable();
        admin = Member.getInstance(this);
        tableMessage = TableMessage.getInstance(this);
        tableContact = TableContact.getInstance(this);
        initView();
        initBottomNavagation();
        initViewPager();
        queue = new ArrayBlockingQueue<>(10);
        threadPoolExecutor = new ThreadPoolExecutor(5, 5, 5, TimeUnit.SECONDS, queue);
        handleEvent();
        createNotificationChannel();
        listenInternetReceiver = new ListenInternetReceiver();
        IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(listenInternetReceiver, filter);
        webSocket = WebSocket.getInstance(this);
        getTotalUnreadMessages();

        chatViewModel.quantity_unread_message.observe(this, t -> {
            if (t > 0 && t <= 5) {
                txtNotifyMessage.setText(String.valueOf(t));
                txtNotifyMessage.setVisibility(View.VISIBLE);
            } else if (t > 5) {
                txtNotifyMessage.setText("5+");
                txtNotifyMessage.setVisibility(View.VISIBLE);
            } else if (t <= 0) {
                txtNotifyMessage.setVisibility(View.GONE);
            }
        });


//        DataClient dataClient = RetrofitClient.getRetrofit().create(DataClient.class);
//        Call<String> call = dataClient.test();
//
//        RetrofitClient.excute(call, s -> Log.d("ZZZZZ",s));
    }

    private void getTotalUnreadMessages() {
        Disposable subscribe = Observable.defer(() ->
                Observable.just(tableMessage.getTotalUnreadChatMessages()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(t -> {
                    chatViewModel.quantity_unread_message.setValue(t);
                });
        compositeDisposable.add(subscribe);
    }

    private MessageFragment messageFragment;
    private ContactFragment contactFragment;
    private RoomFragment roomFragment;
    private TimelineFragment timelineFragment;
    private SettingFragment settingFragment;

    private void initViewPager() {
        messageFragment = new MessageFragment();
        contactFragment = new ContactFragment();
        roomFragment = new RoomFragment();
        timelineFragment = new TimelineFragment();
        settingFragment = new SettingFragment();

        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(messageFragment, "Message");
        pagerAdapter.addFragment(contactFragment, "Contact");
        pagerAdapter.addFragment(roomFragment, "Group");
        pagerAdapter.addFragment(timelineFragment, "Timeline");
        pagerAdapter.addFragment(settingFragment, "Setting");
        viewPager.setAdapter(pagerAdapter);
    }

    private void initBottomNavagation() {
        BottomNavigationMenuView bottomNavigationMenuView =
                (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);

        //tab message
        View viewMessage = bottomNavigationMenuView.getChildAt(0);
        BottomNavigationItemView itemViewMessage = (BottomNavigationItemView) viewMessage;
        View notifyMessage = LayoutInflater.from(this)
                .inflate(R.layout.custom_item_round,
                        bottomNavigationMenuView, false);
        txtNotifyMessage = notifyMessage.findViewById(R.id.text_view_notify);
        itemViewMessage.addView(notifyMessage);

        //tab contact
        View viewContact = bottomNavigationMenuView.getChildAt(1);
        BottomNavigationItemView itemViewContact = (BottomNavigationItemView) viewContact;
        View notifyContact = LayoutInflater.from(this)
                .inflate(R.layout.custom_item_round,
                        bottomNavigationMenuView, false);
        txtNotifyContact = notifyContact.findViewById(R.id.text_view_notify);
        itemViewContact.addView(notifyContact);

        Disposable subscribe = Observable.defer(() -> Observable.just(tableContact.getContactsRequest()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(t -> {
                    if (t.size() > 0) {
                        showNotifyContact(true);
                    } else {
                        showNotifyContact(false);
                    }
                });
        compositeDisposable.add(subscribe);

        //tab timeline
        View viewTimeline = bottomNavigationMenuView.getChildAt(3);
        BottomNavigationItemView itemViewTimeline = (BottomNavigationItemView) viewTimeline;
        View notifyPostStatus = LayoutInflater.from(this)
                .inflate(R.layout.custom_item_round_timeline,
                        bottomNavigationMenuView, false);
        txtNotifyPostStatus = notifyPostStatus.findViewById(R.id.text_view_notify);
        itemViewTimeline.addView(notifyPostStatus);
    }

    public void showNotifiPostStatus(boolean visibility) {
        if (visibility) {
            txtNotifyPostStatus.setVisibility(View.VISIBLE);
        } else {
            txtNotifyPostStatus.setVisibility(View.GONE);
        }
    }

    private void handleEvent() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.tab_message:
                        viewPager.setCurrentItem(MESSAGE_INDEX);
                        bottomNavigationView.getMenu().getItem(MESSAGE_INDEX).setChecked(true);
                        break;
                    case R.id.tab_contact:
                        viewPager.setCurrentItem(CONTACT_INDEX);
                        bottomNavigationView.getMenu().getItem(CONTACT_INDEX).setChecked(true);
                        break;
                    case R.id.tab_group:
                        viewPager.setCurrentItem(ROOM_INDEX);
                        bottomNavigationView.getMenu().getItem(ROOM_INDEX).setChecked(true);
                        break;
                    case R.id.tab_timeline:
                        if (viewPager.getCurrentItem() == TIMELINE_INDEX) {
                            timelineFragment.scrollToTop();
                        } else {
                            viewPager.setCurrentItem(TIMELINE_INDEX);
                            bottomNavigationView.getMenu().getItem(TIMELINE_INDEX).setChecked(true);
                        }
                        break;
                    case R.id.tab_setting:
                        viewPager.setCurrentItem(SETTING_INDEX);
                        bottomNavigationView.getMenu().getItem(SETTING_INDEX).setChecked(true);
                        break;
                }
                return true;
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                bottomNavigationView.getMenu().getItem(i).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
    }

    private void initView() {
        viewPager = findViewById(R.id.view_pager);
        tlbHome = findViewById(R.id.toolbar_home);
        bottomNavigationView = findViewById(R.id.navigation_tab);
        setSupportActionBar(tlbHome);
    }

    public void tranferTab(int position) {
        viewPager.setCurrentItem(position);
        bottomNavigationView.getMenu().getItem(position).setChecked(true);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            channel.setDescription(CHANNEL_DESCRIPTIONE);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void showNotifyContact(boolean visibility) {
        if (visibility) {
            txtNotifyContact.setVisibility(View.VISIBLE);
        } else {
            txtNotifyContact.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onDestroy() {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
        chatViewModel.clear();
        webSocket.destroy();
        unregisterReceiver(listenInternetReceiver);
        super.onDestroy();
    }

    class ListenInternetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (CheckConnection.haveNetworkConnection(context)) {
                webSocket.webSocketConnect();
                if (chatViewModel.flagCheckRoomExist != -2) {
                    chatViewModel.setFlagRoom(ChatViewModel.FLAG_RESUM_ROOM);
                }
            } else {

            }
        }
    }
}
