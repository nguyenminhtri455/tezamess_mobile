package com.example.appchat.presenters.tabsetting.changepassword;

import android.content.Context;
import android.widget.Toast;

import com.example.appchat.R;
import com.example.appchat.callback.ICallBackToPresenter;
import com.example.appchat.customview.CustomToast;
import com.example.appchat.model.tabsetting.changepassword.ModelChangePass;
import com.example.appchat.objectclass.Member;
import com.example.appchat.views.home.tabsetting.changepassword.IViewChangePass;
import com.example.appchat.widget.validate.Validator;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class PresenterChangePass implements IPresenterChangePass {

    private ModelChangePass modelChangePass;
    private IViewChangePass iViewChangePass;
    private Context context;

    private int codeStatus;

    public PresenterChangePass(IViewChangePass iViewChangePass, Context context) {
        modelChangePass = new ModelChangePass();
        this.iViewChangePass = iViewChangePass;
        this.context = context;
    }

    public PresenterChangePass(Context context) {
        modelChangePass = new ModelChangePass();
        this.context = context;
    }

    @Override
    public void checkValid(String passWord, String reenterPassword) {
        Member member = Member.getInstance(context);
        if (!Validator.checkValidatePassword(passWord)) {
            iViewChangePass.dataError(context.getResources().getString(R.string.invalid_password));
            return;
        } else {
            if (!passWord.equals(reenterPassword)) {
                CustomToast.makeText(context, "Password is not the same", Toast.LENGTH_SHORT).show();
            } else {
                checkUpdate(member.getToken(context), member.getPhone(), passWord);
            }
        }
    }

    @Override
    public void checkUpdate(String token, String phoneNumber, String passWord) {
        modelChangePass.checkPassword(token, phoneNumber, passWord, (ICallBackToPresenter) s -> {
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
                        admin.saveCache(context, token);
                        break;
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                        String message = jsonObject.getString("message");
                        iViewChangePass.updateFail(message);
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
    }
}
