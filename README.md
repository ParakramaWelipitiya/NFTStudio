#  NFT Studio: Generative Art & Metadata Engine

A professional-grade, locally hosted desktop application built with JavaFX and SQLite. NFT Studio allows digital artists and developers to easily layer, transform, and generate massive Web3-ready generative art collections (with accompanying JSON metadata) entirely offline.

<img width="1919" height="1019" alt="Screenshot 2026-05-28 044800" src="https://github.com/user-attachments/assets/1adadd16-4efc-4774-a535-c778fb9d4045" />


## Key Features

* **Generative Engine:** Stack unlimited transparent PNG layers into a single compiled image with pixel-perfect accuracy.
* **Web3-Ready Metadata:** Automatically generates blockchain-standard `.json` files alongside every generated image, ready for IPFS deployment.
* **Smart Transformation Importer:** Features a Photoshop-style free-transform tool. Import any image, drag it across the canvas, scale it, and rotate it with real-time UI sliders before committing it to the SQLite database.
* **Midnight Aurora UI:** A sleek, fully custom CSS-driven dark mode interface designed to prevent eye strain during long creative sessions, featuring smooth gradients and borderless panels.
* **Dynamic Data Management:** Edit categories on the fly, delete specific layers, or wipe the entire SQLite database directly from the UI with built-in safety confirmations.
* **Onion-Skin Tracing:** Load a ghosted "base body" to the background to ensure all newly drawn or imported accessories align perfectly upon generation.

##  Tech Stack

* **Language:** Java (JDK 17+)
* **UI Framework:** JavaFX
* **Styling:** CSS (Custom Midnight Aurora Theme)
* **Database:** SQLite (Local JDBC connection)
* **Image Processing:** `java.awt.Graphics2D` & `javax.imageio.ImageIO`

## Getting Started

### Prerequisites
* Java Development Kit (JDK) 17 or higher.
* Maven installed on your machine.

### Installation & Running
1. Clone the repository:
   ```bash
   git clone https://github.com/ParakramaWelipitiya/NFTStudio.git
    ```
2. Navigate to the project directory:
    ```bash
   cd NFT-Studio
   ```
3. Run the application using Maven:
    ```bash
   mvn clean javafx:run
   ```
   
## Project Architecture

#### The application follows a clean MVC (Model-View-Controller) architecture:

```
ui/ - Contains the FXML layouts, custom CSS, and Controllers.

database/ - Handles all SQLite JDBC connections and queries.

engine/ - Contains the core Generative Stacking algorithms and JSON metadata writer.
```

