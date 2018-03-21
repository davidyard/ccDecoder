package app;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class tsDecoder {
    public static void main(String[] args) {
        decode();
    }

    public static void decode(){


            // convert file to byte[]
        byte[] bFile = readBytesFromFile("C:\\Users\\DavidYardimian\\tsFiles\\1080i_1.ts");

            // save byte[] into a file

            //int lineNumber = 1;
            //for (int i = 0; i < bFile.length; i+=188) {
            //System.out.println(bFile.length);
            //byte[] newB = Arrays.copyOfRange(bFile, 0, bFile.length);


                /*if(lineNumber == 1){
                    getPMT(newB);
                }
                if(lineNumber == 2){
                    getPCR(newB);
                }*/
        HashMap<Long, SEINalUnit> sei = getSei(bFile);
        HashMap<Integer, HashSet<Subtitle>> words = decodeCC(sei);
            //System.out.println("608 CC IS: "+words.get(608));
        HashSet<Subtitle> subtitles = words.get(708);
        SortedSet<Subtitle> keys = new TreeSet<Subtitle>(subtitles);
        keys.forEach(text -> {
            System.out.println("STARTTIME: "+text.getStartTime()+" "+text.getCcData()+" ENDTIME: "+text.getEndTime());
        });

            /*PrintWriter writer = new PrintWriter("mockDecoder.txt", "UTF-8");
            writer.println(words.get(608));
            writer.println(words.get(708));
            writer.close();*/
                //lineNumber++;
            //}

    }

    private static String getPMT(byte[] array){
        String pmt = "";//it is 13 bits long so it is split up into 2 bytes
        int numberOfBits = 0;
        //a byte is 8 bits long. We are getting each byte in the 188 byte array
        for(byte b: array){
            //System.out.println(Byte.toUnsignedInt(b));
            int x = Byte.toUnsignedInt(b);//getting int format of byte since the byte is a number
            String s = Integer.toBinaryString(x);//getting the binary representation of the int
            while(s.length() < 8){
                s = 0 + s;//extending the string to have a length of 8 since 0 will only have a length of 1
            }
            if(numberOfBits == 120){//this is where the pmt pid starts at
                pmt += s.substring(3, 8);
            }
            if(numberOfBits == 128){//this is the last part of the pmt pid
                pmt+= s;
            }
            /*System.out.println(s);
            String s1 = s.substring(0, 4);
            String s2 = s.substring(4, 8);
            int decimal1 = Integer.parseInt(s1,2);
            String hexStr1 = Integer.toString(decimal1,16);
            int decimal2 = Integer.parseInt(s2, 2);
            String hexStr2 = Integer.toString(decimal2, 16);
            System.out.println(hexStr1.toUpperCase()+hexStr2.toUpperCase());*/
            numberOfBits+= s.length();
        }

        System.out.println("\nlength is: "+pmt.length()+" the bits are: "+pmt);
        return pmt;
    }

    private static String getPCR(byte[] array){
        String pcr = "";
        int numberOfBits = 0;
        //a byte is 8 bits long. We are getting each byte in the 188 byte array
        for(byte b: array){
            //System.out.println(Byte.toUnsignedInt(b));
            int x = Byte.toUnsignedInt(b);//getting int format of byte since the byte is a number
            String s = Integer.toBinaryString(x);//getting the binary representation of the int
            while(s.length() < 8){
                s = 0 + s;//extending the string to have a length of 8 since 0 will only have a length of 1
            }
            if(numberOfBits == 104){//this is where the pmt pid starts at
                pcr += s.substring(3, 8);
            }
            if(numberOfBits == 112){//this is the last part of the pmt pid
                pcr+= s;
            }
            /*System.out.println(s);
            String s1 = s.substring(0, 4);
            String s2 = s.substring(4, 8);
            int decimal1 = Integer.parseInt(s1,2);
            String hexStr1 = Integer.toString(decimal1,16);
            int decimal2 = Integer.parseInt(s2, 2);
            String hexStr2 = Integer.toString(decimal2, 16);
            System.out.println(hexStr1.toUpperCase()+hexStr2.toUpperCase());*/
            numberOfBits+= s.length();
        }
        System.out.println("\nlength is: "+pcr.length()+" the bits are: "+pcr);
        return pcr;
    }

    private static HashMap<Long, SEINalUnit> getSei(byte[] array){
        //LinkedHashSet<SEINalUnit> seiUnits = new LinkedHashSet<SEINalUnit>();
        HashMap<Long, SEINalUnit> orderedSeiUnits = new HashMap<Long, SEINalUnit>();
        int byteNumber = 0;
        long pts = 0;
        String PESPacketStarter = "00000000000000000000000111100000"; //in hex it is 00 00 01 E0
        int ptsFound = 0;
        String seiKeyCode = "10110101000000000011000101000111010000010011100100110100";
        //in hex it is B5003147413934
        String byteMessage = "";
        String header = "";
        String nullPacketHeader = "010001110001111111111111";//471FFF in hex
        Boolean nullPacket = false;
        //a byte is 8 bits long. We are getting each byte in the whole byte array
        for(int a = 0; a < array.length;a++){
            //System.out.println(Byte.toUnsignedInt(b));
            byteNumber = a%188;
            int x = Byte.toUnsignedInt(array[a]);//getting int format of byte since the byte is a number
            String s = Integer.toBinaryString(x);//getting the binary representation of the int
            while(s.length() < 8){
                s = 0 + s;//extending the string to have a length of 8 since 0 will only have a length of 1
            }
            if(byteNumber < 3){
                header += s;
            }
            if(header.equalsIgnoreCase(nullPacketHeader)){
                nullPacket = true;
                header = "";
            }
            else{
                nullPacket = false;
            }
            if(byteNumber >= 4 && !nullPacket){//this is where the data starts at
                byteMessage += s;
                header = "";
            }
            if(ptsFound == 0){
                if(!PESPacketStarter.contains(byteMessage)){
                    byteMessage = "";
                }
            }
            if(byteMessage.equalsIgnoreCase(PESPacketStarter) && ptsFound == 0){
                ptsFound = 1;
                byteMessage = "";
                byte[] newB = Arrays.copyOfRange(array, a+4, a+16);
                pts = presentationTimeStamp(newB);
            }
            if(ptsFound == 1) {
                if (!seiKeyCode.contains(byteMessage)) {
                    byteMessage = "";
                }
                if (byteMessage.equalsIgnoreCase(seiKeyCode)) {
                    byte[] newB = Arrays.copyOfRange(array, a + 1, a + 189);
                    System.out.println("...\n");
                    for (byte b : newB) {
                        int z = Byte.toUnsignedInt(b);//getting int format of byte since the byte is a number
                        String q = Integer.toBinaryString(z);//getting the binary representation of the int
                        while (q.length() < 8) {
                            q = 0 + q;//extending the string to have a length of 8 since 0 will only have a length of 1
                        }
                        String s1 = q.substring(0, 4);
                        String s2 = q.substring(4, 8);
                        int decimal1 = Integer.parseInt(s1, 2);
                        String hexStr1 = Integer.toString(decimal1, 16);
                        int decimal2 = Integer.parseInt(s2, 2);
                        String hexStr2 = Integer.toString(decimal2, 16);
                        //System.out.print(hexStr1+hexStr2);
                    }
                    //System.out.println("Reached Here");
                    SEINalUnit sei = new SEINalUnit(newB, byteNumber, pts);
                    orderedSeiUnits.put(pts, sei);
                    byteMessage = "";
                    ptsFound = 0;
                }

            }
        }

        return orderedSeiUnits;
    }

    private static HashMap<Integer, HashSet<Subtitle>> decodeCC(HashMap<Long, SEINalUnit> seiUnits){
        HashMap<Integer,HashSet<Subtitle>> words = new HashMap<>();
        HashSet<Subtitle> set = new HashSet<>();
        //String six = "";
        String seven = "";
        Subtitle subtitle = new Subtitle();
        //DecoderFor608CC six08 = new DecoderFor608CC();
        DecoderFor708CC seven08 = new DecoderFor708CC();
        SortedSet<Long> keys = new TreeSet<Long>(seiUnits.keySet());
        for(Long timeStamp: keys) {
            int byteNumber = seiUnits.get(timeStamp).byteNumber;
            //System.out.println("ByteNumber: "+byteNumber);
            String byteMessage = "";
            String first = "";
            String firstByte = "00000011";//should be 03 which means cc_data structure
            String cc_countLengthValue = "";//will be the next byte after 03(firstByte)
            int countLength = 0;//the size of the cc_data. So 20 will give us 60 bytes of closed caption data. example would be D4
            int firstByteFound = 0;//if 1 that means the first byte has been found
            int secondByteFound = 0;//if 1 that means the second byte has been found
            int thirdByteFound = 0;//FF
            for(int a = 0; a < seiUnits.get(timeStamp).seiUnit.length;a++) {
                //System.out.println(Byte.toUnsignedInt(b));
                byteNumber = (byteNumber + 1)%188;//to check if we are going to start a new line, which means we are going to start reading the header data
                int x = Byte.toUnsignedInt(seiUnits.get(timeStamp).seiUnit[a]);//getting int format of byte since the byte is a number
                String s = Integer.toBinaryString(x);//getting the binary representation of the int
                while (s.length() < 8) {
                    s = 0 + s;//extending the string to have a length of 8 since 0 will only have a length of 1
                }
                first += s;
                if(first.equalsIgnoreCase(firstByte)){
                    firstByteFound = 1;//the first Byte is found so continue to the second byte
                    //System.out.println("FIRSTBYTE");
                    continue;
                }
                else{//if we didnt find the firstByte then reset first and start again
                    first = "";
                }
                if(firstByteFound == 1 && byteNumber > 3){
                    secondByteFound = 1;
                    firstByteFound = 0;//to prevent countLength from being changed
                    cc_countLengthValue = s.substring(3,8);
                    countLength = Integer.parseInt(cc_countLengthValue, 2);//gives me a decimal representation of the binary number
                    //System.out.println("COUNT LENGTH: "+countLength);
                    //thirdByteFound = 1;
                }
                if(secondByteFound == 1 && byteNumber > 3){
                    //System.out.println("Reacsjkb");
                    byte[] closedCaptionInformation = Arrays.copyOfRange(seiUnits.get(timeStamp).seiUnit, a+2, a+(countLength*3)+2);
                    //System.out.println("ARRY LENGTH: "+closedCaptionInformation.length+"\n");
                    for (byte bite:closedCaptionInformation) {
                        int z = Byte.toUnsignedInt(bite);//getting int format of byte since the byte is a number
                        String q = Integer.toBinaryString(z);//getting the binary representation of the int
                        while (q.length() < 8) {
                            q = 0 + q;//extending the string to have a length of 8 since 0 will only have a length of 1
                        }
                        String s1 = q.substring(0, 4);
                        String s2 = q.substring(4, 8);
                        int decimal1 = Integer.parseInt(s1,2);
                        String hexStr1 = Integer.toString(decimal1,16);
                        int decimal2 = Integer.parseInt(s2, 2);
                        String hexStr2 = Integer.toString(decimal2, 16);
                        //System.out.print(hexStr1+hexStr2);
                    }
                    //System.out.println("  "+timeStamp+"\n");
                    //six += six08.decode(closedCaptionInformation);
                    subtitle = seven08.decode(closedCaptionInformation, subtitle, timeStamp);
                    break;
                }
            }
            if (subtitle.isEnded()){
                subtitle.setEndTime(timeStamp/90000.0);
                set.add(subtitle);
                subtitle = new Subtitle();
            }
        }
        //words.put(608, six);
        words.put(708, set);
        return words;
    }

    private static byte[] readBytesFromFile(String filePath) {

        FileInputStream fileInputStream = null;
        byte[] bytesArray = null;

        try {

            File file = new File(filePath);
            System.out.println("FILE LENGTH: "+file.length());
            bytesArray = new byte[(int) file.length()];

            //read file into bytes[]
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytesArray);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return bytesArray;

    }

    private static long presentationTimeStamp(byte[] array){
        String pts = "";
        String stamp = "";
        String ptsFlag = "";
        for(int a = 0; a < array.length;a++){
            int x = Byte.toUnsignedInt(array[a]);//getting int format of byte since the byte is a number
            String s = Integer.toBinaryString(x);//getting the binary representation of the int
            while(s.length() < 8){
                s = 0 + s;//extending the string to have a length of 8 since 0 will only have a length of 1
            }
            pts += s;
        }
        stamp += pts.substring(20, 23);//+3
        stamp += pts.substring(24, 39);//+1,+15
        stamp += pts.substring(40, 55);//+1, +15
        //System.out.print(" PTS: "+Long.parseLong(stamp,2)/90000);

        return Long.parseLong(stamp,2);
    }

}
