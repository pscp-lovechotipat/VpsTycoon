package com.vpstycoon.ui.game.status.money;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;

public class MoneyModel {
    private final LongProperty money = new SimpleLongProperty();
    private final DoubleProperty ratting = new SimpleDoubleProperty();

    public MoneyModel(long initialMoney, double initialRatting) {
        setMoney(initialMoney);
        setRatting(initialRatting);
    }

    public long getMoney() {
        return money.get();
    }

    public void setMoney(long money) {
        this.money.set(money);
    }

    public LongProperty moneyProperty() {
        return money;
    }

    public double getRatting() {
        return ratting.get();
    }

    public void setRatting(double ratting) {
        this.ratting.set(ratting);
    }

    public DoubleProperty rattingProperty() {
        return ratting;
    }
}