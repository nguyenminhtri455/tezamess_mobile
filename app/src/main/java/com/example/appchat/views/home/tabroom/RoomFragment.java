package com.example.appchat.views.home.tabroom;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
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
import com.example.appchat.objectclass.Room;
import com.example.appchat.viewmodel.ChatViewModel;
import com.example.appchat.viewmodel.ChatViewModelFactory;
import com.example.appchat.views.home.HomeActivity;
import com.example.appchat.views.home.tabmessage.newroom.NewRoomActivity;
import com.example.appchat.views.home.tabroom.adapter.ManyRoomAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoomFragment extends Fragment {

    private TextView txtQuantityRoom;
    private RecyclerView recyclerViewManyRoom;
    private ProgressBar progressBar;
    private ManyRoomAdapter manyRoomAdapter;
    private List<Room> listManyRoom;
    private ChatViewModel chatViewModel;
    private HomeActivity homeActivity;
    private Handler handler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        homeActivity = (HomeActivity) getActivity();
        handler = new Handler();
        chatViewModel = ViewModelProviders.of(this, ChatViewModelFactory.getInstance(getContext())).get(ChatViewModel.class);
        View view = inflater.inflate(R.layout.fragment_room, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listManyRoom = chatViewModel.getRooms(ChatViewModel.MANY_ROOM);
        if (listManyRoom.size() > 0) {
            initRecyclerViewManyRoom(listManyRoom);
        }else{
            progressBar.setVisibility(View.GONE);
        }

        chatViewModel.flagRoom.observe(this, t -> {
            switch (t) {
                case ChatViewModel.FLAG_INIT_CONVERSATION:
                    if (manyRoomAdapter == null) {
                        initRecyclerViewManyRoom(listManyRoom);
                    }
                    chatViewModel.setFlagRoom(ChatViewModel.FLAG_DEFAULT);
                    break;
//                case ChatViewModel.FLAG_ADD_CONVERSATION:
//                    if (manyRoomAdapter == null) {
//                        initRecyclerViewManyRoom(listManyRoom);
//                    } else {
//                        manyRoomAdapter.notifyDataSetChanged();
//                    }
//                    txtQuantityRoom.setText("(" + manyRoomAdapter.getItemCount() + ")");
//                    chatViewModel.setFlagRoom(ChatViewModel.FLAG_DEFAULT);
//                    break;
                case ChatViewModel.FLAG_RESET_UNREAD_CONVERSATION:
                    if (manyRoomAdapter != null) {
                        manyRoomAdapter.notifyResetQuantityUnReadMessageRoom(chatViewModel.tempIdRoom);
                    }
                    chatViewModel.setFlagRoom(ChatViewModel.FLAG_DEFAULT);
                    break;
//                case ChatViewModel.FLAG_CHANGE_LAST_MESSAGE_CONVERSATION:
//                    if (manyRoomAdapter != null) {
//                        manyRoomAdapter.notifyChangeLastChatMessageInRoom(chatViewModel.tempIdRoom);
//                    }
//                    chatViewModel.setFlagRoom(ChatViewModel.FLAG_DEFAULT);
//                    break;
                case ChatViewModel.FLAG_LEAVE_ROOM:
                    if (manyRoomAdapter != null) {
                        manyRoomAdapter.notifyDataSetChanged();
                    }
                    txtQuantityRoom.setText("(" + manyRoomAdapter.getItemCount() + ")");
                    chatViewModel.setFlagRoom(ChatViewModel.FLAG_DEFAULT);
                    break;
                case ChatViewModel.FLAG_UPDATE_ROOM:
                    if (manyRoomAdapter != null) {
                        manyRoomAdapter.notifyDataSetChanged();
                    }
                    chatViewModel.setFlagRoom(ChatViewModel.FLAG_DEFAULT);
                    break;
                case ChatViewModel.FLAG_UPDATE_CONVERSATION:
                    if (manyRoomAdapter == null) {
                        initRecyclerViewManyRoom(listManyRoom);
                    } else {
                        manyRoomAdapter.notifyDataSetChanged();
                    }
                    txtQuantityRoom.setText("(" + manyRoomAdapter.getItemCount() + ")");
                    chatViewModel.setFlagRoom(ChatViewModel.FLAG_DEFAULT);
                    break;
            }
        });

    }

    private void initRecyclerViewManyRoom(List<Room> listManyRoom) {
        homeActivity.threadPoolExecutor.execute(() -> {
            handler.postDelayed(() -> {
                progressBar.setVisibility(View.GONE);
                txtQuantityRoom.setText("(" + listManyRoom.size() + ")");
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
                recyclerViewManyRoom.addItemDecoration(dividerItemDecoration);
                recyclerViewManyRoom.setLayoutManager(linearLayoutManager);

                manyRoomAdapter = new ManyRoomAdapter(listManyRoom, getContext());
                recyclerViewManyRoom.setAdapter(manyRoomAdapter);
            }, 500);
        });
    }

    private boolean flag = false;

    @Override
    public void onResume() {
        super.onResume();
        if (!flag) {
            flag = !flag;
            return;
        }
        if (manyRoomAdapter == null) {
            initRecyclerViewManyRoom(listManyRoom);
        } else {
            List<Room> listNull = new ArrayList<>();
            for (Room room : listManyRoom) {
                if (room.getLastChatMessage() == null)
                    listNull.add(room);
            }
            listManyRoom.removeAll(listNull);
            Collections.sort(listManyRoom, (o1, o2) -> (int) (o2.getLastChatMessage().getCreatedate() - o1.getLastChatMessage().getCreatedate()));
            listManyRoom.addAll(listNull);
            manyRoomAdapter.notifyDataSetChanged();
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_group, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_addgroup);
        View menuItemActionView = menuItem.getActionView();
        menuItemActionView.setOnClickListener(v -> {
            Intent intent = new Intent(homeActivity, NewRoomActivity.class);
            startActivity(intent);
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void initViews(View view) {
        progressBar = view.findViewById(R.id.progressBar);
        txtQuantityRoom = view.findViewById(R.id.textview_quantity_group);
        recyclerViewManyRoom = view.findViewById(R.id.recyclerview_many_room);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
