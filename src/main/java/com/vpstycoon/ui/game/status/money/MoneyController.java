package com.vpstycoon.ui.game.status.money;

import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.resource.ResourceManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class MoneyController {
    private final MoneyModel moneyModel;
    private final MoneyUI moneyUI;
    private final ResourceManager resourceManager;
    private final Timeline timeline;

    public MoneyController(MoneyModel moneyModel, MoneyUI moneyUI) {
        this.moneyModel = moneyModel;
        this.moneyUI = moneyUI;
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


