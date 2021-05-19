# Android - Arduino controlling via Bluetooth connection

## Table of contents
* [About project](#about-project)
* [Technologies](#technologies)
* [Features](#features)
* [Essential modules](#essential-modules)
* [Example screenshots](#example-screenshots)
* [Preview video](#preview-video)

## About project
Application allows communication via Bluetooth between Android devices and Arduino board.
In this project I've used Arduino Mega2560 with HC-06 bluetooth module and RGB matrices MAX7219.
Arduino's program schema is inside *arduino_app* folder. After successful installation of both programs
the user is able to connect to Arduino's HC-06 module and send data to the arduino's serial port, 
the data will be displayed as a scrolling text on RGB matrices.

#### Branches:
* master (worse version) - code on the master branch works within simple threads that start working after user starts connecting to the device, instance of the class with those threads (BluetoothChat) is stored in BaseApp class that extends Application class. Each fragment in it's onCreate method, retrives instance to the BaseApp object and then gets BluetoothChat instance from there so it can perform tasks on the working thread with bluetooth connection. It is worse version, it doesn't work as good as expected, device connection is often lost and sometimes doesn't want to connect at all.
* as_service (better version) - MainActivity that hosts all different fragments binds to the service in it's onStart() method right after application's start. When user starts connecting to the arduino device, connection threads in service class are started and kept alive until user closes app or clicks disconnect button from navigation bar. This version works better, I haven't noticed any bigger connection issues so far.

## Technologies
* Java
* C++

## Features
* turn BT on/off
* get list of paired devices
* search for new device to pair with
* BT connection is kept between fragments
* first fragment allows to send any type of string to serial port of arduino to be displayed on RBG matrices
* second fragment is a basic calculator, clicking any button sends data to arduino that displays equations that user does using this calculator

## Essential modules
* [Arduino Mega2560](https://store.arduino.cc/arduino-mega-2560-rev3)
* [HC-06](https://www.amazon.com/HiLetgo-Wireless-Bluetooth-Transceiver-Bi-Directional/dp/B07VL6ZH67/ref=sr_1_3?dchild=1&keywords=hc-06&qid=1619189849&sr=8-3)
* [MAX7219](https://www.amazon.com/DAOKI-MAX7219-Control-Display-Raspberry/dp/B07X95H9DT/ref=sr_1_6?dchild=1&keywords=MAX7219&qid=1619189950&sr=8-6)

[connection]: ./readme_images/connection_schema.png "connection"
[app_asking_for_permission]: ./readme_images/app_asking_for_permission.jpg "app asking for permission"
[bt_options]: ./readme_images/bt_options.jpg "bt options"
[successful_connection]: ./readme_images/successful_connection.jpg "successful connection"

## Connection schema
![connection schema][connection]

## Example screenshots
#### Drop down options
![bt options][bt_options]
#### After turning BT on, app will ask for permission
![app asking for permission][app_asking_for_permission]
#### successful connection to device - "connected to name" ('name' = my HC-06's name) 
![successful connection][successful_connection]

## Preview video
