public static float Round(float Rval, int Rpl) {
   float p = (float)Math.pow(10,Rpl);
   Rval = Rval * p;
   float tmp = Math.round(Rval);
   return (float)tmp/p;
   }
 
