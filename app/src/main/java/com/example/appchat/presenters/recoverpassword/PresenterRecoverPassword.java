package com.example.appchat.presenters.recoverpassword;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.appchat.R;
import com.example.appchat.customview.CustomToast;
import com.example.appchat.model.recoverpassword.ModelRecoverPassword;
import com.example.appchat.objectclass.Member;
import com.example.appchat.views.recoverpassword.IViewRecoverPassword;
import com.example.appchat.widget.validate.Validator;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class PresenterRecoverPassword implements IPresenterRecoverPassword {

    private ModelRecoverPassword modelRecoverPassword;
    private IViewRecoverPassword iViewRecoverPassword;
    private Context context;

    private int codeStatus;

    public PresenterRecoverPassword(IViewRecoverPassword iViewRecoverPassword, Context context) {
        modelRecoverPassword = new ModelRecoverPassword();
        this.iViewRecoverPassword = iViewRecoverPassword;
        this.context = context;
    }

    public PresenterRecoverPassword(Context context) {
        modelRecoverPassword = new ModelRecoverPassword();
        this.context = context;
    }

    @Override
    public void checkValid(String email, String passWord, String reenterPassword) {
        if (!Validator.checkValidatePassword(passWord)){
            iViewRecoverPassword.dataError(context.getResources().getString(R.string.invalid_password));
        }else {
            checkUpdate(email, passWord, reenterPassword);
            Toast.makeText(context, "Recover password success!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void checkValidEmail(String email) {
        Member member = Member.getInstance(context);
        if (!Validator.checkValidateEmail(email)) {
            iViewRecoverPassword.dataError("Email invalidate");
        } else {
            getResetCode(email);
        }
    }


    @Override
    public void checkUpdate(String email, String passWord, String reenterPassword) {

        modelRecoverPassword.checkPassword(email, passWord, reenterPassword, s -> {
            try {
                JSONObject jsonObject = new JSONObject(s);
                codeStatus = jsonObject.getInt("status");
                switch (codeStatus) {
                    case 0:
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

                        JSONObject jsonMember = jsonObject.getJSONObject("data");
                        Member admin = Member.getInstance(context);

                        admin.setId(jsonMember.getInt("id"));
                        admin.setPhone(jsonMember.getString("phone"));
                        admin.setName(jsonMember.getString("name"));
                        admin.setPassword(jsonMember.getString("password"));
                        admin.setEmail(jsonMember.getString("email"));
                        admin.setBirthday(simpleDateFormat.parse(jsonMember.getString("birthday")));
                        admin.setGender(jsonMember.getBoolean("gender"));
                        admin.setUrlavatar(jsonMember.getString("urlavatar"));
                        iViewRecoverPassword.updateSucess("succsess");
                        break;
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                        String message = jsonObject.getString("message");
                        iViewRecoverPassword.updateFail(message);
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void getResetCode(String email) {
        modelRecoverPassword.checkResetCode(email, s -> {
            try {
                JSONObject jsonData = new JSONObject(s);
                codeStatus = jsonData.getInt("status");
                switch (codeStatus) {
                    case 0:
                        SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

                        String jsonObject = jsonData.getString("data");
                        iViewRecoverPassword.getResetCodeSuccess(jsonObject);
                        break;
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                        String message = jsonData.getString("message");
                        iViewRecoverPassword.updateFail(message);
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

}
