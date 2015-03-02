/*
 * Copyright (C) 2015 Iasc CHEN
 *
 * This App is developed for Microduino.
 * You need 4 modules at least: Core, BT, LED, and USBTTL.
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
 *
 * Requires NeoPixel Library https://github.com/adafruit/Adafruit_NeoPixel
 */

#include <Adafruit_NeoPixel.h>
//#include <SoftwareSerial.h>

#define PIXEL_PIN    A0    // Digital IO pin connected to the NeoPixels.
#define PIXEL_COUNT  6

// RX, TX
//SoftwareSerial mySerial(4, 5); 

Adafruit_NeoPixel strip = Adafruit_NeoPixel(PIXEL_COUNT, PIXEL_PIN, NEO_GRB + NEO_KHZ800);
int n,red,green,blue;

void setup() {
  strip.begin();
  strip.show(); // Initialize all pixels to 'off'

  // initialize serial:
//  mySerial.begin(9600);
  Serial.begin(115200);
}

void loop() {
  // if there's any serial available, read it:
  while (Serial.available() > 0) {
    red = Serial.parseInt(); 
    green = Serial.parseInt(); 
    blue = Serial.parseInt(); 
    n = Serial.parseInt(); 

    if (Serial.read() == '\n') {
      if(-1 == n){
        colorSet(strip.Color(red, green, blue));
      } else if ((0 <= n) && (n < PIXEL_COUNT)){
        colorSet(strip.Color(red, green, blue), n);    
      }
    }
  }
}

// Fill strip with a color
void colorSet(uint32_t c) {
  Serial.println(c);
  for(uint16_t i=0; i<strip.numPixels(); i++) {
    strip.setPixelColor(i, c);
  }
  strip.show();
}

void colorSet(uint32_t c, uint8_t i) {
  strip.setPixelColor(i, c);
  strip.show();
}



