package com.vpstycoon.ui.game.components;

import com.vpstycoon.audio.AudioManager;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.ui.game.GameplayContentPane;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import java.util.HashMap;
import java.util.Map;


public class RoomObjectsLayer {
    private final Pane monitorLayer;
    private final Pane tableLayer;
    private final Pane serverLayer;
    private final Pane keroroLayer;
    private final Pane musicBoxLayer;
    private final Pane musicStopLayer;
    private final Runnable onMonitorClick;
    private final Runnable onServerClick;
    private final Runnable onKeroroClick;
    private final Runnable onMusicBoxClick;
    private final Runnable onMusicStopClick;
    private AudioManager audioManager;
    private ResourceManager resourceManager = ResourceManager.getInstance();
    private boolean run = resourceManager.isMusicRunning();
    
    
    private static Map<String, Image> imageCache = new HashMap<>();
    private static boolean imagesPreloaded = false;
    
    
    static {
        preloadImages();
    }
    
    
    public static synchronized void preloadImages() {
        if (imagesPreloaded) {
            System.out.println("Room images already preloaded, skipping");
            return;
        }

        System.out.println("Preloading room object images");
        
        // รายการไฟล์ที่ต้องโหลด
        String[] imagePaths = {
            "/images/Object/MusicboxOn.gif", 
            "/images/Object/MusicboxOff.png",
            "/images/Object/Keroro.png",
            "/images/Moniter/MoniterF2.png",
            "/images/Object/Table.png",
            "/images/servers/server2.gif",
            "/images/wallpaper/BackgroundWindow.gif",
            "/images/rooms/room.gif"
        };
        
        boolean allSuccess = true;
        
        // ลองโหลดแต่ละภาพและแสดงสถานะ
        for (String path : imagePaths) {
            Image img = loadImage(path);
            if (img == null) {
                System.err.println("ไม่สามารถโหลดรูปภาพ: " + path);
                allSuccess = false;
            } else {
                System.out.println("โหลดรูปภาพสำเร็จ: " + path);
            }
        }
        
        if (!allSuccess) {
            System.err.println("มีปัญหาในการโหลดรูปภาพบางรายการ กรุณาตรวจสอบว่าไฟล์มีอยู่จริง");
            System.err.println("หรือลองรันคำสั่ง 'mvn clean package' เพื่อสร้างไฟล์ resources ใหม่");
        }

        imagesPreloaded = true;
        System.out.println("Room object images preloading complete" + (allSuccess ? " (all succeeded)" : " (some failed)"));
    }
    
    
    public static Image loadImage(String path) {
        if (!imageCache.containsKey(path)) {
            try {
                // ใช้ method ที่เหมาะสมสำหรับการโหลดรูปภาพจาก resources
                String resourcePath = path;
                
                // ตรวจสอบว่ามี URL สำหรับ resource นี้หรือไม่
                java.io.InputStream resourceStream = RoomObjectsLayer.class.getResourceAsStream(resourcePath);
                
                if (resourceStream == null) {
                    System.err.println("ไม่พบรูปภาพ: " + path);
                    imageCache.put(path, null);
                    return null;
                }
                
                // สร้าง Image จาก InputStream
                Image image = new Image(resourceStream);
                imageCache.put(path, image);
                return image;
            } catch (Exception e) {
                System.err.println("เกิดข้อผิดพลาดในการโหลดรูปภาพ " + path + ": " + e.getMessage());
                e.printStackTrace();
                imageCache.put(path, null);
                return null;
            }
        }
        return imageCache.get(path);
    }

    public RoomObjectsLayer(Runnable onMonitorClick ,Runnable onServerClick, Runnable onKeroroClick, Runnable onMusicBoxClick, Runnable onMusicStopClick) {
        this.onMonitorClick = onMonitorClick;
        this.onServerClick = onServerClick;
        this.audioManager = ResourceManager.getInstance().getAudioManager();
        
        
        this.tableLayer = createTableLayer();
        this.monitorLayer = createMonitorLayer();
        this.serverLayer = createServerLayer();
        this.keroroLayer = createKeroroLayer();
        this.onKeroroClick = onKeroroClick;
        this.onMusicBoxClick = onMusicBoxClick;
        this.musicBoxLayer = createMusicBoxLayer();
        this.musicStopLayer = createMusicStopLayer();
        this.onMusicStopClick = onMusicStopClick;
    }

