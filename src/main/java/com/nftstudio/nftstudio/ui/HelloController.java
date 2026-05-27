package com.nftstudio.nftstudio.ui;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import com.nftstudio.nftstudio.database.DatabaseManager;

import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

public class HelloController {

    @FXML private Canvas drawingCanvas;
    @FXML private Canvas gridCanvas;
    @FXML private ImageView tracingImageView;

    // Transform Overlay Variables
    @FXML private Pane importWrapper;
    @FXML private ImageView importOverlay;
    @FXML private Slider importScaleSlider;
    @FXML private Slider importRotateSlider;
    @FXML private Button confirmImportBtn;
    @FXML private Button cancelImportBtn;

    private double dragStartX = 0;
    private double dragStartY = 0;

    @FXML private ColorPicker colorPicker;
    @FXML private Slider brushSize;
    @FXML private ToggleButton brushToggle;
    @FXML private ToggleButton eraserToggle;
    @FXML private ToggleButton gridToggle;
    @FXML private TextField layerNameInput;
    @FXML private ComboBox<String> categoryDropdown;
    @FXML private ToggleButton dbToggle;
    @FXML private VBox databasePanel;
    @FXML private ListView<String> layerListView;
    @FXML private TextField outputPathInput;
    @FXML private TextField amountInput;

    private GraphicsContext gc;
    private ToggleGroup toolGroup;

    @FXML
    public void initialize() {
        gc = drawingCanvas.getGraphicsContext2D();
        colorPicker.setValue(Color.BLACK);

        toolGroup = new ToggleGroup();
        brushToggle.setToggleGroup(toolGroup);
        eraserToggle.setToggleGroup(toolGroup);
        brushToggle.setSelected(true);

        drawingCanvas.setOnMousePressed(event -> {
            double size = brushSize.getValue();
            if (eraserToggle.isSelected()) gc.clearRect(event.getX() - (size / 2), event.getY() - (size / 2), size, size);
            else {
                gc.setStroke(colorPicker.getValue());
                gc.setLineWidth(size);
                gc.beginPath();
                gc.moveTo(event.getX(), event.getY());
                gc.stroke();
            }
        });

        drawingCanvas.setOnMouseDragged(event -> {
            double size = brushSize.getValue();
            if (eraserToggle.isSelected()) gc.clearRect(event.getX() - (size / 2), event.getY() - (size / 2), size, size);
            else {
                gc.lineTo(event.getX(), event.getY());
                gc.stroke();
            }
        });

        categoryDropdown.getItems().addAll(
                "1_Background", "2_Base_Body", "3_Clothes", "4_Headwear", "5_Accessories"
        );

        String userHome = System.getProperty("user.home");
        outputPathInput.setText(userHome + File.separator + "Pictures" + File.separator + "NFT_Studio_Output");
        refreshLayerList();

        importOverlay.scaleXProperty().bind(importScaleSlider.valueProperty());
        importOverlay.scaleYProperty().bind(importScaleSlider.valueProperty());
        importOverlay.rotateProperty().bind(importRotateSlider.valueProperty());

        importOverlay.setOnMousePressed(e -> {
            dragStartX = e.getSceneX() - importOverlay.getTranslateX();
            dragStartY = e.getSceneY() - importOverlay.getTranslateY();
        });

        importOverlay.setOnMouseDragged(e -> {
            importOverlay.setTranslateX(e.getSceneX() - dragStartX);
            importOverlay.setTranslateY(e.getSceneY() - dragStartY);
        });
    }

    @FXML
    public void startImportMode() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image to Transform");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        File sourceFile = fileChooser.showOpenDialog(drawingCanvas.getScene().getWindow());

