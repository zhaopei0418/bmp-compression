package com.github.bmpcompression.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Arrays;

public class BMPUtil {

    private static final Log logger = LogFactory.getLog(BMPUtil.class);

    private static byte[] bmpType = new byte[]{0x42, 0x4d};

    private static byte color24Type = 0x18;

    public static Color[][] bmpColors = null;

    private static final int SUPPLEMENT_HEIGHT = 45;

    /**
     * 根据文件获取BufferedInputStream
     * @param file
     * @return
     * @throws Exception
     */
    private static BufferedInputStream getBufferedInputStream(File file) throws IOException {
        return new BufferedInputStream(new FileInputStream(file));
    }

    /**
     * 获取文件类型
     * @param file
     * @return
     */
    private static byte[] getBmpType(File file) {
        try {
            return getBmpType(getBufferedInputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取bmp类型
     * @param in
     * @return
     */
    private static byte[] getBmpType(InputStream in) throws IOException {
        byte[] bmpType = new byte[2];
        in.read(bmpType);
        in.close();
        logger.info("bmpType=[" + Arrays.toString(bmpType) + "]");
        return bmpType;
    }

    /**
     * 获取位图颜色类型
     * @param in
     * @return
     */
    private static byte getBmpColorType(InputStream in) throws IOException {
        byte[] colorType = new byte[1];
        // 跳过指定的字节数，读取color类型
        in.skip(28);
        in.read(colorType);
        in.close();
        logger.info("colorType=[" + Arrays.toString(colorType) + "]");
        return colorType[0];
    }

    /**
     * 获取位图颜色类型
     * @param file
     * @return
     */
    private static byte getBmpColorType(File file) {
        try {
            return getBmpColorType(getBufferedInputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 判断文件是否是24位bmp图
     * @param file
     * @return
     */
    public static boolean is24TrueBmp(File file) {
        logger.info("constant bmpType=[" + Arrays.toString(bmpType) + "]");
        logger.info("constant color24Type=[" + color24Type + "]");
        return Arrays.equals(getBmpType(file), bmpType) && getBmpColorType(file) == color24Type;
    }

    /**
     * 判断文件是否是24位bmp图
     * @param content
     * @return
     */
    public static boolean is24TrueBmp(byte[] content) {
        try {
            return Arrays.equals(getBmpType(new ByteArrayInputStream(content)), bmpType)
                    && getBmpColorType(new ByteArrayInputStream(content)) == color24Type;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断位图是否有效,符合宽，高不大于2048,并且还是8的倍数
     * @param in
     * @return
     */
    public static boolean vaild(InputStream in) throws IOException {
        int[] widthHeight = getBmpWidthHeight(in);
        int width = widthHeight[0];
        int height = widthHeight[1];
        logger.info("width=[" + width + "]");
        logger.info("height=[" + height + "]");
        if (width <= 0 || height <= 0 || width > 2048 || height > 2048 ||
                width % 8 != 0 || height % 8 != 0) {
            return false;
        }
        return true;
    }

    /**
     * 判断位图是否有效,符合宽，高不大于2048,并且还是8的倍数
     * @param file
     * @return
     */
    public static boolean vaild(File file) {
        try {
            return vaild(getBufferedInputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取bmp宽度与高度
     * @param in
     * @return
     */
    public static int[] getBmpWidthHeight(InputStream in) throws IOException {
        byte[] width = new byte[4];
        byte[] height = new byte[4];
        in.skip(18);
        in.read(width);
        in.read(height);
        in.close();
        return new int[]{byteArray2Int(width), byteArray2Int(height)};
    }
    /**
     * 获取bmp宽度与高度
     * @param file
     * @return
     */
    private static int[] getBmpWidthHeight(File file) {
        try {
            return getBmpWidthHeight(getBufferedInputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 字节数据转整型
     * @param bytes
     * @return
     */
    private static int byteArray2Int(byte[] bytes) {
        return (bytes[3] & 0xff) << 24 | (bytes[2] & 0xff) << 16 | (bytes[1] & 0xff) << 8 | (bytes[0] & 0xff);
    }

    /**
     * 调整大小，读取bmp数据
     * @param frame
     * @param file
     */
    public static void resize(JFrame frame, File file) {
        int[] widthHeight = getBmpWidthHeight(file);
        bmpColors = new Color[widthHeight[1]][widthHeight[0]];
        readBmpColors(file);
        frame.setSize(widthHeight[0], widthHeight[1] + SUPPLEMENT_HEIGHT);
        frame.setLocationRelativeTo(null);
    }

    /**
     * 调整大小，读取bmp数据
     * @param frame
     * @param content
     * @throws IOException
     */
    public static void resize(JFrame frame, byte[] content) throws IOException {
        int[] widthHeight = getBmpWidthHeight(new ByteArrayInputStream(content));
        bmpColors = new Color[widthHeight[1]][widthHeight[0]];
        readBmpColors(new ByteArrayInputStream(content));
        frame.setSize(widthHeight[0], widthHeight[1] + SUPPLEMENT_HEIGHT);
        frame.setLocationRelativeTo(null);
    }

    /**
     * 绘制bmp图
     * @param g
     */
    public static void paint(Graphics g) {
        if (bmpColors == null || bmpColors.length == 0) {
            return;
        }

        for (int i = 0; i < bmpColors.length; i++) {
            for (int j = 0; j < bmpColors[1].length; j++) {
                g.setColor(bmpColors[i][j]);
                g.drawLine(j, bmpColors.length + SUPPLEMENT_HEIGHT - i, j, bmpColors.length + SUPPLEMENT_HEIGHT - i);
            }
        }
    }

    /**
     * 读取颜色数据
     * @param in
     */
    private static void readBmpColors(InputStream in) throws IOException {
        int blue = 0;
        int green = 0;
        int red = 0;
        int skipnum = 0;
        if (in == null) {
            return;
        }
        if (bmpColors[1].length * 3 % 4 != 0) {
            skipnum = 4 - bmpColors[1].length * 3 % 4;
        }
        // 跳过信息头，直接读取数据
        in.skip(54);
        for (int i = 0; i < bmpColors.length; i++) {
            for (int j = 0; j < bmpColors[1].length; j++) {
                blue = in.read();
                green = in.read();
                red = in.read();
                bmpColors[i][j] = new Color(red, green, blue);
            }

            if (skipnum != 0) {
                in.skip(skipnum);
            }
        }
        in.close();
    }

    /**
     * 读取颜色数据
     * @param file
     */
    private static void readBmpColors(File file) {
        try {
            readBmpColors(new BufferedInputStream(new FileInputStream(file)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
