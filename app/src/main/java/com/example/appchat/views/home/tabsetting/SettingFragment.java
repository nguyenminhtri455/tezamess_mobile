package com.example.appchat.views.home.tabsetting;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.appchat.R;
import com.example.appchat.objectclass.Member;
import com.example.appchat.views.home.HomeActivity;
import com.example.appchat.views.home.tabsetting.about.AboutActivity;
import com.example.appchat.views.home.tabsetting.changepassword.ChangePassActivity;
import com.example.appchat.views.home.tabsetting.language.LanguageAdapter;
import com.example.appchat.views.home.tabsetting.language.LocaleHelper;
import com.example.appchat.views.home.tabsetting.profile.ProfileActivity;

import com.example.appchat.views.home.tabsetting.updateemail.UpdateEmailActivity;

import com.example.appchat.views.home.tabsetting.verificationemail.VerificationEmailActivity;
import com.example.appchat.views.login.LoginActivity;
import com.squareup.picasso.Picasso;

import io.paperdb.Paper;

public class SettingFragment extends Fragment {
    private static final int REQUEST_UPDATE_EMAIL = 0;
    private TextView txtAbout;
    private TextView txtLanguage;
    private TextView txtChangePass;
    private TextView txtLogout;
    private TextView txtProfile;
    private TextView txtUpdateEmail;
    private ImageView imgAvatar;

    Member member;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        handleEvent();

        member = Member.getInstance(getContext());
        Picasso.get()
                .load(member.getUrlavatar())
                .placeholder(R.drawable.account_icon)
                .error(R.drawable.account_icon)
                .into(imgAvatar);

        Paper.init(getContext());
        String language = Paper.book().read("language");
        if (language == null) {
            Paper.book().write("language", "en");
        }
        updateView((String) Paper.book().read("language"));
    }

    private void handleEvent() {
        txtAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentAbout = new Intent(getActivity(), AboutActivity.class);
                startActivity(intentAbout);
            }
        });
        txtLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeLanguageDialog();
            }
        });

        txtChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentChangePass = new Intent(getContext(), ChangePassActivity.class);
                startActivity(intentChangePass);
            }
        });

        txtUpdateEmail.setOnClickListener(t -> {
            if (member.getEmail().equals("null")) {
                Intent intent = new Intent(getContext(), VerificationEmailActivity.class);
                startActivityForResult(intent, REQUEST_UPDATE_EMAIL);
            } else {
                Intent intentChangeEmail = new Intent(getContext(), UpdateEmailActivity.class);
                startActivity(intentChangeEmail);
            }

        });

        txtLogout.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setCancelable(false);
            builder.setTitle(R.string.title_builder);
            builder.setMessage(R.string.message_builder);
            builder.setPositiveButton(R.string.title_yes, (dialog, which) -> {
                Member member = Member.getInstance(getActivity());
                member.logout(getContext());
                getActivity().startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
                dialog.dismiss();
            });
            builder.setNegativeButton(R.string.title_no, (dialog, which) -> dialog.dismiss());
            builder.show();
        });

        txtProfile.setOnClickListener(v -> {
            Intent intentProfile = new Intent(getContext(), ProfileActivity.class);
            startActivity(intentProfile);
        });
    }

    private void updateView(String lang) {
        Context context = LocaleHelper.setLocale(getContext(), lang);
    }

    private void showChangeLanguageDialog() {
        String[] listItems = {"English", "Vietnamese"};
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getContext());
        mBuilder.setTitle("Choose Language");
        mBuilder.setIcon(R.drawable.ic_language_black_24dp);
        mBuilder.setSingleChoiceItems(new LanguageAdapter(getContext(), R.layout.custom_row_language, listItems), -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (which == 0) {
                    Paper.book().write("language", "en");
                    updateView((String) Paper.book().read("language"));


                } else if (which == 1) {
                    Paper.book().write("language", "vi");
                    updateView((String) Paper.book().read("language"));

                }
                getActivity().getSupportFragmentManager().beginTransaction().detach(SettingFragment.this)
                        .attach(SettingFragment.this).commit();
                HomeActivity.CHANGE_LANGUAGE = 1;
                dialog.dismiss();
            }
        });
        mBuilder.show();
    }


    private void initView(View view) {
        txtAbout = view.findViewById(R.id.textview_about);
        txtLanguage = view.findViewById(R.id.textview_language);
        txtChangePass = view.findViewById(R.id.textview_change_password);
        txtLogout = view.findViewById(R.id.textview_logout);
        txtProfile = view.findViewById(R.id.textview_profile);
        txtUpdateEmail = view.findViewById(R.id.textview_change_email);
        imgAvatar = view.findViewById(R.id.imageview_avatar);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_UPDATE_EMAIL) {
            if (resultCode == getActivity().RESULT_OK) {

            }
        }
    }
}
