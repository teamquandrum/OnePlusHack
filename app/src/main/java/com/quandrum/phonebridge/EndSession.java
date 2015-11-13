package com.quandrum.phonebridge;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class EndSession extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            this.stopService(new Intent(this, ScreenshotButton.class));
            this.stopService(new Intent(this, ServiceFloating.class));
        }
        catch (Exception E){
            E.printStackTrace();
        }

        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(ns);
        nMgr.cancel(9999);

        Toast.makeText(this,"Thank you for using PhoneBridge. The session has ended",Toast.LENGTH_SHORT).show();

        finish();

    }
}
