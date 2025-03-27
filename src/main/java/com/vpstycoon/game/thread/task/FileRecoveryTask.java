package com.vpstycoon.game.thread.task;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

/**
 * File Recovery Task
 * The player must recover data by repairing corrupted file fragments
 */
public class FileRecoveryTask extends GameTask {

    private static final Logger LOGGER = Logger.getLogger(FileRecoveryTask.class.getName());
    private static final Random random = new Random();
    private static final int GRID_SIZE = 3; // 3x3 grid
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
                7000, // reward
                25,   // penalty (0.25 * 100)
                3,    // difficulty
                50    // time limit in seconds
        );
        
        // Initialize code fragments
        initializeCodeFragments();
    }
    
    /**
     * Initialize the code fragments with cyberpunk-themed code snippets
     */
    private void initializeCodeFragments() {
        // Create code fragments with cyberpunk programming style
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
        
        // Create list of correct positions (0-8 for a 3x3 grid)
        correctPositions.clear();
        for (int i = 0; i < NUM_FRAGMENTS; i++) {
            correctPositions.add(i);
        }
    }

    @Override
    protected void initializeTaskSpecifics() {
        // Create main task container
        taskPane = new BorderPane();
        CyberpunkEffects.styleTaskPane(taskPane);
        taskPane.setPrefSize(600, 500);
        taskPane.setPadding(new Insets(20));
        
        // Add animated background
        CyberpunkEffects.addAnimatedBackground(taskPane);
        
        // Create title area
        VBox headerBox = new VBox(10);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(20));
        Text titleText = CyberpunkEffects.createTaskTitle("DATA RECOVERY SYSTEM");
        Text descText = CyberpunkEffects.createTaskDescription("Repair corrupted file fragments to recover data");
        headerBox.getChildren().addAll(titleText, descText);
        taskPane.setTop(headerBox);
        
        // Create the file recovery grid
        VBox centerContent = new VBox(20);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.setPadding(new Insets(20));
        
        // Create the fragments grid
        GridPane fragmentsGrid = createFragmentsGrid();
        centerContent.getChildren().add(fragmentsGrid);
        
        // Add data scan visualization
        Pane scanPane = createScanVisualization();
        centerContent.getChildren().add(scanPane);
        
        // Progress indicator and status
        statusLabel = new Label("SCANNING FILE SYSTEM");
        statusLabel.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.BOLD, 16));
        statusLabel.setTextFill(Color.web("#00FFFF"));
        centerContent.getChildren().add(statusLabel);
        
        // Button area
        HBox buttonArea = new HBox(20);
        buttonArea.setAlignment(Pos.CENTER);
        
        // Reset button
        Button resetButton = CyberpunkEffects.createCyberpunkButton("RANDOMIZE", false);
        resetButton.setOnAction(e -> randomizeFragments());
        
        // Verify button
        Button verifyButton = CyberpunkEffects.createCyberpunkButton("VERIFY RECOVERY", true);
        verifyButton.setOnAction(e -> verifyRecovery());
        
        buttonArea.getChildren().addAll(resetButton, verifyButton);
        centerContent.getChildren().add(buttonArea);
        
        taskPane.setCenter(centerContent);
        
        // Add scanning effect to the task pane
        CyberpunkEffects.addScanningEffect(taskPane);
        
        // Add the task pane to the game pane
        gamePane.getChildren().add(taskPane);
        
        // Start with randomized fragments after a brief delay
        PauseTransition initialDelay = new PauseTransition(Duration.seconds(1.5));
        initialDelay.setOnFinished(e -> {
            randomizeFragments();
            statusLabel.setText("RECONSTRUCT CODE SEQUENCE");
        });
        initialDelay.play();
    }
    
    /**
     * Create the grid of file fragments
     */
    private GridPane createFragmentsGrid() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        fragmentPanes.clear();
        
        // Create each fragment placeholder
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                final int index = row * GRID_SIZE + col;
                
                // Create fragment container
                StackPane fragmentPane = createFragmentPane(index);
                fragmentPanes.add(fragmentPane);
                
                // Add to grid
                grid.add(fragmentPane, col, row);
            }
        }
        
        return grid;
    }
    
    /**
     * Create a single fragment pane
     */
    private StackPane createFragmentPane(int index) {
        StackPane fragmentPane = new StackPane();
        fragmentPane.setPrefSize(150, 50);
        
        // Background rectangle
        Rectangle background = new Rectangle(150, 50);
        background.setFill(Color.web("#151530"));
        background.setArcWidth(5);
        background.setArcHeight(5);
        background.setStroke(Color.web("#303060"));
        background.setStrokeWidth(1);
        
        // Text label for code fragment
        Label codeLabel = new Label("");
        codeLabel.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.NORMAL, 14));
        codeLabel.setTextFill(Color.web("#00FF00"));
        
        // Add glitch effect to fragments
        Glow glow = new Glow(0.3);
        codeLabel.setEffect(glow);
        
        fragmentPane.getChildren().addAll(background, codeLabel);
        
        // Make fragment interactive
        final int fragmentIndex = index;
        fragmentPane.setOnMouseClicked(e -> selectFragment(fragmentIndex));
        
        // Hover effect
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
    
    /**
     * Create scan visualization effect
     */
    private Pane createScanVisualization() {
        Pane scanPane = new Pane();
        scanPane.setPrefSize(300, 20);
        
        // Add scan line animation
        Rectangle scanLine = new Rectangle(2, 20);
        scanLine.setFill(Color.web("#00FFFF"));
        scanLine.setX(-10);
        scanPane.getChildren().add(scanLine);
        
        // Add data points
        for (int i = 0; i < 30; i++) {
            Rectangle dataBit = new Rectangle(3, random.nextInt(10) + 5);
            dataBit.setFill(Color.web("#00FF00", 0.7));
            dataBit.setX(i * 10);
            dataBit.setY(random.nextInt(10));
            scanPane.getChildren().add(dataBit);
        }
        
        // Animate scan line
        Timeline scanAnimation = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(scanLine.xProperty(), -10)),
            new KeyFrame(Duration.seconds(2), new KeyValue(scanLine.xProperty(), 310))
        );
        scanAnimation.setCycleCount(Timeline.INDEFINITE);
        scanAnimation.play();
        
        return scanPane;
    }
    
    /**
     * Randomize the fragment positions
     */
    private void randomizeFragments() {
        // Create a shuffled list of indices
        List<Integer> shuffledIndices = new ArrayList<>(correctPositions);
        Collections.shuffle(shuffledIndices);
        
        // Apply the shuffled fragments to the grid
        for (int i = 0; i < NUM_FRAGMENTS; i++) {
            int fragmentIndex = shuffledIndices.get(i);
            StackPane fragmentPane = fragmentPanes.get(i);
            
            // Get the code fragment for this position
            String codeText = (fragmentIndex < codeFragments.size()) ? 
                              codeFragments.get(fragmentIndex) : "ERROR://404";
            
            // Update the label text
            Label codeLabel = (Label) fragmentPane.getChildren().get(1);
            codeLabel.setText(codeText);
            
            // Add glitch effect to some fragments
            if (random.nextDouble() < 0.3) {
                addGlitchEffect(codeLabel);
            }
        }
        
        // Reset selection
        if (selectedFragment != null) {
            selectedFragment.setEffect(null);
            selectedFragment = null;
        }
        selectedFragmentIndex = -1;
        
        // Glitch animation for randomization
        applyGlitchEffect();
        
        // Update status
        statusLabel.setText("FRAGMENTS RANDOMIZED");
        recoveredCount = 0;
    }
    
    /**
     * Apply a glitch effect to the fragments grid
     */
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
    
    /**
     * Add a glitch effect to a code label
     */
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
    
    /**
     * Handle fragment selection for swapping
     */
    private void selectFragment(int index) {
        StackPane clickedFragment = fragmentPanes.get(index);
        
        if (selectedFragmentIndex == -1) {
            // First selection
            selectedFragmentIndex = index;
            selectedFragment = clickedFragment;
            
            // Highlight selected fragment
            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.web("#FF00A0"));
            shadow.setRadius(15);
            shadow.setSpread(0.5);
            selectedFragment.setEffect(shadow);
            
            statusLabel.setText("SELECT ANOTHER FRAGMENT TO SWAP");
        } else if (selectedFragmentIndex == index) {
            // Deselect if clicked again
            selectedFragment.setEffect(null);
            selectedFragmentIndex = -1;
            selectedFragment = null;
            statusLabel.setText("SELECTION CANCELED");
        } else {
            // Second selection - swap fragments
            swapFragments(selectedFragmentIndex, index);
            
            // Reset selection
            selectedFragment.setEffect(null);
            selectedFragmentIndex = -1;
            selectedFragment = null;
            
            statusLabel.setText("FRAGMENTS SWAPPED");
        }
    }
    
    /**
     * Swap two fragments' positions
     */
    private void swapFragments(int indexA, int indexB) {
        StackPane fragmentA = fragmentPanes.get(indexA);
        StackPane fragmentB = fragmentPanes.get(indexB);
        
        // Get the labels
        Label labelA = (Label) fragmentA.getChildren().get(1);
        Label labelB = (Label) fragmentB.getChildren().get(1);
        
        // Swap the text content
        String textA = labelA.getText();
        labelA.setText(labelB.getText());
        labelB.setText(textA);
    }
    
    /**
     * Verify if the fragments are in the correct order
     */
    private void verifyRecovery() {
        recoveredCount = 0;
        
        // Check each fragment position
        for (int i = 0; i < NUM_FRAGMENTS; i++) {
            StackPane fragmentPane = fragmentPanes.get(i);
            Label codeLabel = (Label) fragmentPane.getChildren().get(1);
            String fragmentText = codeLabel.getText();
            
            // Find this fragment's correct position
            int correctPosition = -1;
            for (int j = 0; j < codeFragments.size(); j++) {
                if (codeFragments.get(j).equals(fragmentText)) {
                    correctPosition = j;
                    break;
                }
            }
            
            // Check if this fragment is in the correct position
            if (correctPosition == i) {
                recoveredCount++;
                
                // Highlight correctly positioned fragment
                Rectangle background = (Rectangle) fragmentPane.getChildren().get(0);
                background.setFill(Color.web("#153030"));
                background.setStroke(Color.web("#00FF00"));
                
                codeLabel.setTextFill(Color.web("#00FF00"));
                
                // Add success glow effect
                Glow glow = new Glow(0.5);
                codeLabel.setEffect(glow);
            } else {
                // Reset incorrect fragment appearance
                Rectangle background = (Rectangle) fragmentPane.getChildren().get(0);
                background.setFill(Color.web("#301515"));
                background.setStroke(Color.web("#FF0000"));
                
                codeLabel.setTextFill(Color.web("#FF5555"));
            }
        }
        
        // Update status
        int percentRecovered = (int)((recoveredCount / (double)NUM_FRAGMENTS) * 100);
        statusLabel.setText("RECOVERY: " + percentRecovered + "% COMPLETE");
        
        // Check if all fragments are recovered
        if (recoveredCount == NUM_FRAGMENTS) {
            // Complete the task
            showCompletionSequence();
        } else {
            // Show helpful hint
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(e -> {
                statusLabel.setText("CONTINUE RECOVERY OPERATIONS");
            });
            pause.play();
        }
    }
    
    /**
     * Show completion animation sequence
     */
    private void showCompletionSequence() {
        statusLabel.setText("FILE SUCCESSFULLY RECOVERED");
        
        // Apply success effect
        CyberpunkEffects.styleCompletionEffect(taskPane);
        
        // Animate all fragments
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