    private synchronized Pane createMusicBoxLayer() {
        Pane musicBoxLayer = new Pane();
        musicBoxLayer.setPrefWidth(28);
        musicBoxLayer.setPrefHeight(28);
        musicBoxLayer.setScaleX(3);
        musicBoxLayer.setScaleY(3);
        musicBoxLayer.setTranslateX(155);
        musicBoxLayer.setTranslateY(235);

        if (!run) {
            musicBoxLayer.setVisible(false);
            this.setRun(false);
        }

        String normalStyle = ("""
            -fx-background-image: url('/images/Object/MusicboxOn.gif');
            -fx-background-size: contain;
            -fx-background-repeat: no-repeat;
            -fx-background-position: center;
        """);

        String hoverStyle = ("""
            -fx-background-image: url('/images/Object/MusicboxOn.gif');
            -fx-background-size: contain;
            -fx-background-repeat: no-repeat;
            -fx-background-position: center;
            -fx-effect: dropshadow(gaussian, #07edf5, 10, 0.1, 0, 0);
        """);

        musicBoxLayer.setStyle(normalStyle);
        musicBoxLayer.setOnMouseEntered(event -> {
            musicBoxLayer.setStyle(hoverStyle);
            audioManager.playSoundEffect("hover.wav");
        });
        musicBoxLayer.setOnMouseExited(event -> {musicBoxLayer.setStyle(normalStyle);});

        musicBoxLayer.setOnMouseClicked((MouseEvent e) -> onMusicBoxClick.run());
        return musicBoxLayer;

    }

    private synchronized Pane createMusicStopLayer() {
        Pane musicStopLayer = new Pane();
        musicStopLayer.setPrefWidth(28);
        musicStopLayer.setPrefHeight(28);
        musicStopLayer.setScaleX(3);
        musicStopLayer.setScaleY(3);
        musicStopLayer.setTranslateX(155);
        musicStopLayer.setTranslateY(235);
        if (run) {
            musicStopLayer.setVisible(false);
            this.setRun(true);
        }

        String normalStyle = ("""
            -fx-background-image: url('/images/Object/MusicboxOff.png');
            -fx-background-size: contain;
            -fx-background-repeat: no-repeat;
            -fx-background-position: center;
        """);

        String hoverStyle = ("""
            -fx-background-image: url('/images/Object/MusicboxOff.png');
            -fx-background-size: contain;
            -fx-background-repeat: no-repeat;
            -fx-background-position: center;
            -fx-effect: dropshadow(gaussian, #07edf5, 10, 0.1, 0, 0);
        """);

        musicStopLayer.setStyle(normalStyle);
        musicStopLayer.setOnMouseEntered(event -> {
            musicStopLayer.setStyle(hoverStyle);
            audioManager.playSoundEffect("hover.wav");
        });
        musicStopLayer.setOnMouseExited(event -> {musicStopLayer.setStyle(normalStyle);});

        musicStopLayer.setOnMouseClicked((MouseEvent e) -> onMusicStopClick.run());
        return musicStopLayer;

    }


    private synchronized Pane createKeroroLayer() {
        Pane keroroLayer = new Pane();
        keroroLayer.setPrefWidth(19);
        keroroLayer.setPrefHeight(19);
        keroroLayer.setScaleX(3);
        keroroLayer.setScaleY(3);
        keroroLayer.setTranslateX(130);
        keroroLayer.setTranslateY(410);

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
        monitorLayer.setScaleX(2);
        monitorLayer.setScaleY(2);
        monitorLayer.setTranslateX(375);
        monitorLayer.setTranslateY(235);

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

        
        Image img = loadImage("/images/servers/server2.gif");
        serverLayer.setPrefWidth(img.getWidth());
        serverLayer.setPrefHeight(img.getHeight());
        serverLayer.setScaleX(0.25);
        serverLayer.setScaleY(0.25);
        serverLayer.setTranslateX(340);
        serverLayer.setTranslateY(-210);

        
        String normalStyle = """
            -fx-background-image: url('/images/servers/server2.gif');
            -fx-background-size: contain;
            -fx-background-repeat: no-repeat;
            -fx-background-position: center;
        """;
        serverLayer.setStyle(normalStyle);

        
        String hoverStyle = """
            -fx-background-image: url('/images/servers/server2.gif');
            -fx-background-size: contain;
            -fx-background-repeat: no-repeat;
            -fx-background-position: center;
            -fx-effect: dropshadow(gaussian, #07edf5, 100, 0.1, 0, 0);
        """;

        
        serverLayer.setOnMouseEntered(e -> {
            
            if (!ResourceManager.getInstance().isEmergencyPerformanceMode()) {
                audioManager.playSoundEffect("server.mp3");
            }
            serverLayer.setStyle(hoverStyle);
        }); 
        serverLayer.setOnMouseExited(e -> {
            if (!ResourceManager.getInstance().isEmergencyPerformanceMode()) {
                audioManager.stopSoundEffect("server.mp3");
            }
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

    public Pane getMusicBoxLayer() {return musicBoxLayer;}

    public Pane getMusicStopLayer() {return musicStopLayer;}

    public boolean getRun() {
        return run;
    }

    public void setRun(boolean run) {
        this.run = run;
        resourceManager.setMusicRunning(run);
        System.out.println("Music Run State: " + run);
    }
}

