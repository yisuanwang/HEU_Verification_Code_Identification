# HEU-学生登录页面的验证码识别

## [学生个人中心]https://edusys.wvpn.hrbeu.edu.cn/jsxsd/framework/xsMain.jsp

### 原理：使用Java对图片进行分割、锐化、灰度处理、二值化、降噪处理，得到训练数据(trainData)即每个数字的字模，再将字模以01二维数组方式转化为字符串存储在CharMap中。makeTrainData为制作字模的过程，选取了验证码中颜色较为深的验证码作为标准字模。再对处理过后的验证码字模与标准字模进行模匹配，取比较概率最高的输出。

### some bugs
1、运行CodeIdentification可以看到每个testData的识别效果。正确率为98%，有个阴间验证码trainData/5149.png,我也不晓得是5149还是5148，测试数据就这个错了。
👉![avatar](data/testData/5149.jpg)👈

2、对浅色图片的识别效果不佳，但是经过多次人工测试未发现错误的识别案例。
👉![avatar](data/Screenshot_1642938565.png =144x)👈
👉![avatar](data/Screenshot_1642939005.png =144x)👈
👉![avatar](data/Screenshot_1642939044.png =144x)👈
👉![avatar](data/Screenshot_1642939059.png =144x)👈
👉![avatar](data/Screenshot_1642939067.png =144x)👈

3、现在的代码只能识别固定位置的数字。HEU的验证码是最简单的一种，即只有数字0-9，位置固定，字符数为4，所以验证码图片分割split很简单。
### 制作标准字模
请尝试运行CodeIdentification查看测试案例

1、使用makeTrainData制作trainData

2、使用train2char将trainData转为String

3、CodeIdentification为测试方法

4、Str2Pic可以将str字模转化为图片
## 使用方法
已将所用功能封装在了CodeIdentifyUtil.java中，提供两个静态方法 String Distinguish(String base64) 、String Distinguish(BufferedImage bi)，传入参数为图片的base64编码或者BufferedImage对象

### 你也可以修改图片处理中的参数来达到更好的识别效果,比较重要的参数有

1、binaryImage(BufferedImage image) 二值化方法中的阈值
''' java

    //这里是阈值，白底黑字还是黑底白字，大多数情况下建议白底黑字，后面都以白底黑字为例
    double SW = 235;
2、grayImage(BufferedImage bufferedImage)灰度化方法,试了几种目前这种是最好的
''' java

    int gray = (int) ((b * 29 + g * 150 + r * 77 + 128) >> 8);
    //int gray = (int) ((r + g + b) / 3.0);
    //int gray = (int) (0.3 * r + 0.59 * g + 0.11 * b);
    //int gray = (int) (0.45 * r + 0.1 * g + 0.45 * b);
3、getSharperPicture(BufferedImage originalPic) 锐化方法中的卷积矩阵
''' java

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
    /*
    经过测试，使用5*5的卷积矩阵可以使浅色图片的识别率提高
    */
