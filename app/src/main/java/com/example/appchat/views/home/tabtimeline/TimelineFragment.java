package com.example.appchat.views.home.tabtimeline;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appchat.R;
import com.example.appchat.customview.CircleImage;
import com.example.appchat.customview.CustomToast;
import com.example.appchat.objectclass.Member;
import com.example.appchat.objectclass.Status;
import com.example.appchat.viewmodel.ChatViewModel;
import com.example.appchat.viewmodel.ChatViewModelFactory;
import com.example.appchat.views.home.HomeActivity;
import com.example.appchat.views.home.tabtimeline.adapter.StatusAdapter;
import com.example.appchat.views.home.tabtimeline.poststatus.PostStatusActivity;
import com.example.appchat.websocket.WebSocket;
import com.example.appchat.widget.connection.CheckConnection;
import com.example.appchat.widget.retrofit.DataClient;
import com.example.appchat.widget.retrofit.RetrofitClient;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Retrofit;

public class TimelineFragment extends Fragment {


    public static final int REQUEST_POST_STATUS = 0;
    private CircleImage imgAvatar;

    private TextView txtPostStatus, txtError;
    private RecyclerView recyclerView;
    private CardView cardView;
    private HomeActivity homeActivity;
    private List<Status> statusListAll;
    private StatusAdapter statusAdapter;
    private ChatViewModel chatViewModel;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar, progressBarLoadMore;
    private Retrofit retrofit = RetrofitClient.getRetrofit();
    private DataClient dataClient = retrofit.create(DataClient.class);
    private Call<String> call;
    private CompositeDisposable compositeDisposable;
    private Gson gson = new Gson();
    private Handler handler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        Log.d("BBBBB", "onCreateView");
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);
        initView(view);
        if (statusListAll.isEmpty()) {
            loadStatused();
        }
        swipeRefreshLayout.setColorSchemeResources(R.color.colorBlue);
        return view;
    }

    @Override
    public void onResume() {
//        Log.d("BBBBB", "onResume");
        super.onResume();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
//        Log.d("BBBBB", "onCreate");
        homeActivity = (HomeActivity) getActivity();
        handler = new Handler();
        chatViewModel = ViewModelProviders.of(this, ChatViewModelFactory.getInstance(getContext())).get(ChatViewModel.class);
        compositeDisposable = new CompositeDisposable();
        setHasOptionsMenu(true);
        statusListAll = chatViewModel.getStatuses(ChatViewModel.STATUS_ALL);

        super.onCreate(savedInstanceState);
    }

    private void loadStatused() {
        if (CheckConnection.haveNetworkConnection(getContext())) {
            if (WebSocket.stompClient != null) {
                if (WebSocket.stompClient.isConnected()) {
                    Disposable subscribe = Completable.create(o -> {
                        sizeBeforeLoad = statusListAll.size();
                        Map<String, Object> map = new HashMap<>();
                        map.put("userid", Member.getInstance(getContext()).getId());
                        call = dataClient.getStatuses(Member.getInstance(getContext()).getToken(getContext()), gson.toJson(map));

                        RetrofitClient.excute(call, t -> {

                        });

                        o.onComplete();
                    }).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(() -> Log.d("BBBBB", "get status thanh cong"),
                                    error -> error.getMessage());
                    compositeDisposable.add(subscribe);
                } else {
                    txtError.setText(getResources()
                            .getString(R.string.server_error));
                    txtError.setVisibility(View.VISIBLE);
                }
            } else {
                txtError.setText(getResources()
                        .getString(R.string.server_error));
                txtError.setVisibility(View.VISIBLE);
            }
        } else {
            txtError.setText(getResources()
                    .getString(R.string.notification_noconnection));
            txtError.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStop() {
//        Log.d("BBBBB", "onStop");
        super.onStop();
    }

    @Override
    public void onDestroy() {
//        Log.d("BBBBB", "onDestroy");
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
        super.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        Log.d("BBBBB", "onAttach");
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        Log.d("BBBBB", "onDetach");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        Log.d("BBBBB", "onDestroyView");
    }

    @Override
    public void onPause() {
        super.onPause();
//        Log.d("BBBBB", "onPause");
    }

    @Override
    public void onStart() {
        super.onStart();
//        Log.d("BBBBB", "onStart");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        Log.d("BBBBB", "onViewCreated");
        chatViewModel.flagStatus.observe(this, t -> {
            switch (t) {
                case ChatViewModel.FLAG_GET_STATUSES:
                    swipeRefreshLayout.setRefreshing(false);
                    if (statusListAll.size() != sizeBeforeLoad) {
                        if (statusAdapter == null) {
                            initRecyclerViewStatus(statusListAll);
                        } else {
                            statusAdapter.notifyItemRangeInserted(0, (statusListAll.size() - sizeBeforeLoad));
                        }
                    }
                    chatViewModel.setFlagStatus(ChatViewModel.FLAG_DEFAULT);
                    break;
                case ChatViewModel.FLAG_POST_STATUS:
                    if (statusAdapter == null) {
                        initRecyclerViewStatus(statusListAll);
                    } else {
                        statusAdapter.notifyItemInserted(0);
                    }
                    chatViewModel.setFlagStatus(ChatViewModel.FLAG_DEFAULT);
                    break;
                case ChatViewModel.FLAG_LOADMORE_STATUSES:
                    progressBarLoadMore.setVisibility(View.GONE);
                    if (statusAdapter == null) {
                        initRecyclerViewStatus(statusListAll);
                    } else {
                        statusAdapter.notifyItemRangeInserted(sizeBeforeLoad, 10);
                    }
                    chatViewModel.setFlagStatus(ChatViewModel.FLAG_DEFAULT);
                    break;
            }
        });
        homeActivity.showNotifiPostStatus(false);
        initRecyclerViewStatus(statusListAll);
        initEvents();

    }

    public void showLayoutTimeline(boolean visibility) {
        if (visibility) {
            cardView.setVisibility(View.VISIBLE);
        } else {
            cardView.setVisibility(View.GONE);
        }
    }


    public void scrollToTop() {
        homeActivity.showNotifiPostStatus(false);
        recyclerView.smoothScrollToPosition(0);
    }


    private void initEvents() {
        txtPostStatus.setOnClickListener(t -> {
            Intent intent = new Intent(homeActivity, PostStatusActivity.class);
            startActivityForResult(intent, REQUEST_POST_STATUS);
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadStatused();
        });
    }


    private void initView(View view) {
        cardView = view.findViewById(R.id.layout_timeline);
        imgAvatar = view.findViewById(R.id.imageview_avatar);
        txtPostStatus = view.findViewById(R.id.textview_post_status);
        recyclerView = view.findViewById(R.id.recyclerview_status);
        swipeRefreshLayout = view.findViewById(R.id.swiperefersh);
        txtError = view.findViewById(R.id.textview_error);
        progressBar = view.findViewById(R.id.progressBar);
        progressBarLoadMore = view.findViewById(R.id.progressBarLoadMore);
    }


    private void initRecyclerViewStatus(List<Status> list) {
        homeActivity.threadPoolExecutor.execute(() -> {
            handler.postDelayed(() -> {
                progressBar.setVisibility(View.GONE);
                if (CheckConnection.haveNetworkConnection(getContext())) {
                    if (WebSocket.stompClient != null && WebSocket.stompClient.isConnected()) {
                        txtError.setVisibility(View.GONE);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
                        Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.custom_divider);
                        dividerItemDecoration.setDrawable(drawable);
                        recyclerView.setHasFixedSize(true);
                        recyclerView.addItemDecoration(dividerItemDecoration);
                        recyclerView.setLayoutManager(layoutManager);
                        if (statusAdapter == null) {
                            statusAdapter = new StatusAdapter(getContext(), list);
                        }

                        statusAdapter.setLoadMore(() -> {
                            if (statusAdapter.getY() > 0) {
                                progressBarLoadMore.setVisibility(View.VISIBLE);
                                new Handler().postDelayed(() -> {
                                    progressBarLoadMore.setVisibility(View.GONE);
                                    statusAdapter.setLoaded();
                                    loadMoreStatus(statusListAll.size(), 10);
                                }, 1000);
                            } else {
                                statusAdapter.setLoaded();
                            }
                        });

                        statusAdapter.setRecyclerView(recyclerView);
                        recyclerView.setAdapter(statusAdapter);
                    }
                }
            }, 500);

        });
    }

    private int sizeBeforeLoad;

    private void loadMoreStatus(int start, int limit) {
        if (CheckConnection.haveNetworkConnection(getContext())) {
            if (WebSocket.stompClient != null & WebSocket.stompClient.isConnected()) {

                Disposable subscribe = Completable.create(o -> {
                    sizeBeforeLoad = statusListAll.size();
                    Map<String, Object> map = new HashMap<>();
                    map.put("userid", Member.getInstance(getContext()).getId());
                    map.put("idStart", start);
                    map.put("count", limit);
                    call = dataClient.loadMoreStatused(Member.getInstance(getContext()).getToken(getContext()), gson.toJson(map));

                    RetrofitClient.excute(call, t -> {

                    });

                    o.onComplete();
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> Log.d("BBBBB", "request loadmore status thanh cong"),
                                error -> error.getMessage());
                compositeDisposable.add(subscribe);
            } else {
                CustomToast.makeText(homeActivity, getResources()
                        .getString(R.string.server_error), Toast.LENGTH_SHORT).show();
            }
        } else {
            CustomToast.makeText(homeActivity, getResources()
                    .getString(R.string.notification_noconnection), Toast.LENGTH_SHORT).show();
        }
    }


    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_notification, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_POST_STATUS:
                if (resultCode == PostStatusActivity.RESPONSE_POST_STATUS) {
                    if (statusAdapter == null) {
                        initRecyclerViewStatus(statusListAll);
                    } else {
                        statusAdapter.notifyItemInserted(0);
                        recyclerView.smoothScrollToPosition(0);
                    }
                }
                break;
        }
    }
}
