#include <Wire.h>
#include "Adafruit_LEDBackpack.h"
#include "Adafruit_GFX.h"
#include <SoftwareSerial.h>

// RX, TX
SoftwareSerial mySerial(4, 5); 

Adafruit_BicolorMatrix matrix = Adafruit_BicolorMatrix();

const int interval = 200; 
String msg = "";
String myString_B="";
String myString_M="";
String myString_L="";

int hor[8][8],ver[8][8];

void setup() {
  // initialize serial:
  mySerial.begin(9600);
  Serial.begin(9600);
  matrix.begin(0x70);
}

void loop() {
  if (mySerial.available() > 0) {
    msg = mySerial.readStringUntil('\n');
    if(msg[0]=='B')
      myString_B=msg;
    else if(msg[0]=='M')
      myString_M=msg;
    else if(msg[0]=='L')
      myString_L=msg;

    if(myString_B.length()>64)
    {
      for(int i=0;i<8;i++)
      {
        for(int j=0;j<8;j++)
        {
          hor[i][j]=myString_B[8*i+j+2]-48;
          if(hor[i][j]!=ver[i][i])
          {
            matrix.clear();
            ver[i][j]=hor[i][i];
          }
        }
      }
      /*
      //Serial.println(myString_B);
       Serial.println("------------------------------");
       for(int i=0;i<8;i++)
       {
       for(int j=0;j<8;j++)
       {
       Serial.print(hor[i][j]);
       if(j==7)
       Serial.println("");
       }
       }
       Serial.println("------------------------------");
       */
    }
  }

  for(int i=0;i<8;i++)
  {
    for(int j=0;j<8;j++)
    {
      if(hor[i][j]/1==1)
        matrix.drawPixel(j, i, LED_RED);  
      else if(hor[i][j]/1==2)
        matrix.drawPixel(j, i, LED_YELLOW);  
      else if(hor[i][j]/1==3)
        matrix.drawPixel(j, i, LED_GREEN);  
      matrix.writeDisplay();
    }
  }
}
