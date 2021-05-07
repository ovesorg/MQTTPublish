
# Android Hub App
A simple Mqtt pub-sub mechanisim subscription application to work on android applications.

## Introduction
The aim of the application is to be able to publish to the Mqtt Server(Termux mosquitto in this case) to the specific Topic

### Technologies Used
1. Android Framework
2. Java
3. Eclipse Paho Android Client.

### Installation Procedure
#### Android Studio
If you are working on Android studio,
  1. Clone repository to a local folder.
  2. CD to folder with cloned repository and copy path.
  3. Open Android studio and import project from said path.\
  4. Build project and click Run to install on physical or emulator device

NB/ When installing on physical device make sure to check you have enabled Developer options on your device
  1. Go to Settings on your phone
  2. Navigate to about phone
  3. Click Build Number 7 times to enable developer options
  4. Navigate back to Settings and you should see a button for Developer options. Click it.
  5. Enable on USB debugging(Incase you are using a USB cable to install project app on device)
  6. Enable developer options and you are good to go and install the app on your device.

#### Apk file installation.
If you have the Application apk file in your phone,
  1. Go to phone setttings and enable installation from unknown sources
  2. Navigate to the apk file location and install the application.

#### Installing Termux
1. Go to [Google Playstore](https://play.google.com/store) and search  for Termux
2. Install Termux application. You can get it here [Termux](https://play.google.com/store/apps/details?id=com.termux)
3. Open Termux and you will be met by a terminal on the app.
4. Type pkg update to update libraries.
5. Once step 4 is done, type pkg upgrade.
6. Once step 5 is done, type pkg install mosquitto
7. Initialize mqtt sever by typing mosquitto -v

#### Android Hub
The next application you need to have installed is the Android Hub application
You can find the project repository here [Android Hub App](https://github.com/ovesorg/mobapp-MQTT-AndroidHub.git)
NB/ This application serves as the subscriber to the Mqtt server as the MqttPublish serves as the publisher.

#### Test Scenario
NB/ To test this application you need to have Termux and Android Hub applications installed on your device first.

In our test case we aim to accomplish publishing messages to a topic on the Local Termux application and see the message displayed on the Android Hub application
This can ONLY happen when the MQTT server is running in the background and for that termux needs to have initialized Mosquitto(Our Mqtt Server in this case)
For you to initialize mosquitto, check installing Termux point number 7.
 #### Steps to achieve our goal
  1. Run Mosquitto on termux
  2. Open MqttSubscribe application and type a topic you want to subscribe to eg Sensors/Bulb1
  3. Click the subscribe button
  4. Open for example the device scanner application and get device or plug in the USB OTG cable to the device and once done scanning see the data on the Android Hub application.
  5. Navigate back to the Android Hub application and you should be able to see the message data posted.
  NB/ If you are able to accomplish this then the test is deemed to have been a success but we are also open to sorting out any issues that you might face and any errors or bugs you come accross while engaging in this test case scenario.
  The bottomline is the two applications should communicate data to and from the mosquitto server and thus to each other WITHOUT Internet connection.
