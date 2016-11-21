
void savePrefs(String a, String b, String c, String d, String e) {

  try {
    String[] newSave;
    newSave = new String[5];
    newSave[0] = a;
    newSave[1] = b;
    newSave[2] = c;
    newSave[3] = d;
    newSave[4] = e;
    saveStrings(loadPath, newSave);

    serialDevice = int(a);
    numberOfChannels = 10;
    remoteServerURL = c;
    remoteServerAPI = d;
    updateInterval=int(e)*60;//seconds
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
void loadPreferences() {

  try {
    String lines[] = loadStrings(loadPath);

    if (lines.length >= 4) {
      serialDevice = int(lines[0]);
      numberOfChannels= 10;
      remoteServerURL = lines[2];
      remoteServerAPI = lines[3];
      updateInterval = int(lines[4])*60;
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

