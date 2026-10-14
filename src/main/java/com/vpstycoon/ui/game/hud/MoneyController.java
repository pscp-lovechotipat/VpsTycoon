package com.vpstycoon.ui.game.hud;

import com.vpstycoon.manager.ResourceManager;
import com.vpstycoon.model.company.Company;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class MoneyController {
    private final MoneyModel moneyModel;
    private final ResourceManager resourceManager;
    private final Timeline timeline;

    public MoneyController(MoneyModel moneyModel) {
        this.moneyModel = moneyModel;
        this.resourceManager = ResourceManager.getInstance();

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> update()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void update() {
        Company company = resourceManager.getCompany();
        moneyModel.setMoney(company.getMoney());
        moneyModel.setRating(company.getRating());
    }
}
