package org.oves.mobapp_mqtt_androidhub.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Arrays;

import es.dmoral.toasty.Toasty;
import timber.log.Timber;

public class RemoteMqttClientService extends Service {
    //Field Variables
    private static final String TAG = "RemoteMqttClientService: ";
    private final IBinder iBinder = new RemoteMqttBinder();
    private final String serverUrl = "tcp://127.0.0.1:1883"; // "tcp://mqtt-2.omnivoltaic.com:1883";
    private MqttAndroidClient mqttAndroidClient;
    private MqttConnectOptions mqttConnectOptions;
    private MqttCallbackExtended mqttCallbackExtended;
    private boolean isConnected = false;
    @SuppressLint("HardwareIds")
    private String clientId;
    private Context applicationContext;
    private Gson json;
    private IMqttToken iMqttToken;



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //Connect Mqtt.
        if (!isConnected){
            createAndroidMqttClient();
        }
    }

    @SuppressLint("HardwareIds")
    private void createAndroidMqttClient() {
        //Create client and Connect
        clientId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        mqttAndroidClient = new MqttAndroidClient(RemoteMqttClientService.this, serverUrl, clientId);
        mqttCallbackExtended = new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                isConnected = true;
                Toasty.success(RemoteMqttClientService.this, "Successfully Connected", Toasty.LENGTH_LONG).show();

                //Subscribe to All messages.
                subscribeToAllMessages("#", 1);

            }

            @Override
            public void connectionLost(Throwable cause) {
                isConnected = false;
                Toasty.success(RemoteMqttClientService.this, "Client Disconnected", Toasty.LENGTH_LONG).show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                //Publish Messages to Local Broker
//                publishMessage(topic, message);
                Toasty.success(RemoteMqttClientService.this, "Message: " + message, Toasty.LENGTH_LONG).show();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Toasty.success(RemoteMqttClientService.this, "Delivered", Toasty.LENGTH_LONG).show();
            }
        };
        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setPassword("ankur123".toCharArray());
        mqttConnectOptions.setUserName("ankur");
        mqttAndroidClient.setCallback(mqttCallbackExtended);

        try {
            mqttAndroidClient.connect(mqttConnectOptions);
        } catch (MqttException e) {
            e.printStackTrace();
        }

        Toasty.success(RemoteMqttClientService.this, "createAndroidMqttClient: Connecting to Local Broker", Toasty.LENGTH_LONG).show();

        if (!isConnected) return;

        Toasty.success(RemoteMqttClientService.this, "createAndroidMqttClient: Listening for messages from broker...", Toasty.LENGTH_LONG).show();

    }

    public void connectToRemoteClient(){
        if (isConnected){
            Toasty.info(getApplicationContext(), "Already connected", Toasty.LENGTH_SHORT).show();
        } else {
            createAndroidMqttClient();
        }
    }

    public void disconnect() {
        if (isConnected){
            try {
                mqttAndroidClient.disconnect();
                if(isConnected) isConnected = false;
            } catch (MqttException e) {
                Timber.e(TAG + " Disconnect failed with reason code: " + e.getReasonCode());
                e.printStackTrace();
            }
        } else {
            Toasty.info(RemoteMqttClientService.this, "Disconnect: Broker is already disconnected", Toasty.LENGTH_LONG).show();
        }
    }

    public boolean isClientConnected() {
        if (mqttAndroidClient == null) return false;
        return mqttAndroidClient.isConnected();
    }

    @Override
    public void onDestroy() {
        if (isClientConnected()) disconnect();
    }

    public void publishMessage(String topic, MqttMessage message) {
        json = new Gson();
        byte[] data = message.getPayload();
        String JsonData = json.toJson(data);

        Timber.e("JsonData: %s", JsonData);

        //publish
        if (isConnected){
            try {
                mqttAndroidClient.publish(topic, message);
                Toasty.success(RemoteMqttClientService.this, "Published message " + JsonData, Toasty.LENGTH_LONG).show();
            } catch (MqttException e) {
                e.printStackTrace();
            }

        }
        else {
            Toasty.warning(RemoteMqttClientService.this, "publishMessage: Connect to Broker and retry", Toasty.LENGTH_LONG).show();
        }
    }

    public void subscribeToAllMessages(String topic, int qos) {
        //Sub
        if (isConnected){
            // mqttAndroidClient.subscribe("#", 1);
            try {
                iMqttToken = mqttAndroidClient.subscribe(topic, 0);
                iMqttToken.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.e("iMqttToken onSuccess: ", Arrays.toString(asyncActionToken.getTopics()));
                        Toasty.success(getApplicationContext(), "Subscribing to topics on: " + topic, Toasty.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.e("iMqttToken onFailure: ", exception.getMessage());

                    }
                });
            } catch (MqttException mqttException) {
                mqttException.printStackTrace();
            }
            Toasty.info(RemoteMqttClientService.this, "subscribeToAllMessages: Connecting to Local Broker", Toasty.LENGTH_LONG).show();
        } else {
            Toasty.info(RemoteMqttClientService.this, "subscribeToAllMessages: Connect to Broker to subscribe", Toasty.LENGTH_LONG).show();
        }
    }

    public class RemoteMqttBinder extends Binder {
        public RemoteMqttClientService getService(){return RemoteMqttClientService.this;}
    }
}
