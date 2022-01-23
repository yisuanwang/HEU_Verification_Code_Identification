package com.cjh.codedistinguish;

import cn.hutool.core.img.ImgUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class CodeIdentifyUtil {
    private static Map<String, String> charMap = new HashMap<>();

    static {
        charMap.put("0", "1111111111111000011111000000111001111011100111100110011110011011111001101111100110111110011011111001100111100110011110011101110011110000001111110011111111111111");
        charMap.put("1", "1111111111111110111111110011111110001111110000111111110011111111001111111100111111110011111111001111111100111111110011111111001111111100111111110011111111111111");
        charMap.put("2", "1111111111111000011111000000111001111001101111100111111110011111110011111111001111111001111111001111111101111111100111111100111111100000000110000000011111111111");
        charMap.put("3", "1111111111110000011111001100111001111001101111100111111110111111110011111100011111111100111111111001111111100110111110011001111011110000001111100011111111111111");
        charMap.put("4", "1111111111111111001111111100111111100011111100001111110100111110010011110011001110011100111011110011100000000110000000011111110011111111001111111100111111111111");
        charMap.put("5", "1111111111110000001111000000111101111111110111111110011111111000000111100111001111111110011111111001111111100110111110011011110011100000001111100011111111111111");
        charMap.put("6", "1111111111111110011111111011111111001111111001111111101111111100000011100011000110011110011001111101101111110110011111011001111001100000001111000001111111111111");
        charMap.put("7", "1111111111100000000110000000011111111001111111101111111100111111110111111110011111111011111111001111111101111111110111111110011111111001111111101111111111111111");
        charMap.put("8", "1111111111111000011111001000111001111011101111100110011110111000110011110000011110001100111011111001101111100110111110011001111001100000001111100001111111111111");
        charMap.put("9", "1111111111111000011110000000111001111001101111100110111110011011111001100111001110000000111110000111111110011111110011111111001111111001111111101111111111111111");
    }

    /**
     * 将验证码分割成四个数字bufferedimage
     *
     * @param bufferedImage
     * @return
     * @throws IOException
     */
    private final static int SLICES = 4;// 验证码字符位数
    private final static int PIX_X = 19, PIX_Y = 5;//开始的像素位置
    private final static int UNIT_W = 10, UNIT_H = 16;//每个字符位置
    private static List<BufferedImage> splitImage(BufferedImage bufferedImage) throws IOException {
        int X = PIX_X, Y = PIX_Y;//开始的像素位置
        List<BufferedImage> subImg_list = new ArrayList<BufferedImage>();
        for (int k = 0; k < SLICES; k++) {
            subImg_list.add(bufferedImage.getSubimage(X, Y, UNIT_W, UNIT_H));
            X += UNIT_W;
        }
        return subImg_list;
    }

    /**
     * 图片锐化方法
     *
     * @param originalPic
     * @return
     */
    private static BufferedImage getSharperPicture(BufferedImage originalPic) {
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
    private static BufferedImage grayImage(BufferedImage bufferedImage) throws Exception {
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
    private static BufferedImage binaryImage(BufferedImage image) throws Exception {
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
    private static BufferedImage denoise(BufferedImage image) {
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

    private static boolean isBlack(int colorInt) {
        Color color = new Color(colorInt);
        if (color.getRed() + color.getGreen() + color.getBlue() <= 300) {
            return true;
        }
        return false;
    }

    private static boolean isWhite(int colorInt) {
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
     * @param bi 待识别的验证码 bufferedImage格式
     * @return
     */
    public static String Distinguish(BufferedImage bi) {
        StringBuilder stringBuilder = new StringBuilder("");
        try {
            List<BufferedImage> bi_list = splitImage(bi);
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
     * base64 编码转换为 BufferedImage
     *
     * @param base64
     * @return
     */
    private static BufferedImage base64ToBufferedImage(String base64) {
        Base64.Decoder decoder = Base64.getDecoder();
//        BASE64Decoder decoder = new sun.misc.BASE64Decoder();
        try {
            byte[] bytes1 = decoder.decode(base64);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes1);
            return ImageIO.read(bais);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 因为学校官网下来的是图片base64编码，所以可以直接传入base64编码
     *
     * @param base64
     * @return
     */
    public static String Distinguish(String base64) {
//        BufferedImage bufferedImage = ImgUtil.toImage(base64);
        BufferedImage bufferedImage = base64ToBufferedImage(base64);
        return Distinguish(bufferedImage);
    }

    private static void main(String[] args) {
        /**
         * 用作测试
         */
        List<String> sb = new ArrayList<>();
        //3128
        sb.add("/9j/4AAQSkZJRgABAgAAAQABAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAARCAAeAFADASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD3+iiigCK4uYbWMPM+0FgqgAksT0AA5J+lFvcw3UZeF9wDFWBBBUjqCDyD9aJYEeRZ/LV5olYRljjGev06DnFZ+mhZbrUhOuy7aQCVUJACbcJhs+mTng5PQcVzzqTjVUdLP/K++3y7a36Gbk1JLuaMU8U5kETh/LfY+OzYBx+tVv7XsPt32P7Svn7tu3Bxn0z0z/Xiqnh6JIYb+KMYRL2RVGc4Axii/tUlhi0W1i2xHa0pAwI4wc9f7xI4698+tYPEVnRjUilftq79ktvv6diOebgpIu3epWliwW4m2nbuwFLYGcZOAcDJxzVpWV0V0YMrDIYHIIrFzfx6tqT2UEUqMq5MhC4kCDABHJGCOuB15He9o7QtpFqYHZ4xGAGfOcjg9c45zx09OKujiJTquDVlr07O2/X5bFQm3Jpl2iiiuw1CiiigCpe2b3OySC4a3uI8hJFG4YOMgqeCOB+IFOs7P7L5jvIZp5SDJKygFiAB2HTjp71ZorL2MOfntr/S22vbS+5PIr8xUsbL7F9p/eb/ADp2m+7jbnHH6VnxaPqMEkskesYeVtzsbZSWP1J6eg6CtuiolhaUlFa6bWbW/oyXSi7Lt5szrjTrk3Mk9nqMluZSDIpjV1JAAGAenA59fwq1Z2kVjaR20IPloMDJyT3J/Op6KuFCEJuaWr8313t2v1tuUoRTugooorUo/9k=");
        for (String str : sb) {
            System.out.println(Distinguish(str));
        }
    }
}
