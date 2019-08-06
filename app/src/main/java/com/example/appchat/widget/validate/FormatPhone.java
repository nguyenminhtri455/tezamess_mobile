package com.example.appchat.widget.validate;

public class FormatPhone {

    public static String format(String phone){
        String stringFormat = null;
        String replace = phone.replaceAll("\\(", "");
        String replace1 = replace.replaceAll("\\)", "");
        String replace2 = replace1.replaceAll("\\s", "");
        String replace3 = replace2.replaceAll("-", "");
        stringFormat = replace3.replace("+84", "0");
        return stringFormat;
    }
}
