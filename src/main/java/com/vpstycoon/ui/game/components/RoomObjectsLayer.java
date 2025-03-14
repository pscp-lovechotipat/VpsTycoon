package com.vpstycoon.ui.game.components;

import com.vpstycoon.audio.AudioManager;
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
    private final Pane keroroLayer;
    private final Runnable onMonitorClick;
    private final Runnable onServerClick;
    private final Runnable onKeroroClick;
    private AudioManager audioManager;

    public RoomObjectsLayer(Runnable onMonitorClick ,Runnable onServerClick, Runnable onKeroroClick) {
        this.onMonitorClick = onMonitorClick;
        this.onServerClick = onServerClick;
        this.monitorLayer = createMonitorLayer();
        this.tableLayer = createTableLayer();
        this.serverLayer = createServerLayer();
        this.keroroLayer = createKeroroLayer();
        this.onKeroroClick = onKeroroClick;
        this.audioManager = AudioManager.getInstance();
    }

    private synchronized Pane createKeroroLayer() {
        Pane keroroLayer = new Pane();
        keroroLayer.setPrefWidth(19);
        keroroLayer.setPrefHeight(19);
        keroroLayer.setScaleX(3);
        keroroLayer.setScaleY(3);
        keroroLayer.setTranslateX(130);
        keroroLayer.setTranslateY(430);

        String normalStyle = ("""
            -fx-background-image: url('/images/Object/Keroro.png');
            -fx-background-size: contain;
            -fx-background-repeat: no-repeat;
            -fx-background-position: center;
        """);

        String hoverStyle = ("""
            -fx-background-image: url('/images/Object/Keroro.png');
            -fx-background-size: contain;
            -fx-background-repeat: no-repeat;
            -fx-background-position: center;
            -fx-effect: dropshadow(gaussian, #07edf5, 10, 0.1, 0, 0);
        """);

        keroroLayer.setStyle(normalStyle);
        keroroLayer.setOnMouseEntered(event -> {
            audioManager.playSoundEffect("hover2.wav");
            keroroLayer.setStyle(hoverStyle);
        });
        keroroLayer.setOnMouseExited(event -> {keroroLayer.setStyle(normalStyle);});

        keroroLayer.setOnMouseClicked((MouseEvent e) -> onKeroroClick.run());
        return keroroLayer;

    }
    private synchronized Pane createMonitorLayer() {
        Pane monitorLayer = new Pane();
        monitorLayer.setPrefWidth(50);
        monitorLayer.setPrefHeight(75);
        monitorLayer.setScaleX(3);
        monitorLayer.setScaleY(3);
        monitorLayer.setTranslateX(400);
        monitorLayer.setTranslateY(270);

        String normalStyle = ("""
            -fx-background-image: url('/images/Moniter/MoniterF2.png');
            -fx-background-size: contain;
            -fx-background-repeat: no-repeat;
            -fx-background-position: center;
        """);

        String hoverStyle = ("""
            -fx-background-image: url('/images/Moniter/MoniterF2.png');
            -fx-background-size: contain;
            -fx-background-repeat: no-repeat;
            -fx-background-position: center;
            -fx-effect: dropshadow(gaussian, #07edf5, 20, 0.01, 0, 0);
        """);

        monitorLayer.setStyle(normalStyle);
        monitorLayer.setOnMouseEntered(event -> {
            audioManager.playSoundEffect("hover.wav");
            monitorLayer.setStyle(hoverStyle);
        });
        monitorLayer.setOnMouseExited(event -> {monitorLayer.setStyle(normalStyle);});

        monitorLayer.setOnMouseClicked((MouseEvent e) -> onMonitorClick.run());
        return monitorLayer;
    }

    private synchronized Pane createTableLayer() {
        Pane tableLayer = new Pane();
        tableLayer.setPrefWidth(1000);
        tableLayer.setPrefHeight(1000);
        tableLayer.setStyle("""
            -fx-background-image: url('/images/Object/Table.png');
            -fx-background-size: contain;
            -fx-background-repeat: no-repeat;
            -fx-translate-x: 100px;
            -fx-translate-y: -200px;
        """);
        return tableLayer;
    }

    private Pane createServerLayer() {
        Pane serverLayer = new Pane();

        // get image size to set perf size
        Image img = new Image("/images/servers/server.png");
        serverLayer.setPrefWidth(img.getWidth());
        serverLayer.setPrefHeight(img.getHeight());
        serverLayer.setScaleX(0.2);
        serverLayer.setScaleY(0.2);
        serverLayer.setTranslateX(320);
        serverLayer.setTranslateY(50);

        // Style ปกติ (ตามที่มึงให้มา)
        String normalStyle = """
            -fx-background-image: url('/images/servers/server.png');
            -fx-background-size: contain;
            -fx-background-repeat: no-repeat;
            -fx-background-position: center;
        """;
        serverLayer.setStyle(normalStyle);

        // Style ตอน hover (ใส่ขนาดด้วย)
        String hoverStyle = """
            -fx-background-image: url('/images/servers/server.png');
            -fx-background-size: contain;
            -fx-background-repeat: no-repeat;
            -fx-background-position: center;
            -fx-effect: dropshadow(gaussian, #07edf5, 100, 0.1, 0, 0);
        """;

        // ตั้งค่า hover effect
        serverLayer.setOnMouseEntered(e -> {
            audioManager.playSoundEffect("server.mp3");
            serverLayer.setStyle(hoverStyle);
        }); // เปลี่ยนเป็น hoverStyle ตอนเมาส์เข้า
        serverLayer.setOnMouseExited(e -> {
            audioManager.stopSoundEffect("server.mp3");
            serverLayer.setStyle(normalStyle);
        });

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

    public Pane getKeroroLayer() {return keroroLayer;}
}
