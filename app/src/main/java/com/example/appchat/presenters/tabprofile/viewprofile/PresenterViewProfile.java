package com.example.appchat.presenters.tabprofile.viewprofile;

import android.content.Context;
import android.util.Log;

import com.example.appchat.R;
import com.example.appchat.model.viewprofile.ModelViewProfile;
import com.example.appchat.objectclass.Avatar;
import com.example.appchat.objectclass.Member;
import com.example.appchat.views.home.tabsetting.profile.IViewViewProfile;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PresenterViewProfile implements IPresenterViewProfile {

    private IViewViewProfile iViewViewProfile;
    private ModelViewProfile modelViewProfile;
    private Context context;

    private int codeStatus;

    public PresenterViewProfile(Context context, IViewViewProfile iViewViewProfile) {
        this.context = context;
        this.iViewViewProfile = iViewViewProfile;
        modelViewProfile = new ModelViewProfile();
    }

    @Override
    public void update(String token, String phone, String name, boolean gender, Date birthday, Avatar avatar) {
        modelViewProfile.update(token, phone, name, gender, birthday, avatar, s -> {
            try {
                Log.d("BBBBB",s);
                JSONObject jsonData = new JSONObject(s);
                codeStatus = jsonData.getInt("status");
                switch (codeStatus) {
                    case 0:
                        SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

                        JSONObject jsonObject = jsonData.getJSONObject("data");
                        Member member = Member.getInstance(context);

                        member.setName(jsonObject.getString("name"));
                        member.setEmail(jsonObject.getString("email"));
                        member.setBirthday(formatDate.parse(jsonObject.getString("birthday")));
                        member.setGender(jsonObject.getBoolean("gender"));
                        member.setUrlavatar(jsonObject.getString("urlavatar"));

                        member.updateCache(context);
                        iViewViewProfile.updateSuccess();
                        break;
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                        String message = jsonData.getString("message");
                        iViewViewProfile.updateFailed(message);
                        break;
                }
            } catch (JSONException e) {
                Log.d("BBB",e.getMessage());
                if (s.contains("UnknownHostException")) {
                    iViewViewProfile.connectError(context.getResources().getString(R.string.notification_noconnection));
                } else if (s.contains("SocketTimeoutException")) {
                    iViewViewProfile.connectError(context.getResources().getString(R.string.timeout_connection));
                } else {
                    iViewViewProfile.connectError(context.getResources().getString(R.string.server_error));
                }

            } catch (ParseException e) {
                iViewViewProfile.updateFailed(context.getResources().getString(R.string.invalidate_date));
            }
        });
    }
}
