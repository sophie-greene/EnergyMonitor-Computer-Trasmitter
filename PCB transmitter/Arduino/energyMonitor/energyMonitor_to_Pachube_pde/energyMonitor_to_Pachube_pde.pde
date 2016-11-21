#include <SPI.h>
#include <NewSoftSerial.h>
#include <Ethernet.h>
#include <string.h>

#undef int() // needed by arduino 0022 to allow use of standard libraries
#include <stdio.h> // for function sprintf
#include<stdlib.h>
#define SHARE_FEED_ID              35240    // Server Feed ID
#define UPDATE_INTERVAL            240000    // if the connection is good wait 4 miniutes before updating again - should not be less than 5
#define RESET_INTERVAL              10000
#define PACHUBE_API_KEY            "qHO2LeYI1hKQZichkTP4rW7dbh5mPK9MVabYY9mD_0w" //API key 

// Define the pins used for the software serial port.  Note that we won't
// actually be transmitting anything over the transmit pin.
#define rxPin 8
#define txPin 9
NewSoftSerial mySerial(rxPin, txPin);
char data[200];
double watts[9];
double tmpr;
int cnt=0;
int no=0;
int now[9];
long lastReadingTime;
boolean lastConnected = false;
long lastConnectionTime = 0;
String dataString="";

byte mac[] = { 
  0xCC, 0xAC, 0xBE, 0xEF, 0xFE, 0x81 }; // this is unique on LOCAL network
byte ip[] = { 
  192, 168, 0, 177  };                  // no DHCP so we set our own IP address
byte remoteServer[] = {
  173,203,98,29 };            // pachube.com


void setup()  
{
  // set the data rate for the NewSoftSerial port
  mySerial.begin(57600);
  setupEthernet(); 
  initWatts();
}

void loop()                     // run over and over again
{
  readData();
  writeData();
}



