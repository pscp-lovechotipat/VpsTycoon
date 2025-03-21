package com.vpstycoon.ui.game.status.date;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.LocalDateTime;

public class DateModel {
    private final ObjectProperty<LocalDateTime> date = new SimpleObjectProperty<>();

    public DateModel(LocalDateTime initialDate) {
        this.date.set(initialDate);
    }

    public LocalDateTime getDate() {
        return date.get();
    }

    public void setDate(LocalDateTime date) {
        this.date.set(date); // Simply set the new date without reformatting
    }

    public ObjectProperty<LocalDateTime> dateProperty() {
        return date;
    }
}