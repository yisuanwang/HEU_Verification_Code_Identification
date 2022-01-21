package com.cjh.codedistinguish;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CodeIdentification {
    /**
     * 将验证码分割成四个数字bufferedimage
     *
     * @param path
     * @return
     * @throws IOException
     */
    private static List<BufferedImage> splitImage(String path) throws IOException {
        int PIX_X = 19, PIX_Y = 5;//开始的像素位置
        final int UNIT_W = 10, UNIT_H = 16;//每个字符位置
        File file = new File(path);
        BufferedImage bufferedImage = ImageIO.read(file);
        List<BufferedImage> subImg_list = new ArrayList<BufferedImage>();
        for (int k = 0; k < 4; k++) {
            subImg_list.add(bufferedImage.getSubimage(PIX_X, PIX_Y, UNIT_W, UNIT_H));
            PIX_X += UNIT_W;
        }
        return subImg_list;
    }

    /**
     * 图片锐化方法
     *
     * @param originalPic
     * @return
     */
    public static BufferedImage getSharperPicture(BufferedImage originalPic) {
        int imageWidth = originalPic.getWidth();
        int imageHeight = originalPic.getHeight();
        BufferedImage newPic = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_3BYTE_BGR);
//        float[] data = {-1.0f, -1.0f, -1.0f, -1.0f, 10.0f, -1.0f, -1.0f, -1.0f, -1.0f};
//        float edge = -0.44f, center = 4.5f;

//        float edge = -1.0f, center = 9.0f;
//        float[] data = {
//                edge, edge, edge,
//                edge, center, edge,
//                edge, edge, edge};

        float edge = -1.0f, center = 25.0f;
        float[] data = {
                edge, edge, edge, edge, edge,
                edge, edge, edge, edge, edge,
                edge, edge, center, edge, edge,
                edge, edge, edge, edge, edge,
                edge, edge, edge, edge, edge
        };
        int size = (int) Math.sqrt(data.length);
        Kernel kernel = new Kernel(size, size, data);

        ConvolveOp co = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        co.filter(originalPic, newPic);
        return newPic;
    }

    /**
     * 颜色分量转换为RGB值
     *
     * @param alpha
     * @param red
     * @param green
     * @param blue
     * @return
     */
    private static int colorToRGB(int alpha, int red, int green, int blue) {

        int newPixel = 0;
        newPixel += alpha;
        newPixel = newPixel << 8;
        newPixel += red;
        newPixel = newPixel << 8;
        newPixel += green;
        newPixel = newPixel << 8;
        newPixel += blue;

        return newPixel;

    }

    /**
     * 转为灰度图片
     *
     * @param bufferedImage
     * @return
     * @throws Exception
     */
    public static BufferedImage grayImage(BufferedImage bufferedImage) throws Exception {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        BufferedImage grayBufferedImage = new BufferedImage(width, height, bufferedImage.getType());
        for (int i = 0; i < bufferedImage.getWidth(); i++) {
            for (int j = 0; j < bufferedImage.getHeight(); j++) {
                final int color = bufferedImage.getRGB(i, j);
                final int r = (color >> 16) & 0xff;
                final int g = (color >> 8) & 0xff;
                final int b = color & 0xff;
                int gray = (int) ((b * 29 + g * 150 + r * 77 + 128) >> 8);
//                int gray = (int) ((r + g + b) / 3.0);
//                int gray = (int) (0.3 * r + 0.59 * g + 0.11 * b);
//                int gray = (int) (0.45 * r + 0.1 * g + 0.45 * b);
                int newPixel = colorToRGB(255, gray, gray, gray);
                grayBufferedImage.setRGB(i, j, newPixel);
            }
        }
        return grayBufferedImage;
    }

    /**
     * 图片二值化方法 背景白色
     *
     * @param image
     * @return
     * @throws Exception
     */
    public static BufferedImage binaryImage(BufferedImage image) throws Exception {
        int w = image.getWidth();
        int h = image.getHeight();
        float[] rgb = new float[3];
        double[][] zuobiao = new double[w][h];
        int black = new Color(0, 0, 0).getRGB();
        int white = new Color(255, 255, 255).getRGB();
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int pixel = image.getRGB(x, y);
                rgb[0] = (pixel & 0xff0000) >> 16;
                rgb[1] = (pixel & 0xff00) >> 8;
                rgb[2] = (pixel & 0xff);
                float avg = (rgb[0] + rgb[1] + rgb[2]) / 3;
                zuobiao[x][y] = avg;

            }
        }
        //这里是阈值，白底黑字还是黑底白字，大多数情况下建议白底黑字，后面都以白底黑字为例
        double SW = 235;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (zuobiao[x][y] < SW) {
                    bi.setRGB(x, y, black);
                } else {
                    bi.setRGB(x, y, white);
                }
            }
        }
        return bi;
    }

    /**
     * 降噪，以1个像素点为单位（实际使用中可以循环降噪，或者把单位可以扩大为多个像素点）
     *
     * @param image
     * @return
     */
    public static BufferedImage denoise(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        int white = new Color(255, 255, 255).getRGB();

        if (isWhite(image.getRGB(1, 0)) && isWhite(image.getRGB(0, 1)) && isWhite(image.getRGB(1, 1))) {
            image.setRGB(0, 0, white);
        }
        if (isWhite(image.getRGB(w - 2, 0)) && isWhite(image.getRGB(w - 1, 1)) && isWhite(image.getRGB(w - 2, 1))) {
            image.setRGB(w - 1, 0, white);
        }
        if (isWhite(image.getRGB(0, h - 2)) && isWhite(image.getRGB(1, h - 1)) && isWhite(image.getRGB(1, h - 2))) {
            image.setRGB(0, h - 1, white);
        }
        if (isWhite(image.getRGB(w - 2, h - 1)) && isWhite(image.getRGB(w - 1, h - 2)) && isWhite(image.getRGB(w - 2, h - 2))) {
            image.setRGB(w - 1, h - 1, white);
        }

        for (int x = 1; x < w - 1; x++) {
            int y = 0;
            if (isBlack(image.getRGB(x, y))) {
                int size = 0;
                if (isWhite(image.getRGB(x - 1, y))) {
                    size++;
                }
                if (isWhite(image.getRGB(x + 1, y))) {
                    size++;
                }
                if (isWhite(image.getRGB(x, y + 1))) {
                    size++;
                }
                if (isWhite(image.getRGB(x - 1, y + 1))) {
                    size++;
                }
                if (isWhite(image.getRGB(x + 1, y + 1))) {
                    size++;
                }
                if (size >= 5) {
                    image.setRGB(x, y, white);
                }
            }
        }
        for (int x = 1; x < w - 1; x++) {
            int y = h - 1;
            if (isBlack(image.getRGB(x, y))) {
                int size = 0;
                if (isWhite(image.getRGB(x - 1, y))) {
                    size++;
                }
                if (isWhite(image.getRGB(x + 1, y))) {
                    size++;
                }
                if (isWhite(image.getRGB(x, y - 1))) {
                    size++;
                }
                if (isWhite(image.getRGB(x + 1, y - 1))) {
                    size++;
                }
                if (isWhite(image.getRGB(x - 1, y - 1))) {
                    size++;
                }
                if (size >= 5) {
                    image.setRGB(x, y, white);
                }
            }
        }

        for (int y = 1; y < h - 1; y++) {
            int x = 0;
            if (isBlack(image.getRGB(x, y))) {
                int size = 0;
                if (isWhite(image.getRGB(x + 1, y))) {
                    size++;
                }
                if (isWhite(image.getRGB(x, y + 1))) {
                    size++;
                }
                if (isWhite(image.getRGB(x, y - 1))) {
                    size++;
                }
                if (isWhite(image.getRGB(x + 1, y - 1))) {
                    size++;
                }
                if (isWhite(image.getRGB(x + 1, y + 1))) {
                    size++;
                }
                if (size >= 5) {
                    image.setRGB(x, y, white);
                }
            }
        }

        for (int y = 1; y < h - 1; y++) {
            int x = w - 1;
            if (isBlack(image.getRGB(x, y))) {
                int size = 0;
                if (isWhite(image.getRGB(x - 1, y))) {
                    size++;
                }
                if (isWhite(image.getRGB(x, y + 1))) {
                    size++;
                }
                if (isWhite(image.getRGB(x, y - 1))) {
                    size++;
                }
                //斜上下为空时，去掉此点
                if (isWhite(image.getRGB(x - 1, y + 1))) {
                    size++;
                }
                if (isWhite(image.getRGB(x - 1, y - 1))) {
                    size++;
                }
                if (size >= 5) {
                    image.setRGB(x, y, white);
                }
            }
        }

        //降噪，以1个像素点为单位
        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                if (isBlack(image.getRGB(x, y))) {
                    int size = 0;
                    //上下左右均为空时，去掉此点
                    if (isWhite(image.getRGB(x - 1, y))) {
                        size++;
                    }
                    if (isWhite(image.getRGB(x + 1, y))) {
                        size++;
                    }
                    //上下均为空时，去掉此点
                    if (isWhite(image.getRGB(x, y + 1))) {
                        size++;
                    }
                    if (isWhite(image.getRGB(x, y - 1))) {
                        size++;
                    }
                    //斜上下为空时，去掉此点
                    if (isWhite(image.getRGB(x - 1, y + 1))) {
                        size++;
                    }
                    if (isWhite(image.getRGB(x + 1, y - 1))) {
                        size++;
                    }
                    if (isWhite(image.getRGB(x + 1, y + 1))) {
                        size++;
                    }
                    if (isWhite(image.getRGB(x - 1, y - 1))) {
                        size++;
                    }
                    if (size >= 8) {
                        image.setRGB(x, y, white);
                    }
                }
            }
        }

        return image;
    }

    public static boolean isBlack(int colorInt) {
        Color color = new Color(colorInt);
        if (color.getRed() + color.getGreen() + color.getBlue() <= 300) {
            return true;
        }
        return false;
    }

    public static boolean isWhite(int colorInt) {
        Color color = new Color(colorInt);
        if (color.getRed() + color.getGreen() + color.getBlue() > 300) {
            return true;
        }
        return false;
    }

    // 转字符串
    private static String getString(int[][] imgArr) {
        StringBuilder s = new StringBuilder();
        int unitHeight = imgArr.length;
        int unitWidth = imgArr[0].length;
        for (int y = 0; y < unitHeight; ++y) {
            for (int x = 0; x < unitWidth; ++x) {
                s.append(imgArr[y][x]);
            }
        }
        return s.toString();
    }

    // 相同大小直接对比
    private static int comparedText(String s1, String s2) {
        int n = s1.length();
        int percent = 0;
        for (int i = 0; i < n; ++i) {
            if (s1.charAt(i) == s2.charAt(i)) percent++;
        }
        return percent;
    }

    /**
     * 匹配识别
     *
     * @param imgArr
     * @return
     */
    private static String matchCode(int[][] imgArr) {
        StringBuilder s = new StringBuilder();
        Map<String, String> charMap = CharMap.getCharMap();
        int maxMatch = 0;
        String tempRecord = "";
        for (Map.Entry<String, String> m : charMap.entrySet()) {
            int percent = comparedText(getString(imgArr), m.getValue());
            if (percent > maxMatch) {
                maxMatch = percent;
                tempRecord = m.getKey();
            }
        }
        s.append(tempRecord);
        return s.toString();
    }

    /**
     * 图片到二维01数组
     *
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
     * 验证码识别接口
     *
     * @param path
     * @return
     */
    public static String Distinguish(String path) {
        StringBuilder stringBuilder = new StringBuilder("");
        try {
            List<BufferedImage> bi_list = splitImage(path);
            for (int i = 0; i < bi_list.size(); i++) {
                BufferedImage bufferedImage = bi_list.get(i);
                bufferedImage = getSharperPicture(bufferedImage);
                bufferedImage = grayImage(bufferedImage);
                bufferedImage = binaryImage(bufferedImage);
                bufferedImage = denoise(bufferedImage);
                int[][] imgArr = binaryImg(bufferedImage); // 二值化
                stringBuilder.append(matchCode(imgArr));
            }
        } catch (Exception e) {
            System.err.println(e);
        }
        return stringBuilder.toString();
    }

    /**
     * 获取路径下的所有文件/文件夹
     *
     * @param directoryPath  需要遍历的文件夹路径
     * @param isAddDirectory 是否将子文件夹的路径也添加到list集合中
     * @return
     */
    private static List<String> getAllFile(String directoryPath, boolean isAddDirectory) {
        List<String> list = new ArrayList<String>();
        File baseFile = new File(directoryPath);
        if (baseFile.isFile() || !baseFile.exists()) {
            return list;
        }
        File[] files = baseFile.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                if (isAddDirectory) {
                    list.add(file.getAbsolutePath());
                }
                list.addAll(getAllFile(file.getAbsolutePath(), isAddDirectory));
            } else {
                list.add(file.getAbsolutePath());
            }
        }
        return list;
    }


    //5149 3365 要识别的验证码
//    private static String url2 = + i + ".png";

    public static void main(String[] args) {
        /*
        训练，文件名为正确的答案
         */
        List<String> paths = getAllFile("data\\testData\\", false);
        int cnt=0,correct=0;
        for (String path : paths) {
            String filename = (new File(path).getName()).substring(0,4);
            System.out.print(filename);
            String res = Distinguish(path);
            System.out.println("\tans = " + res);
            correct += filename.endsWith(res)?1:0;
            cnt++;
        }
        System.out.println("识别正确率: "+((float)correct/(float) cnt)*100 + "%");

    }
}
