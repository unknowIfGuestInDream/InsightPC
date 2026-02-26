module com.tlcsdm.insightpc {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.prefs;

    requires org.controlsfx.controls;
    requires com.dlsc.preferencesfx;
    requires atlantafx.base;

    requires com.github.oshi;

    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires com.google.gson;

    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.material;

    opens com.tlcsdm.insightpc to javafx.fxml, javafx.graphics;
    opens com.tlcsdm.insightpc.controller to javafx.fxml;
    opens com.tlcsdm.insightpc.config to com.dlsc.preferencesfx, javafx.base;
    opens com.tlcsdm.insightpc.model to javafx.base, com.dlsc.preferencesfx, com.google.gson;

    exports com.tlcsdm.insightpc;
    exports com.tlcsdm.insightpc.config;
    exports com.tlcsdm.insightpc.controller;
    exports com.tlcsdm.insightpc.controller.tab;
    exports com.tlcsdm.insightpc.model;
    exports com.tlcsdm.insightpc.service;
}
