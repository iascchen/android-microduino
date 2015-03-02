/*
 * Copyright (C) 2015 Iasc CHEN
 *
 * This App is developed for Microduino.
 * You need 4 modules at least: Core, BT, RTC, and USBTTL.
 *
 * 1. Stack them together,
 * 2. upload the sketch vis USBTTL,
 * 3. Play Android App.\n"
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <Wire.h>
#include <Rtc_Pcf8563.h>
// #include <SoftwareSerial.h>

// RX, TX
// SoftwareSerial mySerial(4, 5); 

Rtc_Pcf8563 rtc;

const int interval = 990; // 1000 ms

byte cmdByte;
String msg = "EtOH";
int _year,_month,_day,_hour,_minute,_sec;
String dateStr, ret;

void setup() {
  // initialize serial:
  // mySerial.begin(9600);
  Serial.begin(115200);
}

void loop() {
  // if there's any serial available, read it:
  processCommand();

  delay(1);
  ret = getRtcTimeString();

  Serial.print(ret);
  //mySerial.print(ret);

  delay(interval);
}

void processCommand(){
  if (Serial.available() > 0) {
    cmdByte = Serial.read();

    switch (cmdByte) {
    case 't' :
      _year = Serial.parseInt(); 
      _month = Serial.parseInt(); 
      _day = Serial.parseInt(); 
      _hour = Serial.parseInt();
      _minute = Serial.parseInt();
      _sec = Serial.parseInt();
      setRtcTime(_year, _month, _day, _hour, _minute, _sec);
      break;

    case 'm' :
      msg = Serial.readStringUntil('\n');
      break;
    }
  }
}

void setRtcTime (byte _year, byte _month, byte _day, byte _hour, byte _minute, byte _sec)     
{ 	 
  //clear out all the registers
  rtc.initClock();
  rtc.setDate(_day, 0, _month, 0, _year);
  rtc.setTime(_hour, _minute, _sec);
} 

String getRtcTimeString(){
  dateStr = rtc.formatDate(RTCC_DATE_ASIA);
  dateStr += " ";
  dateStr += rtc.formatTime(RTCC_TIME_HMS);

  return dateStr;
}




