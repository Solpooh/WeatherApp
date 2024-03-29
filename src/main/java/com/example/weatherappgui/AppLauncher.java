package com.example.weatherappgui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;

public class AppLauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
//                // display our weather app gui
                // 날씨 앱 시작
                new WeatherAppGui().setVisible(true);

//                System.out.println(WeatherApp.getLocationData("Tokyo"));

                System.out.println(WeatherApp.getCurrentTime());
            }
        });
    }
}