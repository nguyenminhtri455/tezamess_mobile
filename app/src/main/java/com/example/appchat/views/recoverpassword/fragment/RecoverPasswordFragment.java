package com.example.appchat.views.recoverpassword.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.appchat.R;
import com.example.appchat.objectclass.Member;
import com.example.appchat.presenters.recoverpassword.PresenterRecoverPassword;
import com.example.appchat.views.recoverpassword.RecoverPasswordActivity;


import com.example.appchat.widget.connection.CheckConnection;


public class RecoverPasswordFragment extends Fragment {

    private EditText edtResetCode;
    private EditText edtNewPassword;
    private EditText edtReenterPassword;
    private Button btnRecoverPassword;

    private Member member;

    private RecoverPasswordActivity recoverPasswordActivity;
    private PresenterRecoverPassword presenterRecoverPassword;


    public static RecoverPasswordFragment newInstance() {
        RecoverPasswordFragment fragment = new RecoverPasswordFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recoverpassword, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recoverPasswordActivity = (RecoverPasswordActivity) getActivity();
        presenterRecoverPassword = new PresenterRecoverPassword(recoverPasswordActivity, recoverPasswordActivity);
        member = Member.getInstance(getContext());
        initViews(view);
        handleEvents();
        super.onViewCreated(view, savedInstanceState);
    }

    private void handleEvents() {
        btnRecoverPassword.setOnClickListener(t -> {
            if (CheckConnection.haveNetworkConnection(getActivity())) {
                String code = edtResetCode.getText().toString();
                String yourEmail = recoverPasswordActivity.edtEmail.getText().toString();

//                if (edtNewPassword.getText().toString().trim().equals(edtReenterPassword.getText().toString().trim())
//                        && code.equals(recoverPasswordActivity.resetCode)
//                        && !edtResetCode.getText().toString().equals("")
//                        && !edtNewPassword.getText().toString().equals("")
//                        && !edtReenterPassword.getText().toString().equals("")) {
//                    presenterRecoverPassword.checkUpdate(yourEmail, edtNewPassword.getText().toString().trim()
//                            , edtReenterPassword.getText().toString().trim());
//                    recoverPasswordActivity.updateSucess("");
//                    Toast.makeText(recoverPasswordActivity, "Update password success", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(recoverPasswordActivity, "Update fail", Toast.LENGTH_SHORT).show();
//                }
                if (!code.equals(recoverPasswordActivity.resetCode)) {
                    Toast.makeText(recoverPasswordActivity, "Wrong reset code", Toast.LENGTH_SHORT).show();
                } else {
                    if (edtNewPassword.getText().toString().equals("") && edtReenterPassword.getText().toString().equals("")) {
                        Toast.makeText(recoverPasswordActivity, "Please enter the full password", Toast.LENGTH_SHORT).show();
                    } else {
                        if (edtNewPassword.getText().toString().trim().equals(edtReenterPassword.getText().toString().trim())) {
                            if (edtNewPassword.getText().toString().length() >= 6 &&
                                    edtNewPassword.getText().toString().length() <= 30) {
                                presenterRecoverPassword.checkValid(yourEmail, edtNewPassword.getText().toString().trim()
                                        , edtReenterPassword.getText().toString().trim());
                                recoverPasswordActivity.updateSucess("Recover password success!");
                            } else {
                                Toast.makeText(recoverPasswordActivity, R.string.invalid_password, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(recoverPasswordActivity, "Password is not the same", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            } else {
                CheckConnection.showToast_short(getActivity(), getResources().getString(R.string.notification_noconnection));
            }
        });
    }

    private void initViews(View view) {
        edtResetCode = view.findViewById(R.id.edittext_resetcode);
        edtNewPassword = view.findViewById(R.id.edittext_new_password);
        edtReenterPassword = view.findViewById(R.id.edittext_reenter_password);
        btnRecoverPassword = view.findViewById(R.id.button_recoverpass);

    }
}
