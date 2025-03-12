package com.vpstycoon.ui.game.status.money;

import com.vpstycoon.FontLoader;
import com.vpstycoon.ui.game.GameplayContentPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class MoneyUI extends VBox {
    private final GameplayContentPane parent;
    private final MoneyModel model;

    private Label moneyLabel;
    private Label rattingLabel;

    public MoneyUI(GameplayContentPane parent, MoneyModel model) {
        this.parent = parent;
        this.model = model;

        moneyLabel = new Label();
        rattingLabel = new Label();

        // Bind labels to model properties with format
        moneyLabel.textProperty().bind(model.moneyProperty().asString("Money: %d"));
        rattingLabel.textProperty().bind(model.rattingProperty().asString("Ratting: %.1f"));

        // ตั้งค่าสไตล์ให้ labels
        styleLabel(moneyLabel);
        styleLabel(rattingLabel);

        // เพิ่ม labels เข้าไปใน VBox
        this.getChildren().addAll(moneyLabel, rattingLabel);
        this.setAlignment(Pos.TOP_LEFT);
        this.setSpacing(5);
        this.setPadding(new Insets(50));

        // ตั้งค่าสไตล์ให้ VBox
        this.setStyle("-fx-border-color: #00FFFF; -fx-border-width: 2px;");
    }

    private void styleLabel(Label label) {
        // ตั้งค่าฟอนต์ (แนะนำ "Orbitron" หรือฟอนต์ futuristic อื่นๆ)
        label.setFont(FontLoader.SECTION_FONT); // ต้องโหลดฟอนต์ก่อนถ้าใช้ฟอนต์ภายนอก

        // ตั้งค่าสีข้อความเป็นฟ้านีออน
        label.setTextFill(Color.web("#00FFFF"));

        // เพิ่มเอฟเฟกต์ DropShadow
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.web("#00FFFF")); // เงาสีฟ้านีออน
        dropShadow.setRadius(40); // ขนาดเงา
        dropShadow.setSpread(0.1); // ความเข้มของเงา
        label.setEffect(dropShadow);
    }
}