package com.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtil {

    /**
     * 遍历指定基地址文件夹及其所有子文件夹，找到所有文件。
     * @param baseDirectoryPath 要遍历的文件夹路径
     * @return 找到的所有文件的完整路径列表
     * @throws Exception 模仿 C# 抛出异常供 UI 层处理
     */
    public static List<String> findAllFiles(String baseDirectoryPath) throws Exception {
        // 1. 输入路径校验
        if (baseDirectoryPath == null || baseDirectoryPath.trim().isEmpty()) {
            throw new IllegalArgumentException("文件夹基地址不能为空。");
        }

        // 规范化路径并创建 Path 对象
        Path start = Paths.get(baseDirectoryPath).toAbsolutePath().normalize();

        // 2. 文件夹存在性检查
        if (!Files.exists(start)) {
            throw new IOException("指定的文件夹不存在: " + start);
        }
        if (!Files.isDirectory(start)) {
            throw new IllegalArgumentException("指定的路径不是一个文件夹: " + start);
        }

        // 3. 核心功能实现：使用 JDK 8 的 Files.walk (递归遍历)
        // try-with-resources 确保 Stream 流被关闭
        try (Stream<Path> stream = Files.walk(start)) {
            return stream
                    .filter(Files::isRegularFile) // 只保留文件，过滤掉文件夹
                    .map(Path::toString)          // 转换为字符串路径
                    .collect(Collectors.toList()); // 转换为 List
        } catch (IOException e) {
            // 4. 异常包装处理
            throw new Exception("遍历文件时发生I/O错误: " + e.getMessage(), e);
        }
    }
}
