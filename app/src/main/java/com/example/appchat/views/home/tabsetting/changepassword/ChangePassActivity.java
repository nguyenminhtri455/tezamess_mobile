package com.example.appchat.views.home.tabsetting.changepassword;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.appchat.R;
import com.example.appchat.customview.CustomToast;
import com.example.appchat.customview.ProgressBarDialog;
import com.example.appchat.objectclass.Member;
import com.example.appchat.presenters.tabsetting.changepassword.PresenterChangePass;
import com.example.appchat.websocket.WebSocket;
import com.example.appchat.widget.connection.CheckConnection;

public class ChangePassActivity extends AppCompatActivity implements IViewChangePass {

    private EditText edtOldPassword;
    private EditText edtNewPassword;
    private EditText edtReenterPassword;
    private Button btnUpdatePassword;
    private Toolbar tlbChangePassword;

    private Member admin;

    private ProgressBarDialog progressBarDialog;

    private PresenterChangePass presenterChangePass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass);
        presenterChangePass = new PresenterChangePass(this, this);
        admin = Member.getInstance(this);

        initViews();
        handlerEvents();
        presenterChangePass = new PresenterChangePass(this, this);
    }

    private void handlerEvents() {
        tlbChangePassword.setNavigationIcon(R.drawable.ic_arrow_back_while_24dp);
        tlbChangePassword.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btnUpdatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CheckConnection.haveNetworkConnection(getApplicationContext())) {
                    if (WebSocket.stompClient != null) {
                        if (WebSocket.stompClient.isConnected()) {
                            if (admin.getPassword().equals(edtOldPassword.getText().toString().trim())) {
                                showProgressBar(true);
                                if (edtOldPassword.getText().toString().trim().equals(edtNewPassword.getText().toString().trim())) {
                                    CustomToast.makeText(ChangePassActivity.this, "Same as the old password!", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                presenterChangePass.checkValid(edtNewPassword.getText().toString().trim()
                                        , edtReenterPassword.getText().toString().trim());
                            } else {
                                CustomToast.makeText(ChangePassActivity.this, "Wrong old password!", Toast.LENGTH_SHORT).show();
                            }
                            updateSucess("Change password success");
                        } else {
                            CustomToast.makeText(getApplicationContext(), getResources().getString(R.string.server_error), Toast.LENGTH_SHORT);
                        }
                    } else {
                        CustomToast.makeText(getApplicationContext(), getResources().getString(R.string.server_error), Toast.LENGTH_SHORT);
                    }
                } else {
                    CheckConnection.showToast_short(getApplicationContext(), getResources().getString(R.string.notification_noconnection));
                }
            }
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (edtOldPassword.length() > 5 && edtNewPassword.length() > 5 && edtReenterPassword.length() > 5) {
                    btnUpdatePassword.setEnabled(true);
                } else {
                    btnUpdatePassword.setEnabled(false);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        edtOldPassword.addTextChangedListener(textWatcher);
        edtNewPassword.addTextChangedListener(textWatcher);
        edtReenterPassword.addTextChangedListener(textWatcher);

    }

    private void initViews() {
        edtOldPassword = findViewById(R.id.edittext_old_password);
        edtNewPassword = findViewById(R.id.edittext_new_password);
        edtReenterPassword = findViewById(R.id.edittext_reenter_password);
        btnUpdatePassword = findViewById(R.id.button_changepass);
        tlbChangePassword = findViewById(R.id.toolbar_changepass);
    }

    @Override
    public void dataError(String messgae) {
        showProgressBar(false);
        Toast.makeText(this, messgae, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void connectError(String messgae) {
        showProgressBar(false);
        Toast.makeText(this, messgae, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void updateSucess(String messgae) {
        showProgressBar(false);
        finish();
    }

    @Override
    public void updateFail(String messgae) {
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
}
