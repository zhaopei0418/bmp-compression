package com.github.bmpcompression.util;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.io.*;

public class CompressUtil {

    private static final Log logger = LogFactory.getLog(CompressUtil.class);

    private static final String COMPARESS_EXT = ".IM3";

    /**
     * 获取文件名没有扩展名
     * @param pathName
     * @return
     */
    private static String getFileNameWithoutExt(String pathName) {
        int start = pathName.lastIndexOf(File.separator);
        int end = pathName.lastIndexOf(".");
        if (end != -1) {
            return pathName.substring(start + 1, end);
        }
        return null;
    }

    /**
     * 获取目录名，没有文件名
     * @param pathName
     * @return
     */
    private static String getPathNameWithoutFileName(String pathName) {
        int start = pathName.lastIndexOf(File.separator);
        if (start != -1) {
            return pathName.substring(0, start);
        }
        return null;
    }

    /**
     * 压缩文件，并返回压缩率
     * @param file
     * @return
     */
    public static double compress7zFile(File file) {
        double compressRatio = 0;
        long fileSize = file.length();
        File zoutputFile = new File(getPathNameWithoutFileName(file.getAbsolutePath()) + File.separator
                            + getFileNameWithoutExt(file.getName()) + COMPARESS_EXT);
        SevenZOutputFile sevenZOutputFile = null;
        SevenZArchiveEntry sevenZArchiveEntry = null;
        BufferedInputStream in = null;
        byte[] buffer = new byte[1024];
        int len = -1;
        try {
            sevenZOutputFile = new SevenZOutputFile(zoutputFile);
            sevenZArchiveEntry = sevenZOutputFile.createArchiveEntry(file, file.getName());
            sevenZOutputFile.putArchiveEntry(sevenZArchiveEntry);
            in = new BufferedInputStream(new FileInputStream(file));
            while ((len = in.read(buffer)) != -1) {
                sevenZOutputFile.write(buffer, 0, len);
            }
            sevenZOutputFile.closeArchiveEntry();
            compressRatio = (double) zoutputFile.length() / (double) fileSize * 100.0;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (sevenZOutputFile != null) {
                    sevenZOutputFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return compressRatio;
    }

    /**
     * 读取压缩文件，解压显示图片
     * @param frame
     * @param file
     */
    public static void readAndShow7zFile(JFrame frame, File file) {
        SevenZFile sevenZFile = null;
        SevenZArchiveEntry entry = null;
        byte[] content = null;
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            sevenZFile = new SevenZFile(file);
            entry = sevenZFile.getNextEntry();
            content = new byte[(int) entry.getSize()];
            sevenZFile.read(content);
            if (!BMPUtil.is24TrueBmp(content)) {
                JOptionPane.showMessageDialog(frame, "解压出来的文件不是24位位图!");
            } else if (!BMPUtil.vaild(new ByteArrayInputStream(content))){
                JOptionPane.showMessageDialog(frame, "位图宽，高小于等于0，或者大于2048，或者不是8的倍数!");
            } else {
                BMPUtil.resize(frame, content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (sevenZFile != null) {
                    sevenZFile.close();
                }
                if (byteArrayInputStream != null) {
                    byteArrayInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