        if (sourceFile != null) {
            Image image = new Image(sourceFile.toURI().toString());
            importOverlay.setImage(image);

            double safeScale = 1.0;
            if (image.getWidth() > 800 || image.getHeight() > 600) {
                safeScale = Math.min(800.0 / image.getWidth(), 600.0 / image.getHeight());
            }

            importOverlay.setFitWidth(image.getWidth() * safeScale);
            importOverlay.setFitHeight(image.getHeight() * safeScale);

            importOverlay.setTranslateX((800 - importOverlay.getFitWidth()) / 2);
            importOverlay.setTranslateY((600 - importOverlay.getFitHeight()) / 2);

            importScaleSlider.setValue(1.0);
            importRotateSlider.setValue(0);

            importWrapper.setMouseTransparent(false);
            importScaleSlider.setDisable(false);
            importRotateSlider.setDisable(false);
            confirmImportBtn.setDisable(false);
            cancelImportBtn.setDisable(false);
        }
    }

    @FXML
    public void confirmImport() {
        String layerName = layerNameInput.getText();
        String layerCategory = categoryDropdown.getValue();

        if (layerName == null || layerName.trim().isEmpty() || layerCategory == null) {
            System.out.println("Warning: Type a Name and pick a Category at the top before saving!");
            return;
        }

        String userHome = System.getProperty("user.home");
        File destDir = new File(userHome + File.separator + "Pictures" + File.separator + "NFT_Studio_Layers");
        if (!destDir.exists()) destDir.mkdirs();

        String safeFileName = layerName.replaceAll("\\s+", "_") + ".png";
        File destFile = new File(destDir, safeFileName);

        try {
            WritableImage writableImage = new WritableImage(800, 600);
            javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
            params.setFill(Color.TRANSPARENT);

            importWrapper.snapshot(params, writableImage);
            ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", destFile);

            DatabaseManager.insertLayer(layerName, layerCategory, destFile.getAbsolutePath());
            layerNameInput.clear();
            refreshLayerList();

            // Lock it back up
            importOverlay.setImage(null);
            importWrapper.setMouseTransparent(true);
            importScaleSlider.setDisable(true);
            importRotateSlider.setDisable(true);
            confirmImportBtn.setDisable(true);
            cancelImportBtn.setDisable(true);

            System.out.println("Successfully transformed and saved import!");

        } catch (IOException e) {
            System.out.println("Error saving import: " + e.getMessage());
        }
    }

    @FXML
    public void cancelImport() {
        // Delete the image and shatter the glass shield
        importOverlay.setImage(null);
        importWrapper.setMouseTransparent(true);

        // Lock the UI controls
        importScaleSlider.setDisable(true);
        importRotateSlider.setDisable(true);
        confirmImportBtn.setDisable(true);
        cancelImportBtn.setDisable(true);

        System.out.println("Import cancelled. Drawing tools unlocked.");
    }

    @FXML
    public void toggleDatabasePanel() {
        boolean isVisible = dbToggle.isSelected();
        databasePanel.setVisible(isVisible);
        databasePanel.setManaged(isVisible);
    }

    @FXML
    public void toggleGrid() {
        GraphicsContext gridGc = gridCanvas.getGraphicsContext2D();
        gridGc.clearRect(0, 0, gridCanvas.getWidth(), gridCanvas.getHeight());
        if (gridToggle.isSelected()) {
            gridGc.setStroke(Color.LIGHTGRAY);
            gridGc.setLineWidth(1);
            for (int x = 0; x <= 800; x += 50) gridGc.strokeLine(x, 0, x, 600);
            for (int y = 0; y <= 600; y += 50) gridGc.strokeLine(0, y, 800, y);
        }
    }

    @FXML
    public void clearCanvas() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Canvas Warning");
        alert.setHeaderText("Are you sure you want to delete your drawing?");
        alert.setContentText("This cannot be undone.");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) gc.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
        });
    }

    @FXML
    public void removeTracingImage() { tracingImageView.setImage(null); }

    @FXML
    public void loadTracingImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));
        File file = fileChooser.showOpenDialog(drawingCanvas.getScene().getWindow());
        if (file != null) tracingImageView.setImage(new Image(file.toURI().toString()));
    }

    @FXML
    public void saveLayer() {
        String layerName = layerNameInput.getText();
        String layerCategory = categoryDropdown.getValue();
        if (layerName == null || layerName.trim().isEmpty() || layerCategory == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(layerName.replaceAll("\\s+", "_") + ".png");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));

        File defaultLayerDir = new File(System.getProperty("user.home") + File.separator + "Pictures" + File.separator + "NFT_Studio_Layers");
        if (!defaultLayerDir.exists()) defaultLayerDir.mkdirs();
        fileChooser.setInitialDirectory(defaultLayerDir);

        File file = fileChooser.showSaveDialog(drawingCanvas.getScene().getWindow());
        if (file != null) {
            try {
                WritableImage writableImage = new WritableImage((int) drawingCanvas.getWidth(), (int) drawingCanvas.getHeight());
                javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
                params.setFill(Color.TRANSPARENT);
                drawingCanvas.snapshot(params, writableImage);
                ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", file);
                DatabaseManager.insertLayer(layerName, layerCategory, file.getAbsolutePath());
                layerNameInput.clear();
                refreshLayerList();
            } catch (IOException ex) {}
        }
    }

    @FXML
    public void runGenerator() {
        String outputPath = outputPathInput.getText();
        File directory = new File(outputPath);
        if (!directory.exists()) directory.mkdirs();
        try {
            int amount = Integer.parseInt(amountInput.getText());
            com.nftstudio.nftstudio.engine.GeneratorEngine.generateCollection(DatabaseManager.getLayersByCategory(), amount, outputPath);
            System.out.println("SUCCESS!");
        } catch (NumberFormatException e) {}
    }

    public void refreshLayerList() {
        layerListView.getItems().clear();
        java.util.Map<String, java.util.List<String>> allLayers = DatabaseManager.getLayersByCategory();
        for (String category : allLayers.keySet()) {
            for (String path : allLayers.get(category)) layerListView.getItems().add(path);
        }
    }

    @FXML
    public void deleteSelectedLayer() {
        String selectedPath = layerListView.getSelectionModel().getSelectedItem();
        if (selectedPath != null) {
            DatabaseManager.deleteLayer(selectedPath);
            refreshLayerList();
        }
    }

    @FXML
    public void resetEntireDatabase() {
        DatabaseManager.resetDatabase();
        refreshLayerList();
    }
}