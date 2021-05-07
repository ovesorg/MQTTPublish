package org.oves.mobapp_mqtt_androidhub.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.oves.mobapp_mqtt_androidhub.Events.MessageEvent;
import org.oves.mobapp_mqtt_androidhub.utils.Constants;

import es.dmoral.toasty.Toasty;

public class RemoteBrokerService extends Service {
    private static final String TAG = "RemoteBrokerService";
    private static final RemoteBrokerService remoteServiceInstance = null;
    //Field Variables
    private final IBinder binder = new RemoteBrokerBinder();
    private final String serverURL = Constants.REMOTE_MQTT_SERVER;
    private MqttAndroidClient remoteClient;
    private String clientId;
    private MqttCallbackExtended mqttCallbackExtended;
    private MqttConnectOptions mqttConnectOptions;
    private boolean isRemoteConnected;

    public boolean isRemoteConnected() {
        return isRemoteConnected;
    }

    @SuppressLint("HardwareIds")
    @Override
    public void onCreate() {
        super.onCreate();
        //Connect Client to remote broker
        clientId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        connectToRemote();
    }

    private void connectToRemote() {
        Context context;
        remoteClient = new MqttAndroidClient(RemoteBrokerService.this, serverURL, clientId);
        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setPassword(Constants.MQTT_USER_PASSWORD.toCharArray());
        mqttConnectOptions.setUserName(Constants.MQTT_USER_NAME);
        mqttCallbackExtended = new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Toasty.success(getApplicationContext(), "connectComplete: Remote Connected", Toasty.LENGTH_SHORT).show();
                isRemoteConnected = true;
            }

            @Override
            public void connectionLost(Throwable cause) {
                isRemoteConnected = false;
                Toasty.success(getApplicationContext(), "connectionLost: Remote Connection lost", Toasty.LENGTH_SHORT).show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Toasty.success(getApplicationContext(), "messageArrived: Remote Connection received a message", Toasty.LENGTH_SHORT).show();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Toasty.success(getApplicationContext(), "deliveryComplete: Delivered", Toasty.LENGTH_SHORT).show();
            }
        };
        remoteClient.setCallback(mqttCallbackExtended);

        //Try Connect
        try {
            remoteClient.connect(mqttConnectOptions);
        } catch (MqttException e) {
            e.printStackTrace();
        }

        if (remoteClient.isConnected() && remoteClient != null){
            //Remote Connected

            Toasty.success(getApplicationContext(), "Remote Connected if statement", Toasty.LENGTH_SHORT).show();
        }
        if (remoteClient == null){
            //Remote Client is null
            Toasty.success(getApplicationContext(), "Remote Client is Null", Toasty.LENGTH_SHORT).show();
        }
        assert remoteClient != null;
        if (!remoteClient.isConnected()){
            //Remote Client is not connected
            Toasty.success(getApplicationContext(), "Remote Not Connected", Toasty.LENGTH_SHORT).show();
        }


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public boolean getConnectedStatus(){
        //If null return false.
        if (!isRemoteConnected) return false;
        return remoteClient.isConnected();
    }

    public void publish(final String topic, final String payload) {
        try {
            MqttMessage mqttMessage = new MqttMessage();
            mqttMessage.setPayload(payload.getBytes());
            remoteClient.publish(topic, mqttMessage);
            Toast.makeText(this, "publish: "+mqttMessage, Toast.LENGTH_SHORT).show();
        }
        catch (MqttException e) {
            Log.d(TAG, "Publish failed with reason code: " + e.getReasonCode());
            e.printStackTrace();
        }
    }

    public void subscribe(final String topic, int qos) {
        try {
            remoteClient.subscribe(topic, qos);
            String string = " subsribe completed";
//            EventBus.getDefault().post(string);
            Toast.makeText(RemoteBrokerService.this, "subscribe: "+topic, Toast.LENGTH_SHORT).show();
            remoteClient.setCallback(mqttCallbackExtended);
        }
        catch (MqttException e) {
            Log.d(TAG, "Subscribe failed with reason code: " + e.getReasonCode());
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            remoteClient.disconnect();
        } catch (MqttException e) {
            Log.d(TAG, "Disconnect failed with reason code: " + e.getReasonCode());
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event){

    }

    public class RemoteBrokerBinder extends Binder {
        public RemoteBrokerService getService(){
            return RemoteBrokerService.this;
        }
    }
}
