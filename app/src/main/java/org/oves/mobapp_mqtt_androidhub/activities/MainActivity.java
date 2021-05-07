package org.oves.mobapp_mqtt_androidhub.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.oves.mobapp_mqtt_androidhub.BuildConfig;
import org.oves.mobapp_mqtt_androidhub.R;
import org.oves.mobapp_mqtt_androidhub.adapters.SubscriptionsAdapter;
import org.oves.mobapp_mqtt_androidhub.utils.AppConstant;
import org.oves.mobapp_mqtt_androidhub.utils.TimeAgo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private CardView llPublish;
    private Button btnSubscribe;
    private Button btnConnect;
    private TextInputLayout etTopic;
    private boolean isConnected = false;
    private String mqttClientId;
    private MqttAndroidClient mqttAndroidClient;
    private MqttConnectOptions mqttConnectOptions;
    private String topic;
    private RecyclerView recyclerView;
    private SubscriptionsAdapter adapter;
    private List<String> topics;
    private RelativeLayout topicRel;
    private boolean isValidTopic;
    private IMqttToken iMqttToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        initViews();
    }

    public void onConnectClick(View view) throws MqttException {
        //Check isConnected.
        if (isConnected) {
            //Validate Controls and UI
            validateMQTTControls();
        } else {
            startNewConnection();
        }
    }

    private void startNewConnection() throws MqttException {
        //Create Client
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), AppConstant.MQTT_SERVER, AppConstant.MQTT_CLIENT_ID);
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(false);
        MqttCallbackExtended mqttCallbackExtended = new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Log.e("connectComplete: ", serverURI);
                isConnected = true;
                validateMQTTControls();
            }

            @Override
            public void connectionLost(Throwable cause) {
                isConnected = false;
                validateMQTTControls();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                String timeAgo = TimeAgo.getTimeAgo(System.currentTimeMillis());
                Timber.e("messageArrived Message: " + new String(message.getPayload()));
                Log.e("Time Ago: ", timeAgo);
                topics.add(new String(message.getPayload()));
                adapter.notifyDataSetChanged();

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                try {
                    Log.e("deliveryComplete: ", token.getMessage().toString());
                    Toasty.error(getApplicationContext(), token.getMessage().toString(), Toasty.LENGTH_SHORT).show();

                } catch (MqttException mqttException) {
                    mqttException.printStackTrace();
                }
            }
        };
        mqttAndroidClient.setCallback(mqttCallbackExtended);
        mqttAndroidClient.connect(mqttConnectOptions);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnConnect:
                try {
                    onConnectClick(view);
                } catch (MqttException mqttException) {
                    mqttException.printStackTrace();
                }
                break;
            case R.id.btnSubscribe:
                getInputs();
                try {
                    onSubscribeClick(view);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    /**
     * Subscribe to topic
     */
    public void onSubscribeClick(View view) {
        if (!isValidTopic) return;
        if (isConnected && view.getId() == R.id.btnSubscribe) {
            Log.e("onSubscribeClick: ", "Topic " + MqttClient.generateClientId() + "/" + topic + " QOS: " + "1");

            try {
                iMqttToken = mqttAndroidClient.subscribe(topic, 0);
                iMqttToken.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.e("iMqttToken onSuccess: ", Arrays.toString(asyncActionToken.getTopics()));
                        Toasty.success(getApplicationContext(), "Subscribing to topic: " + topic, Toasty.LENGTH_SHORT).show();
                        Objects.requireNonNull(etTopic.getEditText()).setText("");
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.e("iMqttToken onFailure: ", exception.getMessage());

                    }
                });
            } catch (MqttException mqttException) {
                mqttException.printStackTrace();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        //Menu Inflater
        MenuInflater menuInflater = getMenuInflater();

        menuInflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //Get menu items
        int viewId = item.getItemId();
        String message;

        switch (viewId) {
            //Logout
            case R.id.logout:
//                logout();
                Toasty.success(getApplicationContext(), "Transition to Login...", Toasty.LENGTH_SHORT).show();
                break;
            //Settings Transition
            case R.id.settings:
                Toasty.success(getApplicationContext(), "Transition to Settings...", Toasty.LENGTH_SHORT).show();
                break;
            //Settings Transition
            case R.id.disconnect:
                try {
                    disconnectMqtt();
                } catch (MqttException e) {
                    Log.e("Disconnect: ", e.getMessage());
                    e.printStackTrace();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Disconnect from MQTT Client
     */
    private void disconnectMqtt() throws MqttException {
        if (!(mqttAndroidClient == null) && mqttAndroidClient.isConnected()) {
            mqttAndroidClient.disconnect();
            isConnected = false;
            validateMQTTControls();
            Toasty.success(getApplicationContext(), "Disconnected from Mqtt Server", Toasty.LENGTH_SHORT).show();
        } else {
            Toasty.error(getApplicationContext(), "Already disconnected...", Toasty.LENGTH_SHORT).show();
        }
    }

    private void getInputs() {
        topic = Objects.requireNonNull(etTopic.getEditText()).getText().toString().trim();
        checkTopicValidity();
    }

    private boolean checkTopicValidity() {
        if (topic != null && !topic.isEmpty()) {
            etTopic.setErrorEnabled(false);
            etTopic.setFocusable(false);
            isValidTopic = true;
        } else {
            etTopic.setErrorEnabled(true);
            etTopic.setError("Topic cannot be empty");
            etTopic.setFocusable(true);
            isValidTopic = false;
        }
        return isValidTopic;
    }

    private void initViews() {
        llPublish = findViewById(R.id.llPublish);
        btnSubscribe = findViewById(R.id.btnSubscribe);
        etTopic = findViewById(R.id.etTopic);
        btnConnect = findViewById(R.id.btnConnect);
        recyclerView = findViewById(R.id.recyclerView);
        topicRel = findViewById(R.id.topicRel);
        topicRel.setVisibility(View.GONE);

        //Set Click Listeners
        btnSubscribe.setOnClickListener(this);
        btnConnect.setOnClickListener(this);

        topics = new ArrayList<>();

        adapter = new SubscriptionsAdapter(topics, getApplicationContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        adapter.notifyDataSetChanged();
        validateMQTTControls();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isConnected && mqttAndroidClient.isConnected()) {
            mqttAndroidClient.goAsync();
            validateMQTTControls();
        }
        validateMQTTControls();
    }

    //ValidateControls
    private void validateMQTTControls() {
        llPublish.setVisibility(isConnected ? View.VISIBLE : View.GONE);
        topicRel.setVisibility(isConnected && topics.size() > 0 ? View.VISIBLE : View.GONE);
        btnConnect.setText(isConnected ? getString(R.string.str_connected) : getString(R.string.str_connect));
    }
}