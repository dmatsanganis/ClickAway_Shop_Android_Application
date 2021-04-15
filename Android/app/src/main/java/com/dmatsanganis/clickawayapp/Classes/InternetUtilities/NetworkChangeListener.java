package com.dmatsanganis.clickawayapp.Classes.InternetUtilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import com.dmatsanganis.clickawayapp.Classes.Internet;
import com.dmatsanganis.clickawayapp.R;

public class NetworkChangeListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Internet.isConnectedToInternet(context)) {
            AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
            View alert_dialog = LayoutInflater.from(context).inflate(R.layout.internet_dialog, null);
            builder.setView(alert_dialog);
            AlertDialog dialog = builder.create();
            dialog.setCancelable(false);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();

            Button button = alert_dialog.findViewById(R.id.retry);
            button.setOnClickListener(view -> {
                dialog.dismiss();
                onReceive(context, intent);
            });
        }
    }
}
