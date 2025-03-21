package com.vpstycoon.ui.game.status.date;

import com.vpstycoon.FontLoader;
import com.vpstycoon.ui.game.GameplayContentPane;
import javafx.beans.binding.StringBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
        setSpacing(10);
        setPickOnBounds(false);
        setFocused(false);

        initializeUI();
    }

    public void initializeUI() {
        HBox dateBox = new HBox();
        dateBox.setPadding(new Insets(0,0,30,0));
        dateBox.setAlignment(Pos.BOTTOM_CENTER);

        Text dateText = new Text("Date: ");
        Text dateValue = new Text();

        dateText.setFill(Color.web("#FFD700"));
        dateValue.setFill(Color.web("#FFD700"));

        Font font = FontLoader.SUBTITLE_FONT;
        dateText.setFont(font);
        dateValue.setFont(font);

        // Format LocalDateTime to a string for display
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        dateValue.textProperty().bind(new StringBinding() {
            {
                super.bind(model.dateProperty());
            }

            @Override
            protected String computeValue() {
                return model.getDate().format(formatter);
            }
        });

        dateBox.getChildren().addAll(dateText, dateValue);
        this.getChildren().add(dateBox); // Add dateBox to the StackPane
    }
}
