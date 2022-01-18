package com.ocr.receiptless;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.ocr.receiptless.util.Util;


public class SplashActivity extends AppCompatActivity {

    final int SECONDS = 3;

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // hiding action bar
        getSupportActionBar().hide();

        // setting timer
        Thread timer = new Thread() {
            @Override
            public void run() {
                try {
                    // pause for given seconds
                    Thread.sleep(SECONDS * 1000);
                    if(Util.getUser(SplashActivity.this)==null) {
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        finish();
                    }else{
                        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                        finish();
                    }
                    super.run();
                } catch (InterruptedException e) {}
            }
        };

        // starting timer
        timer.start();
    }
}
