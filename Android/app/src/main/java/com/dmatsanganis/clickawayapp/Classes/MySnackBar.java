package com.dmatsanganis.clickawayapp.Classes;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.dmatsanganis.clickawayapp.R;

public class MySnackBar {
    public void show(String message, Context context, View view, boolean anchor){
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        View snackbarView = snackbar.getView();
        TextView tv = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
            snackbarView.setBackgroundColor(ContextCompat.getColor(context, R.color.dark));
            tv.setTextColor(context.getColor(R.color.white));
        } else {
            snackbarView.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            tv.setTextColor(context.getColor(R.color.dark));
        }
        snackbar.setActionTextColor(ContextCompat.getColor(context, R.color.orange));

        tv.setTextSize(16);
        if (anchor){
            snackbar.setAnchorView(view);
        }
        snackbar.setAction("OK", v -> {
            snackbar.dismiss();
        });
        snackbar.show();
    }
}
