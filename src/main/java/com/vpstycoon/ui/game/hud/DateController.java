package com.vpstycoon.ui.game.hud;

import com.vpstycoon.manager.ResourceManager;
import com.vpstycoon.model.customer.CustomerRequest;
import com.vpstycoon.thread.GameTimeManager;

import java.time.LocalDateTime;

public class DateController implements GameTimeManager.GameTimeListener{
    private final ResourceManager resourceManager = ResourceManager.getInstance();
    private final DateModel dateModel;

    public DateController(DateModel dateModel) {
        this.dateModel = dateModel;

        resourceManager.getGameTimeManager().addTimeListener(this);

        dateModel.setDate(resourceManager.getGameTimeManager().getGameDateTime());
    }

    @Override
    public void onTimeChanged(LocalDateTime newTime, long gameTimeMs) {
        dateModel.setDate(newTime);
    }

    @Override
    public void onRentalPeriodCheck(CustomerRequest request, CustomerRequest.RentalPeriodType period) {
    }
}
