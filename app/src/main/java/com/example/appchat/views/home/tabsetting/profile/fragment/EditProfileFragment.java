package com.example.appchat.views.home.tabsetting.profile.fragment;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.transition.TransitionInflater;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appchat.R;
import com.example.appchat.customview.CircleImage;
import com.example.appchat.customview.CustomToast;
import com.example.appchat.customview.ProgressBarDialog;
import com.example.appchat.databinding.FragmentEditProfileBinding;
import com.example.appchat.objectclass.Avatar;
import com.example.appchat.objectclass.Member;
import com.example.appchat.presenters.tabprofile.viewprofile.PresenterViewProfile;
import com.example.appchat.views.home.tabsetting.profile.ProfileActivity;
import com.example.appchat.websocket.WebSocket;
import com.example.appchat.widget.connection.CheckConnection;
import com.example.appchat.widget.image.ScaleBitmap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EditProfileFragment extends Fragment {

    private ProfileActivity profileActivity;
    private Toolbar tlbProfile;
    private CircleImage imgAvatar;
    private EditText edtName;
    private TextView txtBirthday, txtNotification;
    private RadioButton radMale;
    private RadioButton radFemale;
    private Button btnUpdatePrifile;
    private ProgressBarDialog progressBarDialog;

    private Member member;
    private PresenterViewProfile presenterViewProfile;
    private FragmentEditProfileBinding binding;
    private SimpleDateFormat dateFormat;

    private static final int REQUEST_CODE_AVATAR = 0;
    private boolean flagAvatar;
    private String realPath;
    private Date birthday;

    public static EditProfileFragment newInstance() {
        EditProfileFragment fragment = new EditProfileFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postponeEnterTransition();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSharedElementEnterTransition(TransitionInflater.from(getContext()).inflateTransition(android.R.transition.move));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        initData();
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_profile, container, false);
        binding.setMember(member);
        binding.setFormatDate(dateFormat);
        return binding.getRoot();
    }

    private void initData() {
        profileActivity = (ProfileActivity) getActivity();
        member = Member.getInstance(getContext());
        presenterViewProfile = new PresenterViewProfile(getContext(), profileActivity);
        dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        birthday = member.getBirthday();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initViews();
        initActionbar();
        handleEvents();
        super.onViewCreated(view, savedInstanceState);
    }

    private void initActionbar() {
        tlbProfile.setNavigationIcon(R.drawable.ic_arrow_back_while_24dp);
        tlbProfile.setNavigationOnClickListener(t -> {
            profileActivity.onBackPressed();
        });
    }


    private void initViews() {
        tlbProfile = binding.toolbarViewprofile;
        imgAvatar = binding.imgAvatar;
        edtName = binding.edittextNameEditprofile;
        txtBirthday = binding.textviewBirthdayEditprofile;
        txtNotification = binding.txtNotification;
        radMale = binding.radioMaleEditprofile;
        radFemale = binding.radioFemaleEditprofile;
        btnUpdatePrifile = binding.btnUpdateProfile;
    }


    private void handleEvents() {
        txtBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogCalender();
            }
        });

        edtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (edtName.getText().toString().trim().length() >= 3) {
                    btnUpdatePrifile.setEnabled(true);
                } else {
                    btnUpdatePrifile.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnUpdatePrifile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtName.getText().toString().trim().length() >= 3) {
                    if (CheckConnection.haveNetworkConnection(getContext())) {
                        if (WebSocket.stompClient != null) {
                            if (WebSocket.stompClient.isConnected()) {
                                showProgressbar(true);
                                String name = edtName.getText().toString().trim();
                                boolean gender;
                                if (radMale.isChecked()) {
                                    gender = true;
                                } else {
                                    gender = false;
                                }
                                Avatar avatar = encodeBase64Avatar();
                                presenterViewProfile.update(member.getToken(getContext()), member.getPhone(), name, gender, birthday, avatar);
                            } else {
                                CustomToast.makeText(profileActivity, getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            CustomToast.makeText(profileActivity, getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        CustomToast.makeText(profileActivity, getResources().getString(R.string.notification_noconnection), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(profileActivity,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_AVATAR);
                } else {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, REQUEST_CODE_AVATAR);
                }
            }
        });
    }

    private Avatar encodeBase64Avatar() {
        if (flagAvatar) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ((BitmapDrawable) imgAvatar.getDrawable()).getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String encoded = Base64.encodeToString(byteArray, Base64.NO_WRAP);
            int index = realPath.lastIndexOf("/");
            String name1 = realPath.substring(++index);
            String[] split = name1.split("\\.");
            String name = split[0] + System.currentTimeMillis() + member.getPhone() + "." + split[1];
            Avatar avatar = new Avatar(encoded, name);
            return avatar;
        }
        return null;
    }

    private void showDialogCalender() {
        Calendar now = Calendar.getInstance();
        now.setTime(member.getBirthday());
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH);
        final int day = now.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog pickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                birthday = calendar.getTime();
                txtBirthday.setText(simpleDateFormat.format(birthday));
            }
        }, year, month, day);
        pickerDialog.show();
    }

    public String getRealPathFromURI(Uri contentUri) {
        String path = null;
        String[] proj = {MediaStore.MediaColumns.DATA};
        Cursor cursor = profileActivity.getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            path = cursor.getString(column_index);
        }
        cursor.close();
        return path;
    }

    public void showProgressbar(boolean visibility) {
        if (progressBarDialog == null) {
            progressBarDialog = new ProgressBarDialog();
        }
        if (visibility) {
            showNotification("", false);
            progressBarDialog.show(profileActivity.getSupportFragmentManager(), "progress");
        } else {
            progressBarDialog.dismiss();
        }
    }

    public void showNotification(String message, boolean visibility) {
        if (visibility) {
            txtNotification.setText(message);
            txtNotification.setVisibility(View.VISIBLE);
        } else {
            txtNotification.setVisibility(View.GONE);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_AVATAR:
                if (resultCode == profileActivity.RESULT_OK) {
                    Uri uri = data.getData();
                    realPath = getRealPathFromURI(uri);
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(profileActivity.getContentResolver(), uri);
//            Log.d("BBB","truoc khi scale" + bitmap.getByteCount());
                        Bitmap scaleAfterBitmap = ScaleBitmap.scaleBitmap(bitmap, 1024 * 1024);
//            Log.d("BBB","sau khi scale" + scaleAfter.getByteCount());

                        if (bitmap != scaleAfterBitmap) {
                            bitmap.recycle();
                        }
                        imgAvatar.setImageBitmap(scaleAfterBitmap);
                        flagAvatar = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }
}
