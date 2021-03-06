package com.treetime.dev.appduration;

import android.app.AppOpsManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    public static String FACEBOOK_COUNTER = "Facebook Counter";
    public static String WHATSAPP_COUNTER = "WhatsApp Counter";
    private TextView facebook_view;
    private TextView whatsapp_view;

    private static final String TAG = "Estatísticas de uso > ";//REMOVER DEPOIS SOMENTE COMENTARIO

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("App Duration", MODE_PRIVATE);
        if(!checkUsageStatsAllowedOrNot()){
            Intent usageAccessIntent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            usageAccessIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(usageAccessIntent);
            if(checkUsageStatsAllowedOrNot()){
                startService(new Intent(MainActivity.this, BackgroundService.class));
            }
            else {
                Toast.makeText(getApplicationContext(), "Please give access", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            startService(new Intent(MainActivity.this, BackgroundService.class));
        }
        facebook_view = findViewById(R.id.facebook_time);
        whatsapp_view = findViewById(R.id.whatsapp_time);
        TimerTask updateView = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        long facebook_time = sharedPreferences.getLong(FACEBOOK_COUNTER, 0);
                        long second = (facebook_time/1000)%60;
                        long minute = (facebook_time/(1000*60))%60;
                        long hour = (facebook_time/(1000*60*60));
                        String facebook_val = hour + " h " + minute + " m " + second + " s ";
                        facebook_view.setText(facebook_val);

                        long whatsapp_time = sharedPreferences.getLong(WHATSAPP_COUNTER, 0);
                        second = (whatsapp_time/1000)%60;
                        minute = (whatsapp_time/(1000*60))%60;
                        hour = (whatsapp_time/(1000*60*60));
                        String whatsapp_val = hour + " h " + minute + " m " + second + " s ";
                        whatsapp_view.setText(whatsapp_val);
                    }
                });
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(updateView, 0, 1000);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean checkUsageStatsAllowedOrNot(){
        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) getSystemService(APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            return (mode == AppOpsManager.MODE_ALLOWED);
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "Error connot found any usage stats manager", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDestroy(){
        if(checkUsageStatsAllowedOrNot()){
            startService(new Intent(MainActivity.this, BackgroundService.class));
        }
        super.onDestroy();
    }
}