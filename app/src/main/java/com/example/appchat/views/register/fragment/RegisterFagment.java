package com.example.appchat.views.register.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.appchat.R;
import com.example.appchat.customview.CustomPassword;
import com.example.appchat.presenters.register.Presenter_Register;
import com.example.appchat.views.register.RegisteActivity;
import com.example.appchat.widget.connection.CheckConnection;

public class RegisterFagment extends Fragment {

    private static final String ARG_PARAM1 = "param1";

    private String name;

    private EditText edPhoneNumber;
    private CustomPassword edPassword;
    private Button btnRegister;
    private RegisteActivity registeActivity;
    private TextView txtNotify;

    private Presenter_Register presenterRegister;

    public static RegisterFagment newInstance(String... params) {
        RegisterFagment fragment = new RegisterFagment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, params[0]);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            name = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        registeActivity = (RegisteActivity) getActivity();
        presenterRegister = new Presenter_Register(registeActivity, registeActivity);
        initViews(view);
        handleEvents();
        super.onViewCreated(view, savedInstanceState);
    }

    private void handleEvents() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (edPassword.isEnabled()) {
                    if (edPhoneNumber.getText().toString().trim().length() > 0 && edPassword.getText().toString().trim().length() > 0) {
                        btnRegister.setEnabled(true);
                    } else {
                        btnRegister.setEnabled(false);
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        edPhoneNumber.addTextChangedListener(textWatcher);
        edPassword.addTextChangedListener(textWatcher);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CheckConnection.haveNetworkConnection(getActivity())) {
                    registeActivity.showProgressBar(true);
                    showNotification(false, "");
                    presenterRegister.checkValidate(
                            name,
                            edPhoneNumber.getText().toString().trim(),
                            edPassword.getText().toString().trim());
                } else {
                    CheckConnection.showToast_short(getActivity(), getResources().getString(R.string.notification_noconnection));
                }
            }
        });
    }

    private void initViews(View view) {
        edPhoneNumber = view.findViewById(R.id.edittext_phonenumber_register);
        edPassword = view.findViewById(R.id.edittext_password_register);
        btnRegister = view.findViewById(R.id.button_register);
        txtNotify = view.findViewById(R.id.txt_notification);
    }

    public void showNotification(boolean visibility, String message) {
        if (visibility) {
            txtNotify.setText(message);
            txtNotify.setVisibility(View.VISIBLE);
        } else {
            txtNotify.setVisibility(View.INVISIBLE);
        }
    }
}
