void readData(){

  if (mySerial.available() > 163) {
    cnt = 0;
    while (mySerial.available()){
      char t = (char)mySerial.read(); //get character from serial buffer
      data[cnt]=t;  //start filling the buffer
      cnt +=1; //move on to next character
      if ( t == '\n')  //if LF then we have a full line
        break;
    }//empty the NewSoftSerial copy to my buffer
    int channel=convertStringtoInteger(parseSingleElement(data, "sensor"));

    if(no==0){
      tmpr=convertStringtoFloat(parseSingleElement(data, "tmpr"));
      no=no+1;
    } 
    else {
      tmpr=((no*tmpr)+(convertStringtoFloat(parseSingleElement(data, "tmpr"))))/(no+1);
      no=no+1;
    }
    if(now[channel]==0){
      watts[channel]=convertStringtoFloat(parseSingleElement(data, "watts"));
      now[channel]=now[channel]+1;
    }
    else{
      watts[channel]=((now[channel]*watts[channel])+(convertStringtoFloat(parseSingleElement(data, "watts"))))/(now[channel]+1);
      now[channel]=now[channel]+1;
    }
  }//end of data available
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

int convertStringtoInteger(String input){
  char ss[20];
  input.toCharArray(ss, sizeof(input));
  return atoi(ss );
}
double convertStringtoFloat(String input){
  char ss[20];
  input.toCharArray(ss, sizeof(input));
  return atof(ss );
}
void initWatts(){
  for (int i=0;i<9;i++){
    watts[i]=0;
    now[i]=0;
  }
  tmpr=0;
  no=0;
}



