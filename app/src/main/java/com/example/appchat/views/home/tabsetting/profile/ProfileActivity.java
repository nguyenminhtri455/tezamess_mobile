package com.example.appchat.views.home.tabsetting.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.appchat.R;
import com.example.appchat.customview.CircleImage;
import com.example.appchat.objectclass.Member;
import com.example.appchat.views.home.tabsetting.verificationemail.VerificationEmailActivity;
import com.example.appchat.views.home.tabsetting.profile.fragment.EditProfileFragment;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;

public class ProfileActivity extends AppCompatActivity implements IViewViewProfile {

    private  static final int REQUEST_UPDATE_EMAIL = 0;
    private CircleImage imgAvatar;
    private TextView txtGender;
    private TextView txtBirthday;
    private TextView txtPhone;
    public TextView txtEmail;
    private Toolbar tlbProfile;
    private RelativeLayout layoutMain;
    private AppBarLayout appbarLayout;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private EditProfileFragment editProfileFragment;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private Member member;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        member = Member.getInstance(this);
        fragmentManager = getSupportFragmentManager();

        initView();
        initActionBar();
        handlerEvents();
        setMember();
    }

    private void initActionBar() {
        setSupportActionBar(tlbProfile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tlbProfile.setNavigationIcon(R.drawable.ic_arrow_back_while_24dp);
        tlbProfile.setNavigationOnClickListener(t -> {
            onBackPressed();
        });
    }

    private void setMember() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Picasso.get()
                .load(member.getUrlavatar())
                .placeholder(R.drawable.account_icon)
                .error(R.drawable.account_icon)
                .into(imgAvatar);
        collapsingToolbarLayout.setTitle(member.getName());
        txtPhone.setText(member.getPhone());
        txtBirthday.setText(simpleDateFormat.format(member.getBirthday()));

        if (!member.getEmail().equals("null")) {
            txtEmail.setText(member.getEmail());
            txtEmail.setEnabled(false);
        } else {
            txtEmail.setText(getResources().getString(R.string.textview_verify_email_view_profile));
            txtEmail.setEnabled(true);
        }

        if (!member.isGender()) {
            txtGender.setText(getResources().getString(R.string.textview_female_view_profile));
        } else {
            txtGender.setText(getResources().getString(R.string.textview_male_view_profile));
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_edit) {
            layoutMain.setVisibility(View.GONE);
            appbarLayout.setVisibility(View.GONE);
            editProfileFragment = EditProfileFragment.newInstance();
            fragmentTransaction = fragmentManager.beginTransaction();
//            fragmentTransaction.addSharedElement(imgAvatar, ViewCompat.getTransitionName(imgAvatar));
            fragmentTransaction.replace(R.id.layout_main2, editProfileFragment);
            fragmentTransaction.commit();
        }
        return true;
    }

    private void handlerEvents() {
        txtEmail.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, VerificationEmailActivity.class);
            startActivityForResult(intent,REQUEST_UPDATE_EMAIL);
        });
    }

    private void initView() {
        appbarLayout = findViewById(R.id.layout_name);
        collapsingToolbarLayout = findViewById(R.id.collapsingLayout);
        layoutMain = findViewById(R.id.layout_main);
        imgAvatar = findViewById(R.id.imageview_avatar);
        txtPhone = findViewById(R.id.textview_phone_tabprofile);
        txtBirthday = findViewById(R.id.textview_birthday_tabprofile);
        txtGender = findViewById(R.id.textview_gender_tabprofile);
        txtEmail = findViewById(R.id.textview_email_tabprofile);
        tlbProfile = findViewById(R.id.toolbar_profile);

    }

    @Override
    public void updateSuccess() {
        editProfileFragment.showProgressbar(false);
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(editProfileFragment).commit();
        setMember();
        appbarLayout.setVisibility(View.VISIBLE);
        layoutMain.setVisibility(View.VISIBLE);
    }

    @Override
    public void updateFailed(String messge) {

    }

    @Override
    public void connectError(String messge) {

    }

    @Override
    public void onBackPressed() {
        if (editProfileFragment != null && editProfileFragment.isVisible()) {
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(editProfileFragment).commit();
            appbarLayout.setVisibility(View.VISIBLE);
            layoutMain.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_UPDATE_EMAIL){
            if(resultCode == RESULT_OK){
                txtEmail.setText(member.getEmail());
                txtEmail.setEnabled(false);
            }
        }
    }
}
