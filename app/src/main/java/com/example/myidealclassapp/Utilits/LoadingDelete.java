package com.example.myidealclassapp.Utilits;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.example.myidealclassapp.R;

public class LoadingDelete {

    private final View itemDelete;
    private final Animation fadeIn;
    private final Animation fadeOut;

    public LoadingDelete(Context context, View itemDelete) {
        this.itemDelete = itemDelete;
        fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out);
    }

    public void show() {
        if (itemDelete.getVisibility() != View.VISIBLE) {
            itemDelete.setVisibility(View.VISIBLE);
            itemDelete.startAnimation(fadeIn);
        }
    }

    public void hide() {
        if (itemDelete.getVisibility() == View.VISIBLE) {
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) { }

                @Override
                public void onAnimationEnd(Animation animation) {
                    itemDelete.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) { }
            });
            itemDelete.startAnimation(fadeOut);
        }
    }
}
