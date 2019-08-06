package com.example.appchat.views.home.tabtimeline.poststatus;

import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.asksira.bsimagepicker.BSImagePicker;
import com.bumptech.glide.Glide;
import com.example.appchat.R;
import com.example.appchat.customview.CustomToast;
import com.example.appchat.customview.ProgressBarDialog;
import com.example.appchat.objectclass.Avatar;
import com.example.appchat.objectclass.Contact;
import com.example.appchat.objectclass.Member;
import com.example.appchat.objectclass.Status;
import com.example.appchat.viewmodel.ChatViewModel;
import com.example.appchat.viewmodel.ChatViewModelFactory;
import com.example.appchat.views.home.tabtimeline.poststatus.adapter.PreviewImageAdapter;
import com.example.appchat.websocket.WebSocket;
import com.example.appchat.widget.connection.CheckConnection;
import com.example.appchat.widget.image.ScaleBitmap;
import com.example.appchat.widget.mapjson.MappedStatusToJson;
import com.example.appchat.widget.retrofit.DataClient;
import com.example.appchat.widget.retrofit.RetrofitClient;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Retrofit;

public class PostStatusActivity extends AppCompatActivity implements BSImagePicker.OnMultiImageSelectedListener,
        BSImagePicker.ImageLoaderDelegate {

    public static final int RESPONSE_POST_STATUS = 2;

    private Toolbar toolbar;
    private EditText editTextContent;
    private ImageView imgChooseImage;
    private PreviewImageAdapter previewImageAdapter;
    private RecyclerView recyclerViewPreviewImage;
    private List<Uri> uris;
    private ProgressBarDialog progressBarDialog;
    private Retrofit retrofit = RetrofitClient.getRetrofit();
    private DataClient dataClient = retrofit.create(DataClient.class);
    private Call<String> call;
    private CompositeDisposable compositeDisposable;
    private ChatViewModel chatViewModel;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_status);
        initViews();
        initActionbar();
        initEvents();
        uris = new ArrayList<>();
        progressBarDialog = new ProgressBarDialog();
        compositeDisposable = new CompositeDisposable();
        chatViewModel = ViewModelProviders.of(this, ChatViewModelFactory.getInstance(this)).get(ChatViewModel.class);

        chatViewModel.flagStatus.observe(this, t -> {
            switch (t) {
                case ChatViewModel.FLAG_POST_STATUS:
                    setResult(RESPONSE_POST_STATUS);
                    finish();
                    showProgressBarDiaglog(false);
                    chatViewModel.setFlagStatus(ChatViewModel.FLAG_DEFAULT);
                    break;
            }
        });
    }

    private void initEvents() {
        imgChooseImage.setOnClickListener(t -> {
            showMultiSelectionPicker();
        });
    }

    private void initActionbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_while_24dp);
        toolbar.setNavigationOnClickListener(t -> {
            onBackPressed();
        });
    }

    private void initRecyclerViewPreviewImage(List<Uri> uris) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewPreviewImage.setLayoutManager(linearLayoutManager);
        previewImageAdapter = new PreviewImageAdapter(this, uris);
        recyclerViewPreviewImage.setAdapter(previewImageAdapter);
    }

    private void showProgressBarDiaglog(boolean visibility) {
        if (visibility) {
            progressBarDialog.show(getSupportFragmentManager(), "dialog");
        } else {
            progressBarDialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_post_status, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (CheckConnection.haveNetworkConnection(this)) {
            if (WebSocket.stompClient != null) {
                if (WebSocket.stompClient.isConnected()) {
                    String body = editTextContent.getText().toString().trim();
                    if (body.isEmpty()) {
                        CustomToast.makeText(this, getResources().getString(R.string.enter_body), Toast.LENGTH_SHORT).show();
                    } else {
                        showProgressBarDiaglog(true);
                        Disposable subscribe = Completable.create(o -> {
                            Status status = new Status();
                            status.setBody(body);
                            status.setUserid(new Contact(Member.getInstance(this).getId()));
                            status.setCreatedate(System.currentTimeMillis());
                            for (Uri uri : uris) {
                                try {
                                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                                    Bitmap scaleAfterBitmap = ScaleBitmap.scaleBitmap(bitmap, 1024 * 1024);

                                    if (bitmap != scaleAfterBitmap) {
                                        bitmap.recycle();
                                    }
                                    Avatar avatar = ScaleBitmap.encodeBase64Avatar(PostStatusActivity.this
                                            , scaleAfterBitmap, uri.getPath());
                                    status.getList().add(avatar);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            Map<String, Object> map = MappedStatusToJson.mapStatusToJson(status);

                            call = dataClient.postStatus(Member.getInstance(this).getToken(this), gson.toJson(map));

                            RetrofitClient.excute(call, t -> {

                            });

                            o.onComplete();
                        }).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> Log.d("BBBBB", "post status thanh cong"),
                                        error -> error.getMessage());
                        compositeDisposable.add(subscribe);
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
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_post_status);
        editTextContent = findViewById(R.id.edittext_context_status);
        imgChooseImage = findViewById(R.id.image_view_choose_images);
        recyclerViewPreviewImage = findViewById(R.id.recyclerview_preview_image_status);
    }

    public void showMultiSelectionPicker() {
        BSImagePicker multiSelectionPicker = new BSImagePicker.Builder("com.example.appchat.fileprovider")
                .isMultiSelect() //Set this if you want to use multi selection mode.
                .setMinimumMultiSelectCount(1) //Default: 1.
                .setMaximumMultiSelectCount(9) //Default: Integer.MAX_VALUE (i.e. User can select as many images as he/she wants)
                .setMultiSelectBarBgColor(android.R.color.white) //Default: #FFFFFF. You can also set it to a translucent color.
                .setMultiSelectTextColor(R.color.primary_text) //Default: #212121(Dark grey). This is the message in the multi-select bottom bar.
                .setMultiSelectDoneTextColor(R.color.colorAccent) //Default: #388e3c(Green). This is the color of the "Done" TextView.
                .setOverSelectTextColor(R.color.error_text) //Default: #b71c1c. This is the color of the message shown when user tries to select more than maximum select count.
                .disableOverSelectionMessage() //You can also decide not to show this over select message.
                .build();
        multiSelectionPicker.show(getSupportFragmentManager(), "picker");
    }

    @Override
    public void onMultiImageSelected(List<Uri> uriList, String tag) {
//        uris.clear();

        if ((uris.size() + uriList.size() > 9)) {
            CustomToast.makeText(this, "chi duoc chon 9 tam hinh", Toast.LENGTH_SHORT).show();
        } else {
            uris.removeAll(uriList);
            uris.addAll(uriList);
            if (previewImageAdapter == null) {
                initRecyclerViewPreviewImage(uris);
            } else {
                previewImageAdapter.notifyDataSetChanged();
            }
        }
    }


    @Override
    public void loadImage(File imageFile, ImageView ivImage) {
        Glide.with(PostStatusActivity.this).load(imageFile).into(ivImage);
    }
}
