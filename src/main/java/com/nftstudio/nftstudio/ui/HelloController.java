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

public class HelloController {

    @FXML private Canvas drawingCanvas;
    @FXML private ColorPicker colorPicker;
    @FXML private Slider brushSize;
    @FXML private TextField layerNameInput;
    @FXML private ComboBox<String> categoryDropdown;

    private GraphicsContext gc;

    @FXML
    public void initialize() {
        gc = drawingCanvas.getGraphicsContext2D();

        // Set the initial color picker value to black
        colorPicker.setValue(Color.BLACK);

        drawingCanvas.setOnMousePressed(event -> {
            // Before we draw, grab the current color and size from the toolbar
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
    }

    // This method runs when the "Clear Canvas" button is clicked
    @FXML
    public void clearCanvas() {
        // Erases everything from coordinates (0,0) to the full width and height
        gc.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
    }

    @FXML
    public void saveLayer() {
        // Grab the name and category the user typed in
        String layerName = layerNameInput.getText();
        String layerCategory = categoryDropdown.getValue();

        // Basic validation so we don't save empty data
        if (layerName == null || layerName.trim().isEmpty() || layerCategory == null) {
            System.out.println("Warning: Please enter a name and select a category before saving!");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save NFT Layer");
        // Pre-fill the save name with what they typed
        fileChooser.setInitialFileName(layerName.replaceAll("\\s+", "_") + ".png");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));

        File file = fileChooser.showSaveDialog(drawingCanvas.getScene().getWindow());

        if (file != null) {
            try {
                WritableImage writableImage = new WritableImage((int) drawingCanvas.getWidth(), (int) drawingCanvas.getHeight());
                drawingCanvas.snapshot(null, writableImage);
                ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", file);

                // NEW: Send the data to SQLite!
                DatabaseManager.insertLayer(layerName, layerCategory, file.getAbsolutePath());

                // Clear the text box after a successful save
                layerNameInput.clear();

            } catch (IOException ex) {
                System.out.println("Error saving the image: " + ex.getMessage());
            }
        }
    }
}