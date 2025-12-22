package com.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class MD5Util {

    //    缓冲区大小为8MB
    public static final int BUFFER_SIZE = 8 * 1024 * 1024;

    /**
     * 计算文件的 MD5 校验和
     */
    public static String getFileCheckSum(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }

        // 使用 try-with-resources 自动关闭文件流
        try (InputStream is = new FileInputStream(file)) {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[MD5Util.BUFFER_SIZE]; // 8KB 缓冲区
            int read;
            while ((read = is.read(buffer)) != -1) {
                md5.update(buffer, 0, read);
            }
            byte[] hashBytes = md5.digest();

            // 将字节数组转换为十六进制字符串 (类似 C# 的 BitConverter)
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02X", b));
            }
            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 比较两个文件是否相同
     */
    public static boolean isFileEqual(String filePath1, String filePath2) {
        if (filePath1 == null || filePath1.isEmpty() || filePath2 == null || filePath2.isEmpty()) {
            return false;
        }

        String file1CheckSum = getFileCheckSum(filePath1);
        String file2CheckSum = getFileCheckSum(filePath2);

        if (file1CheckSum == null || file2CheckSum == null) {
            return false;
        }

        return file1CheckSum.equalsIgnoreCase(file2CheckSum);
    }
}
