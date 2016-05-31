package com.stegatective.abcd.j_steg;


import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;


/**
 * Created by Mariah on 5/14/2016.
 */
public class SteganographyManager {

    private String watermark = asciiToBinary("ABCD");
    private Set<xyCoord> isUsed = null;
    private Random rand = new Random(0);

    private static int hash;



    //Create a small inner class that will be used to keep track of the coordnates
    private class xyCoord{

        protected int x;
        protected int y;

        public xyCoord(int x, int y){
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj){
            if(this == obj)
                return true;

            if(!(obj instanceof xyCoord))
                return false;

            xyCoord that = (xyCoord)obj;
            return((this.x == that.x)&&(this.y == that.y));
        }

        @Override
        public int hashCode(){
            hash = (x + y)%26;
            return hash;
        }
    }

    //encrypt the image with the information from the user
    public String encryptIMG(Context context, Bitmap selectedIMG, File userMessage, String pinNumber){

        int xmax = selectedIMG.getWidth(); // img length
        int ymax = selectedIMG.getHeight(); // img width

        isUsed = new HashSet<>();
        rand = new Random(0);

        //Encrypt the watermark into the image first
        encrypt(xmax,ymax,selectedIMG, watermark);

        //Encrypt the pinNumber into the image
        encrypt(xmax,ymax,selectedIMG,asciiToBinary(pinNumber));

        Integer seed = Integer.parseInt(pinNumber);
        rand = new Random(seed);

        //Convert the file into a byte array
        byte[] bytes = fileToByte(userMessage);

        //encrypt the byte array into the file
        encrypt(xmax,ymax,selectedIMG, bytes);

        userMessage.delete();

        //Create a new folder on the devices library called "J-Steg"
        File imagePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                + File.separator + "J-Steg" + File.separator );
        imagePath.mkdir();

        //Create a name for the image with the current time from the system
        File file = new File(imagePath,"JS_" + System.currentTimeMillis() +".jpg" );

        OutputStream out = null;
        try {
            out = new FileOutputStream(file);

            //Compress the image into JPEG format with 100% quality
            selectedIMG.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

        } catch (Exception e){
            e.printStackTrace();
        }

