package com.example.appchat.views.recoverpassword;

import android.content.Intent;
import android.opengl.Visibility;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.appchat.R;
import com.example.appchat.customview.CustomToast;
import com.example.appchat.customview.ProgressBarDialog;
import com.example.appchat.objectclass.Member;
import com.example.appchat.presenters.recoverpassword.PresenterRecoverPassword;
import com.example.appchat.views.home.tabsetting.changepassword.ChangePassActivity;
import com.example.appchat.views.login.LoginActivity;
import com.example.appchat.views.recoverpassword.fragment.RecoverPasswordFragment;
import com.example.appchat.views.register.fragment.RegisterFagment;
import com.example.appchat.widget.connection.CheckConnection;

public class RecoverPasswordActivity extends AppCompatActivity implements IViewRecoverPassword {


    private Button btnGetResetCode;
    public EditText edtEmail;
    private ProgressBarDialog progressBarDialog;
    private PresenterRecoverPassword presenterRecoverPassword;
    private Member admin;

    public String resetCode = null;

    private LinearLayout relativeLayoutReSetCode;
    private RecoverPasswordFragment recoverPasswordFragment;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_password);

        presenterRecoverPassword = new PresenterRecoverPassword(this, this);
        admin = Member.getInstance(this);
        fragmentManager = getSupportFragmentManager();
        initView();
        handlerEvents();

    }

    private void handlerEvents() {
        btnGetResetCode.setOnClickListener(t -> {
            if (CheckConnection.haveNetworkConnection(getApplicationContext())) {
                presenterRecoverPassword.checkValidEmail(edtEmail.getText().toString());
            } else {
                CheckConnection.showToast_short(getApplicationContext(), getResources().getString(R.string.notification_noconnection));
            }
        });
    }

    private void initView() {
        btnGetResetCode = findViewById(R.id.button_get_resetcode);
        edtEmail = findViewById(R.id.edittext_email);
        relativeLayoutReSetCode = findViewById(R.id.layout_name_register);
    }

    @Override
    public void dataError(String messgae) {
//        showProgressBar(false);
        Toast.makeText(this, messgae, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void connectError(String messgae) {
//        showProgressBar(false);
        Toast.makeText(this, messgae, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void sentResetCode(String message) {

    }

    @Override
    public void updateSucess(String messgae) {
        finish();
    }

    @Override
    public void updateFail(String messgae) {

    }

    @Override
    public void getResetCodeSuccess(String code) {

        resetCode = code;
        relativeLayoutReSetCode.setVisibility(View.GONE);
        recoverPasswordFragment = RecoverPasswordFragment.newInstance();
//
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        fragmentTransaction.add(R.id.cardview_recover_password, recoverPasswordFragment);
        fragmentTransaction.commit();

    }

    private void showProgressBar(boolean visibility) {
        if (progressBarDialog == null) {
            progressBarDialog = new ProgressBarDialog();
        }
        if (visibility) {
            progressBarDialog.show(getSupportFragmentManager(), "diaglog");
        } else {
            progressBarDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        if (recoverPasswordFragment != null && !recoverPasswordFragment.isHidden()) {
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.hide(recoverPasswordFragment);
            fragmentTransaction.commit();
            relativeLayoutReSetCode.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }
}
