package com.oli.chauffeeau;

import android.content.Intent;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MqttService {
    //private static final String SERVER_URI = "tcp://broker.hivemq.com:1883"; // Serveur de test HiveMQ
    private static final String SERVER_URI = "tcp://homeassistant:1883";
    public static final String ACTION_MQTT_SUCCES = "SERVEUR_MQTT_SUCCES";
    public static final String ACTION_MQTT_FAILURE = "SERVEUR_MQTT_FAILURE";
    public static final String TAG = "MonLog";
    public static final String ACTION_MQTT_MESSAGE = "message";
    private static final String MOSQUITO_USER_NAME = "homeassistant";
    private static final String MOSQUITO_MDP = "ohShiruheipi0eganierah7io2aithee9ahthe9Aa9iQu8cifiu2ui3ahdiutha9";
    public static final String ACTION_MQTT_MESSAGE_PUBLIE = "ACTION_MQTT_MESSAGE_PUBLIE";
    private static final String TOPIC = "chauffe_eau/COMMUTATEUR";
    private final MainActivity main;

    private MqttClient mqttClient;

    private final IMqttActionListener mqttActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
                      Log.i(TAG, "Succes");
                       broadcastUpdate(ACTION_MQTT_SUCCES, "Succes");
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            Log.w(TAG, "Erreur : " + exception.getMessage());
            broadcastUpdate(ACTION_MQTT_FAILURE, "Erreur : " + exception.getMessage());
        }
    };
    private final MqttCallback mqttCallback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            Log.i(TAG, "Connexion au serveur MQTT perdue : " + cause.getMessage());
            broadcastUpdate(ACTION_MQTT_MESSAGE, "Connexion au serveur MQTT perdue : " + cause.getMessage());
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            Log.i(TAG, "Message reçu. Topic : " + topic + " :  " + message.toString());
            broadcastUpdate(ACTION_MQTT_MESSAGE, "Message reçu. Topic : " + topic + " :  " + message.toString());
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

            //             Log.i(TAG, "Message reçu par le serveur " + token.getMessage().toString());
            broadcastUpdate(ACTION_MQTT_MESSAGE, "Message reçu par le serveur ");
        }
    };

    public MqttService(MainActivity mainActivity) {
        this.main = mainActivity;
    }

    public void publier() {
        @NotNull String message=message();
        mqttClient.publish(TOPIC, message, 0, true, mqttActionListener);
        broadcastUpdate(ACTION_MQTT_MESSAGE_PUBLIE, TOPIC);
    }

    public void connecterClientMqtt() {
        String clientId = MqttAsyncClient.generateClientId();
        this.mqttClient = new MqttClient(this.main, SERVER_URI, clientId, new MemoryPersistence());
        this.mqttClient.connect(MOSQUITO_USER_NAME, MOSQUITO_MDP, mqttActionListener, mqttCallback);
    }

    private void broadcastUpdate(final String action, String message) {
        final Intent intent = new Intent(action);
        if (message != null) intent.putExtra(ACTION_MQTT_MESSAGE, message);
        main.sendBroadcast(intent);
    }

    public String message() {
        String sHeure = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
        StringBuffer json = new StringBuffer("{");
        json.append("\"time\":\"" + sHeure+"\",\"value\"" + ":\"" + "ON" + "\"");
        json.append("}");
        return json.toString();
    }

}