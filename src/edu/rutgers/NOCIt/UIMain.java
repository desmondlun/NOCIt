package edu.rutgers.NOCIt;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import edu.rutgers.NOCIt.Control.AutoSaver;
import edu.rutgers.NOCIt.Control.Settings;
import edu.rutgers.NOCIt.Data.Constants;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * The entry point for the UI.
 *
 * @author Rob Carpenter
 * @author James Kelley
 */
public class UIMain extends Application {

	/** The logger. */
	public static Logger logger = LogManager.getLogger("Logger");

	/* (non-Javadoc)
	 * @see javafx.application.Application#start(javafx.stage.Stage)
	 */
	@Override
	public void start(Stage stage) throws IOException {
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> Platform.runLater(() -> reportError(t, e)));
		Thread.currentThread().setUncaughtExceptionHandler(this::reportError);

		try {
			//Handle creating log directory if it doesn't exist
			System.out.println(""+Settings.getSettingsPath());
			File logDirectory = new File(Settings.getSettingsPath() + "/logs/");
			if (!logDirectory.exists()) {
				logDirectory.mkdir();
			}

			//Handle configuring logger and log file output
			DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
			Date date = new Date();
			File logFile = new File(Settings.getSettingsPath() + "/logs/log-" + dateFormat.format(date) + Constants.LOG_FILE_EXTENSION);
			FileAppender apndr = new FileAppender(new PatternLayout("%d %-5p [%c{1}] %m%n"), logFile.getAbsolutePath(), true);
			logger.addAppender(apndr);
			logger.setLevel((Level) Level.ALL);

			//Load app
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("UI.fxml"));

			Scene scene = new Scene((Parent) loader.load());
			stage.setTitle(Constants.APPLICATION_NAME);
			stage.setScene(scene);
			Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
			stage.setMinWidth(600);
			stage.setMinHeight(600);
			// For screen sizes smaller than maximum and preferred sizes in UI.fxml, gui is 
			// set to screen size on open so that entire gui is visible. Maximize
			// button sets size to screen size.
			if (primaryScreenBounds.getWidth() > 1440 || primaryScreenBounds.getHeight() > 890) {
				stage.setMaximized(false);
			} else {
				// set initial size to screen size
				stage.setWidth(primaryScreenBounds.getWidth());
				stage.setHeight(primaryScreenBounds.getHeight());
				stage.maximizedProperty().addListener(new ChangeListener<Boolean>() {
					@Override
					public void changed(
							ObservableValue<? extends Boolean> observable,
							Boolean oldValue, Boolean newValue) {
						// if maximized set to screen size
						if (newValue.booleanValue()) {
							stage.setWidth(primaryScreenBounds.getWidth());
							stage.setHeight(primaryScreenBounds.getHeight());
						}
					}
				});
			}
			stage.getIcons().add(new Image(getClass().getResourceAsStream("dna2.png")));

			final UIController controller = loader.getController();
			controller.setStage(stage); 

			stage.show();

			if (Constants.AUTOSAVE_ON) {
				//Initialize the Autosaver that will automatically save progress on a fixed time interval
				AutoSaver autoSaver = new AutoSaver(controller);
				controller.setAutoSaver(autoSaver);
			}

			//Set shutdown hook
			stage.onCloseRequestProperty().set((new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					event.consume();
					controller.closeMethod();
					//controller.close(null);
				}
			}));
		}
		catch (Throwable t) {
			reportError(Thread.currentThread(), t);
		}
	}
	
    private void reportError(Thread t, Throwable e) {    	
    	UIController.displayErrorDialog("Error", "An uncaught exception was thrown in thread " + t 
    			+ ". See log file for more information.");
    	logger.error("Error", e);
    	Platform.exit();	
    }

	/**
	 * The main method.
	 *
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		try {
			launch(args);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(Constants.ERROR_LAUNCHING_NOCIt, e);
		}
	}
}
