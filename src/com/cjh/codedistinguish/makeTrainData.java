package com.cjh.codedistinguish;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class makeTrainData {
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

    private static int PIX_X = 19, PIX_Y = 5;//开始的像素位置
    private static final int UNIT_W = 10, UNIT_H = 16;//每个字符位置

    /**
     * 将验证码分割成四个数字bufferedimage
     *
     * @param path
     * @return
     * @throws IOException
     */
    private static List<BufferedImage> splitImage(String path) throws IOException {
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

    public static int isBlack(int colorInt, int whiteThreshold) {
        final Color color = new Color(colorInt);
        if (color.getRed() + color.getGreen() + color.getBlue() <= whiteThreshold) {
            return 1;
        }
        return 0;
    }

    /**
     * 图片对比度调整
     *
     * @param imgsrc
     * @param contrast
     * @return
     */
    public static BufferedImage img_color_contrast(BufferedImage imgsrc, int contrast) {
        try {
            int contrast_average = 128;
            //创建一个不带透明度的图片
            BufferedImage back = new BufferedImage(imgsrc.getWidth(), imgsrc.getHeight(), BufferedImage.TYPE_INT_RGB);
            int width = imgsrc.getWidth();
            int height = imgsrc.getHeight();
            int pix;
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int pixel = imgsrc.getRGB(j, i);
                    Color color = new Color(pixel);

                    if (color.getRed() < contrast_average) {
                        pix = color.getRed() - Math.abs(contrast);
                        if (pix < 0) pix = 0;
                    } else {
                        pix = color.getRed() + Math.abs(contrast);
                        if (pix > 255) pix = 255;
                    }
                    int red = pix;
                    if (color.getGreen() < contrast_average) {
                        pix = color.getGreen() - Math.abs(contrast);
                        if (pix < 0) pix = 0;
                    } else {
                        pix = color.getGreen() + Math.abs(contrast);
                        if (pix > 255) pix = 255;
                    }
                    int green = pix;
                    if (color.getBlue() < contrast_average) {
                        pix = color.getBlue() - Math.abs(contrast);
                        if (pix < 0) pix = 0;
                    } else {
                        pix = color.getBlue() + Math.abs(contrast);
                        if (pix > 255) pix = 255;
                    }
                    int blue = pix;

                    color = new Color(red, green, blue);
                    int x = color.getRGB();
                    back.setRGB(j, i, x);
                }
            }
            return back;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 图片亮度调整
     *
     * @param image
     * @param param
     * @throws IOException
     */
    public static BufferedImage lumAdjustment(BufferedImage image, int param) throws IOException {
        if (image == null) {
            return null;
        } else {
            int rgb, R, G, B;
            for (int i = 0; i < image.getWidth(); i++) {
                for (int j = 0; j < image.getHeight(); j++) {
                    rgb = image.getRGB(i, j);
                    R = ((rgb >> 16) & 0xff) + param;
                    G = ((rgb >> 8) & 0xff) + param;
                    B = (rgb & 0xff) + param;

                    rgb = ((clamp(255) & 0xff) << 24) | ((clamp(R) & 0xff) << 16) | ((clamp(G) & 0xff) << 8)
                            | ((clamp(B) & 0xff));
                    image.setRGB(i, j, rgb);
                }
            }
        }
        return image;
    }

    // 判断a,r,g,b值，大于256返回256，小于0则返回0,0到256之间则直接返回原始值
    private static int clamp(int rgb) {
        if (rgb > 255)
            return 255;
        if (rgb < 0)
            return 0;
        return rgb;
    }

    //5149 3365
    private static String url2 = "C:/Users/ASUS/Desktop/新建文件夹/1851.jpg";

    public static void main(String[] args) {
        try {
            List<BufferedImage> bi_list = splitImage(url2);
            for (int i = 0; i < bi_list.size(); i++) {
                BufferedImage bufferedImage = bi_list.get(i);
                writeImage(bufferedImage, "data\\split\\" + i + ".jpg");

//                bufferedImage = lumAdjustment(bufferedImage,100);
//                writeImage(bufferedImage, "data\\lumAdjustment\\" + i + ".jpg");

                bufferedImage = getSharperPicture(bufferedImage);
                writeImage(bufferedImage, "data\\sharper\\" + i + ".jpg");

//                bufferedImage = img_color_contrast(bufferedImage, 10);
//                writeImage(bufferedImage, "data\\img_color_contrast\\" + i + ".jpg");

                bufferedImage = grayImage(bufferedImage);
                writeImage(bufferedImage, "data\\gray\\" + i + ".jpg");

                bufferedImage = binaryImage(bufferedImage);
                writeImage(bufferedImage, "data\\binary\\" + i + ".jpg");

                bufferedImage = denoise(bufferedImage);
                writeImage(bufferedImage, "data\\denoise\\" + i + ".jpg");


            }
        } catch (Exception e) {

        }
    }
}
