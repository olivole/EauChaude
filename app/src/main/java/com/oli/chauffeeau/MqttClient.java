package com.oli.chauffeeau;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.Charsets;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.jetbrains.annotations.NotNull;

import mqtt.MqttAndroidClient;

public final class MqttClient {
    private MqttAndroidClient mqttClient;

    public MqttClient(@NotNull Context context, @NotNull String serverURI, @NotNull String clientID, MemoryPersistence memoryPersistence) {
        this.mqttClient = new MqttAndroidClient(context, serverURI, clientID);
        Intent notificationIntent = new Intent(context, MqttClient.class);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE);
        }
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel("YOUR_CHANNEL_ID",
                    "Battery monitor client",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DESCRIPTION");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
            Notification notification = new Notification.Builder(context, channel.getId())
                    //                  .setSmallIcon(R.mipmap.battery)
                    .setContentTitle("Battery monitor client")
                    .setContentText("Infos batterie.")
                    .setContentIntent(pendingIntent).build();// only for gingerbread and newer versions
            //           this.mqttClient.setForegroundService(notification, 3);
        }
    }

    public final void connect(@NotNull String username, @NotNull String mdp, @NotNull IMqttActionListener cbConnect, @NotNull MqttCallback cbClient) {
        this.mqttClient.setCallback(cbClient);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(mdp.toCharArray());
        options.setKeepAliveInterval(60);//seconds
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);

        try {
            this.mqttClient.connect(options, null, cbConnect);
        } catch (Exception ex8) {
            ex8.printStackTrace();
        }
    }

    public final void subscribe(@NotNull String topic, int qos, @NotNull IMqttActionListener cbSubscribe) {
        try {
            this.mqttClient.subscribe(topic, qos, (Object) null, cbSubscribe);
        } catch (Exception ex5) {
            ex5.printStackTrace();
        }
    }

    public final void unsubscribe(@NotNull String topic, @NotNull IMqttActionListener cbUnsubscribe) {

        try {
            this.mqttClient.unsubscribe(topic, (Object) null, cbUnsubscribe);
        } catch (Exception ex4) {
            ex4.printStackTrace();
        }
    }

    public final void publish(@NotNull String topic, @NotNull String msg, int qos, boolean retained, @NotNull IMqttActionListener cbPublish) {

        try {
            MqttMessage message = new MqttMessage();
            byte[] messageBytes = msg.getBytes(Charsets.UTF_8);
            message.setPayload(messageBytes);
            message.setQos(qos);
            message.setRetained(retained);
            this.mqttClient.publish(topic, message, (Object) null, cbPublish);
        } catch (Exception ex10) {
            ex10.printStackTrace();
        }
    }

    public final void disconnect(@NotNull IMqttActionListener cbDisconnect) {
        try {
            this.mqttClient.disconnect(null, cbDisconnect);
        } catch (Exception ex3) {
            ex3.printStackTrace();
        }
    }
}
