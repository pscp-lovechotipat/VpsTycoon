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
    private Text ratingText;
    private Text ratingValue;

    public MoneyUI(GameplayContentPane parent, MoneyModel model) {
        this.parent = parent;
        this.model = model;

        // สร้าง Text สำหรับ "Money:" และค่าเงิน
        moneyText = new Text("Money: ");
        moneyValue = new Text();
        moneyValue.textProperty().bind(model.moneyProperty().asString("%d"));

        // สร้าง Text สำหรับ "Rating:" และค่าเรตติ้ง
        ratingText = new Text("Rating: ");
        ratingValue = new Text();
        ratingValue.textProperty().bind(model.ratingProperty().asString("%.1f"));

        // ตั้งค่าสี
        moneyText.setFill(Color.web("#00FFFF")); // สีฟ้านีออน
        moneyValue.setFill(Color.web("#FFD700")); // สีทอง
        ratingText.setFill(Color.web("#00FFFF")); // สีฟ้านีออน
        ratingValue.setFill(Color.web("#FFD700")); // สีทอง

        // ตั้งค่าฟอนต์
        Font font = FontLoader.SECTION_FONT;
        moneyText.setFont(font);
        moneyValue.setFont(font);
        ratingText.setFont(font);
        ratingValue.setFont(font);

        // เพิ่มเอฟเฟกต์เงา
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.web("#00FFFF"));
        dropShadow.setRadius(20);
        dropShadow.setSpread(0.5);

        // สร้าง HBox สำหรับแถว Money
        HBox moneyBox = new HBox(moneyText, moneyValue);
        moneyBox.setAlignment(Pos.CENTER_LEFT);
        moneyBox.setSpacing(5);

        // สร้าง HBox สำหรับแถว Rating
        HBox ratingBox = new HBox(ratingText, ratingValue);
        ratingBox.setAlignment(Pos.CENTER_LEFT);
        ratingBox.setSpacing(5);

        // เพิ่ม HBox เข้าไปใน VBox
        this.getChildren().addAll(moneyBox, ratingBox);
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