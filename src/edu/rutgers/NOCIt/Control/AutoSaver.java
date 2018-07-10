/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.rutgers.NOCIt.Control;

import edu.rutgers.NOCIt.UIController;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;

/**
 *
 * @author Rob Carpenter
 */
public class AutoSaver {
	private Timer timer = new Timer();
	private UIController uiController;

	public AutoSaver(UIController controller) {
		this.uiController = controller;

		TimerTask task = new TimerTask() {
			public void run() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						uiController.autoSave();
					}
				});
			}
		};

		timer.schedule(task, 0, 1*60*1000);
	}

	public void stop() {
		this.timer.cancel();
	}
}
