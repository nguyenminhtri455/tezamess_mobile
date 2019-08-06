package com.example.appchat.objectclass;

import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;

import com.android.databinding.library.baseAdapters.BR;
import com.example.appchat.R;
import com.example.appchat.customview.CircleImage;
import com.example.appchat.database.TableContact;
import com.example.appchat.database.TableMessage;
import com.example.appchat.database.TableParticipation;
import com.example.appchat.database.TableRoom;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Member extends BaseObservable {
    private int id;
    private String name;
    private String phone;
    private String password;
    private Date birthday;
    private boolean gender;
    private String urlavatar;
    private String email;

    private volatile static Member member;

    private Member() {
    }

    public static Member getInstance(Context context) {
        if (member == null) {
            member = new Member();
            SharedPreferences cache = context.getSharedPreferences("info", Context.MODE_PRIVATE);
            if (cache.getInt("id", -1) != -1) {
                SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
                member.setId(cache.getInt("id", 0));
                member.setName(cache.getString("name", ""));
                member.setPhone(cache.getString("phone", ""));
                member.setPassword(cache.getString("password", ""));
                member.setEmail(cache.getString("email", ""));
                member.setGender(cache.getBoolean("gender", false));
                member.setUrlavatar(cache.getString("urlavatar", ""));

                try {
                    member.setBirthday(format.parse(cache.getString("birthday", "")));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return member;
    }

    @Bindable
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (this.name == null) {
            this.name = name;
            return;
        }
        if (!this.name.equals(name)) {
            this.name = name;
            notifyPropertyChanged(BR.name);
        }
    }

    @Bindable
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        if (this.phone == null) {
            this.phone = phone;
            return;
        }
        if (!this.phone.equals(phone)) {
            this.phone = phone;
            notifyPropertyChanged(BR.phone);
        }
    }

    @Bindable
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (this.email == null) {
            this.email = email;
            return;
        }
        if (!this.email.equals(email)) {
            this.email = email;
            notifyPropertyChanged(BR.email);
        }
    }

    @Bindable
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (this.password == null) {
            this.password = password;
            return;
        }
        if (!this.password.equals(password)) {
            this.password = password;
            notifyPropertyChanged(BR.password);
        }
    }

    @Bindable
    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        if (this.birthday == null) {
            this.birthday = birthday;
            return;
        }
        if (this.birthday.compareTo(birthday) != 0) {
            this.birthday = birthday;
            notifyPropertyChanged(BR.birthday);
        }
    }

    @Bindable
    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        if (this.gender != gender) {
            this.gender = gender;
            notifyPropertyChanged(BR.gender);
        }
    }

    @Bindable
    public String getUrlavatar() {
        return urlavatar;
    }

    public void setUrlavatar(String urlavatar) {
        if (this.urlavatar == null) {
            this.urlavatar = urlavatar;
            return;
        }
        if (!this.urlavatar.equals(urlavatar)) {
            this.urlavatar = urlavatar;
        }
    }


    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", birthday=" + birthday +
                ", gender=" + gender +
                ", urlavatar='" + urlavatar + '\'' +
                '}';
    }


    public void saveCache(Context context, String token) {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        SharedPreferences cache = context.getSharedPreferences("info", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = cache.edit();
        editor.putInt("id", id);
        editor.putString("name", name);
        editor.putString("phone", phone);
        editor.putString("password", password);
        editor.putString("email", email);
        editor.putString("birthday", format.format(birthday));
        editor.putBoolean("gender", gender);
        editor.putString("urlavatar", urlavatar);
        editor.putString("token", token);
        editor.apply();
    }

    public void updateCache(Context context) {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        SharedPreferences cache = context.getSharedPreferences("info", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = cache.edit();
        editor.putString("name", name);
        editor.putString("birthday", format.format(birthday));
        editor.putString("urlavatar", urlavatar);
        editor.putBoolean("gender", gender);
        editor.apply();
    }

    public void clearCache(Context context) {
        SharedPreferences cache = context.getSharedPreferences("info", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = cache.edit();
        editor.clear();
        editor.apply();
    }

    public String getToken(Context context) {
        String token = null;
        SharedPreferences cache = context.getSharedPreferences("info", Context.MODE_PRIVATE);
        if ((token = cache.getString("token", "")).isEmpty()) {
            return null;
        }
        return token;
    }

    public void logout(Context context) {
        TableContact.getInstance(context).deleteContacts();
        TableRoom.getInstance(context).deleteRooms();
        TableMessage.getInstance(context).deleteChatMessages();
        TableParticipation.getInstance(context).deleteParticipations();
//        TableContact.getInstance(context).close();
        clearCache(context);
        member = null;
    }

    @BindingAdapter({"app:src"})
    public static void bindingImage(CircleImage circleImage, String url) {
        Picasso.get()
                .load(url)
                .placeholder(R.drawable.account_icon)
                .error(R.drawable.account_icon)
                .into(circleImage);
    }
}
