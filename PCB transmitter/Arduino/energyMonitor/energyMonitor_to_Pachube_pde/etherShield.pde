Client localClient(remoteServer, 80);
unsigned int interval;

char buff[64];
int pointer = 0;

void setupEthernet(){
  resetEthernetShield();
  Client remoteClient(255);
  delay(500);
  interval = RESET_INTERVAL;
}

void clean_buffer() {
  pointer = 0;
  memset(buff,0,sizeof(buff)); 
}

void resetEthernetShield(){
  Ethernet.begin(mac, ip);
}


