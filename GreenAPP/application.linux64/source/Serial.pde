void setupSerial(int serial_speed) {
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

