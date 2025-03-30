package com.vpstycoon.game.thread.task;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.*;
import java.util.logging.Logger;


public class FileRecoveryTask extends GameTask {

    private static final Logger LOGGER = Logger.getLogger(FileRecoveryTask.class.getName());
    private static final Random random = new Random();
    private static final int GRID_SIZE = 3; 
    private static final int NUM_FRAGMENTS = GRID_SIZE * GRID_SIZE;
    
    private final List<String> codeFragments = new ArrayList<>();
    private final List<Integer> correctPositions = new ArrayList<>();
    private final List<StackPane> fragmentPanes = new ArrayList<>();
    
    private int selectedFragmentIndex = -1;
    private StackPane selectedFragment = null;
    private Label statusLabel;
    private int recoveredCount = 0;
    private BorderPane taskPane;

    public FileRecoveryTask() {
        super(
                "File Recovery",
                "Repair and recover corrupted data files",
                "/images/task/recovery_task.png",
                7000, 
                25,   
                3,    
                50    
        );
        
        
        initializeCodeFragments();
    }
    
    
    private void initializeCodeFragments() {
        
        List<String> codeSnippets = Arrays.asList(
            "function hack(){",
            "  decrypt(key);",
            "  bypass(firewall);",
            "  inject(payload);",
            "  getAccess();",
            "  coverTracks();",
            "  execute();",
            "  exit(0);",
            "}"
        );
        
        codeFragments.clear();
        codeFragments.addAll(codeSnippets);
        
        
        correctPositions.clear();
        for (int i = 0; i < NUM_FRAGMENTS; i++) {
            correctPositions.add(i);
        }
    }

    @Override
    protected void initializeTaskSpecifics() {
        
        taskPane = new BorderPane();
        CyberpunkEffects.styleTaskPane(taskPane);
        taskPane.setPrefSize(600, 500);
        taskPane.setPadding(new Insets(20));
        
        
        CyberpunkEffects.addAnimatedBackground(taskPane);
        
        
        VBox headerBox = new VBox(10);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(10));
        
