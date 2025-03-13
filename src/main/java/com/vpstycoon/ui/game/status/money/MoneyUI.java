package com.vpstycoon.ui.game.status.money;

import com.vpstycoon.FontLoader;
import com.vpstycoon.ui.game.GameplayContentPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class MoneyUI extends VBox {
    private final GameplayContentPane parent;
    private final MoneyModel model;

    private Text moneyText;
    private Text moneyValue;
    private Text rattingText;
    private Text rattingValue;

    public MoneyUI(GameplayContentPane parent, MoneyModel model) {
        this.parent = parent;
        this.model = model;

        // สร้าง Text สำหรับ "Money:" และค่าเงิน
        moneyText = new Text("Money: ");
        moneyValue = new Text();
        moneyValue.textProperty().bind(model.moneyProperty().asString("%d"));

        // สร้าง Text สำหรับ "Ratting:" และค่าเรตติ้ง
        rattingText = new Text("Ratting: ");
        rattingValue = new Text();
        rattingValue.textProperty().bind(model.rattingProperty().asString("%.1f"));

        // ตั้งค่าสี
        moneyText.setFill(Color.web("#00FFFF")); // สีฟ้านีออน
        moneyValue.setFill(Color.web("#FFD700")); // สีทอง
        rattingText.setFill(Color.web("#00FFFF")); // สีฟ้านีออน
        rattingValue.setFill(Color.web("#FFD700")); // สีทอง

        // ตั้งค่าฟอนต์
        Font font = FontLoader.SECTION_FONT;
        moneyText.setFont(font);
        moneyValue.setFont(font);
        rattingText.setFont(font);
        rattingValue.setFont(font);

        // เพิ่มเอฟเฟกต์เงา
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.web("#00FFFF"));
        dropShadow.setRadius(20);
        dropShadow.setSpread(0.5);

        // สร้าง HBox สำหรับแถว Money
        HBox moneyBox = new HBox(moneyText, moneyValue);
        moneyBox.setAlignment(Pos.CENTER_LEFT);
        moneyBox.setSpacing(5);

        // สร้าง HBox สำหรับแถว Ratting
        HBox rattingBox = new HBox(rattingText, rattingValue);
        rattingBox.setAlignment(Pos.CENTER_LEFT);
        rattingBox.setSpacing(5);

        // เพิ่ม HBox เข้าไปใน VBox
        this.getChildren().addAll(moneyBox, rattingBox);
        this.setAlignment(Pos.TOP_LEFT);
        this.setSpacing(10);
        this.setPadding(new Insets(10)); // Padding น้อยๆ เพื่อความสวยงาม

        // ตั้งค่าสไตล์ (ขอบและพื้นหลัง)
        this.setStyle("-fx-border-color: #00FFFF; -fx-border-width: 2px;");
        this.setEffect(dropShadow);

        // ทำให้ขนาดของ VBox พอดีกับเนื้อหา
        this.setMaxWidth(Region.USE_PREF_SIZE);
        this.setMaxHeight(Region.USE_PREF_SIZE);
    }
}