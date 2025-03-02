package com.vpstycoon.ui.game.components;

import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.awt.*;

/**
 * Contains all the room objects (monitor, table, server).
 */
public class RoomObjectsLayer {
    private final Pane monitorLayer;
    private final Pane tableLayer;
    private final Pane serverLayer;
    private final Runnable onMonitorClick;
    private final Runnable onServerClick;

    public RoomObjectsLayer(Runnable onMonitorClick ,Runnable onServerClick) {
        this.onMonitorClick = onMonitorClick;
        this.onServerClick = onServerClick;
        this.monitorLayer = createMonitorLayer();
        this.tableLayer = createTableLayer();
        this.serverLayer = createServerLayer();
    }

    private Pane createMonitorLayer() {
        Pane monitorLayer = new Pane();
        monitorLayer.setPrefWidth(50);
        monitorLayer.setPrefHeight(75);
        monitorLayer.setStyle("""
            -fx-background-image: url('/images/Moniter/MoniterF2.png');
            -fx-background-size: contain;
            -fx-background-repeat: no-repeat;
            -fx-background-position: center;
            -fx-scale-x: 2.5;
            -fx-scale-y: 2.5;
            -fx-translate-x: 475px;
            -fx-translate-y: 300px;
        """);
        monitorLayer.setOnMouseClicked((MouseEvent e) -> onMonitorClick.run());
        return monitorLayer;
    }

    private Pane createTableLayer() {
        Pane tableLayer = new Pane();
        tableLayer.setPrefWidth(1000);
        tableLayer.setPrefHeight(1000);
        tableLayer.setStyle("""
            -fx-background-image: url('/images/Table/Table.png');
            -fx-background-size: contain;
            -fx-background-repeat: no-repeat;
            -fx-translate-x: 100px;
            -fx-translate-y: -200px;
        """);
        return tableLayer;
    }

    private Pane createServerLayer() {
        Pane serverLayer = new Pane();
        serverLayer.setPrefWidth(400);
        serverLayer.setPrefHeight(500);
        serverLayer.setStyle("""
        -fx-background-image: url('/images/servers/server.png');
        -fx-background-size: contain;
        -fx-background-repeat: no-repeat;
        -fx-background-position: center;
        -fx-translate-x: 550px;
        -fx-translate-y: 350px;
    """);

        serverLayer.setOnMouseClicked(e -> {
            e.consume();
            System.out.println("Server Layer Clicked. Opening Rack Info...");
            onServerClick.run();
        });

        return serverLayer;
    }


    public Pane getMonitorLayer() {
        return monitorLayer;
    }

    public Pane getTableLayer() {
        return tableLayer;
    }

    public Pane getServerLayer() {
        return serverLayer;
    }
}
