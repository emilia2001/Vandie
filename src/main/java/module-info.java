module com.example.socialnetworkfinal {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;

    opens com.example.socialnetworkfinal to javafx.fxml;
    exports com.example.socialnetworkfinal;
    opens com.example.socialnetworkfinal.service to javafx.fxml;
    //opens com.example.socialnetworkfinal.controller to javafx.fxml;
    //exports com.example.socialnetworkfinal.controller;

    opens com.example.socialnetworkfinal.domain to javafx.graphics, javafx.fxml, javafx.base;
    //exports com.example.socialnetworkfinal;
    //opens com.example.socialnetworkfinal to javafx.fxml;
    requires org.apache.pdfbox;
}