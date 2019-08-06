package com.example.appchat.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.example.appchat.R;

public class CustomPassword extends android.support.v7.widget.AppCompatEditText {
    Drawable eye, eyeOff;
    boolean showPassWord;

    public CustomPassword(Context context) {
        super(context);
        init(null);
    }

    public CustomPassword(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CustomPassword(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.EditTextPassWord);
            showPassWord = typedArray.getBoolean(R.styleable.EditTextPassWord_showPassWord, false);
            typedArray.recycle();
        }

        eye = ContextCompat.getDrawable(getContext(), R.drawable.ic_remove_red_eye_black_24dp);
        eyeOff = ContextCompat.getDrawable(getContext(), R.drawable.ic_visibility_off_black_24dp);

        setting();
    }

    private void setting() {
        setInputType(InputType.TYPE_CLASS_TEXT | (showPassWord ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD : InputType.TYPE_TEXT_VARIATION_PASSWORD));

        Drawable drawable = showPassWord ? eye : eyeOff;
        Drawable[] drawables = getCompoundDrawables();
        setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], drawable, drawables[3]);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP &&
                (getRight() - getCompoundDrawables()[2].getBounds().width() <= event.getX())) {
            showPassWord = !showPassWord;
            setting();
            invalidate();
        }
        return super.onTouchEvent(event);
    }
}
