package org.oves.mobapp_mqtt_androidhub.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;

import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.oves.mobapp_mqtt_androidhub.R;
import org.oves.mobapp_mqtt_androidhub.adapters.SubscriptionsAdapter;
import org.oves.mobapp_mqtt_androidhub.services.LocalMqttService;
import org.oves.mobapp_mqtt_androidhub.services.RemoteMqttClientService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class AndroidMqttHub extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    private final boolean isChecked = false;
    //Field Variables
    private LocalMqttService localMqttService;
    private boolean mBound;
    private SwitchCompat onOffSwitch;
    private Intent localIntent;
    private String topic;
    private RecyclerView recyclerView;
    private SubscriptionsAdapter adapter;
    private List<String> topics;
    private List<String> serialList;
    private RelativeLayout topicRel;
    private boolean isValidTopic;
    private CardView llPublish;
    private Button btnSubscribe;
//    private Button btnConnect;
    private TextInputLayout etTopic;
    private boolean isConnected;
    private final ServiceConnection localMqttServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            LocalMqttService.LocalMqttBinder localMqttBinder = (LocalMqttService.LocalMqttBinder) service;
            localMqttService = localMqttBinder.getService();
            isConnected = localMqttService.isConnected();

            //check if
            if (isConnected && localMqttService.isClientConnected()){
                mBound = true;
//                isChecked = true;
                onOffSwitch.setChecked(true);

                Toasty.success(getApplicationContext(), "Mqtt Broker Connected", Toast.LENGTH_SHORT).show();
            }
            else {
//                onOffSwitch.setChecked(false);
//                onOffSwitch.setEnabled(false);
//                mBound = false;
//                isChecked = false;
                mBound = false;
                isConnected = false;
                onOffSwitch.setChecked(false);
                Toasty.success(getApplicationContext(), "Mqtt Broker Not Connected", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Toasty.success(getApplicationContext(), "Mqtt Broker Disconnected", Toast.LENGTH_SHORT).show();
            mBound = false;
            isConnected = false;
            onOffSwitch.setChecked(false);
        }
    };
    private IMqttToken iMqttToken;
    private LinearLayoutManager linearLayoutManager;
    private RemoteMqttClientService remoteMqttClientService;
    private boolean isRemoteConnected;
    private boolean remoteBind;
    private final ServiceConnection remoteMqttServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            RemoteMqttClientService.RemoteMqttBinder remoteMqttBinder = (RemoteMqttClientService.RemoteMqttBinder) service;
            remoteMqttClientService = remoteMqttBinder.getService();
            isRemoteConnected = true;


            //check if
            if (isRemoteConnected && remoteMqttClientService.isClientConnected()){
                remoteBind = true;
//                isChecked = true;
                onOffSwitch.setChecked(true);

                Toasty.success(getApplicationContext(), "Remote broker Connected", Toast.LENGTH_SHORT).show();
            }
            else {
//                onOffSwitch.setChecked(false);
//                onOffSwitch.setEnabled(false);
//                mBound = false;
//                isChecked = false;
                remoteBind = false;
                isRemoteConnected = false;
                onOffSwitch.setChecked(false);
                Toasty.success(getApplicationContext(), "Remote Broker Not Connected", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Toasty.success(getApplicationContext(), "Remote Broker Disconnected", Toast.LENGTH_SHORT).show();
            remoteBind = false;
            isRemoteConnected = false;
            onOffSwitch.setChecked(false);
        }
    };
    private boolean isRemoteChecked;
    private Intent remoteIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_mqtt_hub);

        //Initializations
        initViews();
    }

    private void initViews() {

        onOffSwitch = findViewById(R.id.localMqttSwitch);

        onOffSwitch.setChecked(isConnected && isRemoteConnected);

        //Click Listeners
        onOffSwitch.setOnCheckedChangeListener(this);
//                (new CompoundButton.OnCheckedChangeListener() {
//
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                Timber.e("Switch State= %s", isChecked);
//
//                ;
////                if (isChecked){
////                    //Switch is ON
////                    //TODO Create verification for MQTT service connection then later start transaction
////                    Toast.makeText(getApplicationContext(), "Client Switch Connected", Toast.LENGTH_SHORT).show();
////                    isChecked = true;
////                    connectToClient();
////                } else {
////                    //Switch is off
////                    //TODO Connect to MQTT service and check for messages in the local broker
////                    Toast.makeText(getApplicationContext(), "Client Switch Disconnected", Toast.LENGTH_SHORT).show();
////                    isChecked = false;
////                    disconnectClient();
////                }
//            }
//
//        });

//        llPublish = findViewById(R.id.llPublish);
//        btnSubscribe = findViewById(R.id.btnSubscribe);
//        etTopic = findViewById(R.id.etTopic);
//        btnConnect = findViewById(R.id.btnConnect);
        recyclerView = findViewById(R.id.recyclerView);
        topicRel = findViewById(R.id.topicRel);
        topicRel.setVisibility(View.GONE);

        //Set Click Listeners
//        btnSubscribe.setOnClickListener(this);
//        btnConnect.setOnClickListener(this);

        topics = new ArrayList<>();
        topics.add("Test Topic");

        //TODO do not close local communication to connect to remote use two services
//        RemoteClientSetup();

        adapter = new SubscriptionsAdapter(topics, getApplicationContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        adapter.notifyDataSetChanged();
        validateMQTTControls();

        setupRecyclerView();


    }

    public void onConnectClick(View view) throws MqttException {
        //Check isConnected.
        if (isConnected) {
            //Validate Controls and UI
            validateMQTTControls();
        }
        else {
            startService(new Intent(getApplicationContext(), LocalMqttService.class));
            startService(new Intent(getApplicationContext(), RemoteMqttClientService.class));
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

    /**
     * Subscribe to topic
     */
    public void onSubscribeClick(View view) {
        if (!isValidTopic) return;
        if (isConnected && view.getId() == R.id.btnSubscribe) {
            Log.e("onSubscribeClick: ", "Topic " + MqttClient.generateClientId() + "/" + topic + " QOS: " + "1");
        localMqttService.subscribeToAllMessages("#", 1);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(localMqttServiceConnection);
//        unbindService(remoteMqttServiceConnection);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * Setup RecyclerView
     */
    private void setupRecyclerView() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        } else {
            linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            adapter = new SubscriptionsAdapter(serialList, getApplicationContext());
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setAdapter(adapter);
            recyclerView.setHasFixedSize(true);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        localIntent = new Intent(this, LocalMqttService.class);
        remoteIntent = new Intent(this, RemoteMqttClientService.class);
        bindService(localIntent, localMqttServiceConnection, Context.BIND_AUTO_CREATE);
        bindService(remoteIntent, remoteMqttServiceConnection, Context.BIND_AUTO_CREATE);
//        startService(localIntent);
//        startService(remoteIntent);
//        startService(new Intent(getApplicationContext(), LocalMqttService.class));
//        startService(remoteIntent);
    }

    //TODO OnClick Should be removed completely onOffSwitch is controlled by an onCheckedChanged listener
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.localMqttSwitch:
//                if (!isConnected){
//                    onOffSwitch.setChecked(true);
////                    onOffSwitch.setEnabled(true);
////                    Toasty.success(getApplicationContext(), "Checked: ON", Toasty.LENGTH_SHORT).show();
////                    Toasty.success(getApplicationContext(), "Checked: ON", Toasty.LENGTH_SHORT).show();
////                    onOffSwitch.setChecked(false);
////                    isChecked = false;
//
//                    connectToClient();
//
//                } else {
//                    onOffSwitch.setChecked(false);
////                    onOffSwitch.setEnabled(false);
////                    onOffSwitch.setChecked(true);
////                    isChecked = true;
//                    Toasty.success(getApplicationContext(), "Checked: OFF", Toasty.LENGTH_SHORT).show();
//                    disconnectClient();
//                }
                break;
        }
    }

    public void RemoteClientSetup(){
        //Check if Local is connected
        if (isConnected){
            //Connected
            //Disconnect from Local Client
            localMqttService.disconnect();
            localMqttService.unbindService(localMqttServiceConnection);
            //Connect to Remote Client
            remoteMqttClientService.connectToRemoteClient();
            Intent remoteClientIntent = new Intent(this, RemoteMqttClientService.class);
            bindService(remoteClientIntent, remoteMqttServiceConnection, Context.BIND_AUTO_CREATE);
            startService(remoteClientIntent);
            remoteMqttClientService.connectToRemoteClient();
            //Publish message to remote
            remoteMqttClientService.publishMessage("test", new MqttMessage("test payload".getBytes()));
            //Disconnect from Remote Client
            remoteMqttClientService.disconnect();
            //Reconnect to Local Client
            localMqttService.connectClient();

            Toasty.error(getApplicationContext(), "Local Client connected", Toasty.LENGTH_SHORT).show();
        }
        else {
            // Not Connected
            Toasty.error(getApplicationContext(), "Local Client Not connected", Toasty.LENGTH_SHORT).show();
        }
    }

    private void connectToClient() {
        localMqttService.connectClient();
//        remoteMqttClientService.connectToRemoteClient();
        Toasty.success(getApplicationContext(), "Client Connecting", Toasty.LENGTH_SHORT).show();
//        if (localMqttService.isClientConnected()){
//            onOffSwitch.setChecked(true);
//            onOffSwitch.setEnabled(true);
//
//        } else {
//            onOffSwitch.setChecked(false);
//            onOffSwitch.setEnabled(false);
//        }
    }

    private void disconnectClient() {
        localMqttService.disconnect();
//        remoteMqttClientService.disconnect();
        Toasty.success(getApplicationContext(), "Clients disconnecting", Toasty.LENGTH_SHORT).show();
//        if (!localMqttService.isClientConnected()){
//            onOffSwitch.setChecked(false);
//            onOffSwitch.setEnabled(false);
//        } else {
//            onOffSwitch.setChecked(true);
//            onOffSwitch.setEnabled(true);
//        }
    }

    //ValidateControls
    private void validateMQTTControls() {
//        llPublish.setVisibility(isConnected ? View.VISIBLE : View.GONE);
        topicRel.setVisibility(isConnected && topics.size() > 0 ? View.VISIBLE : View.GONE);
//        btnConnect.setText(isConnected ? getString(R.string.str_connected) : getString(R.string.str_connect));
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (buttonView.getId() == R.id.localMqttSwitch){
//            if (checkLocal()){
//
//                Toasty.success(getApplicationContext(), "Local Connected ", Toasty.LENGTH_SHORT).show();
//            } else {
//                Toasty.success(getApplicationContext(), "Local Disconnected ", Toasty.LENGTH_SHORT).show();
//            }
//
//            if (checkRemote()){
//                Toasty.success(getApplicationContext(), "Remote Connected ", Toasty.LENGTH_SHORT).show();
//            } else {
//                Toasty.success(getApplicationContext(), "Remote Disconnected ", Toasty.LENGTH_SHORT).show();
//            }

            if (isChecked){
                Toasty.success(getApplicationContext(), "Checked ", Toasty.LENGTH_SHORT).show();
                if (checkLocal() && checkRemote()){
                    onOffSwitch.setChecked(true);
                    onOffSwitch.setEnabled(true);
                } else {
                    onOffSwitch.setChecked(false);
                }
//                if (isRemoteConnected && isConnected){
//                    onOffSwitch.setChecked(true);
//                    Toasty.success(getApplicationContext(), "All good", Toasty.LENGTH_SHORT).show();
//                } else if (!isRemoteConnected && isConnected){
//                    onOffSwitch.setChecked(false);
//                    Toasty.success(getApplicationContext(), "Remote not connected", Toasty.LENGTH_SHORT).show();
//                } else if (isRemoteConnected && !isConnected){
//                    onOffSwitch.setChecked(false);
//                    Toasty.success(getApplicationContext(), "Local not connected", Toasty.LENGTH_SHORT).show();
//                } else {
//                    onOffSwitch.setChecked(false);
//                    Toasty.success(getApplicationContext(), "Status Unknown", Toasty.LENGTH_SHORT).show();
//                }

            } else {
                onOffSwitch.setChecked(false);
                Toasty.success(getApplicationContext(), "UnChecked ", Toasty.LENGTH_SHORT).show();
            }
        }
//            if (localMqttService.isClientConnected() && isConnected) {
//                if (isChecked){
//                    onOffSwitch.setChecked(true);
////                    onOffSwitch.setEnabled(true);
//                    Toasty.error(getApplicationContext(), "Checked1", Toasty.LENGTH_SHORT).show();
//
//    //            Toasty.error(getApplicationContext(), "Checked", Toasty.LENGTH_SHORT).show();
//                } else {
//                    onOffSwitch.setChecked(false);
////                    onOffSwitch.setEnabled(false);
//                    Toasty.error(getApplicationContext(), "Not Checked", Toasty.LENGTH_SHORT).show();
//                }
//            }
//            else {
//                onOffSwitch.setChecked(false);
////                onOffSwitch.setEnabled(false);
//                Toasty.error(getApplicationContext(), "Checked2", Toasty.LENGTH_SHORT).show();
//            }


    }

    private boolean checkLocal(){
        if (isConnected){
            Toasty.success(getApplicationContext(), "Local Connected ", Toasty.LENGTH_SHORT).show();
        }
        else {
            localIntent = new Intent(this, LocalMqttService.class);
            bindService(localIntent, localMqttServiceConnection, Context.BIND_AUTO_CREATE);
            startService(localIntent);
            Toasty.success(getApplicationContext(), "Local Connecting " + isConnected, Toasty.LENGTH_SHORT).show();
        }
        return isConnected;
    }

    private boolean checkRemote(){
        if (isRemoteConnected){
            isRemoteConnected = true;
            Toasty.success(getApplicationContext(), "Remote connected ", Toasty.LENGTH_SHORT).show();
        } else {
            isRemoteConnected = false;
            remoteIntent = new Intent(this, RemoteMqttClientService.class);
            bindService(remoteIntent, remoteMqttServiceConnection, Context.BIND_AUTO_CREATE);
            startService(remoteIntent);
            Toasty.success(getApplicationContext(), "Remote Connecting " + isRemoteConnected, Toasty.LENGTH_SHORT).show();
        }
        return isRemoteConnected;
    }
}