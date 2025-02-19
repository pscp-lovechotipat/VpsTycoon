package com.vpstycoon.ui;

import com.vpstycoon.Config;
import com.vpstycoon.ScreenResolution;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SettingsMenu {
    private Stage primaryStage;
    private ScreenResolution selectedResolution = Config.getResolution();
    private boolean fullscreen = Config.isFullscreen();
    private NeonShadow neonShadow = new NeonShadow();

    public SettingsMenu(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void show() {
        primaryStage.setTitle("Settings");

        // Resolution Selector
        ComboBox<ScreenResolution> resolutionComboBox = new ComboBox<>();
        resolutionComboBox.getItems().addAll(ScreenResolution.values());
        resolutionComboBox.setValue(selectedResolution);
        resolutionComboBox.setOnAction(e -> selectedResolution = resolutionComboBox.getValue());

        // Fullscreen Toggle
        CheckBox fullscreenCheckBox = new CheckBox("Enable Fullscreen");
        fullscreenCheckBox.setSelected(fullscreen);
        fullscreenCheckBox.setOnAction(e -> fullscreen = fullscreenCheckBox.isSelected());

        // Apply Button (Saves the settings)
        Image applyGIF = new Image("url:https://piskel-imgstore-b.appspot.com/img/0ff0e5b3-eee6-11ef-a53f-394195046342.gif");
        ImageView applyImageView = new ImageView(applyGIF);
        applyImageView.setFitWidth(43*4);
        applyImageView.setFitHeight(11*4);
        Button applyButton = new Button("", applyImageView);
        applyButton.setStyle("-fx-background-color: transparent;");
        applyButton.setOnAction(e -> {
            Config.setResolution(selectedResolution);
            Config.setFullscreen(fullscreen);
            primaryStage.setFullScreen(fullscreen);
            System.out.println("Applied Resolution: " + selectedResolution + " | Fullscreen: " + fullscreen);
            new MainMenu(primaryStage).show();
        });
        //hover
        applyButton.setOnMouseEntered(event -> applyImageView.setEffect(neonShadow.neon()));
        applyButton.setOnMouseExited(event -> applyImageView.setEffect(null));


        // Back Button
        Image backGIF = new Image("url:https://piskel-imgstore-b.appspot.com/img/35e760ca-eecd-11ef-8c4c-394195046342.gif");
        ImageView backImageView = new ImageView(backGIF);
        backImageView.setFitWidth(43*4);
        backImageView.setFitHeight(11*4);
        Button backButton = new Button("", backImageView);
        backButton.setStyle("-fx-background-color: transparent;");
        backButton.setOnAction(e -> new MainMenu(primaryStage).show());
        //hover
        backButton.setOnMouseEntered(event -> backImageView.setEffect(neonShadow.neon()));
        backButton.setOnMouseExited(event -> backImageView.setEffect(null));

        VBox vbox = new VBox(20, resolutionComboBox, fullscreenCheckBox, applyButton, backButton);
        vbox.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();
        root.setCenter(vbox);

        primaryStage.setScene(new Scene(root, Config.getResolution().getWidth(), Config.getResolution().getHeight()));
        primaryStage.setFullScreen(Config.isFullscreen());
    }
}
