package com.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {
    // 定义静态常量
    private static final String MAIN_VIEW_PATH = "/com/view/FileCheckSum.fxml";
    private static final String CHINESE_FONRT_PATH = "/com/fonts/wqy-microhei.ttc";
    private static final String APP_NAME = "校验和批处理生成工具";

    @Override
    public void init() throws Exception {
        // 关键：在应用初始化阶段加载字体（只需一次）
        Font.loadFont(Main.class.getResourceAsStream(Main.CHINESE_FONRT_PATH), 10);
        // 注意：size 参数仅用于缓存，实际控件可设任意字号
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 加载时引用常量
        Parent root = FXMLLoader.load(getClass().getResource(MAIN_VIEW_PATH));

        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle(APP_NAME);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
