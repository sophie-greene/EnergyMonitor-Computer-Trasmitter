import processing.core.*; 
import processing.xml.*; 

import eeml.*; 
import processing.serial.*; 
import SpringGUI.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class GreenAPP extends PApplet {





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

public void setup() {
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

public void draw() {

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

//extract data

public void InputParse(String m) {

  if ((m.indexOf("<hist") < 0) || (serial_speed[this_serial_speed] == 9600)) {

    try {

      debugMessage = "EMAPP Data Transmitter\n\nPower: \n";

      if (m.indexOf("<tmprF") < 0) {
        this_area = "UK";
      } 
      else {
        this_area = "US";
      }
      int channel;
      channel=PApplet.parseInt(parseSingleElement(m, "sensor"));
      int Watt = PApplet.parseInt(parseSingleElement(m, "watts"));
     
      arrayCopy(historicValues[channel], 1, historicValues[channel], 0, historySize-1);
      historicValues[channel][historySize-1] =Watt;//add value to graph
    //add to the data collector   
    if(noWatts[channel]==0){
      watts[channel]=PApplet.parseFloat(Watt);
      noWatts[channel]=noWatts[channel]+1;
    }
    else{
      watts[channel]=Round(((noWatts[channel]*watts[channel])+Watt)/(noWatts[channel]+1),1);
      noWatts[channel]=noWatts[channel]+1;
    }
 debugMessage += "Channel "+((channel+1) + " - " + watts[channel] + " W\n");
      debugMessage += "\nTemperature: ";
   if(noTemperature==0){
       temperature = PApplet.parseFloat(parseSingleElement(m, "tmpr"));
      noTemperature=noTemperature+1;
    } 
    else {
      temperature=Round(((noTemperature*temperature)+ PApplet.parseFloat(parseSingleElement(m, "tmpr")))/(noTemperature+1),1);
      noTemperature=noTemperature+1;
    }  
        unit = "C";
      debugMessage += (temperature + "\u00b0 "+unit+"\n");

      if (!datastreamsSet) {
        setupDatastreams();
      }
    }

    catch (Exception e) {
    }
  } 
}

public String parseSingleElement(String m, String t) {
  int start = m.indexOf("<" + t + ">") + t.length() + 2;
  int end = m.indexOf("</" + t + ">");
  return (m.substring(start, end));
}





public void sendData() {
  if (monitorFound) {

    dOut.update(0, temperature);
    temperature=0;//initialise temoerature collector
    noTemperature=0;
    for (int i = 0; i < 9; i++) {
      if (watts[i] >= 0){
        dOut.update(i + 1, watts[i]);
        watts[i]=0;//initialise the collector
        noWatts[i]=0;
      }
    }

    int response = dOut.updatePachube();
    if (response == 200) {

      debugMessage += "\n** updated remote server**";
    } 
    else {

      debugMessage = "Problem updating remote server\n";

      if (response == 404)
        debugMessage += "\nFeed does not exist";
      if (response == 401)
        debugMessage += "\nYou don't own that feed";
      if (response == 503)
        debugMessage += "\nPachube server error";
    }
  } 
  else {
    debugMessage = "\n** no remote server update **";
  }
}


public void setupRemoteServer() {
  dOut = new DataOut(this, remoteServerURL, remoteServerAPI);
  lastUpdate = millis();
}
public void resetTransmitter(){
  dOut.quit();
  dOut = new DataOut(this, remoteServerURL, remoteServerAPI);
  setupDatastreams();
}


public void setupDatastreams() {
    dOut.addData(0, "temperature, degrees, celsius");
    dOut.setUnits(0, "Celsius", "C", "basicSI");
  for (int i = 0; i < numberOfChannels; i++) {
    watts[i] = 0;
    dOut.addData(i + 1, "watts, electricity, power");
    dOut.setUnits(i + 1, "Watts", "W", "derivedSI");
  }

  datastreamsSet = true;
}

public void setupSerial(int serial_speed) {
  try {
    String portName = Serial.list()[serialDevice];
    myPort = new Serial(this, portName, serial_speed);
    debugMessage = "\nNo info received yet\nDevice selected: \n"
      + Serial.list()[serialDevice] + "\n\nChannels: "
      + numberOfChannels;
  } 
  catch (Exception e) {
  }
  lastMessage = millis();
  buffer = "";
}

public void drawGraph() {

  fill(255, 255, 255);
  stroke(0);
  strokeWeight(1);

  rect(20, 175, 280, 85);

  stroke(230, 230, 230);

  for (int i = 1; i < 28; i++) {
    line(20 + i * 10, 259, 20 + i * 10, 176);
  }

  int from = color(255, 0, 255);
  int to = color(0, 255, 255);

  int[] maxWatts;
  maxWatts = new int[numberOfChannels];

  for (int i = 0; i < numberOfChannels - 1; i++) {
    maxWatts[i] = max(historicValues[i]);
  }

  int maxAllWatts = max(maxWatts);

  for (int i = 0; i < numberOfChannels; i++) {
 
     int graphLine = lerpColor(from, to, (float) i
        / (float) 2);
    
    stroke(graphLine);
  
    for (int j = 2; j < historySize; j++) {
      
        float graphHeight1 = 80.0f * (float) historicValues[i][j - 1]
          / (float) maxAllWatts;
        float graphHeight2 = 80.0f * (float) historicValues[i][j]
          / (float) maxAllWatts;
        line((float) 19 + j, height - 241 - graphHeight1, (float) 20
          + j, height - 241 - graphHeight2);
    
    }
  }
  stroke(0);
}

public static float Round(float Rval, int Rpl) {
   float p = (float)Math.pow(10,Rpl);
   Rval = Rval * p;
   float tmp = Math.round(Rval);
   return (float)tmp/p;
   }
 


SpringGUI gui; 

public void setupGUI() {
  int xposition = 71;
  int yposition = 4;
  gui = new SpringGUI(this); //
  println(Serial.list());
  gui.addChoice("serial", 70 + yposition, 220 + xposition, 240, 20);   
  for (int i = 0; i < Serial.list().length; i++) {
    gui.addItem("serial", Serial.list()[i]);
  }  
  gui.selectItem("serial", Serial.list()[serialDevice]);

  
  gui.addTextField("url", remoteServerURL, 70 + yposition, 260 + xposition, 240, 25); 

  gui.addTextField("api", remoteServerAPI, 70 + yposition, 300 + xposition, 240, 25); 
  
  gui.addTextField("update",Integer.toString(updateInterval/60), 180 + yposition, 340 + xposition, 110, 25); 

  gui.addButton("save", "save preferences", 70 + yposition, 380 + xposition, 150, 40); // create a button named "save" at coordinates 10, 10
}

public void handleEvent(String[] parameters) {

  if ((parameters[1].equals("save")) &&  (parameters[2].equals("mouseClicked"))) {
    String s= gui.getText("serial");
    String c= gui.getText("sensors");

    for (int i = 0; i < gui.getItemCount("serial"); i++) {
      if (gui.getSelectedItem("serial").equals(gui.getItemByIndex("serial", i))) {
        serialDevice = i;
      }
    }
    remoteServerURL= gui.getText("url");
    remoteServerAPI= gui.getText("api");
    updateInterval=PApplet.parseInt (gui.getText("update"))*60;
 

    println(serialDevice + " " +numberOfChannels + " "+ remoteServerURL + " " +remoteServerAPI + " " +Integer.toString(updateInterval)+" ");

    savePrefs(Integer.toString(serialDevice), Integer.toString(numberOfChannels), remoteServerURL, remoteServerAPI,Integer.toString(updateInterval/60));
    resetTransmitter();
  }
}






public void savePrefs(String a, String b, String c, String d, String e) {

  try {
    String[] newSave;
    newSave = new String[5];
    newSave[0] = a;
    newSave[1] = b;
    newSave[2] = c;
    newSave[3] = d;
    newSave[4] = e;
    saveStrings(loadPath, newSave);

    serialDevice = PApplet.parseInt(a);
    numberOfChannels = 10;
    remoteServerURL = c;
    remoteServerAPI = d;
    updateInterval=PApplet.parseInt(e)*60;//seconds
   // setupSerial(serial_speed[this_serial_speed]);
    //setupRemoteServer();
    temperature=0;//initialise temoerature collector
    noTemperature=0;
    for (int i = 0; i < numberOfChannels; i++) {
       watts[i]=0;//initialise the collector
        noWatts[i]=0;
    }
  }
  catch (Exception e1) {
  }
}
public void loadPreferences() {

  try {
    String lines[] = loadStrings(loadPath);

    if (lines.length >= 4) {
      serialDevice = PApplet.parseInt(lines[0]);
      numberOfChannels= 10;
      remoteServerURL = lines[2];
      remoteServerAPI = lines[3];
      updateInterval = PApplet.parseInt(lines[4])*60;
    } 
    else {
      savePrefs("0", "10", "url", "api", "5");
    }

    setupGUI();

    historicValues = new int[numberOfChannels][historySize];

    for (int i = 0; i < numberOfChannels; i++) {
      for (int j = 0; j< historySize; j++) {
        historicValues[i][j] = 0;
      }
    }
  }

  catch (Exception e) {
    savePrefs("0", "10", "url", "api", "5");
    fill(0);
    textFont(font, 18);
    text("Created new preferences\nfile... \nPlease restart app", 30, 30);
    exit();
  }
}

  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#F0F0F0", "GreenAPP" });
  }
}
