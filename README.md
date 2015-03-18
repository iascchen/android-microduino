# README

Android Apps for Microdino.

These Apps are always communicating Microduino via BLE.

## ble-led

Android APP control Microduino LED Board.
Tested on Android 4.3(Samsung S3) and 4.4(Sony Xperia).

You need 4 modules at least: Core, BT, LED, and USBTTL.

1. Stack them together,
2. upload the sketch vis USBTTL,
3. Play Android App.

![](ble-led/docs/led-1.png)
![](ble-led/docs/led-2.png)

## ble-clock

Sync RTC time with Phone.

Android APP control Microduino RTC, 
Tested on Android 4.3(Samsung S3) and 4.4(Sony Xperia).

You need 4 modules at least: Core, BT, RTC, and USBTTL.

1. Stack them together,
2. upload the sketch vis USBTTL,
3. Play Android App.

![](ble-clock/docs/clock-1.png)

## ble-voice

Manage the commands of voice controller.
Tested on Android 4.4(Sony Xperia).

Home page is voice commands list. 

![](ble-voice/docs/ble-voice-1.png)

Click each item, will enter the command editor.

![](ble-voice/docs/ble-voice-2.png)

Click "NEW" button on the top-right conner, create new command.

![](ble-voice/docs/ble-voice-3.png)

Click "UPLOAD" button on the top-right conner, will start voice commands set uploading process. Select your Microduino ble device, then upload the commands.

![](ble-voice/docs/ble-voice-4.png)
![](ble-voice/docs/ble-voice-5.png)

If you want to reset all commands to factory setting, please select meanu "Reset Cmds Set". 

![](ble-voice/docs/ble-voice-6.png)

## ble-8x8ledpad

Control 8x8 LEDPad.
Tested on Android 4.4(Sony Xperia).

Home page is select BLE device. 

![](ble-voice/docs/ledpad-1.png)

List of LED matrix

![](ble-voice/docs/ledpad-2.png)

Click first item "Text", You can upload text message.

![](ble-voice/docs/ledpad-3.png)

Click "NEW" button on the top-right conner, create new matrix.

You can draw something by cilck the led matrix on screen. If you want to clean the button, just click again. Long press will change color. There are 3 colors : Red, Yellow, Green.

On the top-right cornor you can find a switch to change draw mode. If the switch is ON, the changes on phone screen will send to LED Pad one by one. If the switch is OFF, you need press the "Upload" button to refresh LEDPad 

You can save or delete a matrix.

If you want to reset all matrix to factory setting, please select meanu "Reset Matrix Set". 
