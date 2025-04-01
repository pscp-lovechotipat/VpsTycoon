package com.vpstycoon.ui.cutscene;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.Random;


public class CutsceneBackground {
    private static final Random random = new Random();
    
    
    private static final Color[] THEME_COLORS = {
        Color.web("#8A2BE2"), 
        Color.web("#1A0033"), 
        Color.web("#380066"), 
        Color.web("#00FFFF"), 
        Color.web("#FF00A0"), 
        Color.web("#39FF14")  
    };
    
    
    public static void applyDynamicBackground(StackPane pane) {
        
        Pane backgroundLayer = new Pane();
        backgroundLayer.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        
        
        LinearGradient gradient = new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#0F001A")),
            new Stop(0.6, Color.web("#1A0033")),
            new Stop(1, Color.web("#2E0052"))
        );
        
        backgroundLayer.setBackground(new Background(new BackgroundFill(
            gradient, CornerRadii.EMPTY, Insets.EMPTY
        )));
        
        
        addStars(backgroundLayer, 150);
        
        
        addNetworkGrid(backgroundLayer);
        
        
        addFloatingParticles(backgroundLayer);
        
        
        addGlitchEffects(backgroundLayer);
        
        
        addLightBeams(backgroundLayer);
        
        
        pane.getChildren().add(0, backgroundLayer);
    }
    
    
    private static void addStars(Pane pane, int count) {
        for (int i = 0; i < count; i++) {
            double size = random.nextDouble() * 2 + 1;
            double x = random.nextDouble() * 1200;
            double y = random.nextDouble() * 800;
            
            Circle star = new Circle(x, y, size);
            star.setFill(Color.WHITE.deriveColor(1, 1, 1, random.nextDouble() * 0.5 + 0.3));
            
            
            Timeline twinkle = new Timeline(
                new KeyFrame(Duration.ZERO, 
                    new KeyValue(star.opacityProperty(), random.nextDouble() * 0.5 + 0.3)),
                new KeyFrame(Duration.seconds(random.nextDouble() * 3 + 2), 
                    new KeyValue(star.opacityProperty(), random.nextDouble() * 0.8 + 0.2))
            );
            twinkle.setCycleCount(Animation.INDEFINITE);
            twinkle.setAutoReverse(true);
            twinkle.play();
            
            pane.getChildren().add(star);
        }
    }
    
    
    private static void addNetworkGrid(Pane pane) {
        
        for (int i = 0; i < 10; i++) {
            double y = i * 80 + random.nextDouble() * 20;
            Line line = new Line(0, y, 1200, y);
            line.setStroke(THEME_COLORS[0].deriveColor(1, 1, 1, 0.2));
            line.setStrokeWidth(0.5);
            
            
            line.setOpacity(0.4);
            line.getStrokeDashArray().addAll(10.0, 5.0);
            
            pane.getChildren().add(line);
        }
        
        
        for (int i = 0; i < 15; i++) {
            double x = i * 80 + random.nextDouble() * 20;
            Line line = new Line(x, 0, x, 800);
            line.setStroke(THEME_COLORS[0].deriveColor(1, 1, 1, 0.2));
            line.setStrokeWidth(0.5);
            
            line.setOpacity(0.4);
            line.getStrokeDashArray().addAll(10.0, 5.0);
            
            pane.getChildren().add(line);
        }
    }
    
    
    private static void addFloatingParticles(Pane pane) {
        for (int i = 0; i < 20; i++) {
            double size = random.nextDouble() * 8 + 5;
            double x = random.nextDouble() * 1200;
            double y = random.nextDouble() * 800;
            
            Rectangle particle = new Rectangle(x, y, size, size);
            
            
            Color particleColor = THEME_COLORS[random.nextInt(THEME_COLORS.length)]
                .deriveColor(1, 1, 1, 0.5);
            particle.setFill(particleColor);
            
            
            Glow glow = new Glow(0.5);
            DropShadow shadow = new DropShadow(10, particleColor);
            glow.setInput(shadow);
            particle.setEffect(glow);
            
            
            Timeline float1 = new Timeline(
                new KeyFrame(Duration.ZERO, 
                    new KeyValue(particle.translateXProperty(), 0),
                    new KeyValue(particle.translateYProperty(), 0)),
                new KeyFrame(Duration.seconds(random.nextDouble() * 15 + 10), 
                    new KeyValue(particle.translateXProperty(), (random.nextDouble() - 0.5) * 40),
                    new KeyValue(particle.translateYProperty(), (random.nextDouble() - 0.5) * 40))
            );
            float1.setCycleCount(Animation.INDEFINITE);
            float1.setAutoReverse(true);
            float1.play();
            
            
            Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO, 
                    new KeyValue(particle.opacityProperty(), 0.5)),
                new KeyFrame(Duration.seconds(random.nextDouble() * 4 + 2), 
                    new KeyValue(particle.opacityProperty(), 1.0))
            );
            pulse.setCycleCount(Animation.INDEFINITE);
            pulse.setAutoReverse(true);
            pulse.play();
            
            pane.getChildren().add(particle);
        }
    }
    
    
    private static void addGlitchEffects(Pane pane) {
        Timeline glitchTimeline = new Timeline(
            new KeyFrame(Duration.seconds(3), e -> {
                if (random.nextDouble() < 0.3) {  
                    
                    double width = random.nextDouble() * 300 + 50;
                    double height = random.nextDouble() * 30 + 5;
                    double x = random.nextDouble() * 1200;
                    double y = random.nextDouble() * 800;
                    
                    Rectangle glitch = new Rectangle(x, y, width, height);
                    glitch.setFill(THEME_COLORS[random.nextInt(THEME_COLORS.length)]
                        .deriveColor(1, 1, 1, 0.4));
                    
                    
                    Bloom bloom = new Bloom(0.3);
                    glitch.setEffect(bloom);
                    
                    pane.getChildren().add(glitch);
                    
                    
                    FadeTransition fade = new FadeTransition(Duration.millis(300), glitch);
                    fade.setFromValue(0.7);
                    fade.setToValue(0);
                    fade.setOnFinished(event -> pane.getChildren().remove(glitch));
                    fade.play();
                }
            })
        );
        glitchTimeline.setCycleCount(Animation.INDEFINITE);
        glitchTimeline.play();
    }
    
    
    private static void addLightBeams(Pane pane) {
        Timeline beamTimeline = new Timeline(
            new KeyFrame(Duration.seconds(8), e -> {
                if (random.nextDouble() < 0.4) {  
                    createLightBeam(pane);
                }
            })
        );
        beamTimeline.setCycleCount(Animation.INDEFINITE);
        beamTimeline.play();
    }
    
    private static void createLightBeam(Pane pane) {
        double height = random.nextDouble() * 200 + 100;
        double y = random.nextDouble() * 800;
        
        Rectangle beam = new Rectangle(-300, y, 300, height);
        
        
        Color beamColor = THEME_COLORS[random.nextInt(3)].deriveColor(1, 1, 1, 0.15);
        beam.setFill(beamColor);
        
        
        DropShadow glow = new DropShadow();
        glow.setColor(beamColor);
        glow.setRadius(20);
        glow.setSpread(0.3);
        beam.setEffect(glow);
        
        pane.getChildren().add(beam);
        
        
        Timeline beamMove = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(beam.translateXProperty(), 0)),
            new KeyFrame(Duration.seconds(7), 
                new KeyValue(beam.translateXProperty(), 1500))
        );
        
        beamMove.setOnFinished(event -> pane.getChildren().remove(beam));
        beamMove.play();
    }
    
    
    public static void applySpotlight(Node targetNode, StackPane parent) {
        
        Rectangle overlay = new Rectangle(0, 0, parent.getWidth(), parent.getHeight());
        overlay.widthProperty().bind(parent.widthProperty());
        overlay.heightProperty().bind(parent.heightProperty());
        overlay.setFill(Color.BLACK.deriveColor(1, 1, 1, 0.7));
        
        
        Circle spotlight = new Circle(100);
        spotlight.setFill(Color.TRANSPARENT);
        spotlight.centerXProperty().bind(targetNode.layoutXProperty().add(targetNode.translateXProperty()).add(targetNode.boundsInLocalProperty().get().getWidth() / 2));
        spotlight.centerYProperty().bind(targetNode.layoutYProperty().add(targetNode.translateYProperty()).add(targetNode.boundsInLocalProperty().get().getHeight() / 2));
        
        
        overlay.setClip(spotlight);
        
        
        parent.getChildren().add(1, overlay);
        
        
        Timeline pulse = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(spotlight.radiusProperty(), 120)),
            new KeyFrame(Duration.seconds(2), 
                new KeyValue(spotlight.radiusProperty(), 140))
        );
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();
    }
} 

