package com.controller;

import com.bean.Message;
import com.util.FileUtil;
import com.util.MD5Util;
import com.util.SHA256Util;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

public class MainController {
    @FXML
    private TableColumn colFullPath;
    @FXML
    private TableColumn colFileName;
    @FXML
    private TableColumn colCheckSum;
    @FXML
    private TableView filePathAndCheckSumTableView;
    @FXML
    private ProgressBar progressBar1;
    @FXML
    private TextField dirChooseTextField;
    @FXML
    private ComboBox<String> checkSumAlgorithmComboBox;

    // 用来模拟 文件获取列表状态下IsBusy 状态
    private Task<List<String>> currentTask;
    // 定义一个 Task 引用，用于记录当前正在运行的任务
    private Task<Void> currentHashTask;


    @FXML
    public void initialize() {
        // 关键：将表格列与 Message 类的属性名绑定
        // 注意：这里的字符串必须和 Message 里的 getXXX 方法名后缀一致（首字母小写）
        colFullPath.setCellValueFactory(new PropertyValueFactory<>("completeFilePath"));
        colFileName.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        colCheckSum.setCellValueFactory(new PropertyValueFactory<>("checkSum"));

        // 1. 进度条初始设置
        // JavaFX 进度条范围是 0.0 到 1.0
        progressBar1.setProgress(0.0);
        progressBar1.setVisible(false); // 默认隐藏

        // 2. 校验算法 ComboBox 初始化
        checkSumAlgorithmComboBox.getItems().clear();
        checkSumAlgorithmComboBox.getItems().addAll("SHA-256", "MD5");
        checkSumAlgorithmComboBox.getSelectionModel().select(0); // 默认选中第一个

        // 3. 表格列绑定 (基于你之前的 Message 类)
        colFullPath.setCellValueFactory(new PropertyValueFactory<>("completeFilePath"));
        colFileName.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        colCheckSum.setCellValueFactory(new PropertyValueFactory<>("checkSum"));
    }


    @FXML
    private void chooseBaseDirBtnClick(ActionEvent event) {
        // 1. 创建目录选择器对象
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("请选择一个要搜索的文件夹");

        // 2. 设置初始目录 (对应 SelectedPath)
        String currentPath = dirChooseTextField.getText();
        if (currentPath != null && !currentPath.trim().isEmpty()) {
            File defaultDirectory = new File(currentPath);
            // 必须确保路径存在且是一个目录，否则会报错
            if (defaultDirectory.exists() && defaultDirectory.isDirectory()) {
                directoryChooser.setInitialDirectory(defaultDirectory);
            }
        }

        // 3. 显示对话框
        // 需要传入当前的 Stage 窗口
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File selectedDirectory = directoryChooser.showDialog(stage);

        // 4. 处理结果
        if (selectedDirectory != null) {
            dirChooseTextField.setText(selectedDirectory.getAbsolutePath());
        }
    }

    @FXML
    private void getFilePathBtnClick(ActionEvent actionEvent) {
        // 1. 检查是否正在运行 (对应 IsBusy)
        if (currentTask != null && currentTask.isRunning()) {
            new Alert(Alert.AlertType.INFORMATION, "文件查找正在进行中...").show();
            return;
        }

        // 2. 检查输入
        String path = dirChooseTextField.getText();
        if (path == null || path.trim().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "请先选择或输入文件夹路径！").show();
            return;
        }

