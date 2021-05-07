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

public class LocalMqttService extends Service {
    private static final String TAG = "LocalMqttService: ";
    private final IBinder iBinder = new LocalMqttBinder();
    private final String serverUrl = "tcp://127.0.0.1:1883";
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

    public boolean isConnected() {
        return isConnected;
    }

    @SuppressLint("HardwareIds")
    private void createAndroidMqttClient() {
        //Create client and Connect
        clientId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        mqttAndroidClient = new MqttAndroidClient(LocalMqttService.this, serverUrl, clientId);
        mqttCallbackExtended = new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                isConnected = true;
                Toasty.success(LocalMqttService.this, "Successfully Connected", Toasty.LENGTH_LONG).show();

                //Subscribe to All messages.
                subscribeToAllMessages("#", 1);

            }

            @Override
            public void connectionLost(Throwable cause) {
                isConnected = false;
                Toasty.success(LocalMqttService.this, "Client Disconnected", Toasty.LENGTH_LONG).show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                //Publish Messages to Local Broker
                publishMessage(topic, message);
                Toasty.success(LocalMqttService.this, "Message: " + message, Toasty.LENGTH_LONG).show();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Toasty.success(LocalMqttService.this, "Delivered", Toasty.LENGTH_LONG).show();
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

        Toasty.success(LocalMqttService.this, "createAndroidMqttClient: Connecting to Local Broker", Toasty.LENGTH_LONG).show();

        if (!isConnected) return;

        Toasty.success(LocalMqttService.this, "createAndroidMqttClient: Listening for messages from broker...", Toasty.LENGTH_LONG).show();
    }

    public void connectClient(){
        if (isConnected){
            Toasty.info(getApplicationContext(), "Already connected", Toasty.LENGTH_SHORT).show();
        }
        else {
            createAndroidMqttClient();
        }
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
                Toasty.success(LocalMqttService.this, "publishMessage: Published message " + JsonData, Toasty.LENGTH_LONG).show();
            } catch (MqttException e) {
                e.printStackTrace();
            }

        } else {
            Toasty.warning(LocalMqttService.this, "publishMessage: Connect to Broker and retry", Toasty.LENGTH_LONG).show();
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
            Toasty.info(LocalMqttService.this, "subscribeToAllMessages: Connecting to Local Broker", Toasty.LENGTH_LONG).show();
        } else {
            Toasty.info(LocalMqttService.this, "subscribeToAllMessages: Connect to Broker to subscribe", Toasty.LENGTH_LONG).show();
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
            Toasty.info(LocalMqttService.this, "Disconnect: Broker is already disconnected", Toasty.LENGTH_LONG).show();
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

    public class LocalMqttBinder extends Binder {
        public LocalMqttService getService(){
            return LocalMqttService.this;
        }
    }

}

/*public class LocalMqttService extends Service {
    public static final String TAG = LocalMqttService.class.getSimpleName();
    private static final int CONNECT_TIMEOUT = 2000;
    private final IBinder mBinder = new LocalBinder();
    private MqttAsyncClient mMqttClient = null;

    public static final String MQTT_HOST = "tcp://127.0.0.1:1883";
    public static final String USERNAME = "admin";
    public static final String PASSWORD = "admin123";
//    private MyPref myPref;

    public class LocalBinder extends Binder {
        public LocalMqttService getService() {
            return LocalMqttService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        if (isConnected()) disconnect();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public boolean connect() {
        try {
            @SuppressLint("HardwareIds")
            String clientId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            mMqttClient = new MqttAsyncClient(MQTT_HOST, clientId, null);
//            myPref = new MyPref(this);

        } catch (MqttException e) {
            e.printStackTrace();
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        MqttConnectOptions options = new MqttConnectOptions();

        try {
            options.setUserName(USERNAME);
            options.setPassword(PASSWORD.toCharArray());
            final IMqttToken connectToken = mMqttClient.connect(options);
            connectToken.waitForCompletion(CONNECT_TIMEOUT);

//            Toast.makeText(this, "connection: "+mMqttClient.isConnected(), Toast.LENGTH_SHORT).show();
        } catch (MqttException e) {
//            Toast.makeText(this, "Mqtt Connection failed ", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Connection attempt failed with reason code: " + e.getReasonCode() + ":" + e.getCause());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void publish(final String topic, final String payload) {
        try {
            MqttMessage mqttMessage = new MqttMessage();
            mqttMessage.setPayload(payload.getBytes());
            mMqttClient.publish(topic, mqttMessage);
            Toast.makeText(this, "publish: "+mqttMessage, Toast.LENGTH_SHORT).show();
        }
        catch (MqttException e) {
            Log.d(TAG, "Publish failed with reason code: " + e.getReasonCode());
            e.printStackTrace();
        }
    }

    public void subscribe(final String topic, int qos) {
        try {
            mMqttClient.subscribe(topic, qos);
            String string = " subsribe completed";
//            EventBus.getDefault().post(string);
            Toast.makeText(this, "subscribe: "+topic, Toast.LENGTH_SHORT).show();
            mMqttClient.setCallback(new MyMqttCallback());
        }
        catch (MqttException e) {
            Log.d(TAG, "Subscribe failed with reason code: " + e.getReasonCode());
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            mMqttClient.disconnect();
        } catch (MqttException e) {
            Log.d(TAG, "Disconnect failed with reason code: " + e.getReasonCode());
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        if (mMqttClient == null) return false;
        return mMqttClient.isConnected();
    }

    public class MyMqttCallback implements MqttCallback {
        public void connectionLost(Throwable cause) {
            System.out.println("---------------------"+"MQTT Server connection lost: " + cause.toString());
            cause.printStackTrace();
        }

        public void messageArrived(String topic, MqttMessage message) {
            System.out.println("---------------------"+"Message arrived: " + topic + ":" + message.toString());
//            EventBus.getDefault().post("Topic : "+topic+" Message : "+message.toString());
//            EventBus.getDefault().post(new MqttStringEvent(topic, message.toString()));
        }

        public void deliveryComplete(IMqttDeliveryToken token) {
            System.out.println("---------------------"+"Delivery complete "+ Arrays.toString(token.getTopics()));
        }
    }
}*/
