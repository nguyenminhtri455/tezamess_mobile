package com.example.appchat.customview;

import android.content.Context;
import android.content.Intent;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ProgressBar;

import com.example.appchat.views.home.HomeActivity;

public class ProgressBarAnimation extends Animation {
    private Context mContext;
    private ProgressBar progressBar;
    private float from;
    private float to;

    public ProgressBarAnimation(Context mContext, ProgressBar progressBar, float from, float to){
        this.mContext = mContext;
        this.progressBar = progressBar;
        this.from = from;
        this.to = to;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        float value = from + (to - from) * interpolatedTime;
        progressBar.setProgress((int) value);

        if (value == to) {
            mContext.startActivity(new Intent(mContext, HomeActivity.class));
        }
    }
}
