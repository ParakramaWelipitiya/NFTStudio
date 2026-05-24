module com.nftstudio.nftstudio {
    requires javafx.controls;
    requires javafx.fxml;

    requires javafx.swing;


    opens com.nftstudio.nftstudio to javafx.fxml;
    exports com.nftstudio.nftstudio;
    exports com.nftstudio.nftstudio.ui;
    opens com.nftstudio.nftstudio.ui to javafx.fxml;
}