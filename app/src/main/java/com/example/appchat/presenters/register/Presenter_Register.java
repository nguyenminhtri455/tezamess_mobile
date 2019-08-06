package com.example.appchat.presenters.register;

import android.content.Context;
import android.util.Log;

import com.example.appchat.R;
import com.example.appchat.callback.ICallBackToPresenter;
import com.example.appchat.model.register.ModelRegister;
import com.example.appchat.objectclass.Member;
import com.example.appchat.widget.validate.Validator;
import com.example.appchat.views.register.IViewRegister;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Presenter_Register implements IPresenter_Register {

    private IViewRegister iViewRegister;
    private ModelRegister modelRegister;
    private Context context;
    private int codeStatus;

    public Presenter_Register(IViewRegister iView_register, Context context) {
        this.iViewRegister = iView_register;
        modelRegister = new ModelRegister();
        this.context = context;
    }


    @Override
    public void checkValidate(String name, String phoneNumber, String password) {
        if (!Validator.checkValidatePhoneNumber(phoneNumber)) {
            iViewRegister.validateError(context.getResources().getString(R.string.invalid_phone_number));
            return;
        }

        if (!Validator.checkValidatePassword(password)) {
            iViewRegister.validateError(context.getResources().getString(R.string.invalid_password));
            return;
        }

        register(name, phoneNumber, password);
    }

    @Override
    public void register(final String name, String phoneNumber, String password) {

        modelRegister.register(name, phoneNumber, password, new ICallBackToPresenter() {
            @Override
            public void callBack(String s) {
                Log.d("BBB",s);
                try {
                    JSONObject jsonData = new JSONObject(s);
                    codeStatus = jsonData.getInt("status");
                    switch (codeStatus) {
                        case 0:
                            SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

                            JSONObject jsonObject = jsonData.getJSONObject("data");
                            Member member = Member.getInstance(context);

                            member.setId(jsonObject.getInt("id"));
                            member.setPhone(jsonObject.getString("phone"));
                            member.setName(jsonObject.getString("name"));
                            member.setPassword(jsonObject.getString("password"));
                            member.setBirthday(formatDate.parse(jsonObject.getString("birthday")));
                            member.setGender(jsonObject.getBoolean("gender"));
                            member.setUrlavatar(jsonObject.getString("urlavatar"));
                            member.setEmail(jsonObject.getString("email"));
                            String token = jsonData.getString("token");
                            member.saveCache(context,token);

                            iViewRegister.registerSuccess(context.getResources().getString(R.string.notification_welcom) + member.getName());
                            break;
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                            String message = jsonData.getString("message");
                            iViewRegister.registerFailed(message);
                            break;
                    }
                } catch (JSONException e) {
                    Log.d("BBB",e.getMessage());
                    if (s.contains("UnknownHostException")) {
                        iViewRegister.connectError(context.getResources().getString(R.string.notification_noconnection));
                    } else if (s.contains("SocketTimeoutException")) {
                        iViewRegister.connectError(context.getResources().getString(R.string.timeout_connection));
                    } else {
                        iViewRegister.connectError(context.getResources().getString(R.string.server_error));
                    }

                } catch (ParseException e) {
                    iViewRegister.registerFailed(context.getResources().getString(R.string.invalidate_date));
                }
            }
        });
    }
}
