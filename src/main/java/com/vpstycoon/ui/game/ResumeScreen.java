package com.vpstycoon.ui.game;

import com.vpstycoon.ui.components.buttons.Menu.MenuButton;
import com.vpstycoon.ui.components.buttons.Menu.MenuButtonType;
import com.vpstycoon.ui.navigation.Navigator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ResumeScreen extends StackPane {
    private final Navigator navigator;
    private final Runnable onResumeGame;

    public ResumeScreen(Navigator navigator, Runnable onResumeGame) {
        this.navigator = navigator;
        this.onResumeGame = onResumeGame;
        setupUI();
    }

    private void setupUI() {
        // พื้นหลังสีเข้มโปร่งใส
        Rectangle background = new Rectangle();
        background.widthProperty().bind(widthProperty());
        background.heightProperty().bind(heightProperty());
        background.setFill(Color.rgb(0, 0, 0, 0.7));

        VBox menuBox = new VBox(20);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setPadding(new Insets(20));
        menuBox.setMaxWidth(300);
        menuBox.setMaxHeight(400);
        menuBox.setStyle("-fx-background-color: #2C3E50; -fx-background-radius: 10;");

        // ปุ่ม Resume (กลับไปเล่นเกม)
        MenuButton resumeButton = new MenuButton(MenuButtonType.RESUME);
        resumeButton.setOnAction(e -> onResumeGame.run());

        // ปุ่ม Main Menu (กลับไปหน้าหลัก)
        MenuButton mainMenuButton = new MenuButton(MenuButtonType.MAIN_MENU);
        mainMenuButton.setOnAction(e -> navigator.showMainMenu());

        // ปุ่ม Quit (ออกจากเกม)
        MenuButton quitButton = new MenuButton(MenuButtonType.QUIT);
        quitButton.setOnAction(e -> System.exit(0));

        menuBox.getChildren().addAll(resumeButton, mainMenuButton, quitButton);

        getChildren().addAll(background, menuBox);

        // คลิกนอกเมนูเพื่อกลับไปเล่นเกม
        background.setOnMouseClicked(e -> {
            if (e.getTarget() == background) {
                onResumeGame.run();
            }
        });
    }
} 