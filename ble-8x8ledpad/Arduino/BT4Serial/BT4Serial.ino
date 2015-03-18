/*
 * Copyright (C) 2015 Iasc CHEN
 *
 * This App is developed for Microduino.
 * You need 4 modules at least: Core, RTC, and USBTTL.
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
#include <SoftwareSerial.h>

// RX, TX
SoftwareSerial mySerial(4, 5); 

const int interval = 200; 
String msg = "";

void setup() {
  // initialize serial:
  mySerial.begin(9600);
  Serial.begin(9600);
}

void loop() {
  if (mySerial.available() > 0) {
      msg = mySerial.readStringUntil('\n');
      Serial.println(msg);
  }
}
