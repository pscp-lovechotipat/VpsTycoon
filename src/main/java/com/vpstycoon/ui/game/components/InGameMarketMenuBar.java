package com.vpstycoon.ui.game.components;

import com.vpstycoon.ui.game.GameplayContentPane;
import com.vpstycoon.ui.game.utils.UIUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class InGameMarketMenuBar extends StackPane {
    private final GameplayContentPane parent;

    public InGameMarketMenuBar(GameplayContentPane parent) {
        this.parent = parent;

        Button openMarketButton = UIUtils.createModernButton("Open Market", "#FF9800");
        setPadding(new Insets(40));
        openMarketButton.setOnAction(e -> {
            // ซ่อนปุ่ม Open Market
            setVisible(false);

            // ซ่อน MenuBar
            parent.getMenuBar().setVisible(false);

            // ล้างเนื้อหาเก่าใน parent และแสดงเฉพาะหน้าร้านค้า
            parent.getGameArea().getChildren().clear();
            parent.openMarket();
        });

        // วางปุ่มที่ด้านล่างขวา
        StackPane.setAlignment(openMarketButton, Pos.BOTTOM_RIGHT);

        // เพิ่มปุ่มเข้า StackPane
        this.getChildren().add(openMarketButton);
    }
}