package com.example.appchat.widget.validate;

public class Validator {

    public static boolean checkValidatePhoneNumber(String phoneNumber){
        String REGEX_PHONE = "0[0-9]{9}";
        if(!phoneNumber.matches(REGEX_PHONE)){
            return false;
        }
        return true;
    }

    public static boolean checkValidatePassword(String password){
        String REGEX_PASSWORD = "[^\\s]{6,30}";
        if(!password.matches(REGEX_PASSWORD)){
            return false;
        }
        return true;
    }

    public static boolean checkValidateEmail(String email){
        String REGEX_EMAIL = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if(!email.matches(REGEX_EMAIL)){
            return false;
        }
        return true;
    }

    public static boolean checkValidateResetCode(String code){
        String REGEX_CODE = "[0-9]{6}";
        if(!code.matches(REGEX_CODE)){
            return false;
        }
        return true;
    }

}
