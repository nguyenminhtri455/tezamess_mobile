package com.example.appchat.views.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appchat.R;
import com.example.appchat.customview.CustomPassword;
import com.example.appchat.customview.ProgressBarDialog;
import com.example.appchat.presenters.login.PresenterLogin;
import com.example.appchat.views.recoverpassword.RecoverPasswordActivity;
import com.example.appchat.views.home.HomeActivity;
import com.example.appchat.views.register.RegisteActivity;
import com.example.appchat.views.welcome.WelcomActivity;
import com.example.appchat.widget.connection.CheckConnection;

import io.reactivex.disposables.CompositeDisposable;

public class LoginActivity extends AppCompatActivity implements IViewLogin {

    private Button btnLogin;
    private Button btnRegister;
    private Toolbar tlbLogin;
    private EditText edtUsername;
    private CustomPassword edtPassword;
    private ProgressBarDialog progressBarDialog;
    private TextView txtForgotPassword;

    public CompositeDisposable compositeDisposable;

    private String userName;
    private String passWord;

    private final int RESULT_REGISTER = 1;
    private PresenterLogin presenterLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        initToolbar();
        handleEvent();
        WelcomActivity.flagLogin = true;
        compositeDisposable = new CompositeDisposable();
        presenterLogin = new PresenterLogin(this, this);
    }

    private void initToolbar() {
        tlbLogin.setNavigationIcon(R.drawable.ic_arrow_back_while_24dp);
        tlbLogin.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void handleEvent() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (edtPassword.isEnabled()) {
                    if (edtUsername.length() > 0 && edtPassword.length() > 0) {
                        btnLogin.setEnabled(true);
                    } else {
                        btnLogin.setEnabled(false);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        edtUsername.addTextChangedListener(textWatcher);
        edtPassword.addTextChangedListener(textWatcher);

        btnLogin.setOnClickListener(v -> {
            if (CheckConnection.haveNetworkConnection(getApplicationContext())) {
                showProgressBar(true);
                userName = edtUsername.getText().toString();
                passWord = edtPassword.getText().toString();
                if (userName.length() > 0 && passWord.length() > 0) {
                    presenterLogin.checkValid(userName, passWord);
                }
            } else {
                CheckConnection.showToast_short(getApplicationContext(), getResources().getString(R.string.notification_noconnection));
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisteActivity.class);
                startActivityForResult(intent, RESULT_REGISTER);
            }
        });

        txtForgotPassword.setOnClickListener(v -> {
            Intent intentRecoverPassword = new Intent(LoginActivity.this, RecoverPasswordActivity.class);
            startActivity(intentRecoverPassword);
        });
    }

    private void initView() {
        btnLogin = findViewById(R.id.button_login);
        btnRegister = findViewById(R.id.button_register);
        tlbLogin = findViewById(R.id.toolbar_login);
        edtUsername = findViewById(R.id.edittext_phonenumber_login);
        edtPassword = findViewById(R.id.edittext_password_login);
        txtForgotPassword = findViewById(R.id.textview_recover_password);
    }

    @Override
    public void dataError(String error) {
        showProgressBar(false);
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void loginSucess(String success) {
        Toast.makeText(LoginActivity.this, success, Toast.LENGTH_SHORT).show();
        showProgressBar(false);
        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        finish();
    }

    @Override
    public void loginFail(String failed) {
        showProgressBar(false);
        Toast.makeText(this, failed, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void connectError(String messgae) {
        showProgressBar(false);
        Toast.makeText(this, messgae, Toast.LENGTH_SHORT).show();
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    finish();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
    }
}
