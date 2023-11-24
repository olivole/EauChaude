package com.oli.chauffeeau;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MonLog";

    private MqttService mqttService;
    private ExecutorService executor = Executors.newFixedThreadPool(1);
    private Button btEnvoyer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialiserVuePrincipal();
        this.mqttService = new MqttService(this);
        demarrerServiceMqtt();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case MqttService.ACTION_MQTT_SUCCES:
                    afficherToast("MQTT : Succes");
                    break;
                case MqttService.ACTION_MQTT_FAILURE:
                    afficherToast("MQTT : Problème rencontré");
                    break;
                case MqttService.ACTION_MQTT_MESSAGE:
                    afficherToast(intent.getStringExtra(MqttService.ACTION_MQTT_MESSAGE));
                    break;
                case MqttService.ACTION_MQTT_MESSAGE_PUBLIE:
                    afficherToast(intent.getStringExtra(MqttService.ACTION_MQTT_MESSAGE));
                    break;
            }
        }
    };

    private void initialiserVuePrincipal() {
        setContentView(R.layout.activity_main);
        this.btEnvoyer = findViewById(R.id.btEnvoyer);
        this.btEnvoyer.setOnClickListener(e->publier());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @SuppressLint("ObsoleteSdkInt")
    private static boolean isInternetConnected(Context getApplicationContext) {
        boolean status = false;
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (cm.getActiveNetwork() != null && cm.getNetworkCapabilities(cm.getActiveNetwork()) != null) {
                    // connected to the internet
                    status = true;
                }
            } else {
                if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting()) {
                    // connected to the internet
                    status = true;
                }
            }
        }
        return status;
    }
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, creerIntentFilter());
    }

    private void demarrerServiceMqtt() {
        if (isInternetConnected(this)) {
            afficherToast("Connexion au serveur MQTT en cours...");
            this.mqttService.connecterClientMqtt();
        } else {
            afficherToast("Wifi indisponible!");
        }
    }

    private static IntentFilter creerIntentFilter() {
        // Bluetooth
        final IntentFilter intentFilter = new IntentFilter();
        // MQTT
        intentFilter.addAction(MqttService.ACTION_MQTT_SUCCES);
        intentFilter.addAction(MqttService.ACTION_MQTT_FAILURE);
        intentFilter.addAction(MqttService.ACTION_MQTT_MESSAGE);
        intentFilter.addAction(MqttService.ACTION_MQTT_MESSAGE_PUBLIE);
        return intentFilter;
    }

    private void afficherToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
        // Log.i(TAG, message);
    }

    private void publier() {
        if (this.mqttService == null) {
            onResume();
        } else {
            this.mqttService.publier();
        }
    }


}
