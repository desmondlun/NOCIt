/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.rutgers.NOCIt.Data;

import edu.rutgers.NOCIt.UIController;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;

/**
 *
 * @author rcarpenter
 */
public class TimeElapsed {
    private Timer timer = null;
    private UIController uiController;
    private Label label;
    private NumberFormat twoDigitFormat = new DecimalFormat("00");
    private int secondsCount = 0;

    public TimeElapsed(UIController controller, Label label) {
        this.uiController = controller;
        this.label = label;
    }

    public void start() {
        secondsCount = 0;
        timer = new Timer();

        TimerTask task = new TimerTask() {
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                    	label.setAlignment(Pos.CENTER_LEFT);                    	
                        label.setText(Constants.TIME_ELAPSED + incrementAndGetTime());                                                
                    }
                });
            }
        };

        timer.schedule(task, 0, 1000);
    }

    public void stop() {
        if (timer != null) {
            this.timer.cancel();
            this.timer = null;

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                	label.setAlignment(Pos.CENTER_LEFT);                	
                    label.setText(Constants.TIME_ELAPSED + incrementAndGetTime() + " ... Done");                    
                    
                }
            });
        }
    }

    public String incrementAndGetTime() {
        secondsCount++;
        long minutes = secondsCount / 60;
        long secondsRemainder = secondsCount - (minutes * 60);
        return twoDigitFormat.format(minutes) + ":" + twoDigitFormat.format(secondsRemainder);
    }
}
