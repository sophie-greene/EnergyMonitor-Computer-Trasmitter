import SpringGUI.*;

SpringGUI gui; 

void setupGUI() {
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

void handleEvent(String[] parameters) {

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
    updateInterval=int (gui.getText("update"))*60;
 

    println(serialDevice + " " +numberOfChannels + " "+ remoteServerURL + " " +remoteServerAPI + " " +Integer.toString(updateInterval)+" ");

    savePrefs(Integer.toString(serialDevice), Integer.toString(numberOfChannels), remoteServerURL, remoteServerAPI,Integer.toString(updateInterval/60));
    resetTransmitter();
  }
}





