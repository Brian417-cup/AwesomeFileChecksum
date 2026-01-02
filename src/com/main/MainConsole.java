package com.main;

import com.util.*;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class MainConsole {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== 文件校验和批量处理工具（控制台版） ===\n");

        // 1. 输入基目录
        String baseDir = promptDirectory("请输入要扫描的文件夹路径");
        if (baseDir == null) return;

        // 2. 选择算法
        String algorithm = promptAlgorithm();
        if (algorithm == null) return;

        // 3. 输入输出 CSV 路径（带默认建议）
        String defaultCsv = Paths.get(baseDir).getFileName() + "_checksums.csv";
        String outputCsv = promptOutputCsv("请输入结果 CSV 文件路径（回车使用默认）", defaultCsv);
        if (outputCsv == null) return;

        // 4. 确认执行
        System.out.println("\n--- 配置摘要 ---");
        System.out.println("扫描目录 : " + baseDir);
        System.out.println("校验算法 : " + algorithm);
        System.out.println("输出文件 : " + outputCsv);
        System.out.println("----------------\n");

        if (!promptConfirm("确认开始处理？(y/n)")) {
            System.out.println("操作已取消");
            return;
        }

        // 5. 执行处理
        try {
            List<String> files = FileUtil.findAllFiles(baseDir);
            System.out.println("\n共找到 " + files.size() + " 个文件，开始计算校验和...\n");

            Path outputPath = Paths.get(outputCsv).toAbsolutePath().normalize();
            Files.createDirectories(outputPath.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
                writer.write('\ufeff');
                writer.write("文件路径,校验和(" + algorithm + ")");
                writer.newLine();

                int success = 0, error = 0;
                for (int i = 0; i < files.size(); i++) {
                    String filePath = files.get(i);
                    String checksum;

                    if ("MD5".equals(algorithm)) {
                        checksum = MD5Util.getFileCheckSum(filePath);
                    } else {
                        checksum = SHA256Util.getFileCheckSum(filePath);
                    }

                    if (checksum == null) {
                        checksum = "ERROR";
                        error++;
                    } else {
                        success++;
                    }

                    String line = CSVUtil.formatCsvValue(filePath) + "," + CSVUtil.formatCsvValue(checksum);
                    writer.write(line);
                    writer.newLine();

                    // 进度提示（每10个或最后一个）
                    if (i % 10 == 0 || i == files.size() - 1) {
                        System.out.printf("\r已处理: %d/%d (成功: %d, 失败: %d)",
                                i + 1, files.size(), success, error);
                    }
                }
                System.out.println("\n\n✅ 处理完成！结果已保存至: " + outputPath);
            }

        } catch (Exception e) {
            System.err.println("\n❌ 处理失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ———————— 辅助交互方法 ————————

    private static String promptDirectory(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("❌ 路径不能为空，请重新输入");
                continue;
            }

            input = PathUtil.sanitizePath(input);
            Path path = Paths.get(input).toAbsolutePath().normalize();
            if (!Files.exists(path)) {
                System.out.println("❌ 路径不存在: " + path);
                continue;
            }
            if (!Files.isDirectory(path)) {
                System.out.println("❌ 该路径不是一个文件夹: " + path);
                continue;
            }
            return path.toString();
        }
    }

    private static String promptAlgorithm() {
        while (true) {
            System.out.println("\n请选择校验算法:");
            System.out.println("1) MD5");
            System.out.println("2) SHA-256");
            System.out.print("请输入选项 (1 或 2): ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    return "MD5";
                case "2":
                    return "SHA256";
                default:
                    System.out.println("❌ 无效选项，请输入 1 或 2");
            }
        }
    }

    private static String promptOutputCsv(String prompt, String defaultPath) {
        System.out.println("\n" + prompt + ": " + defaultPath);
        System.out.print(">");
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? defaultPath : PathUtil.sanitizePath(input);
    }

    private static boolean promptConfirm(String prompt) {
        while (true) {
            System.out.print(prompt + " ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("y") || input.equals("yes")) {
                return true;
            } else if (input.equals("n") || input.equals("no")) {
                return false;
            }
            System.out.println("请输入 y 或 n");
        }
    }
}
