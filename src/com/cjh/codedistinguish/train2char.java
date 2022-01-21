package com.cjh.codedistinguish;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.SyncFailedException;
import java.util.ArrayList;
import java.util.List;

/**
 * 将训练的图片转化为string
 */
public class train2char {

    /**
     * 图片到二维01数组
     * @param img
     * @return
     */
    private static int[][] binaryImg(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int rgbThres = 150;//traindata一定是黑白的二值图片，所以这个值可以随意
        int[][] imgArr = new int[height][width];
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                    imgArr[y][x] = 1;
                    continue;
                }
                int pixel = img.getRGB(x, y);
                if (((pixel & 0xff0000) >> 16) < rgbThres && ((pixel & 0xff00) >> 8) < rgbThres && (pixel & 0xff) < rgbThres) {
                    imgArr[y][x] = 0;
                } else {
                    imgArr[y][x] = 1;
                }
            }
        }
        return imgArr;
    }

    /**
     * 二维数组输出为str
     * @param imgArr
     * @return
     */
    private static String Arr2Str(int imgArr[][]){
        StringBuilder stringBuilder = new StringBuilder("");
        for (int i1=0;i1<imgArr.length;i1++){
            for (int i2=0;i2<imgArr[0].length;i2++){
                stringBuilder.append(String.valueOf(imgArr[i1][i2]));
            }
        }
        return stringBuilder.toString();
    }

    public static void main(String[] args) {
        List<String> char_map = new ArrayList<String>();
        try {
            for (int i = 0; i <= 9; i++) {
                String path = "data\\trainData\\" + i + ".png";
                File file = new File(path);
                BufferedImage bufferedImage = ImageIO.read(file);
                int[][] imgArr = binaryImg(bufferedImage);
                char_map.add(Arr2Str(imgArr));
            }
        } catch (Exception e) {

        }

        for (String str:char_map){
            System.out.println(str);
        }

    }
}
