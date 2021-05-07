package org.oves.mobapp_mqtt_androidhub.utils;

public class AppConstant {
    /* Base URL*/
    public static final String BASE_URL = "https://users-service.omnivoltaic.com/graphql";
    public static boolean IS_DEBUG = true;
    public static boolean IS_CLEAN_SESSION = false;

    /*Client ID Configuration*/
    public static boolean IS_RANDOM_CLIENT_ID = false;
    public static String MQTT_CLIENT_ID = "mqtt-android-app-Sub";

    /*Authentication for MQTT*/
    public static boolean IS_PASSWORD_AUTHENTICATION = false;
    public static String MQTT_USER_NAME = "ankur";
    public static String MQTT_USER_PASSWORD = "ankur123";

    /*Test Data*/
    public static boolean IS_TEST_DATA = false;
    public static boolean IS_CUSTOM_TOPIC = true;
    /*MQTT Configurations*/
    public static String MQTT_SERVER = "tcp://127.0.0.1:1883";
    public static String DEFAULT_PAYLOAD = "Hello Bridge!";
    public static Integer DEFAULT_QOS_LEVEL = 1;
    public static String DEFAULT_TOPIC = "Android/BRIDGE/Test";
}
