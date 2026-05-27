package com.nftstudio.nftstudio.ui;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import com.nftstudio.nftstudio.database.DatabaseManager;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class HelloController {

    @FXML private Canvas drawingCanvas;
    @FXML private ColorPicker colorPicker;
    @FXML private Slider brushSize;
    @FXML private TextField layerNameInput;
    @FXML private ComboBox<String> categoryDropdown;
    @FXML private TextField outputPathInput;
    @FXML private TextField amountInput;
    @FXML private ListView<String> layerListView;

    // NEW VARIABLE FOR TRACING
    @FXML private ImageView tracingImageView;

    private GraphicsContext gc;

    @FXML
    public void initialize() {
        gc = drawingCanvas.getGraphicsContext2D();
        colorPicker.setValue(Color.BLACK);

        drawingCanvas.setOnMousePressed(event -> {
            gc.setStroke(colorPicker.getValue());
            gc.setLineWidth(brushSize.getValue());
            gc.beginPath();
            gc.moveTo(event.getX(), event.getY());
            gc.stroke();
        });

        drawingCanvas.setOnMouseDragged(event -> {
            gc.lineTo(event.getX(), event.getY());
            gc.stroke();
        });

        categoryDropdown.getItems().addAll(
                "1_Background",
                "2_Base_Body",
                "3_Clothes",
                "4_Headwear",
                "5_Accessories"
        );

        // --- NEW: DYNAMIC FOLDER AUTOMATION ---
        // 1. Get the current computer user's home directory (e.g., C:\Users\Sarah)
        String userHome = System.getProperty("user.home");

        // 2. Build a path to their Pictures folder using File.separator (so it works on Mac too)
        String defaultOutputPath = userHome + File.separator + "Pictures" + File.separator + "NFT_Studio_Output";

        // 3. Inject it into the UI Text Box automatically!
        outputPathInput.setText(defaultOutputPath);
        // --------------------------------------

        refreshLayerList();
    }

    // NEW METHOD: Load the background tracing image
    @FXML
    public void loadTracingImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a Base Image to Trace");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));

        File file = fileChooser.showOpenDialog(drawingCanvas.getScene().getWindow());

        if (file != null) {
            Image tracingImage = new Image(file.toURI().toString());
            tracingImageView.setImage(tracingImage);
            System.out.println("Loaded tracing image: " + file.getName());
        }
    }

    @FXML
    public void clearCanvas() {
        gc.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
        // Optional: Also clear the tracing image when you clear the canvas
        tracingImageView.setImage(null);
    }

    @FXML
    public void saveLayer() {
        String layerName = layerNameInput.getText();
        String layerCategory = categoryDropdown.getValue();

        if (layerName == null || layerName.trim().isEmpty() || layerCategory == null) {
            System.out.println("Warning: Please enter a name and select a category before saving!");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save NFT Layer");
        fileChooser.setInitialFileName(layerName.replaceAll("\\s+", "_") + ".png");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));

        // --- NEW: AUTOMATIC FILE CHOOSER DIRECTORY ---
        String userHome = System.getProperty("user.home");
        File defaultLayerDir = new File(userHome + File.separator + "Pictures" + File.separator + "NFT_Studio_Layers");

        // If the folder doesn't exist yet, create it quietly in the background
        if (!defaultLayerDir.exists()) {
            defaultLayerDir.mkdirs();
        }

        // Tell the popup window to start inside this specific folder
        fileChooser.setInitialDirectory(defaultLayerDir);
        // ---------------------------------------------

        File file = fileChooser.showSaveDialog(drawingCanvas.getScene().getWindow());

        if (file != null) {
            try {
                WritableImage writableImage = new WritableImage((int) drawingCanvas.getWidth(), (int) drawingCanvas.getHeight());

                // --- NEW TRANSPARENCY FIX ---
                javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
                params.setFill(javafx.scene.paint.Color.TRANSPARENT); // Force clear background!

                // Pass the params into the snapshot
                drawingCanvas.snapshot(params, writableImage);
                // ----------------------------

                ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", file);

                DatabaseManager.insertLayer(layerName, layerCategory, file.getAbsolutePath());
                layerNameInput.clear();

                refreshLayerList();

            } catch (IOException ex) {
                System.out.println("Error saving the image: " + ex.getMessage());
            }
        }
    }

    @FXML
    public void runGenerator() {
        String outputPath = outputPathInput.getText();
        File directory = new File(outputPath);

        if (!directory.exists()) {
            directory.mkdirs();
            System.out.println("Created new output directory at: " + outputPath);
        }

        try {
            int amount = Integer.parseInt(amountInput.getText());
            java.util.Map<String, java.util.List<String>> allLayers = DatabaseManager.getLayersByCategory();
            com.nftstudio.nftstudio.engine.GeneratorEngine.generateCollection(allLayers, amount, outputPath);
            System.out.println("\nSUCCESS! Check your folder: " + outputPath);
        } catch (NumberFormatException e) {
            System.out.println("Error: Please enter a valid number for the amount!");
        }
    }

    public void refreshLayerList() {
        layerListView.getItems().clear();
        java.util.Map<String, java.util.List<String>> allLayers = DatabaseManager.getLayersByCategory();
        for (String category : allLayers.keySet()) {
            for (String path : allLayers.get(category)) {
                layerListView.getItems().add(path);
            }
        }
    }

    @FXML
    public void deleteSelectedLayer() {
        String selectedPath = layerListView.getSelectionModel().getSelectedItem();
        if (selectedPath != null) {
            DatabaseManager.deleteLayer(selectedPath);
            refreshLayerList();
        } else {
            System.out.println("Please select a layer from the list first!");
        }
    }

    @FXML
    public void resetEntireDatabase() {
        DatabaseManager.resetDatabase();
        refreshLayerList();
    }

    @FXML
    public void importExternalLayer() {
        String layerName = layerNameInput.getText();
        String layerCategory = categoryDropdown.getValue();

        if (layerName == null || layerName.trim().isEmpty() || layerCategory == null) {
            System.out.println("Warning: Please enter a Name and select a Category before importing!");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select PNG to Import");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));

        File sourceFile = fileChooser.showOpenDialog(drawingCanvas.getScene().getWindow());

        if (sourceFile != null) {
            try {
                String userHome = System.getProperty("user.home");
                File destDir = new File(userHome + File.separator + "Pictures" + File.separator + "NFT_Studio_Layers");
                if (!destDir.exists()) {
                    destDir.mkdirs();
                }

                String safeFileName = layerName.replaceAll("\\s+", "_") + ".png";
                File destFile = new File(destDir, safeFileName);

                // --- NEW SMART RESIZING LOGIC ---
                // 1. Read the raw image they imported
                BufferedImage originalImage = ImageIO.read(sourceFile);

                if (originalImage != null) {
                    // 2. Create a new invisible 800x600 canvas (matching your engine's strict rules)
                    BufferedImage standardizedImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2d = standardizedImage.createGraphics();

                    // 3. Calculate how to scale the image so it fits 800x600 perfectly without squishing
                    double scale = Math.min(800.0 / originalImage.getWidth(), 600.0 / originalImage.getHeight());
                    int newWidth = (int) (originalImage.getWidth() * scale);
                    int newHeight = (int) (originalImage.getHeight() * scale);

                    // 4. Calculate the exact math to place it dead-center
                    int x = (800 - newWidth) / 2;
                    int y = (600 - newHeight) / 2;

                    // 5. Paint the resized image onto the invisible canvas
                    g2d.drawImage(originalImage, x, y, newWidth, newHeight, null);
                    g2d.dispose();

                    // 6. Save this new perfect 800x600 version instead of the raw file
                    ImageIO.write(standardizedImage, "png", destFile);
                }
                // --------------------------------

                DatabaseManager.insertLayer(layerName, layerCategory, destFile.getAbsolutePath());
                layerNameInput.clear();
                refreshLayerList();

                System.out.println("Successfully imported, resized, and centered: " + destFile.getName());

            } catch (IOException e) {
                System.out.println("Error importing file: " + e.getMessage());
            }
        }
    }
}