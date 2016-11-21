//extract data

void InputParse(String m) {

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
      channel=int(parseSingleElement(m, "sensor"));
      int Watt = int(parseSingleElement(m, "watts"));
     
      arrayCopy(historicValues[channel], 1, historicValues[channel], 0, historySize-1);
      historicValues[channel][historySize-1] =Watt;//add value to graph
    //add to the data collector   
    if(noWatts[channel]==0){
      watts[channel]=float(Watt);
      noWatts[channel]=noWatts[channel]+1;
    }
    else{
      watts[channel]=Round(((noWatts[channel]*watts[channel])+Watt)/(noWatts[channel]+1),1);
      noWatts[channel]=noWatts[channel]+1;
    }
 debugMessage += "Channel "+((channel+1) + " - " + watts[channel] + " W\n");
      debugMessage += "\nTemperature: ";
   if(noTemperature==0){
       temperature = float(parseSingleElement(m, "tmpr"));
      noTemperature=noTemperature+1;
    } 
    else {
      temperature=Round(((noTemperature*temperature)+ float(parseSingleElement(m, "tmpr")))/(noTemperature+1),1);
      noTemperature=noTemperature+1;
    }  
        unit = "C";
      debugMessage += (temperature + "° "+unit+"\n");

      if (!datastreamsSet) {
        setupDatastreams();
      }
    }

    catch (Exception e) {
    }
  } 
}

String parseSingleElement(String m, String t) {
  int start = m.indexOf("<" + t + ">") + t.length() + 2;
  int end = m.indexOf("</" + t + ">");
  return (m.substring(start, end));
}




