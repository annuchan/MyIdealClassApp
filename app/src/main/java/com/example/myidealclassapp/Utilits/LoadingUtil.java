package com.example.myidealclassapp.Utilits;  // или Utilits, как у тебя в проекте

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.example.myidealclassapp.R;

public class LoadingUtil {
    private static AlertDialog dialog;
    private static final int AUTO_DISMISS_DELAY_MS = 10000;
    private static Handler handler = new Handler();
    private static Runnable dismissRunnable;

    public static void showLoading(Context context) {
        if (dialog != null && dialog.isShowing()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);
        builder.setView(view);
        builder.setCancelable(false);

        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Animation fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        view.startAnimation(fadeIn);

        dialog.show();

        dismissRunnable = () -> {
            if (dialog != null && dialog.isShowing()) {
                hideLoadingWithAnimation(context);
            }
        };
        handler.postDelayed(dismissRunnable, AUTO_DISMISS_DELAY_MS);
    }

    public static void hideLoadingWithAnimation(Context context) {
        if (dialog != null && dialog.isShowing()) {
            View view = dialog.findViewById(R.id.root_layout);
            if (view != null) {
                Animation fadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationEnd(Animation animation) {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                            dialog = null;
                        }
                    }
                    @Override public void onAnimationRepeat(Animation animation) {}
                });
                view.startAnimation(fadeOut);
            } else {
                dialog.dismiss();
                dialog = null;
            }
            handler.removeCallbacks(dismissRunnable);
        }
    }

    public static void hideLoading() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
            handler.removeCallbacks(dismissRunnable);
        }
    }
}
