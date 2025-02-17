package com.vpstycoon;

import com.vpstycoon.ui.MainMenu;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        new MainMenu(primaryStage).show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
