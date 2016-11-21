
#include <NewSoftSerial.h>

NewSoftSerial mySerial(8, 9);//reads input from EnviR monitor

void setup()  
{
  Serial.begin(57600);
  Serial.println("Goodnight moon!");

  // set the data rate for the NewSoftSerial port
  mySerial.begin(57600);
 
}

void loop()                     // run over and over again
{

  if (mySerial.available()) {
      Serial.println((char)mySerial.read());
  }
  if (Serial.available()) {
      mySerial.println((char)Serial.read());
  }
}
