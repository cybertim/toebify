package com.dskwrk.java;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main  extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI.fxml"));
        Parent root = loader.load();
        ((GUIController)loader.getController()).setHostServices(getHostServices());

        Scene scene = new Scene(root);
        stage.setResizable(false);
        stage.setTitle("Toebify");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args)  {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        launch();
    }

}
