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

public class CurrentCost_EnviR_to_Pachube extends PApplet {

//Augest 2011, Somoud
//updated the currentCostParse function to read the channel number of the source 
//this is included in the sensor tag i.e.<sensor>channel</sensor>
//removed the number of clamps
//reads all 9 channels of the EnviR CurrentCost




Serial myPort; 
int val;
String buffer = "";
String message = "";
int startPos;
int endPos;
XMLElement xml;
int[] watts;
float temperature = 0;
DataOut dOut;
int updateInterval = 60; // seconds
double lastUpdate;
double lastMessage; 
int numberOfChannels;
String debugMessage = "";
PFont font;
String pachubeURL;
String pachubeAPI = "";
int serialDevice;
boolean foundCC = false;
String loadPath = "CurrentCostPreferences.txt";
PImage pachube;
int historySize = 210;

int[][] historicValues;

String this_model;
String this_area;

boolean modelVerified = false;

boolean datastreamsSet = false;

int[] serial_speed = { 
    57600, 9600 };

int this_serial_speed;
String unit = "";

public void setup() 
{
    size(250, 470);

    frameRate(2);

    watts = new int[9];
    font = loadFont("Univers-Bold-12.vlw"); 
    loadPreferences();
    this_model = "";
    this_area = "";

    if (!pachubeAPI.equals("")){

        try {

            this_serial_speed = 0;

            setupSerial(serial_speed[this_serial_speed]);
            setupPachube();

            fill(0,255,0);

            debugMessage = "CurrentCost\n\nNo info received yet\nDevice selected: \n"+Serial.list()[serialDevice]+"\n\nChannels: "+numberOfChannels;

        }
        catch (Exception e){
           // println("There was a problem starting up. Was the serial port detected?" + e);
        }

        smooth();
    }


    pachube = loadImage("pachube.png");
}

public void draw()
{

    float energyStatus = constrain(watts[0]/10, 0, 255);
    background(energyStatus, 255-energyStatus, 0);

    while (myPort.available() > 0) {

        if (!modelVerified) {


        }

        String inBuffer = myPort.readString();   
        if (inBuffer != null) {
            buffer += inBuffer;

            //println(buffer);

            if (!modelVerified) {    

                boolean nonsenseXML = false;

                for (int i = 0; i < buffer.length(); i++){
                    int b = (int)buffer.charAt(i);
                    if (b > 255){
                        nonsenseXML = true;
                    } 
                }

                if (nonsenseXML){
                    this_serial_speed = (this_serial_speed+1)%2;
                    //println("Received nonsensical XML... trying new serial port speed: "+serial_speed[this_serial_speed]);
                    myPort.clear();
                    myPort.stop();
                    setupSerial(serial_speed[this_serial_speed]);
                    debugMessage = "Trying new serial port \nspeed: " +serial_speed[this_serial_speed] + ". Please wait\na few seconds";
                    lastUpdate = millis();
                }
            } 

            startPos = buffer.indexOf("<msg>");
            endPos = buffer.indexOf("</msg>");

            if ((startPos >=0) && (endPos > 0)){
                if (endPos > startPos){              
                    // println("Found full message from CurrentCost");
                    // println(buffer + "---" + startPos + "---" + endPos);

                    message = buffer.substring(startPos, endPos);
                    buffer = "";
                    currentCostParse(message);
                    foundCC = true;

                    modelVerified = true;
                    lastMessage = millis();

                } 
                else {                
                    buffer = buffer.substring(startPos, buffer.length());
                }
            }
        }
    } 

    if ((millis() - lastUpdate) > updateInterval * 1000){
        if (modelVerified){
            pachube();
            lastUpdate = millis();
        }
    }

    if (millis() < lastUpdate) lastUpdate = millis();


    fill(0,0,0);

    textFont(font, 16);
    text(debugMessage, 30, 30);

    textFont(font, 12);
    text("Time to Pachube update: " + (int)(updateInterval - (millis()-lastUpdate)/1000) + " secs", 30, 255);

fill(250,250,250);
    rect(-1,260,width+2,height-260);

    drawPachube();
    drawGraph();
}

public void currentCostParse(String m){

    if ((m.indexOf("<hist") < 0) || (serial_speed[this_serial_speed] == 9600)){

        try{

            debugMessage = "Energy Monitor Transmitter\n\nPower: \n";

            if (m.indexOf("<tmprF") < 0){
                this_area = "UK";   
            } 
            else {
                this_area = "US";   
            }
            int channel;
            channel=PApplet.parseInt(parseSingleElement(m,"sensor"));
                watts[channel] = PApplet.parseInt(parseDoubleElement(m,"ch1","watts"));
                debugMessage += "Channel "+((channel+1) + " - " + watts[channel] + " W\n");
                arrayCopy(historicValues[channel], 1, historicValues[channel], 0, historySize-1);
                historicValues[channel][historySize-1] = watts[channel];
           
            debugMessage += "\nTemperature: ";


            if (this_area.equals("UK")){
                temperature = PApplet.parseFloat(parseSingleElement(m,"tmpr"));
                unit = "C";
            } 
            else {
                temperature = PApplet.parseFloat(parseSingleElement(m,"tmprF"));
                unit = "F";
            }
            debugMessage += (temperature + "\u00c2\u00b0 "+unit+"\n");

            //println(debugMessage);

            if (!datastreamsSet){
                setupDatastreams();   
            }



        }

        catch (Exception e){

           // println("There was a problem parsing this message: \n" + m + "\n\n" +e);

        }

    } 
    else {

       // println("History message - ignored");

    }

}

public String parseSingleElement(String m, String t){
    int start = m.indexOf("<"+t+">") + t.length()+2;
    int end = m.indexOf("</"+t+">");
    return( m.substring(start, end));
}

public String parseDoubleElement(String m, String e, String w){
    int start = m.indexOf("<"+e+">") + e.length()+2;
    int end = m.indexOf("</"+e+">");
    String t = m.substring(start, end);
    start = t.indexOf("<"+w+">") + w.length()+2;
    end = t.indexOf("</"+w+">");
    return( t.substring(start, end));
}

public void pachube(){
    if (foundCC){

        dOut.update(0, temperature);
        for (int i = 0; i < 9; i++){
          if(watts[i]>=0)   dOut.update(i+1, watts[i]); 
        } 

        int response = dOut.updatePachube(); 
        if (response == 200){
            //println("Pachube updated!");   
            debugMessage += "\n** updated Pachube **";
        } 
        else {

           debugMessage = "Problem updating Pachube\n";

            if (response == 404) debugMessage += "\nFeed does not exist";
            if (response == 401) debugMessage += "\nYou don't own that feed";
            if (response == 503) debugMessage += "\nPachube server error";

        }
    }
    else 
    {
        debugMessage = "No CurrentCost found\n** no Pachube update **";
    }
}

public void loadPreferences(){

    try{
        String lines[] = loadStrings(loadPath);

        if (lines.length >= 4){
            serialDevice = PApplet.parseInt(lines[0]);
            numberOfChannels = 9;
            pachubeURL = lines[2];
            pachubeAPI = lines[3];
           /* println(serialDevice);
            println(numberOfChannels);
            println(pachubeURL);
            println(pachubeAPI);*/

        } 
        else {
            savePrefs("0","1","http://","API_KEY");
            //exit();   
        }

        setupGUI();

        historicValues = new int[numberOfChannels][historySize];

        for (int i = 0; i < numberOfChannels; i++){
            for (int j = 0; j< historySize; j++){
                historicValues[i][j] = 0;
            }   

        }

    }

    catch (Exception e){
        savePrefs("0","1","http://","API_KEY");
        fill(0);
        textFont(font, 18);
        text("Created new preferences\nfile... \nPlease restart app", 30, 30);
        exit();
    }
}

public void savePrefs(String a, String b, String c, String d){

    try {
        String[] newSave;
        newSave = new String[4];
        newSave[0] = a;
        newSave[1] = b;
        newSave[2] = c;
        newSave[3] = d;
        saveStrings(loadPath, newSave);

        serialDevice = PApplet.parseInt(a);
        numberOfChannels = constrain(PApplet.parseInt(b),1,9);
        pachubeURL = c;
        pachubeAPI = d;
       /* println(serialDevice);
        println(numberOfChannels);
        println(pachubeURL);
        println(pachubeAPI);*/
        setupSerial(serial_speed[this_serial_speed]);
        setupPachube();
        historicValues = new int[numberOfChannels][historySize];

        for (int i = 0; i < numberOfChannels; i++){
            for (int j = 0; j< historySize; j++){
                historicValues[i][j] = 0;
            }   

        }

       // println("Saved preferences");

    }
    catch (Exception e){
        //println("There was a problem saving preferences: " + e);   
    }
}

public void setupPachube(){
    dOut = new DataOut(this, pachubeURL, pachubeAPI);
    lastUpdate = millis();

}

public void setupDatastreams(){

    if (unit == "C"){
        dOut.addData(0,"temperature, degrees, celsius");
        dOut.setUnits(0, "Celsius","C","basicSI");
    } 
    else if (unit == "F"){
        dOut.addData(0,"temperature, degrees, fahrenheit");
        dOut.setUnits(0, "Fahrenheit","F","conversionBasedUnits");
    }

    for (int i = 0; i < numberOfChannels; i++){
        watts[i] = 0;   
        dOut.addData(i+1,"watts, electricity, power");
        dOut.setUnits(i+1, "Watts","W","derivedSI");
    }

    datastreamsSet = true;

}



public void drawPachube(){
   image(pachube, 30, 265);
   
}

public void drawGraph(){

    fill(255,255,255);
    stroke(0);
    strokeWeight(1);

    rect(20,175,210,65);

    stroke(230,230,230);

    for (int i = 1; i < 21; i++){        
        line(20 + i*10, 239, 20 + i* 10, 176);
    }

    int from = color(255, 0, 255);
    int to = color(0, 255,255 );

    int[] maxWatts;
    maxWatts = new int[numberOfChannels];

    for (int i = 0; i < numberOfChannels-1; i++){        
        maxWatts[i] = max(historicValues[i]);
    }

    int maxAllWatts = max(maxWatts);

    for (int i =0; i < numberOfChannels; i++){      

        int graphLine = lerpColor(from, to, (float)i/(float)numberOfChannels);  
        stroke(graphLine);

        for (int j = 2; j < historySize; j++){
            float graphHeight1=63.0f*(float)historicValues[i][j-1]/(float)maxAllWatts;
            float graphHeight2=63.0f*(float)historicValues[i][j]/(float)maxAllWatts;
            line((float)19+j, height-231-graphHeight1, (float)20+j, height-231-graphHeight2);
        }

    }

    stroke(0);

}


public void setupSerial(int serial_speed){
    try {
        String portName = Serial.list()[serialDevice];
        myPort = new Serial(this, portName, serial_speed);
       // println("Set up serial port with speed: " + serial_speed);
        debugMessage = "CurrentCost\n\nNo info received yet\nDevice selected: \n"+Serial.list()[serialDevice]+"\n\nChannels: "+numberOfChannels;
       // println(debugMessage);

    } 
    catch (Exception e){

       // println("There was a problem setting up serial communication: " + e);

    }
    lastMessage = millis();
    buffer = "";
}


SpringGUI gui; 

public void setupGUI() {
    int xposition = 51;
    int yposition = 4;
    gui = new SpringGUI(this); //
    println(Serial.list());
    gui.addChoice("serial", 10 + yposition, 270 + xposition, 220, 20);   
    for (int i = 0; i < Serial.list().length; i++){
        gui.addItem("serial", Serial.list()[i]);
    }  
    gui.selectItem("serial", Serial.list()[serialDevice]);

   
    gui.addTextField("url", pachubeURL, 10 + yposition, 310 + xposition, 220, 25); 

    gui.addTextField("api", pachubeAPI, 10 + yposition, 350 + xposition, 220, 25); 

    gui.addButton("save", "save preferences", 10 + yposition, 390 + xposition, 220, 20); // create a button named "myButton" at coordinates 10, 10

}

public void handleEvent(String[] parameters) {

    if ((parameters[1].equals("save")) &&  (parameters[2].equals("mouseClicked"))){

        String s= gui.getText("serial");
        String c= gui.getText("sensors");

        for (int i = 0; i < gui.getItemCount("serial"); i++){
            //println(gui.getItemByIndex("serial", i));
            if (gui.getSelectedItem("serial").equals(gui.getItemByIndex("serial", i))){
                serialDevice = i;
            }
        }

        numberOfChannels = PApplet.parseInt(gui.getSelectedItem("sensors").substring(0,1));


        pachubeURL= gui.getText("url");
        pachubeAPI= gui.getText("api");

        println(serialDevice + " " +numberOfChannels + " "+ pachubeURL + " " +pachubeAPI + " " );

        savePrefs(str(serialDevice), str(numberOfChannels), pachubeURL, pachubeAPI);
        setupPachube();
    }

}





  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#F0F0F0", "CurrentCost_EnviR_to_Pachube" });
  }
}
