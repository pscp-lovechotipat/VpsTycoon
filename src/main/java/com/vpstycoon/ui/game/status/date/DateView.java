package com.vpstycoon.ui.game.status.date;

import com.vpstycoon.FontLoader;
import com.vpstycoon.ui.game.GameplayContentPane;
import javafx.beans.binding.StringBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.time.format.DateTimeFormatter;

public class DateView extends VBox {
    private final GameplayContentPane parent;
    private final DateModel model;

    public DateView(GameplayContentPane parent, DateModel model) {
        this.parent = parent;
        this.model = model;

        setAlignment(Pos.BOTTOM_CENTER);
        setSpacing(15);  // เพิ่ม spacing ให้ดูโปร่งขึ้น
        setPickOnBounds(false);
        setFocused(false);

        // เพิ่ม background สไตล์ cyberpunk
        setStyle("-fx-border-color: #00FFFF; " +
                "-fx-border-width: 1px; " +
                "-fx-border-radius: 5px;");

        initializeUI();
    }

    public void initializeUI() {
        HBox dateBox = new HBox();
        dateBox.setPadding(new Insets(0, 0, 30, 0));
        dateBox.setAlignment(Pos.BOTTOM_CENTER);
        dateBox.setSpacing(15);

        // ส่วนแสดงวันที่ (ยังคงเป็นสีทอง)
        Text dateLabel = new Text("Date: ");
        Text dateValue = new Text();

        dateLabel.setFill(Color.web("#FFD700"));
        dateValue.setFill(Color.web("#FFD700"));

        Font font = FontLoader.SUBTITLE_FONT;
        dateLabel.setFont(font);
        dateValue.setFont(font);

        // เพิ่ม neon glow effect ให้วันที่
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#FFD700"));
        glow.setRadius(10);
        glow.setSpread(0.3);
        dateLabel.setEffect(glow);
        dateValue.setEffect(glow);

        // Format วันที่
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        dateValue.textProperty().bind(new StringBinding() {
            {
                super.bind(model.dateProperty());
            }

            @Override
            protected String computeValue() {
                return model.getDate().format(dateFormatter);
            }
        });

        // ส่วนแสดงวินาทีที่เหลือ (เปลี่ยนเป็นสีฟ้า)
        Text timeLabel = new Text("Next day in: ");
        Text timeValue = new Text();

        Color cyberBlue = Color.web("#00FFFF");  // สีฟ้าแบบ cyberpunk
        timeLabel.setFill(cyberBlue);
        timeValue.setFill(cyberBlue);

        timeLabel.setFont(font);
        timeValue.setFont(font);

        // เพิ่ม neon glow effect สีฟ้า
        DropShadow blueGlow = new DropShadow();
        blueGlow.setColor(cyberBlue);
        blueGlow.setRadius(10);
        blueGlow.setSpread(0.3);
        timeLabel.setEffect(blueGlow);
        timeValue.setEffect(blueGlow);

        // Bind วินาที
        timeValue.textProperty().bind(model.timeRemainingProperty());

        // เพิ่มทั้งสองส่วนลงใน HBox
        dateBox.getChildren().addAll(dateLabel, dateValue, timeLabel, timeValue);
        this.getChildren().add(dateBox);
    }
}