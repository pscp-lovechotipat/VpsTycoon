package com.vpstycoon.ui.game.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Creates a circular status button with number and label.
 */
public class CircleStatusButton {
    private final VBox container;
    
    public CircleStatusButton(String labelText, int number, Color topColor, Color bottomColor) {
        this.container = createContainer(labelText, number, topColor, bottomColor);
    }
    
    private VBox createContainer(String labelText, int number, Color topColor, Color bottomColor) {
        VBox container = new VBox(12);
        container.setAlignment(Pos.CENTER);

        // Create outer white circle (shadow)
        Circle outerCircle = new Circle(38);
        outerCircle.setEffect(new DropShadow(10, Color.BLACK));
        Stop[] outerCircleStops = new Stop[] {
                new Stop(0, Color.rgb(255, 255, 255)),
                new Stop(1, Color.rgb(220, 220, 220))
        };
        LinearGradient outerCircleGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, outerCircleStops);
        outerCircle.setFill(outerCircleGradient);

        // Create inner colored circle (gradient)
        Circle innerCircle = new Circle(30);
        DropShadow innerShadow = new DropShadow();
        innerShadow.setRadius(2);
        innerShadow.setColor(Color.rgb(0, 0, 0, 0.4));
        innerShadow.setOffsetY(2);
        innerCircle.setEffect(innerShadow);
        
        // Create gradient from specified colors
        Stop[] stops = new Stop[] {
                new Stop(0, topColor),
                new Stop(1, bottomColor)
        };
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);
        innerCircle.setFill(gradient);

        // Add number
        Label numberLabel = new Label(String.valueOf(number));
        numberLabel.setTextFill(Color.WHITE);
        numberLabel.setFont(Font.font("System", FontWeight.BOLD, 20));

        // Stack circles and number
        StackPane circleStack = new StackPane();
        circleStack.getChildren().addAll(outerCircle, innerCircle, numberLabel);

        // Create label below
        Label textLabel = new Label(labelText);
        textLabel.setPrefWidth(80);
        
        // Create gradient for label
        Stop[] labelStops = new Stop[] {
                new Stop(0, Color.rgb(255, 255, 255)),
                new Stop(1, Color.rgb(220, 220, 220))
        };
        LinearGradient labelGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, labelStops);
        
        // Set background and border
        textLabel.setBackground(new Background(new BackgroundFill(
                labelGradient, new CornerRadii(4), Insets.EMPTY)));
        
        // Add padding to label
        textLabel.setPadding(new Insets(6));
        
        // Set shadow
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));
        shadow.setRadius(10);
        shadow.setOffsetY(1);
        textLabel.setEffect(shadow);
        
        // Set text
        textLabel.setTextFill(Color.rgb(100, 100, 100));
        textLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        textLabel.setAlignment(Pos.CENTER);

        // Add all to VBox
        container.getChildren().addAll(circleStack, textLabel);
        
        return container;
    }
    
    public VBox getContainer() {
        return container;
    }
} 