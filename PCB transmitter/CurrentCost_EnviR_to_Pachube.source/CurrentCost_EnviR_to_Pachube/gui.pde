import SpringGUI.*;

SpringGUI gui; 

void setupGUI() {
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

void handleEvent(String[] parameters) {

    if ((parameters[1].equals("save")) &&  (parameters[2].equals("mouseClicked"))){

        String s= gui.getText("serial");
        String c= gui.getText("sensors");

        for (int i = 0; i < gui.getItemCount("serial"); i++){
            //println(gui.getItemByIndex("serial", i));
            if (gui.getSelectedItem("serial").equals(gui.getItemByIndex("serial", i))){
                serialDevice = i;
            }
        }

        numberOfChannels = int(gui.getSelectedItem("sensors").substring(0,1));


        pachubeURL= gui.getText("url");
        pachubeAPI= gui.getText("api");

        println(serialDevice + " " +numberOfChannels + " "+ pachubeURL + " " +pachubeAPI + " " );

        savePrefs(str(serialDevice), str(numberOfChannels), pachubeURL, pachubeAPI);
        setupPachube();
    }

}





