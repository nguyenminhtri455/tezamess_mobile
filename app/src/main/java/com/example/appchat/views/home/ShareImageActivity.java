package com.example.appchat.views.home;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.example.appchat.R;

public class ShareImageActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_image);
        initViews();
        initActionbar();

        Intent intent = getIntent();
        byte[] images = intent.getByteArrayExtra("image");
        Bitmap bitmap = BitmapFactory.decodeByteArray(images, 0, images.length);


        imageView.setImageBitmap(bitmap);

    }

    private void initActionbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_while_24dp);
        toolbar.setNavigationOnClickListener(t -> {
            onBackPressed();
        });
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_share_image);
        imageView = findViewById(R.id.img_share);
    }
}
