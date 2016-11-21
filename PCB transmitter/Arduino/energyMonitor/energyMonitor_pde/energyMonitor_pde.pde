
#include <NewSoftSerial.h>
//monitor input tx connected to digital pin 8
NewSoftSerial mySerial(3, 4);

int val;
String buffer = "";
String message = "";
int startPos;
int endPos;
int watts [9];
float temperature = 0;
//DataOut dOut;
int updateInterval = 60; // seconds
long lastUpdate;
long lastMessage; 
int numberOfChannels;
String pachubeURL;
String pachubeAPI = "";

boolean foundCC = false;

int historySize = 210;

int historicValues [9][210] ;
int msgCounter=0;
String this_model;
String this_area;

boolean modelVerified = false;

boolean datastreamsSet = false;

int serial_speed []= { 
    57600, 9600 };

int this_serial_speed;
String unit = "";
void setup()  
{
  Serial.begin(57600);
  // set the data rate for the NewSoftSerial port
  mySerial.begin(57600);
  Serial.println("connecting ...");
  //adjust input baud rate tell you get xml data
//  adjustSoftSerialSpeed();
}

void loop()                     // run over and over again
{
        char inBuffer = (char)mySerial.read();   
       if (inBuffer >=29) {
            buffer.concat(inBuffer);
            Serial.println(buffer);
            if(buffer.length()>=164){ 
                buffer="";
              //  mySerial.flush();
               }
           
        
        }
        //delay(10);
}

void currentCostParse(String m){

    if ((m.indexOf("<hist") < 0) || (serial_speed[this_serial_speed] == 9600)){

       

             Serial.println("Energy Monitor Transmitter\n\nPower: \n");

            if (m.indexOf("<tmprF") < 0){
                this_area = "UK";   
            } 
            else {
                this_area = "US";   
            }
            int channel;
          
            channel=convertStringtoInteger(parseSingleElement(m,"sensor"));
                watts[channel] = convertStringtoInteger(parseDoubleElement(m,"ch1","watts"));
                Serial.print( "Channel "+((channel+1) + " - " + String(watts[channel]) + " W\n"));
                arrayCopy(historicValues[channel], 1, historicValues[channel], 0, historySize-1);
                historicValues[channel][historySize-1] = watts[channel];
           
            Serial.print( "\nTemperature: ");


            if (this_area.equals("UK")){
                temperature = convertStringtoFloat(parseSingleElement(m,"tmpr"));
                unit = "C";
            } 
            else {
                temperature = convertStringtoFloat(parseSingleElement(m,"tmprF"));
                unit = "F";
            }
            Serial.print(String(parseSingleElement(m,"tmpr") )+ "° "+unit+"\n");

            //println(debugMessage);

            if (!datastreamsSet){
                //setupDatastreams();   
            }

    } 
    else {

       // println("History message - ignored");

    }

}

String parseSingleElement(String m, String t){
    int start = m.indexOf("<"+t+">") + t.length()+2;
    int ends = m.indexOf("</"+t+">");
    return( m.substring(start, ends));
}

String parseDoubleElement(String m, String e, String w){
    int start = m.indexOf("<"+e+">") + e.length()+2;
    int ends = m.indexOf("</"+e+">");
    String t = m.substring(start, ends);
    start = t.indexOf("<"+w+">") + w.length()+2;
    ends = t.indexOf("</"+w+">");
    return( t.substring(start, ends));
}
void arrayCopy(int * src, int srcPos,int * dest, int destPos, int length){
  for(int i=0;i<length;i++){
  dest[destPos+i]=src[srcPos+i];
  }
  
}
 int convertStringtoInteger(String input){
           
            char ss[20];
            input.toCharArray(ss, sizeof(ss));
             return atoi(ss );
   }
  float convertStringtoFloat(String input){
           
            char ss[20];
            input.toCharArray(ss, sizeof(ss));
             return atof(ss );
           }
