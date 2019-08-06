package com.example.appchat.views.home.tabsetting.verificationemail;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.appchat.R;
import com.example.appchat.customview.ProgressBarDialog;
import com.example.appchat.objectclass.Member;
import com.example.appchat.presenters.tabprofile.viewprofile.verificationemail.PresenterVerificationEmail;
import com.example.appchat.widget.connection.CheckConnection;


public class VerificationEmailActivity extends AppCompatActivity implements IViewVerificationEmail {

    private Toolbar tlbVerificationEmail;
    private Button btnSaveChanges;
    private EditText edtEmailAddress;

    private Member admin;

    private ProgressBarDialog progressBarDialog;
    private PresenterVerificationEmail presenterVerificationEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_email);
        presenterVerificationEmail = new PresenterVerificationEmail(this, this);
        admin = Member.getInstance(this);
        progressBarDialog = new ProgressBarDialog();
        initViews();
        handlerEvents();
    }


    private void handlerEvents() {
        tlbVerificationEmail.setNavigationIcon(R.drawable.ic_arrow_back_while_24dp);
        tlbVerificationEmail.setNavigationOnClickListener(v -> onBackPressed());

        btnSaveChanges.setOnClickListener(t -> {
            if (CheckConnection.haveNetworkConnection(getApplicationContext())) {
                if (edtEmailAddress.getText().toString().trim().length() > 0){
                    showProgressBar(true);
                    presenterVerificationEmail.checkValid(edtEmailAddress.getText().toString().trim());
                }
            } else {
                CheckConnection.showToast_short(getApplicationContext(), getResources().getString(R.string.notification_noconnection));
            }
        });
    }

    private void initViews() {
        tlbVerificationEmail = findViewById(R.id.toolbar_verification_email);
        btnSaveChanges = findViewById(R.id.button_savechanges);
        edtEmailAddress = findViewById(R.id.edittext_email_address);
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
        Toast.makeText(this, messgae, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
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
            if(progressBarDialog.isAdded()){
                progressBarDialog.dismiss();
            }
        }
    }
}
