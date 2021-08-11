package org.oves.mobapp_mqtt_androidhub.utils;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import es.dmoral.toasty.Toasty;
import timber.log.Timber;

public class MqttRemoteService extends Service implements MqttCallbackExtended {
    private final IBinder binder = new MyBinder();
    //Field Variables
    private MqttMessage mqttMessage;
    private MqttAndroidClient mqttAndroidClient;
    private MqttConnectOptions mqttConnectOptions;
    private IMqttToken iMqttToken;
    private String clientID;
    private String serverUrl;
    private boolean isConnected = false;

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        ToastMe("onStart: started");
        Timber.e("Started MqttAndroidClient Service connected %s", mqttAndroidClient.isConnected() );

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ToastMe("StartCommand Hit");

//        if (mqttAndroidClient.isConnected()){
//            ToastMe("Connected");
//        } else {
            ToastMe("Setting up to connect");
            serverUrl = "tcp://mqtt-2.omnivoltaic.com:1883";
            clientID = "installer_app";
            mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUrl, clientID);
            mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setUserName("ankur");
            mqttConnectOptions.setPassword("ankur123".toCharArray());
            mqttConnectOptions.setCleanSession(false);
            mqttAndroidClient.setCallback(this);
            try{
                mqttAndroidClient.connect(mqttConnectOptions);
            } catch (MqttException e) {
                e.printStackTrace();
                Timber.e("MqttException: %s", e.getMessage());
            }
//        }

        return START_STICKY;
    }

    /**
     * Order to follow on Mqtt Protocol standard
     * dt/ov/01/ovPump1900000300/objectToSend
     * payAccountDetails/ov/01/InstallerApp/PayAccountObject
     * */
    public void publishMessage(String topic, MqttMessage message){
        message = new MqttMessage(message.getPayload());
        try{
            iMqttToken = mqttAndroidClient.publish(topic, message);
            Timber.e("Topic Message data: %s, %s", topic, message);
        }  catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.stopSelf();
        ToastMe("onDestroy: destroyed");
        Timber.e("MqttAndroidClient Service connected %s", mqttAndroidClient.isConnected());
    }

    private void ToastMe(String message){
        Toasty.success(getApplicationContext(), message, Toasty.LENGTH_SHORT).show();
        Timber.e("Log: %s", message);
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        ToastMe("UnBinding");
        return super.onUnbind(intent);

    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        ToastMe("ReBinding");
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        isConnected = true;
        if (isConnected){
            ToastMe("Client Connected: " + mqttAndroidClient.isConnected());
            Timber.e("IsConnected: %s", isConnected);
        }
        else {
            Timber.e("Client Not Connected: %s", isConnected);
            ToastMe("Client Not connected");
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        isConnected = false;
        ToastMe("Client Disconnected");
        Timber.e("IsConnected: %s", isConnected);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        if (token.isComplete()){
            ToastMe("Delivery complete " + token.isComplete());
        } else {
            ToastMe("Delivery incomplete" + token.isComplete());
        }

    }

    public class MyBinder extends Binder{
        public MqttRemoteService getService(){
            return MqttRemoteService.this;
        }
    }
}
