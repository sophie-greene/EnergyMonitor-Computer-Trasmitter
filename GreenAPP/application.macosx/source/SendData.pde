
void sendData() {
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


void setupRemoteServer() {
  dOut = new DataOut(this, remoteServerURL, remoteServerAPI);
  lastUpdate = millis();
}
void resetTransmitter(){
  dOut.quit();
  dOut = new DataOut(this, remoteServerURL, remoteServerAPI);
  setupDatastreams();
}


void setupDatastreams() {
    dOut.addData(0, "temperature, degrees, celsius");
    dOut.setUnits(0, "Celsius", "C", "basicSI");
  for (int i = 0; i < numberOfChannels; i++) {
    watts[i] = 0;
    dOut.addData(i + 1, "watts, electricity, power");
    dOut.setUnits(i + 1, "Watts", "W", "derivedSI");
  }

  datastreamsSet = true;
}

