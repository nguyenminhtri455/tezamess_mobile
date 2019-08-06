package com.example.appchat.views.home.tabsetting.about;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.appchat.R;

public class AboutActivity extends AppCompatActivity {

    private Toolbar tblAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initView();
        handleEvent();
    }

    private void handleEvent() {
        tblAbout.setNavigationIcon(R.drawable.ic_arrow_back_while_24dp);
        tblAbout.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AboutActivity.super.onBackPressed();
            }
        });
    }

    private void initView() {
        tblAbout = findViewById(R.id.toolbar_about);
    }
}
