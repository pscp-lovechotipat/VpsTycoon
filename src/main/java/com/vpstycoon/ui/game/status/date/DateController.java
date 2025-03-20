package com.vpstycoon.ui.game.status.date;

import com.vpstycoon.game.resource.ResourceManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class DateController {
    private final ResourceManager resourceManager = ResourceManager.getInstance();
    private final DateModel dateModel;
    private final DateView dateView;
    private final Timeline timeline;

    public DateController(DateModel dateModel, DateView dateView) {
        this.dateModel = dateModel;
        this.dateView = dateView;

        // Set up Timeline to update every second
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> update()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play(); // Start the timeline
    }

    public void update() {
        dateModel.setDate(resourceManager.getCurrentState().getLocalDateTime());
        // หรือถ้าจะ bind โดยตรง:
        // dateModel.dateProperty().bind(resourceManager.getCurrentState().localDateTimeProperty());
    }
}