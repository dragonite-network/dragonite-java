package com.vecsight.dragonite.proxy.gui;

import com.vecsight.dragonite.proxy.gui.controller.DragoniteController;
import com.vecsight.dragonite.proxy.gui.utils.BareBonesBrowserLaunch;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.pmw.tinylog.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Main extends javafx.application.Application {

    private DragoniteController dragoniteController;

    private TrayIcon trayIcon;

    @Override
    public void start(Stage primaryStage) throws Exception {
        createTrayIcon(primaryStage);
        Platform.setImplicitExit(false);
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/DragoniteController.fxml"));
        AnchorPane anchorPane = fxmlLoader.load();
        primaryStage.setTitle("Dragonite Proxy X");
        primaryStage.setScene(new Scene(anchorPane, 1000, 680));
        primaryStage.setResizable(false);
        dragoniteController = fxmlLoader.getController();
        dragoniteController.init();

        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    private void createTrayIcon(final Stage stage) throws IOException {

        if (SystemTray.isSupported()) {

            // get the SystemTray instance
            SystemTray tray = SystemTray.getSystemTray();

            // load an image
            BufferedImage image = ImageIO.read(Main.class.getResource("/Dragonite-Proxy-X.png"));

            stage.setOnCloseRequest(t -> hide(stage));

            // create a action listener to listen for default action executed on the tray icon
            final ActionListener closeListener = e -> {
                dragoniteController.isClosed = true;
                dragoniteController.saveConfig();
                dragoniteController.dragoniteProxyStop();
                System.exit(0);
            };

            ActionListener showListener = e -> Platform.runLater(stage::show);

            ActionListener aboutListener = e -> BareBonesBrowserLaunch.openURL("https://github.com/dragonite-network/dragonite-java");

            // create a popup menu
            PopupMenu popup = new PopupMenu();

            MenuItem showItem = new MenuItem("Dragonite Proxy X");
            showItem.addActionListener(showListener);
            popup.add(showItem);

            MenuItem aboutItem = new MenuItem("About");
            aboutItem.addActionListener(aboutListener);
            popup.add(aboutItem);

            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(closeListener);
            popup.add(exitItem);

            // construct a TrayIcon
            trayIcon = new TrayIcon(image, "Dragonite Proxy X", popup);

            // set Auto size
            trayIcon.setImageAutoSize(true);

            // set the TrayIcon properties
            trayIcon.addActionListener(showListener);

            // add the tray image
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                Logger.error(e);
            }
        }
    }


    private void hide(final Stage stage) {
        Platform.runLater(() -> {
            if (SystemTray.isSupported()) {
                stage.hide();
            } else {
                dragoniteController.isClosed = true;
                dragoniteController.saveConfig();
                dragoniteController.dragoniteProxyStop();
                System.exit(0);
            }
        });
    }
}
