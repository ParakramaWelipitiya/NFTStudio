module com.nftstudio.nftstudio {
    requires javafx.controls;
    requires javafx.fxml;

    requires javafx.swing;
    requires java.sql;
    requires com.google.gson;


    opens com.nftstudio.nftstudio to javafx.fxml;
    exports com.nftstudio.nftstudio;
    exports com.nftstudio.nftstudio.ui;
    opens com.nftstudio.nftstudio.ui to javafx.fxml;
    opens com.nftstudio.nftstudio.engine to com.google.gson;
}