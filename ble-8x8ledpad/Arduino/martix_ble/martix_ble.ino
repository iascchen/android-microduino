#include <Wire.h>
#include "Adafruit_LEDBackpack.h"
#include "Adafruit_GFX.h"
#include <SoftwareSerial.h>

// RX, TX
SoftwareSerial mySerial(4, 5); 

#define my_serial mySerial

Adafruit_BicolorMatrix matrix = Adafruit_BicolorMatrix();

String msg = "";
String myString_B="";
String myString_M="";

int hor[8][8],ver[8][8];

int color;

void setup() {
  // initialize serial:
  my_serial.begin(9600);
  Serial.begin(9600);
  matrix.begin(0x70);
  matrix.clear();
  delay(100);
}

void loop() {
  if (my_serial.available() > 0) {
    msg = my_serial.readStringUntil('\n');
    if(msg[0]=='B')
      image();
    else if(msg[0]=='L')
      draw();
  }
  if(msg[0]=='M')
    text();
}

void image()
{
  for(int i=0;i<8;i++)
  {
    for(int j=0;j<8;j++)
    {
      hor[i][j]=msg[8*i+j+2]-48;
      if(hor[i][j]!=ver[i][i])
      {
        matrix.clear();
        ver[i][j]=hor[i][i];
      }
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

void text()
{
  int time=150;
  String msg_display=msg.substring(6,msg.length());
  int longnum=msg_display.length();

  int color =msg.charAt(2)-48;
  int c =msg.charAt(4)-48;

  matrix.setTextSize(1);
  matrix.setTextWrap(false);
  //0:R-L  1:D-U  2:L-R  3:U-D
  matrix.setRotation(c);
  for (int8_t x=0; x>=-longnum*6-8; x--) 
  {
    matrix.clear();
    switch (color)
    {
    case 1:  
      matrix.setTextColor(LED_RED);
      break;
    case 2:  
      matrix.setTextColor(LED_YELLOW);
      break;
    case 3:  
      matrix.setTextColor(LED_GREEN);
      break;
    default:
      matrix.setTextColor(LED_RED);
    }
    matrix.setCursor(x+8,0);
    matrix.print(msg_display);
    matrix.writeDisplay();
    if (my_serial.available() > 0)
    {
      msg = my_serial.readStringUntil('\n');
      if(msg[0]!='M'||msg_display!=msg.substring(2,msg.length()))
      {
        msg_display=msg.substring(2,msg.length());
        time=0;
        matrix.clear();
      }
      else
        time=150;
    }
    delay(time);
  }
}

void draw()
{
  int y =msg.charAt(2)-48;
  int x =msg.charAt(4)-48;
  int color =msg.charAt(6)-48;

  if(color==0)
    matrix.drawPixel(x, y, LED_OFF); 
  else if(color==1)
    matrix.drawPixel(x, y, LED_RED); 
  else if(color==2)
    matrix.drawPixel(x, y, LED_YELLOW);  
  else if(color==3)
    matrix.drawPixel(x, y, LED_GREEN);  

  matrix.writeDisplay();
}

