package org.oves.mobapp_mqtt_androidhub.models;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public class SubscriptionModel extends MqttMessage {
    private String topic;
    private String message;
    private String messageId;

    public SubscriptionModel(String topic, String message, String messageId) {
        this.topic = topic;
        this.message = message;
        this.messageId = messageId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
