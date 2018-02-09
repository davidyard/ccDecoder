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

    public static void audioDecoder() {
        byte[] bFile = readBytesFromFile("C:\\Users\\DavidYardimian\\tsFiles\\1080p_1.ts");

        // save byte[] into a file

        String audioHeader = "010001110000000111100010";//4701e2
        int header1 = 0;
        int header2 = 1;
        int header3 = 2;
        try {
            File dstFile = new File("demo.mp3");
            //InputStream input = new ByteArrayInputStream(bFile);
            FileOutputStream out = new FileOutputStream(dstFile);
            for (int i = 0; i < bFile.length; i += 188) {
                String header = "";
                int x = Byte.toUnsignedInt(bFile[header1+i]);//getting int format of byte since the byte is a number
                int y = Byte.toUnsignedInt(bFile[header2+i]);
                int z = Byte.toUnsignedInt(bFile[header3+i]);
                String sx = Integer.toBinaryString(x);//getting the binary representation of the int
                String sy = Integer.toBinaryString(y);
                String sz = Integer.toBinaryString(z);
                while (sx.length() < 8) {
                    sx = 0 + sx;//extending the string to have a length of 8 since 0 will only have a length of 1
                }
                while (sy.length() < 8) {
                    sy = 0 + sy;//extending the string to have a length of 8 since 0 will only have a length of 1
                }
                while (sz.length() < 8) {
                    sz = 0 + sz;//extending the string to have a length of 8 since 0 will only have a length of 1
                }
                header = sx + sy + sz;
                /*String s1 = header.substring(8, 12);
                String s2 = header.substring(12, 16);
                int decimal1 = Integer.parseInt(s1,2);
                String hexStr1 = Integer.toString(decimal1,16);
                int decimal2 = Integer.parseInt(s2, 2);
                String hexStr2 = Integer.toString(decimal2, 16);
                System.out.println(hexStr1+hexStr2);*/
                if (header.equalsIgnoreCase(audioHeader)) {
                    System.out.println("REach");
                    out.write(bFile, i + 4, i + 188);
                }
            }
            out.close();
        } catch (IOException e){
            System.out.println("It failed.");
        }
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
            TreeMap<Long, SEINalUnit> sei = seiFound(bFile);
            HashMap<Integer, String> words = decodeCC(sei);
            System.out.println("608 CC IS: "+words.get(608));
            System.out.println("708 CC IS: "+words.get(708));
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

    private static TreeMap<Long, SEINalUnit> seiFound(byte[] array){
        //LinkedHashSet<SEINalUnit> seiUnits = new LinkedHashSet<SEINalUnit>();
        TreeMap<Long, SEINalUnit> orderedSeiUnits = new TreeMap<Long, SEINalUnit>();
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
                byte[] newB = Arrays.copyOfRange(array, a+6, a+11);
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

    private static HashMap<Integer, String> decodeCC(TreeMap<Long, SEINalUnit> seiUnits){
        HashMap<Integer,String> words = new HashMap<>();
        String six = "";
        String seven = "";
        DecoderFor608CC six08 = new DecoderFor608CC();
        DecoderFor708CC seven08 = new DecoderFor708CC();
        for(Map.Entry<Long,SEINalUnit> entry : seiUnits.entrySet()) {
            int byteNumber = entry.getValue().byteNumber;
            //System.out.println("ByteNumber: "+byteNumber);
            String byteMessage = "";
            String first = "";
            String firstByte = "00000011";//should be 03 which means cc_data structure
            String cc_countLengthValue = "";//will be the next byte after 03(firstByte)
            int countLength = 0;//the size of the cc_data. So 20 will give us 60 bytes of closed caption data. example would be D4
            int firstByteFound = 0;//if 1 that means the first byte has been found
            int secondByteFound = 0;//if 1 that means the second byte has been found
            int thirdByteFound = 0;//FF
            for(int a = 0; a < entry.getValue().seiUnit.length;a++) {
                //System.out.println(Byte.toUnsignedInt(b));
                byteNumber = (byteNumber + 1)%188;//to check if we are going to start a new line, which means we are going to start reading the header data
                int x = Byte.toUnsignedInt(entry.getValue().seiUnit[a]);//getting int format of byte since the byte is a number
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
                    System.out.println("COUNT LENGTH: "+countLength);
                    //thirdByteFound = 1;
                }
                if(secondByteFound == 1 && byteNumber > 3){
                    //System.out.println("Reacsjkb");
                    byte[] closedCaptionInformation = Arrays.copyOfRange(entry.getValue().seiUnit, a+2, a+(countLength*3)+2);
                    System.out.println("ARRY LENGTH: "+closedCaptionInformation.length+"\n");
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
                        System.out.print(hexStr1+hexStr2);
                    }
                    System.out.println("  "+entry.getKey()+"\n");
                    six += six08.decode(closedCaptionInformation);
                    seven += seven08.decode(closedCaptionInformation);
                    break;
                }
            }
        }
        words.put(608, six);
        words.put(708, seven);
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
        for(int a = 0; a < array.length;a++){
            int x = Byte.toUnsignedInt(array[a]);//getting int format of byte since the byte is a number
            String s = Integer.toBinaryString(x);//getting the binary representation of the int
            while(s.length() < 8){
                s = 0 + s;//extending the string to have a length of 8 since 0 will only have a length of 1
            }
            pts += s;
        }
        stamp += pts.substring(4, 7);
        stamp += pts.substring(8, 22);
        stamp += pts.substring(24, 38);
        return Long.parseLong(stamp,2);
    }

}
