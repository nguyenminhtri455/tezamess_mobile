package com.example.appchat.views.register;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.appchat.R;
import com.example.appchat.customview.CustomToast;
import com.example.appchat.customview.ProgressBarDialog;
import com.example.appchat.presenters.login.PresenterLogin;
import com.example.appchat.views.home.HomeActivity;
import com.example.appchat.views.register.fragment.RegisterFagment;

import java.util.ArrayList;
import java.util.List;

public class RegisteActivity extends AppCompatActivity implements IViewRegister {
    private Toolbar tlbRegister;
    private Button btnNext;
    private LinearLayout layoutNameRegister;
    private EditText edNameRegister;
    private ProgressBarDialog progressBarDialog;

    private RegisterFagment registerFagment;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private PresenterLogin presenterLogin;
    List<String> listPhone = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initView();
        handleEvent();

        presenterLogin = new PresenterLogin(this);
    }

    private void handleEvent() {
        tlbRegister.setNavigationIcon(R.drawable.ic_arrow_back_while_24dp);
        tlbRegister.setNavigationOnClickListener(v -> onBackPressed());

        btnNext.setOnClickListener(v -> {
            layoutNameRegister.setVisibility(View.GONE);
            registerFagment = RegisterFagment.newInstance(edNameRegister.getText().toString().trim());
            fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            fragmentTransaction.add(R.id.cardview_register, registerFagment);
            fragmentTransaction.commit();
        });

        edNameRegister.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (edNameRegister.getText().toString().trim().length() >= 3) {
                    btnNext.setEnabled(true);
                } else {
                    btnNext.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initView() {
        tlbRegister = findViewById(R.id.toolbar_register);
        btnNext = findViewById(R.id.button_next_register);
        layoutNameRegister = findViewById(R.id.layout_name_register);
        edNameRegister = findViewById(R.id.edittext_name_register);
    }

    @Override
    public void validateError(String error) {
        showProgressBar(false);
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void registerSuccess(String success) {
        showProgressBar(false);
        CustomToast.makeText(this, success, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        startActivity(new Intent(RegisteActivity.this, HomeActivity.class));
        finish();
    }

    @Override
    public void registerFailed(String failed) {
        showProgressBar(false);
        CustomToast.makeText(this, failed, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void connectError(String message) {
        showProgressBar(false);
        registerFagment.showNotification(true, message);
    }

    public void showProgressBar(boolean visibility) {
        if (progressBarDialog == null)
            progressBarDialog = new ProgressBarDialog();
        if (visibility) {
            progressBarDialog.show(fragmentManager, "progress");
        } else {
            progressBarDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        if (registerFagment != null && !registerFagment.isHidden()) {
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.hide(registerFagment);
            fragmentTransaction.commit();
            layoutNameRegister.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }
}
