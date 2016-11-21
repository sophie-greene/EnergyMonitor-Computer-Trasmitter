String dataT;
unsigned long last_connect;

int content_length;

void writeData(){
  if ((millis() - last_connect) > interval){
    if (localClient.connect()) {
      // here we assign comma-separated values to 'data', which will update server datastreams
      // we use all the analog-in values, but could of course use anything else millis(), digital
      // inputs
      char  out [20]= "";
     dataT = dtostrf(tmpr, 5, 1, out);
      for(int i=0;i<9;i++){
        dataT += ",";
        dataT += dtostrf(watts[i], 5, 1, out);
      }

      content_length = dataT.length();
      localClient.print("PUT /api/");
      localClient.print(SHARE_FEED_ID);
      localClient.print(".csv HTTP/1.1\nHost: pachube.com\nX-PachubeApiKey: ");
      localClient.print(PACHUBE_API_KEY);

      localClient.print("\nUser-Agent: Arduino (Pachube In Out v1.1)");
      localClient.print("\nContent-Type: text/csv\nContent-Length: ");
      localClient.print(content_length,DEC);
      localClient.print("\nConnection: close\n\n");
      localClient.print(dataT);
      localClient.print("\n");

      initWatts();
      interval = UPDATE_INTERVAL;
      last_connect=millis();
      disconnect_pachube();
    }else{
      ip[2]=ip[2]+1;
      disconnect_pachube();
      interval=RESET_INTERVAL;
     
    }

  } 


}

void disconnect_pachube(){

  localClient.stop();
  resetEthernetShield();
}




