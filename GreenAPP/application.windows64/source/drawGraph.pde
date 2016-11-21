void drawGraph() {

  fill(255, 255, 255);
  stroke(0);
  strokeWeight(1);

  rect(20, 175, 280, 85);

  stroke(230, 230, 230);

  for (int i = 1; i < 28; i++) {
    line(20 + i * 10, 259, 20 + i * 10, 176);
  }

  color from = color(255, 0, 255);
  color to = color(0, 255, 255);

  int[] maxWatts;
  maxWatts = new int[numberOfChannels];

  for (int i = 0; i < numberOfChannels - 1; i++) {
    maxWatts[i] = max(historicValues[i]);
  }

  int maxAllWatts = max(maxWatts);

  for (int i = 0; i < numberOfChannels; i++) {
 
     color graphLine = lerpColor(from, to, (float) i
        / (float) 2);
    
    stroke(graphLine);
  
    for (int j = 2; j < historySize; j++) {
      
        float graphHeight1 = 80.0 * (float) historicValues[i][j - 1]
          / (float) maxAllWatts;
        float graphHeight2 = 80.0 * (float) historicValues[i][j]
          / (float) maxAllWatts;
        line((float) 19 + j, height - 241 - graphHeight1, (float) 20
          + j, height - 241 - graphHeight2);
    
    }
  }
  stroke(0);
}