        // 3. 创建任务 (对应 DoWork)
        currentTask = new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                // 这里在后台线程运行
                return FileUtil.findAllFiles(path);
            }
        };

        // 4. 任务开始前的准备 (UI 线程)
        progressBar1.setVisible(true);
        progressBar1.progressProperty().bind(currentTask.progressProperty()); // 绑定进度条（如果有百分比）

        // 5. 任务成功完成后的回调 (对应 RunWorkerCompleted)
        currentTask.setOnSucceeded(e -> {
            progressBar1.setVisible(false);
            List<String> files = currentTask.getValue();

            // 更新表格数据
            for (String fullPath : files) {
                File file = new File(fullPath);
                // 参数 1: 完整路径
                // 参数 2: 文件名 (从 File 对象获取)
                // 参数 3: 校验和 (初始为空字符串，等待下一步计算)
                Message msg = new Message(fullPath, file.getName(), "");

                filePathAndCheckSumTableView.getItems().add(msg);
            }

            // 释放进度条绑定并归零
            progressBar1.progressProperty().unbind();
            progressBar1.setProgress(0);

            // 弹出提示
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("查找完成");
            alert.setHeaderText(null);
            alert.setContentText("成功找到 " + files.size() + " 个文件。");
            alert.show();
        });

        // 6. 任务失败后的回调
        currentTask.setOnFailed(e -> {
            progressBar1.setVisible(false);
            Throwable ex = currentTask.getException();
            new Alert(Alert.AlertType.ERROR, "文件操作失败：" + ex.getMessage()).show();
        });

        // 7. 启动线程
        Thread thread = new Thread(currentTask);
        thread.setDaemon(true); // 设置为守护线程，主程序关闭时自动结束
        thread.start();
    }

    @FXML
    private void checkSumBtnClick(ActionEvent actionEvent) {
        // 1. 检查表格是否为空
        List<Message> items = filePathAndCheckSumTableView.getItems();
        if (items == null || items.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "文件列表为空，请先获取文件列表！").show();
            return;
        }

        // 2. 检查是否正在运行 (对应 IsBusy)
        if (currentHashTask != null && currentHashTask.isRunning()) {
            new Alert(Alert.AlertType.WARNING, "校验和计算正在进行中...").show();
            return;
        }

        // 3. 获取算法
        String selectedAlgorithm = checkSumAlgorithmComboBox.getValue();
        if (selectedAlgorithm == null) {
            new Alert(Alert.AlertType.ERROR, "请选择一个有效的哈希算法！").show();
            return;
        }

        // 4. 创建计算任务 (Task 对应 BackgroundWorker)
        Task<Void> hashTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int total = items.size();

                // 预处理：先切回 UI 线程将所有行置为 "计算中..."
                Platform.runLater(() -> {
                    for (Message m : items) {
                        m.setCheckSum("计算中...");
                    }
                    filePathAndCheckSumTableView.refresh();
                });

                for (int i = 0; i < total; i++) {
                    Message msg = items.get(i);
                    String path = msg.getCompleteFilePath();
                    String resultHash;

                    // 根据算法调用工具类
                    if ("SHA-256".equals(selectedAlgorithm)) {
                        resultHash = SHA256Util.getFileCheckSum(path);
                    } else if ("MD5".equals(selectedAlgorithm)) {
                        resultHash = MD5Util.getFileCheckSum(path);
                    } else {
                        resultHash = "不支持的算法";
                    }

                    // 更新当前行的数据
                    msg.setCheckSum(resultHash);

                    // 报告进度 (对应 ReportProgress)
                    updateProgress(i + 1, total);

                    // 强制刷新表格显示当前行的进度
                    Platform.runLater(() -> filePathAndCheckSumTableView.refresh());
                }
                return null;
            }
        };

        // 5. 绑定 UI 状态
        progressBar1.setVisible(true);
        progressBar1.progressProperty().bind(hashTask.progressProperty());

        // 6. 完成回调 (对应 RunWorkerCompleted)
        hashTask.setOnSucceeded(e -> {
            progressBar1.progressProperty().unbind();
            progressBar1.setVisible(false);
            new Alert(Alert.AlertType.INFORMATION, "校验值计算完成！").show();
        });

        hashTask.setOnFailed(e -> {
            progressBar1.progressProperty().unbind();
            progressBar1.setVisible(false);
            Throwable ex = hashTask.getException();
            new Alert(Alert.AlertType.ERROR, "计算失败: " + ex.getMessage()).show();
        });

        // 7. 启动
        Thread thread = new Thread(hashTask);
        thread.setDaemon(true);
        thread.start();
        currentHashTask = hashTask; // 保存当前引用以供后续 Busy 检查
    }

    @FXML
    private void exportToCSVBtnClick(ActionEvent actionEvent) {
        // 1. 检查表格是否有数据
        List<Message> items = filePathAndCheckSumTableView.getItems();
        if (items == null || items.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "没有数据可以导出！").show();
            return;
        }

        // 2. 弹出保存文件对话框 (对应 SaveFileDialog)
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存 CSV 文件");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV文件 (*.csv)", "*.csv"));
        fileChooser.setInitialFileName("export_result.csv");

        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        File saveFile = fileChooser.showSaveDialog(stage);

        if (saveFile != null) {
            // 3. 创建异步任务
            Task<Void> exportTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    int total = items.size();

                    // 使用 BufferedWriter 提高性能
                    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream(saveFile), "UTF-8"))) {

                        // 写入 BOM 头防止 Excel 打开中文乱码
                        writer.write('\ufeff');

                        // 写入表头
                        writer.write("文件完整路径,文件名,校验和");
                        writer.newLine();

                        for (int i = 0; i < total; i++) {
                            Message msg = items.get(i);
                            // CSV 格式：逗号分隔
                            String line = String.format("%s,%s,%s",
                                    msg.getCompleteFilePath(),
                                    msg.getFileName(),
                                    msg.getCheckSum());
                            writer.write(line);
                            writer.newLine();

                            // 更新进度条 (0.0 - 1.0)
                            updateProgress(i + 1, total);
                        }
                    }
                    return null;
                }
            };

            // 4. UI 绑定
            progressBar1.setVisible(true);
            progressBar1.progressProperty().bind(exportTask.progressProperty());

            // 5. 完成回调
            exportTask.setOnSucceeded(e -> {
                progressBar1.progressProperty().unbind();
                progressBar1.setVisible(false);
                new Alert(Alert.AlertType.INFORMATION, "导出成功！").show();
            });

            exportTask.setOnFailed(e -> {
                progressBar1.progressProperty().unbind();
                progressBar1.setVisible(false);
                Throwable ex = exportTask.getException();
                new Alert(Alert.AlertType.ERROR, "导出失败: " + ex.getMessage()).show();
            });

            // 6. 启动
            Thread thread = new Thread(exportTask);
            thread.setDaemon(true);
            thread.start();
        }
    }
}
