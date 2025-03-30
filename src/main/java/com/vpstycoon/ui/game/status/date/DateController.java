package com.vpstycoon.ui.game.status.date;

import com.vpstycoon.game.manager.CustomerRequest;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.game.thread.GameTimeManager;

import java.time.LocalDateTime;

public class DateController implements GameTimeManager.GameTimeListener{
    private final ResourceManager resourceManager = ResourceManager.getInstance();
    private final DateModel dateModel;
    private final DateView dateView;

    public DateController(DateModel dateModel, DateView dateView) {
        this.dateModel = dateModel;
        this.dateView = dateView;

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
