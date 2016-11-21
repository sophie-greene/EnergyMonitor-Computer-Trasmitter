import eeml.*;

import processing.serial.*;

// Augest 2011

Serial myPort;
int val;
String buffer = "";
String message = "";
int startPos;
int endPos;
XMLElement xml;
float[] watts;
float temperature = 0;
int [] noWatts;
int noTemperature=0;
DataOut dOut;
int updateInterval = 60; // seconds
double lastUpdate;
double lastMessage;
int numberOfChannels;
String debugMessage = "";
PFont font;
String remoteServerURL;
String remoteServerAPI = "";
int serialDevice;
boolean monitorFound = false;
String loadPath = "preferences.txt";
int historySize = 280;//holds the width of the history graph

int[][] historicValues;

String this_model;
String this_area;

boolean modelVerified = false;

boolean datastreamsSet = false;

int[] serial_speed = { 
  57600, 9600
};

int this_serial_speed;
String unit = "";

void setup() {
  size(320, 500);//window size
  frameRate(2);
  watts = new float[10];
  noWatts = new int[10];
  font = loadFont("Univers-Bold-12.vlw");
  loadPreferences();
  this_model = "";
  this_area = "";

  if (!remoteServerAPI.equals("")) {

    try {
      this_serial_speed = 0;
      setupSerial(serial_speed[this_serial_speed]);
      setupRemoteServer();
      fill(0, 255, 0);
      debugMessage = "\nNo info received yet\nDevice selected: \n"
        + Serial.list()[serialDevice] + "\n\nChannels: "
        + numberOfChannels;
    } 
    catch (Exception e) {
    }
  }
}

void draw() {

  background(255, 255, 255);

  while (myPort.available () > 0) {
    if (!modelVerified) {
    }
    String inBuffer = myPort.readString();
    if (inBuffer != null) {
      buffer += inBuffer;
      if (!modelVerified) {

        boolean nonsenseXML = false;

        for (int i = 0; i < buffer.length(); i++) {
          int b = (int) buffer.charAt(i);
          if (b > 255) {
            nonsenseXML = true;
          }
        }

        if (nonsenseXML) {
          this_serial_speed = (this_serial_speed + 1) % 2;
          myPort.clear();
          myPort.stop();
          setupSerial(serial_speed[this_serial_speed]);
          debugMessage = "Trying new serial port \nspeed: "
            + serial_speed[this_serial_speed]
            + ". Please wait\na few seconds";
          lastUpdate = millis();
        }
      }

      startPos = buffer.indexOf("<msg>");
      endPos = buffer.indexOf("</msg>");

      if ((startPos >= 0) && (endPos > 0)) {
        if (endPos > startPos) {
          message = buffer.substring(startPos, endPos);
          buffer = "";
          InputParse(message);
          monitorFound = true;
          modelVerified = true;
          lastMessage = millis();
        } 
        else {
          buffer = buffer.substring(startPos, buffer.length());
        }
      }
    }
  }

  if ((millis() - lastUpdate) > updateInterval * 1000) {
    if (modelVerified) {
      sendData();
      lastUpdate = millis();
    }
  }

  if (millis() < lastUpdate)
    lastUpdate = millis();

  fill(0, 0, 0);

  textFont(font, 16);
  text(debugMessage, 30, 30);
  
  textFont(font, 12);
  text("Time to remote server update: "
    + (int) (updateInterval - (millis() - lastUpdate) / 1000)
    + " secs", 30, 275);
  fill(255, 255, 255);
  rect(-1, 280, width + 2, height - 280);
 
  fill(0, 0, 0);
  text("Serial Port", 10, 306);
  text("Feed URL", 10, 346);
  text("API KEY", 10, 386);
  text("Transmit frequency(Minutes)", 10, 426);


  drawGraph();
}

