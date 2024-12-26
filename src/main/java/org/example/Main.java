package org.example;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static final String SOURCE_FILE_1="src/main/resources/1.jpg";
    public static final String SOURCE_FILE_2="src/main/resources/2.jpg";
    public static final String DESTINATION_FILE_1="src/main/resources/out/1.jpg";
    public static final String DESTINATION_FILE_2="src/main/resources/out/2.jpg";

    public static void main(String[] args) throws IOException {
        Instant start = Instant.now();

        BufferedImage originalImage1 = ImageIO.read(new File(SOURCE_FILE_1));
        // creating the frame for the image
        BufferedImage resultImage1 = new BufferedImage(originalImage1.getWidth(), originalImage1.getHeight(), BufferedImage.TYPE_INT_RGB);
//        recolorSingelThreaded(originalImage1, resultImage1);
        recolorMultiThreaded(originalImage1, resultImage1, 8);
        File outputFile = new File(DESTINATION_FILE_1);
        ImageIO.write(resultImage1, "jpg", outputFile);

        BufferedImage originalImage2 = ImageIO.read(new File(SOURCE_FILE_2));
        BufferedImage resultImage2 = new BufferedImage(originalImage2.getWidth(), originalImage2.getHeight(), BufferedImage.TYPE_INT_RGB);
//        recolorSingelThreaded(originalImage2, resultImage2);
        recolorMultiThreaded(originalImage1, resultImage1, 8);
        File outputFile2 = new File(DESTINATION_FILE_2);
        ImageIO.write(resultImage2, "jpg", outputFile2);

        Instant stop = Instant.now();

        System.out.println("Duretioan is MS: "+(stop.toEpochMilli() -start.toEpochMilli()));
    }

    public static void recolorMultiThreaded(BufferedImage originalImage, BufferedImage resultImage, int noOfThreads) {
        List<Thread> threads = new ArrayList<>();
        int width = originalImage.getWidth();
        int height = originalImage.getHeight()/noOfThreads;
        for (int i = 0; i < noOfThreads; i++) {
            final int threadMultiplyer = i;
            Thread thread = new Thread(() -> {
                int leftCorner = 0;
                int topCorner = height * threadMultiplyer;
                recolorImage(originalImage, resultImage, leftCorner, topCorner,height,width);
            });
            threads.add(thread);
        }
        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public static void recolorSingelThreaded(BufferedImage originalImage, BufferedImage resultImage) {
        recolorImage(originalImage, resultImage, 0, 0,originalImage.getHeight(),originalImage.getWidth());
    }


    public static void recolorImage(BufferedImage originalImage, BufferedImage resultImage, int letCorner, int topCorner,
                                    int height, int width){
        for (int x = letCorner; x<letCorner+width && x<originalImage.getWidth(); x++) {
            for (int y = topCorner; y<topCorner+height && y<originalImage.getHeight(); y++) {
                recolorPixel(originalImage, resultImage,x,y);
            }
        }

    }



    public static int getBlue(int rgb) {
        return (rgb & 0x000000FF) ;
    }
    public static int getGreen(int rgb) {
        return (rgb & 0x0000FF00) >> 8;
    }
    public static int getRed(int rgb) {
        return (rgb & 0x00FF0000) >> 16;
    }

    public static int createRGBOutOfColors(int red, int green, int blue) {
        int rgb = 0;

        rgb |= blue;
        rgb |= green << 8; // shift the green value 1 byte or 8 bits to the left and OR operation
        rgb |= red << 16; // shift the red value 2 bytes or 16 bits to the left and OR operation

        rgb |= 0xFF000000; //Alpha
        return rgb;
    }

    public static boolean isShadeGray(int red, int green, int blue) {
        return Math.abs(red-green) < 50 && Math.abs(red-blue) < 28 && Math.abs(green -blue) < 50;
    }

    public static void recolorPixel(BufferedImage originalImage, BufferedImage resultImage, int xCoordinate, int yCoordinate) {
        // get the rgb value of the component
        int rgb = originalImage.getRGB(xCoordinate, yCoordinate);

        //get blue, green and  red value from rgb value
        int blue = getBlue(rgb);
        int green = getGreen(rgb);
        int red = getRed(rgb);

        // variables for the new color value of the pixel
        int newBlue;
        int newGreen;
        int newRed;
        // for shade we are going to
        // increase the red value 10 and checking the the value does not surpass 255 rgb value
        // decrease green by 50 and make sure not less than 0
        if (isShadeGray(red,green,blue)){
            newRed = Math.max(0, red - 15);
            newGreen = Math.max(0, green - 50);
            newBlue = Math.min(255, blue + 30);
        } else {
            newRed = red;
            newGreen = green;
            newBlue = blue;
        }

        int newRGB = createRGBOutOfColors(newRed, newGreen, newBlue);
        setRGB(resultImage,xCoordinate,yCoordinate,newRGB);

    }

    public static void setRGB(BufferedImage image, int xCoordinate, int yCoordinate, int rgb){
        image.getRaster().setDataElements(xCoordinate, yCoordinate, image.getColorModel().getDataElements(rgb, null));
    }
}