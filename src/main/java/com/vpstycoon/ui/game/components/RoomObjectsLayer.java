package com.vpstycoon.ui.game.components;

import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

/**
 * Contains all the room objects (monitor, table, server).
 */
public class RoomObjectsLayer {
    private final Pane monitorLayer;
    private final Pane tableLayer;
    private final Pane serverLayer;
    private final Runnable onMonitorClick;

    public RoomObjectsLayer(Runnable onMonitorClick) {
        this.onMonitorClick = onMonitorClick;
        this.monitorLayer = createMonitorLayer();
        this.tableLayer = createTableLayer();
        this.serverLayer = createServerLayer();
    }

    private Pane createMonitorLayer() {
        // Load image
        Image monitorImage = new Image("/images/Moniter/MoniterF2.png");

        // Create pane and set properties
        Pane monitorLayer = new Pane();
        monitorLayer.setPrefWidth(100);
        monitorLayer.setPrefHeight(100);
        monitorLayer.setStyle("""
            -fx-background-image: url('/images/Moniter/MoniterF2.png');
            -fx-background-size: contain;
            -fx-background-repeat: no-repeat;
            -fx-background-position: center;
            -fx-scale-x: 2;
            -fx-scale-y: 2;
            -fx-translate-x: 750px;
            -fx-translate-y: 520px;
        """);
        monitorLayer.setOnMouseClicked((MouseEvent e) -> onMonitorClick.run());
        return monitorLayer;
    }

    private Pane createTableLayer() {
        Pane tableLayer = new Pane();
        tableLayer.setPrefWidth(400);
        tableLayer.setPrefHeight(400);
        tableLayer.setStyle("""
            -fx-background-image: url('/images/Table/Table.png');
            -fx-background-size: contain;
            -fx-background-repeat: no-repeat;
            -fx-background-position: center;
            -fx-translate-x: 400px;
            -fx-translate-y: 350px;
        """);
        return tableLayer;
    }

    private Pane createServerLayer() {
        Pane serverLayer = new Pane();
        serverLayer.setPrefWidth(400);
        serverLayer.setPrefHeight(400);
        serverLayer.setStyle("""
            -fx-background-image: url('/images/servers/server.png');
            -fx-background-size: contain;
            -fx-background-repeat: no-repeat;
            -fx-background-position: center;
            -fx-translate-x: 550px;
            -fx-translate-y: 350px;
        """);
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