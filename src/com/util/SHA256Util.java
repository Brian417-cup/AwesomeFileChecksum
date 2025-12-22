package com.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class SHA256Util {

    //    缓冲区大小为8MB
    public static final int BUFFER_SIZE = 8 * 1024 * 1024;

    /**
     * 计算文件的 SHA-256 校验和
     */
    public static String getFileCheckSum(String filePath) {
        if (filePath == null) return null;

        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            return null;
        }

        // 使用 try-with-resources 确保流被正确关闭
        try (InputStream is = new FileInputStream(file)) {
            // 只需将算法改为 SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] buffer = new byte[SHA256Util.BUFFER_SIZE]; // 8KB 缓冲区
            int read;
            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }

            byte[] hashBytes = digest.digest();

            // 转换为十六进制字符串 (大写)
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                // %02X 表示十六进制，不足两位补0
                sb.append(String.format("%02X", b));
            }
            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 比较两个文件的 SHA-256 是否相同
     */
    public static boolean isFileEqual(String filePath1, String filePath2) {
        if (filePath1 == null || filePath2 == null || filePath1.isEmpty() || filePath2.isEmpty()) {
            return false;
        }

        String checkSum1 = getFileCheckSum(filePath1);
        String checkSum2 = getFileCheckSum(filePath2);

        if (checkSum1 == null || checkSum2 == null) {
            return false;
        }

        return checkSum1.equalsIgnoreCase(checkSum2);
    }
}