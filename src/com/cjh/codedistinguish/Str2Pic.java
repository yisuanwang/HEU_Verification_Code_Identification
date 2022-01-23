package com.cjh.codedistinguish;

import org.w3c.dom.css.RGBColor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class Str2Pic {

    //w = 10 h = 16
    private static int[][] str2int(String str) {
        int[][] imgArr = new int[16][10];
        int idx = 0;
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 10; j++) {
                if (str.charAt(idx) == '1') {
                    imgArr[i][j] = 1;
                } else {
                    imgArr[i][j] = 0;
                }
                idx++;
            }
        }
        return imgArr;
    }

    private static BufferedImage Arr2buf(int imgArr[][]) {
        BufferedImage bufferedImage = new BufferedImage(10, 16, BufferedImage.TYPE_BYTE_BINARY);
        for (int x = 0; x < 16; ++x) {
            for (int y = 0; y < 10; ++y) {
                if (imgArr[x][y] == 0) {
                    bufferedImage.setRGB(y,x, Color.BLACK.getRGB());
                } else {
                    bufferedImage.setRGB(y,x, Color.WHITE.getRGB());
                }
            }
        }
        return bufferedImage;
    }

    private static void writeImage(BufferedImage sourceImg, String filename) {
        File imageFile = new File(filename);
        try (FileOutputStream outStream = new FileOutputStream(imageFile)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(sourceImg, "png", out);
            byte[] data = out.toByteArray();
            outStream.write(data);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }


    public static void main(String[] args) {
        String str_sb = "1111111111100000000110000000011000000001100000000110000000011000000001100000000110000000011000000001100000000110000000011000000001100000000110000000011111111111";
        String str_bz = "1111111111111000011110000000111001111001101111100110111110011011111001100111001110000000111110000111111110011111110011111111001111111001111111101111111111111111";

        BufferedImage bufferedImage = Arr2buf(str2int(str_bz));
        writeImage(bufferedImage, "data\\0.png");


        BufferedImage bufferedImage2 = Arr2buf(str2int(str_sb));
        writeImage(bufferedImage2, "data\\1.png");
    }
}