        //Create save the image to the gallery information must be added
        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put("_data", file.getAbsolutePath());

        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        return file.getAbsolutePath();
    }

    //decrypt the the information from the image after the user selects it
    public String decryptIMG(Bitmap selectedIMG, String pinNumber){
        //Set the MAX_X and MAX_Y of the random number generator to be the length and width of image
        int xmax = selectedIMG.getWidth(); // img length
        int ymax = selectedIMG.getHeight(); // img width

        //Create a random number generator seeded with the pinNumber provided by the user
        rand = new Random(0);

        //Create a set that will be used to keep track of the
        isUsed = new HashSet<>();

        File filePath = new File(Environment.getExternalStorageDirectory(), "J-Steg");

        if(!filePath.exists()){
            filePath.mkdirs();//This will create the folder "J-Steg"
        }
        //This will create a file named with the timeStamp of the time the picture was encrypted
        File messagePath = new File(filePath, "temp.txt");

        FileWriter writer = null;
        try {
            writer = new FileWriter(messagePath);
        }
        catch (IOException e){
            e.printStackTrace();
        }

        //Check if the image has been watermarked
        if(isWatermarked(xmax,ymax,selectedIMG)) {
            //Check if the provided user pin matches the pin used in the image
            if(verifyPin(xmax,ymax,selectedIMG,asciiToBinary(pinNumber))) {

                Integer seed = Integer.parseInt(pinNumber);
                rand = new Random(seed);

                int pixelCounter = 0;
                xyCoord coord = getPosition(xmax, ymax);
                int pixel = selectedIMG.getPixel(coord.x, coord.y);
                String value = "";

                //Retrieve the information from the image
                while(!value.equals("00000011")) {
                    StringBuilder ascii = new StringBuilder();
                    for (int j = 0; j < 8; j++) {

                        if (pixelCounter == 3) {
                            pixelCounter = 0;
                            coord = getPosition(xmax, ymax);
                            pixel = selectedIMG.getPixel(coord.x, coord.y);
                        }

                        switch (pixelCounter) {
                            case 0:
                                ascii.append(Color.red(pixel) % 2);
                                pixelCounter++;
                                break;
                            case 1:
                                ascii.append(Color.green(pixel) % 2);
                                pixelCounter++;
                                break;
                            case 2:
                                ascii.append(Color.blue(pixel) % 2);
                                pixelCounter++;
                                break;
                        }
                    }
                    value = ascii.toString();

                    //Until the EOT value is found continue to put the character into the file
                    if(!value.equals("00000011")){
                        try {
                            writer.append((char)Integer.parseInt(value,2));
                        } catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }

            }
            //If the user provides the wrong pin number
            else {
                try {
                    writer.append("INCORRECT PIN!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //If the image is not watermarked
        else {
            try {
                writer.append("THE SELECTED IMAGE IS NOT ENCRYPTED!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Close the file and return the path name
        try {
            writer.close();
        }catch (IOException e){
            e.printStackTrace();
        }

        return filePath.getAbsolutePath();
    }

    //This function will encrypt the given String into the provided picture
    private void encrypt(int xmax, int ymax, Bitmap image, String message){
        int pixelCounter = 0;
        xyCoord coord = getPosition(xmax, ymax);
        int pixel = image.getPixel(coord.x, coord.y);
        int r = Color.red(pixel), g = Color.green(pixel), b = Color.blue(pixel);


        for(int i = 0; i < message.length(); i++){

            if(pixelCounter == 3) {
                image.setPixel(coord.x, coord.y, Color.argb(255,r,g,b));

                pixelCounter = 0;

                coord = getPosition(xmax, ymax);
                pixel = image.getPixel(coord.x, coord.y);
                r = Color.red(pixel); g = Color.green(pixel); b = Color.blue(pixel);
            }

            switch (pixelCounter){
                case 0:
                    r = (Color.red(pixel) & 254) | ((int)message.charAt(i) - '0');
                    pixelCounter++;
                    break;
                case 1:
                    g = (Color.green(pixel) & 254) | ((int)message.charAt(i) - '0');
                    pixelCounter++;
                    break;
                case 2:
                    b = (Color.blue(pixel) & 254) | ((int)message.charAt(i) - '0');
                    pixelCounter++;
                    break;
            }

            if(i+1 == message.length()){
                image.setPixel(coord.x, coord.y, Color.argb(255,r,g,b));
            }

        }
    }

    //Encrypt the provided byte array into the image
    private void encrypt(int xmax, int ymax, Bitmap image, byte[] bytes){
        int pixelCounter = 0;
        xyCoord coord = getPosition(xmax, ymax);
        int pixel = image.getPixel(coord.x, coord.y);
        int r = Color.red(pixel), g = Color.green(pixel), b = Color.blue(pixel);

        for(int i = 0; i <= bytes.length; i++){

            int val = 0;
            if(i < bytes.length) {
                val = bytes[i];
            }
            else if (i == bytes.length) {
                val = 3;
            }

            for(int j = 0; j < 8; j++) {
                if (pixelCounter == 3) {
                    image.setPixel(coord.x, coord.y, Color.argb(255, r, g, b));

                    pixelCounter = 0;

                    coord = getPosition(xmax, ymax);
                    pixel = image.getPixel(coord.x, coord.y);

                    r = Color.red(pixel);
                    g = Color.green(pixel);
                    b = Color.blue(pixel);

                }

                switch (pixelCounter) {
                    case 0:
                        r = (Color.red(pixel) & 254) | ((val & 128) == 0 ? 0 : 1);
                        val <<= 1;
                        pixelCounter++;
                        break;
                    case 1:
                        g = (Color.green(pixel) & 254) | ((val & 128) == 0 ? 0 : 1);
                        val <<= 1;
                        pixelCounter++;
                        break;
                    case 2:
                        b = (Color.blue(pixel) & 254) | ((val & 128) == 0 ? 0 : 1);
                        val <<= 1;
                        pixelCounter++;
                        break;
                }

                if (i + 1 > bytes.length) {
                    image.setPixel(coord.x, coord.y, Color.argb(255, r, g, b));
                }
            }

        }
    }

    //Get a new set of coordnates that haven't been used
    private xyCoord getPosition(int xmax, int ymax) {
        int newX, newY;
        newX = rand.nextInt(xmax);
        newY = rand.nextInt(ymax);
        xyCoord position = new xyCoord(newX, newY);
        if (!isUsed.contains(position)) {
            isUsed.add(position);
            return position;
        } else {
            return getPosition(xmax, ymax);
        }
    }

    //This function will convert a string into binary
    private static String asciiToBinary(String str){
        //Create a byte array of the bytes in the string
        byte[] bytes = str.getBytes();

        //Create a string builder to construct the string of the binary value
        StringBuilder binary = new StringBuilder();

        //For all the bytes in bytes
        for (byte b : bytes){
            int val = b;

            for (int i = 0; i < 8; i++){
                binary.append((val & 128) == 0 ? 0 : 1);
                val <<= 1;
            }
        }
        return binary.toString();
    }

    //This function will convert the provided file information into a byte array
    private byte[] fileToByte(File file){
        try {
            FileInputStream input = new FileInputStream(file);

            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];

            try {
                for (int numRead; (numRead = input.read(buff)) != -1; ) {
                    byteOutputStream.write(buff, 0, numRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return byteOutputStream.toByteArray();

        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        return null;
    }

    //This function will test the provided image for the watermark and determine if it is encrypted
    private Boolean isWatermarked(int xmax, int ymax, Bitmap image){
        StringBuilder ascii = new StringBuilder();
        int pixelCounter = 0;
        xyCoord coord = getPosition(xmax, ymax);
        int pixel = image.getPixel(coord.x, coord.y);

        for(int i = 0; i < watermark.length(); i++){

            if(pixelCounter == 3) {
                pixelCounter = 0;
                coord = getPosition(xmax, ymax);
                pixel = image.getPixel(coord.x, coord.y);
            }

            switch (pixelCounter){
                case 0:
                    ascii.append(Color.red(pixel) % 2);
                    pixelCounter++;
                    break;
                case 1:
                    ascii.append(Color.green(pixel) % 2);
                    pixelCounter++;
                    break;
                case 2:
                    ascii.append(Color.blue(pixel) % 2);
                    pixelCounter++;
                    break;
            }

        }
        String mark = ascii.toString();
        if(watermark.equals(mark))
            return true;
        else
            return false;
    }

    //Check if the pin that the user provided is the one in the image
    private Boolean verifyPin(int xmax, int ymax, Bitmap image, String pinNumber){
        StringBuilder ascii = new StringBuilder();
        int pixelCounter = 0;
        xyCoord coord = getPosition(xmax, ymax);
        int pixel = image.getPixel(coord.x, coord.y);

        for(int i = 0; i < pinNumber.length(); i++){

            if(pixelCounter == 3) {
                pixelCounter = 0;
                coord = getPosition(xmax, ymax);
                pixel = image.getPixel(coord.x, coord.y);
            }

            switch (pixelCounter){
                case 0:
                    ascii.append(Color.red(pixel) % 2);
                    pixelCounter++;
                    break;
                case 1:
                    ascii.append(Color.green(pixel) % 2);
                    pixelCounter++;
                    break;
                case 2:
                    ascii.append(Color.blue(pixel) % 2);
                    pixelCounter++;
                    break;
            }

        }

        String pin = ascii.toString();
        if(pinNumber.equals(pin))
            return true;
        else
            return false;
    }

}
