package com.example.appchat.views.home.tabmessage;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.example.appchat.R;
import com.example.appchat.objectclass.Room;
import com.example.appchat.viewmodel.ChatViewModel;
import com.example.appchat.viewmodel.ChatViewModelFactory;
import com.example.appchat.views.home.HomeActivity;
import com.example.appchat.views.home.tabcontact.searchfriend.view.SearchFirendActivity;
import com.example.appchat.views.home.tabmessage.newroom.NewRoomActivity;
import com.example.appchat.views.home.tabmessage.roomchat.adapter.ConversationAdapter;
import com.example.appchat.views.home.tabsetting.profile.ProfileActivity;

import java.util.Collections;
import java.util.List;

public class MessageFragment extends Fragment {
    private final int REQUEST_SEARCH = 0;
    private HomeActivity homeActivity;
    private ChatViewModel chatViewModel;
    private Handler handler;

    private RecyclerView recyclerViewConversation;
    private LinearLayout layoutNoConversation;
    private ProgressBar progressBar;
    private ConversationAdapter conversationAdapter;
    private List<Room> listRoom;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeActivity = (HomeActivity) getActivity();
        handler = new Handler();
        chatViewModel = ViewModelProviders.of(this, ChatViewModelFactory.getInstance(getContext())).get(ChatViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        initViews(view);
        setHasOptionsMenu(true);

        listRoom = chatViewModel.getRooms(ChatViewModel.ALL_ROOM);
//        if (listRoom.size() > 0) {
            initConversation(listRoom);
//        }
//        showLayoutConversation();
        return view;
    }

    private void initViews(View view) {
        recyclerViewConversation = view.findViewById(R.id.recyclerview_message);
        layoutNoConversation = view.findViewById(R.id.layout_no_conversation);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void showLayoutConversation() {
        if (listRoom.size() > 0) {
            layoutNoConversation.setVisibility(View.GONE);
        } else {
            layoutNoConversation.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chatViewModel.flagRoom.observe(this, t -> {
            switch (t) {
                case ChatViewModel.FLAG_INIT_CONVERSATION:
                    if (conversationAdapter == null) {
                        initConversation(listRoom);
                    } else {
                        conversationAdapter.notifyDataSetChanged();
                    }
                    showLayoutConversation();
                    chatViewModel.setFlagRoom(ChatViewModel.FLAG_DEFAULT);
                    break;
                case ChatViewModel.FLAG_RESET_UNREAD_CONVERSATION:
                    if (conversationAdapter != null) {
                        conversationAdapter.notifyResetQuantityUnReadMessageRoom(chatViewModel.tempIdRoom);
                    }
                    chatViewModel.setFlagRoom(ChatViewModel.FLAG_DEFAULT);
                    break;
                case ChatViewModel.FLAG_LEAVE_ROOM:
                    if (conversationAdapter != null) {
                        conversationAdapter.notifyDataSetChanged();
                    }
                    showLayoutConversation();
                    chatViewModel.setFlagRoom(ChatViewModel.FLAG_DEFAULT);
                    break;
                case ChatViewModel.FLAG_UPDATE_ROOM:
                    if (conversationAdapter != null) {
                        conversationAdapter.notifyDataSetChanged();
                    }
                    showLayoutConversation();
                    chatViewModel.setFlagRoom(ChatViewModel.FLAG_DEFAULT);
                    break;
                case ChatViewModel.FLAG_UPDATE_CONVERSATION:
                    if (conversationAdapter == null) {
                        initConversation(listRoom);
                    } else {
                        conversationAdapter.notifyDataSetChanged();
                    }
                    showLayoutConversation();
                    chatViewModel.setFlagRoom(ChatViewModel.FLAG_DEFAULT);
                    break;
            }
        });
    }

    public synchronized void initConversation(List<Room> rooms) {
        homeActivity.threadPoolExecutor.execute(() -> {
            handler.postDelayed(() -> {
                showLayoutConversation();
                progressBar.setVisibility(View.GONE);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
                recyclerViewConversation.addItemDecoration(dividerItemDecoration);
                recyclerViewConversation.setLayoutManager(linearLayoutManager);
                conversationAdapter = new ConversationAdapter(rooms, getContext());
                recyclerViewConversation.setAdapter(conversationAdapter);
            }, 500);
        });

    }


    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_tab_message, menu);
        MenuItem menuItemCreateGroup = menu.findItem(R.id.item_plus);
        View menuItemActionView = menuItemCreateGroup.getActionView();
        menuItemActionView.setOnClickListener(v -> {
            showProfileMenuPopup(v);
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @SuppressLint("RestrictedApi")
    private void showProfileMenuPopup(View v) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_sub_plus, popup.getMenu());
        popup.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.item_create_group:
                    Intent intent = new Intent(homeActivity, NewRoomActivity.class);
                    startActivity(intent);
                    break;
                case R.id.item_add_friend:
                    startActivityForResult(new Intent(homeActivity, SearchFirendActivity.class), REQUEST_SEARCH);
                    break;
            }
            return false;
        });
        MenuPopupHelper menuHelper = new MenuPopupHelper(getContext(), (MenuBuilder) popup.getMenu(), v);
        menuHelper.setForceShowIcon(true);
        menuHelper.show();
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

    private boolean flag = false;

    @Override
    public void onResume() {
        super.onResume();
        if (!flag) {
            flag = !flag;
            return;
        }
        showLayoutConversation();
        if (conversationAdapter != null) {
            Collections.sort(listRoom, (r1, r2) -> (int) (r2.getLastChatMessage().getCreatedate() - r1.getLastChatMessage().getCreatedate()));
            conversationAdapter.notifyDataSetChanged();
        } else {
//            if (listRoom.size() > 0 && listRoom.get(0).getLastChatMessage() != null && listRoom.get(0).getContacts().size() > 0) {
//                Collections.sort(listRoom, (r1, r2) -> (int) (r2.getLastChatMessage().getCreatedate() - r1.getLastChatMessage().getCreatedate()));
//                initConversation(listRoom);
//            }
            if (listRoom.size() > 0) {
                Collections.sort(listRoom, (r1, r2) -> (int) (r2.getLastChatMessage().getCreatedate() - r1.getLastChatMessage().getCreatedate()));
                initConversation(listRoom);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
