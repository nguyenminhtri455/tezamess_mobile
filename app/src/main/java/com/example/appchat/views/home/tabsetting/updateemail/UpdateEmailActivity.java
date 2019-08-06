package com.example.appchat.views.home.tabsetting.updateemail;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.appchat.R;
import com.example.appchat.customview.CustomToast;
import com.example.appchat.objectclass.Member;
import com.example.appchat.presenters.tabprofile.viewprofile.verificationemail.PresenterVerificationEmail;
import com.example.appchat.views.home.tabsetting.verificationemail.IViewVerificationEmail;
import com.example.appchat.widget.connection.CheckConnection;

public class UpdateEmailActivity extends AppCompatActivity implements IViewVerificationEmail {

    private EditText edtPassword;
    private EditText edtNewEmail;
    private Toolbar toolbar;
    private Button btnUpdate;
    private Member member;
    private PresenterVerificationEmail presenterVerificationEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);
        member = Member.getInstance(this);
        presenterVerificationEmail = new PresenterVerificationEmail(this, this);
        initViews();
        handlerEvents();
    }

    private void handlerEvents() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_while_24dp);
        toolbar.setNavigationOnClickListener(t -> {
            onBackPressed();
        });

        btnUpdate.setOnClickListener(t -> {
            if (CheckConnection.haveNetworkConnection(getApplicationContext())) {
                if (!member.getPassword().equals(edtPassword.getText().toString().trim())) {
                    Toast.makeText(UpdateEmailActivity.this, "Mật khẩu không đúng!", Toast.LENGTH_SHORT).show();
                } else {
                    String email = edtNewEmail.getText().toString().trim();
                    presenterVerificationEmail.checkValid(email);
                }
            } else {
                CheckConnection.showToast_short(getApplicationContext(), getResources().getString(R.string.notification_noconnection));
            }
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (edtPassword.length() > 5 && edtNewEmail.length() > 0) {
                    btnUpdate.setEnabled(true);
                } else {
                    btnUpdate.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        edtPassword.addTextChangedListener(textWatcher);
        edtNewEmail.addTextChangedListener(textWatcher);
    }


    private void initViews() {
        edtPassword = findViewById(R.id.edittext_password);
        edtNewEmail = findViewById(R.id.edittext_new_email);
        btnUpdate = findViewById(R.id.button_changeemail);
        toolbar = findViewById(R.id.toolbar_changeemail);
    }

    @Override
    public void dataError(String messgae) {
        CustomToast.makeText(this, messgae, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void connectError(String messgae) {

    }

    @Override
    public void updateSucess(String messgae) {
        CustomToast.makeText(this, messgae, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void updateFail(String messgae) {

    }
}
