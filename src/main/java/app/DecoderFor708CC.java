package app;

import java.util.HashMap;

public class DecoderFor708CC {

    public String decode(byte[] array){
        String closedCaption = "";
        HashMap<String, String> table = new AsciiTable().makeTable();
        HashMap<String, Integer> settings = new AsciiTable().windowSettings();
        //HashMap<Integer, String> asciiTable = new HashMap<>();
        int counter = 0;
        int valid = 0;
        int field = -1;
        int hasCCText = 0;//if 1 means there is text
        int windowSetting = 0;//if 0 then ccdata is not a window setting
        for (byte b: array) {
            String cc = "";
            if(counter == 0){//header of the cc_data so this part tells you if 608 or 708
                int x = Byte.toUnsignedInt(b);
                String s = Integer.toBinaryString(x);//getting the binary representation of the int
                while(s.length() < 8){
                    s = 0 + s;//extending the string to have a length of 8 since 0 will only have a length of 1
                }
                valid = Integer.parseInt(s.substring(5,6), 2);//if true means cc_valid is 1 and data should be taken
                if(valid == 1){
                    field = Integer.parseInt(s.substring(6,8), 2);// for example ff = valid = 1 and field will be 11 = 3 in binary
                }
            }
            if(counter != 0){//cc_data_1 or cc_data_2
                if(field == 3){
                    int x = Byte.toUnsignedInt(b);
                    String s = Integer.toBinaryString(x);
                    while(s.length() < 8){
                        s = 0 + s;//extending the string to have a length of 8 since 0 will only have a length of 1
                    }
                    String s1 = s.substring(2, 3);
                    hasCCText = Integer.parseInt(s1,2);
                }
                if(field == 2 && hasCCText == 1){
                    int x = Byte.toUnsignedInt(b);
                    String s = Integer.toBinaryString(x);
                    while(s.length() < 8){
                        s = 0 + s;//extending the string to have a length of 8 since 0 will only have a length of 1
                    }
                    String s1 = s.substring(0, 4);
                    String s2 = s.substring(4, 8);
                    int decimal1 = Integer.parseInt(s1,2);
                    String hexStr1 = Integer.toString(decimal1,16);
                    int decimal2 = Integer.parseInt(s2, 2);
                    String hexStr2 = Integer.toString(decimal2, 16);
                    cc = hexStr1+hexStr2;
                    if(settings.containsKey(cc) && windowSetting == 0){
                        windowSetting = settings.get(cc);
                    }
                    else{
                        String ascii = table.get(cc);
                        if(ascii != null && windowSetting == 0){
                            closedCaption += ascii;
                        }
                    }

                }
            }
            if(windowSetting > 0){
                windowSetting--;
            }
            counter = (counter + 1)%3;
        }
        //System.out.println(closedCaption);
        return closedCaption;
    }

}
