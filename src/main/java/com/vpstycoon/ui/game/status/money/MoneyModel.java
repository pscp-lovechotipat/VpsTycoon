package com.vpstycoon.ui.game.status.money;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;

public class MoneyModel {
    private final LongProperty money = new SimpleLongProperty();
    private final DoubleProperty rating = new SimpleDoubleProperty();

    public MoneyModel(long initialMoney, double initialRating) {
        setMoney(initialMoney);
        setRating(initialRating);
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

    public double getRating() {
        return rating.get();
    }

    public void setRating(double rating) {
        this.rating.set(rating);
    }

    public DoubleProperty ratingProperty() {
        return rating;
    }
}

