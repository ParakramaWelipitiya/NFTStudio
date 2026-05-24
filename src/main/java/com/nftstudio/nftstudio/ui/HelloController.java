package com.nftstudio.nftstudio.ui;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class HelloController {

    @FXML private Canvas drawingCanvas;
    @FXML private ColorPicker colorPicker;
    @FXML private Slider brushSize;

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
    }

    // This method runs when the "Clear Canvas" button is clicked
    @FXML
    public void clearCanvas() {
        // Erases everything from coordinates (0,0) to the full width and height
        gc.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
    }

    @FXML
    public void saveLayer() {
        // 1. Set up the Windows Save Dialog
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save NFT Layer");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));

        // Open the dialog and wait for the user to pick a location
        File file = fileChooser.showSaveDialog(drawingCanvas.getScene().getWindow());

        if (file != null) {
            try {
                // 2. Take a "snapshot" of the canvas
                WritableImage writableImage = new WritableImage(
                        (int) drawingCanvas.getWidth(),
                        (int) drawingCanvas.getHeight()
                );
                drawingCanvas.snapshot(null, writableImage);

                // 3. Convert and save it to the hard drive
                ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", file);

                System.out.println("Layer saved successfully at: " + file.getAbsolutePath());
            } catch (IOException ex) {
                System.out.println("Error saving the image: " + ex.getMessage());
            }
        }
    }
}