        Text descText = CyberpunkEffects.createTaskDescription("Repair corrupted file fragments to recover data");
        headerBox.getChildren().addAll(descText);
        taskPane.setTop(headerBox);
        
        
        VBox centerContent = new VBox(20);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.setPadding(new Insets(20));
        
        
        GridPane fragmentsGrid = createFragmentsGrid();
        centerContent.getChildren().add(fragmentsGrid);
        
        
        Pane scanPane = createScanVisualization();
        centerContent.getChildren().add(scanPane);
        
        
        statusLabel = new Label("SCANNING FILE SYSTEM");
        statusLabel.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.BOLD, 16));
        statusLabel.setTextFill(Color.web("#00FFFF"));
        centerContent.getChildren().add(statusLabel);
        
        
        HBox buttonArea = new HBox(20);
        buttonArea.setAlignment(Pos.CENTER);
        
        
        Button resetButton = CyberpunkEffects.createCyberpunkButton("RANDOMIZE", false);
        resetButton.setOnAction(e -> randomizeFragments());
        
        
        Button verifyButton = CyberpunkEffects.createCyberpunkButton("VERIFY RECOVERY", true);
        verifyButton.setOnAction(e -> verifyRecovery());
        
        buttonArea.getChildren().addAll(resetButton, verifyButton);
        centerContent.getChildren().add(buttonArea);
        
        taskPane.setCenter(centerContent);
        
        
        CyberpunkEffects.addScanningEffect(taskPane);
        
        
        gamePane.getChildren().add(taskPane);
        
        
        PauseTransition initialDelay = new PauseTransition(Duration.seconds(1.5));
        initialDelay.setOnFinished(e -> {
            randomizeFragments();
            statusLabel.setText("RECONSTRUCT CODE SEQUENCE");
        });
        initialDelay.play();
    }
    
    
    private GridPane createFragmentsGrid() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        fragmentPanes.clear();
        
        
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                final int index = row * GRID_SIZE + col;
                
                
                StackPane fragmentPane = createFragmentPane(index);
                fragmentPanes.add(fragmentPane);
                
                
                grid.add(fragmentPane, col, row);
            }
        }
        
        return grid;
    }
    
    
    private StackPane createFragmentPane(int index) {
        StackPane fragmentPane = new StackPane();
        fragmentPane.setPrefSize(150, 50);
        
        
        Rectangle background = new Rectangle(150, 50);
        background.setFill(Color.web("#151530"));
        background.setArcWidth(5);
        background.setArcHeight(5);
        background.setStroke(Color.web("#303060"));
        background.setStrokeWidth(1);
        
        
        Label codeLabel = new Label("");
        codeLabel.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.NORMAL, 14));
        codeLabel.setTextFill(Color.web("#00FF00"));
        
        
        Glow glow = new Glow(0.3);
        codeLabel.setEffect(glow);
        
        fragmentPane.getChildren().addAll(background, codeLabel);
        
        
        final int fragmentIndex = index;
        fragmentPane.setOnMouseClicked(e -> selectFragment(fragmentIndex));
        
        
        fragmentPane.setOnMouseEntered(e -> {
            background.setFill(Color.web("#252550"));
            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.web("#00FFFF", 0.5));
            shadow.setRadius(10);
            fragmentPane.setEffect(shadow);
        });
        
        fragmentPane.setOnMouseExited(e -> {
            background.setFill(Color.web("#151530"));
            if (fragmentPane != selectedFragment) {
                fragmentPane.setEffect(null);
            }
        });
        
        return fragmentPane;
    }
    
    
    private Pane createScanVisualization() {
        Pane scanPane = new Pane();
        scanPane.setPrefSize(300, 20);
        
        
        Rectangle scanLine = new Rectangle(2, 20);
        scanLine.setFill(Color.web("#00FFFF"));
        scanLine.setX(-10);
        scanPane.getChildren().add(scanLine);
        
        
        for (int i = 0; i < 30; i++) {
            Rectangle dataBit = new Rectangle(3, random.nextInt(10) + 5);
            dataBit.setFill(Color.web("#00FF00", 0.7));
            dataBit.setX(i * 10);
            dataBit.setY(random.nextInt(10));
            scanPane.getChildren().add(dataBit);
        }
        
        
        Timeline scanAnimation = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(scanLine.xProperty(), -10)),
            new KeyFrame(Duration.seconds(2), new KeyValue(scanLine.xProperty(), 310))
        );
        scanAnimation.setCycleCount(Timeline.INDEFINITE);
        scanAnimation.play();
        
        return scanPane;
    }
    
    
    private void randomizeFragments() {
        
        List<Integer> shuffledIndices = new ArrayList<>(correctPositions);
        Collections.shuffle(shuffledIndices);
        
        
        for (int i = 0; i < NUM_FRAGMENTS; i++) {
            int fragmentIndex = shuffledIndices.get(i);
            StackPane fragmentPane = fragmentPanes.get(i);
            
            
            String codeText = (fragmentIndex < codeFragments.size()) ? 
                              codeFragments.get(fragmentIndex) : "ERROR";
            
            
            Label codeLabel = (Label) fragmentPane.getChildren().get(1);
            codeLabel.setText(codeText);
            
            
            if (random.nextDouble() < 0.3) {
                addGlitchEffect(codeLabel);
            }
        }
        
        
        if (selectedFragment != null) {
            selectedFragment.setEffect(null);
            selectedFragment = null;
        }
        selectedFragmentIndex = -1;
        
        
        applyGlitchEffect();
        
        
        statusLabel.setText("FRAGMENTS RANDOMIZED");
        recoveredCount = 0;
    }
    
    
    private void applyGlitchEffect() {
        ParallelTransition glitchEffect = new ParallelTransition();
        
        for (StackPane fragmentPane : fragmentPanes) {
            FadeTransition fade = new FadeTransition(Duration.millis(100), fragmentPane);
            fade.setFromValue(1.0);
            fade.setToValue(0.5);
            fade.setAutoReverse(true);
            fade.setCycleCount(4);
            glitchEffect.getChildren().add(fade);
        }
        
        glitchEffect.play();
    }
    
    
    private void addGlitchEffect(Label codeLabel) {
        Timeline glitchEffect = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(codeLabel.opacityProperty(), 1.0)),
            new KeyFrame(Duration.millis(100), 
                new KeyValue(codeLabel.translateXProperty(), 2),
                new KeyValue(codeLabel.opacityProperty(), 0.8)),
            new KeyFrame(Duration.millis(200), 
                new KeyValue(codeLabel.translateXProperty(), -2),
                new KeyValue(codeLabel.opacityProperty(), 0.9)),
            new KeyFrame(Duration.millis(300), 
                new KeyValue(codeLabel.translateXProperty(), 0),
                new KeyValue(codeLabel.opacityProperty(), 1.0))
        );
        glitchEffect.setCycleCount(Timeline.INDEFINITE);
        glitchEffect.play();
    }
    
    
    private void selectFragment(int index) {
        StackPane clickedFragment = fragmentPanes.get(index);
        
        if (selectedFragmentIndex == -1) {
            
            selectedFragmentIndex = index;
            selectedFragment = clickedFragment;
            
            
            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.web("#FF00A0"));
            shadow.setRadius(15);
            shadow.setSpread(0.5);
            selectedFragment.setEffect(shadow);
            
            statusLabel.setText("SELECT ANOTHER FRAGMENT TO SWAP");
        } else if (selectedFragmentIndex == index) {
            
            selectedFragment.setEffect(null);
            selectedFragmentIndex = -1;
            selectedFragment = null;
            statusLabel.setText("SELECTION CANCELED");
        } else {
            
            swapFragments(selectedFragmentIndex, index);
            
            
            selectedFragment.setEffect(null);
            selectedFragmentIndex = -1;
            selectedFragment = null;
            
            statusLabel.setText("FRAGMENTS SWAPPED");
        }
    }
    
    
    private void swapFragments(int indexA, int indexB) {
        StackPane fragmentA = fragmentPanes.get(indexA);
        StackPane fragmentB = fragmentPanes.get(indexB);
        
        
        Label labelA = (Label) fragmentA.getChildren().get(1);
        Label labelB = (Label) fragmentB.getChildren().get(1);
        
        
        String textA = labelA.getText();
        labelA.setText(labelB.getText());
        labelB.setText(textA);
    }
    
    
    private void verifyRecovery() {
        recoveredCount = 0;
        
        
        for (int i = 0; i < NUM_FRAGMENTS; i++) {
            StackPane fragmentPane = fragmentPanes.get(i);
            Label codeLabel = (Label) fragmentPane.getChildren().get(1);
            String fragmentText = codeLabel.getText();
            
            
            int correctPosition = -1;
            for (int j = 0; j < codeFragments.size(); j++) {
                if (codeFragments.get(j).equals(fragmentText)) {
                    correctPosition = j;
                    break;
                }
            }
            
            
            if (correctPosition == i) {
                recoveredCount++;
                
                
                Rectangle background = (Rectangle) fragmentPane.getChildren().get(0);
                background.setFill(Color.web("#153030"));
                background.setStroke(Color.web("#00FF00"));
                
                codeLabel.setTextFill(Color.web("#00FF00"));
                
                
                Glow glow = new Glow(0.5);
                codeLabel.setEffect(glow);
            } else {
                
                Rectangle background = (Rectangle) fragmentPane.getChildren().get(0);
                background.setFill(Color.web("#301515"));
                background.setStroke(Color.web("#FF0000"));
                
                codeLabel.setTextFill(Color.web("#FF5555"));
            }
        }
        
        
        int percentRecovered = (int)((recoveredCount / (double)NUM_FRAGMENTS) * 100);
        statusLabel.setText("RECOVERY: " + percentRecovered + "% COMPLETE");
        
        
        if (recoveredCount == NUM_FRAGMENTS) {
            
            showCompletionSequence();
        } else {
            
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(e -> {
                statusLabel.setText("CONTINUE RECOVERY OPERATIONS");
            });
            pause.play();
        }
    }
    
    
    private void showCompletionSequence() {
        statusLabel.setText("FILE SUCCESSFULLY RECOVERED");
        
        
        CyberpunkEffects.styleCompletionEffect(taskPane);
        
        
        ParallelTransition successAnimation = new ParallelTransition();
        
        for (StackPane fragmentPane : fragmentPanes) {
            FadeTransition fade = new FadeTransition(Duration.millis(500), fragmentPane);
            fade.setFromValue(1.0);
            fade.setToValue(0.7);
            fade.setAutoReverse(true);
            fade.setCycleCount(4);
            
            Rectangle background = (Rectangle) fragmentPane.getChildren().get(0);
            background.setFill(Color.web("#153030"));
            background.setStroke(Color.web("#00FF00"));
            
            Label codeLabel = (Label) fragmentPane.getChildren().get(1);
            codeLabel.setTextFill(Color.web("#00FF00"));
            
            successAnimation.getChildren().add(fade);
        }
        
        successAnimation.setOnFinished(e -> {
            PauseTransition completionDelay = new PauseTransition(Duration.seconds(1));
            completionDelay.setOnFinished(event -> completeTask());
            completionDelay.play();
        });
        
        successAnimation.play();
    }
} 
