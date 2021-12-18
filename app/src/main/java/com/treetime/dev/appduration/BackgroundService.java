package com.treetime.dev.appduration;

import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.treetime.dev.appduration.MainActivity.FACEBOOK_COUNTER;
import static com.treetime.dev.appduration.MainActivity.WHATSAPP_COUNTER;


public class BackgroundService extends Service {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    int level, scale;
    float batteryPct;
    public BackgroundService(){

    }

    private static final String TAG = "Estatísticas de uso > ";//REMOVER DEPOIS SOMENTE COMENTARIO

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sharedPreferences = getSharedPreferences("App Duration", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        //Determinar o estado de carregamento atual da bateria
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        final Intent batteryStatus = this.registerReceiver(null, intentFilter);

        TimerTask detectApp = new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                sharedPreferences = getSharedPreferences("App Duration", MODE_PRIVATE);
                editor = sharedPreferences.edit();
                UsageStatsManager usageStatsManager = (UsageStatsManager)getSystemService(USAGE_STATS_SERVICE);

                long endTime = System.currentTimeMillis();
                long beginTime = endTime-(1000);

                List<UsageStats> usageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, beginTime, endTime);
                if(usageStats != null){
                    for(UsageStats usageStat : usageStats){
                        if(usageStat.getPackageName().toLowerCase().contains("com.whatsapp")){
                            editor.putLong(WHATSAPP_COUNTER, usageStat.getTotalTimeInForeground());
                        }

                        if(usageStat.getPackageName().toLowerCase().contains("com.facebook.katana")){
                            editor.putLong(FACEBOOK_COUNTER, usageStat.getTotalTimeInForeground());
                        }

                        editor.apply();
                        Log.d(TAG, usageStat.getPackageName() + " = " + usageStat.getTotalTimeInForeground());

                        //Imprimir uma lista dos pacotes instalados e seus determinados tempos de uso
                        if (!usageStat.getPackageName().toLowerCase().contains("com.android") &&
                                !usageStat.getPackageName().toLowerCase().contains("com.google")) {
                            System.out.println("TREETIME DEV: " + usageStat.getPackageName() + " = " + usageStat.getTotalTimeInForeground());
                        }
                    }
                    //determinando o nível da bateria
                    level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    batteryPct = level * 100 / (float)scale;
                    System.out.println("TREETIME DEV: Nível da bateria - " + batteryPct);

                }
            }
        };
        Timer detectAppTimer = new Timer();
        detectAppTimer.scheduleAtFixedRate(detectApp, 0, 1000);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
