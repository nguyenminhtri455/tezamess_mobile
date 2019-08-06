package com.example.appchat.presenters.tabprofile.viewprofile.verificationemail;

import android.content.Context;

import com.example.appchat.R;
import com.example.appchat.model.tabprofile.verificationemail.ModelVerificationEmail;
import com.example.appchat.objectclass.Member;
import com.example.appchat.views.home.tabsetting.verificationemail.IViewVerificationEmail;
import com.example.appchat.widget.validate.Validator;

import org.json.JSONException;
import org.json.JSONObject;

public class PresenterVerificationEmail implements IPresenterVerificationEmail {

    private ModelVerificationEmail modelVerificationEmail;
    private IViewVerificationEmail iViewVerificationEmail;
    private Context context;

    private int codeStatus;

    public PresenterVerificationEmail(IViewVerificationEmail iViewVerificationEmail, Context context) {
        modelVerificationEmail = new ModelVerificationEmail();
        this.iViewVerificationEmail = iViewVerificationEmail;
        this.context = context;
    }


    @Override
    public void checkValid(String email) {
        Member member = Member.getInstance(context);
        if (!Validator.checkValidateEmail(email)) {
            iViewVerificationEmail.dataError(context.getResources().getString(R.string.invalid_email));
        } else {
            checkConfirm(member.getToken(context), member.getPhone(), email);

        }
    }

    @Override
    public void checkConfirm(String token, String phone, String email) {
        modelVerificationEmail.checkEmail(token, phone, email, s -> {
            try {
                JSONObject jsonObject = new JSONObject(s);
                codeStatus = jsonObject.getInt("status");
                switch (codeStatus) {
                    case 0:
                        JSONObject jsonMember = jsonObject.getJSONObject("data");
                        Member admin = Member.getInstance(context);
                        admin.setEmail(jsonMember.getString("email"));
                        admin.saveCache(context, token);
                        iViewVerificationEmail.updateSucess("Update Email Success");
                        break;
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                        String message = jsonObject.getString("message");
                        iViewVerificationEmail.updateFail(message);
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }
}
