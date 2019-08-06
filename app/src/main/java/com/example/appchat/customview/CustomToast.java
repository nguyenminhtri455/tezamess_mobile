package com.example.appchat.customview;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class CustomToast{

  private static Toast toast;

  public static Toast makeText(Context context, CharSequence text, int duration) {
    toast = Toast.makeText(context,text, duration);
    toast.setGravity(Gravity.CENTER, 0, 0);

    return toast;
  }

  public void show(){
    toast.show();
  }
}