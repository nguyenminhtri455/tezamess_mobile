package com.example.appchat.views.welcome;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;

import com.example.appchat.R;
import com.example.appchat.objectclass.Member;
import com.example.appchat.views.home.HomeActivity;
import com.example.appchat.views.login.LoginActivity;

public class WelcomActivity extends AppCompatActivity {

    private static final long TIME_OUT = 1500;
    private ProgressBar pgbWaidLoad;
    private CountDownTimer mCountDownTimer;
    public static boolean flagLogin = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcom);
        pgbWaidLoad = (ProgressBar) findViewById(R.id.progressbar_main);
        initProgressTimer();
    }

    private void initProgressTimer() {
        pgbWaidLoad.setMax(100);
        mCountDownTimer = new CountDownTimer(TIME_OUT, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (pgbWaidLoad != null) {
                    int progress = (int) (((float) (TIME_OUT - millisUntilFinished) / (float) TIME_OUT) * 100);
                    pgbWaidLoad.setProgress(progress);
                }
            }

            @Override
            public void onFinish() {
                pgbWaidLoad.setProgress(100);
                Member member = Member.getInstance(getApplicationContext());
                if (member.getName() != null) {
                    flagLogin = false;
                    startActivity(new Intent(WelcomActivity.this, HomeActivity.class));
                } else {
                    startActivity(new Intent(WelcomActivity.this, LoginActivity.class));
                }
                finish();
            }
        };
        mCountDownTimer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }
}
