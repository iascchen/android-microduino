# README

** ATTENTION:** This App use Microduino Cloud Service —— [mCotton](http://mcotton-01.chinacloudapp.cn/). Now this service is in very early version, and only opened  for internal developer. ** We will reset the use account and data at any time **, so please just try and don't store your critical data on it.

This App modified based on TI SmartConfig App, and add more configer info for Microduino Wifi Device. 

## wifi-config

Related Microduino module is CC3000 wifi module.

When wifi device reboot, if it can't find SmartConfig information and its Microduino Device ID (my_app_kit_id), It will start the process of configration.

1. Please regist a new account from [mCotton](http://mcotton-01.chinacloudapp.cn/).
2. Start mWifiConfig APP and login.
3. Restart your wifi device.
4. Click start button on APP, and start smart config process. After device wifi module smart config successfuly, it can get an ip address. Then device will broadcast its MAC address via UDP. Then call regDevId API on mCotton.
5. Mobile App got the UDP broadcast message, will call waitDevId API on mCotton, and bind the device with user's accpunt.
6. Wifi device call genDevId to fetch the device id. and store it into EPROM.
7. Send data to server with device id.
8. You can read the data on [My Device page of mCotton](http://mcotton-01.chinacloudapp.cn/myappkits).
