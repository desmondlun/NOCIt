package edu.rutgers.NOCIt;

import edu.rutgers.NOCIt.Data.Constants;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.util.Callback;

/**
 * Default behavior of a TableView cell in JavaFX requires the Enter key to be
 * pressed to commit an entry. This class overrides the default behavior and
 * edits are committed when the cell loses focus. Entry are validated when a
 * cell loses focus and if an entry is not valid, focus is returned to the cell
 * after an alert is displayed and the user clicks the OK button on the alert.
 * This class is based on
 * http://stackoverflow.com/questions/7880494/uitableview-better-editing-through
 * -binding
 * 
 * @author James Kelley
 * @author Desmond Lun
 */
public class AnalyticalThresholdsCellFactory  
implements Callback<TableColumn<ObservableList<?>,String>,TableCell<ObservableList<?>,String>> {

	@Override
	public TableCell<ObservableList<?>, String> call(TableColumn<ObservableList<?>, String> param) {
		TextFieldCell textFieldCell = new TextFieldCell();
		return textFieldCell;
	}

	/**
	 * Binds cell observable value to textField update item. Retrieve the actual
	 * String Property that should be bound to the TextField. If the TextField
	 * is currently bound to a different StringProperty, the old property is
	 * unbound and new property is bound.
	 * 
	 * If the new value is not valid, validEntry method sets the old value into
	 * the cell. Table cell requests focus after invalid entry. Table cell text
	 * is selected when cell gains focus.
	 */
	public static class TextFieldCell extends TableCell<ObservableList<?>,String> {
		private TextField textField;
		private StringProperty boundToCurrently = null;
		private String oldCellValue = "";

		public TextFieldCell() {
			textField = new TextField();
			textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							textField.selectAll();
						}
					});
					TextField tf = (TextField)getGraphic();
					ObservableValue<String> ov = getTableColumn().getCellObservableValue(getIndex());
					String value = ov.getValue();
					if (!value.trim().equals("")) {
						if (!validEntry(tf, value, getIndex())) {
							// Returns focus to cell where invalid value was entered
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									try {
										if ((Integer.parseInt(oldCellValue) < 1 || Integer.parseInt(oldCellValue) > Constants.ANALYTICAL_THRESHOLD_MAX_VALUE)) {
											tf.setText("1");					    					
										} else {
											tf.setText(oldCellValue);
										}
									}
									catch (NumberFormatException nfe) {
										tf.setText("1");
									}

									tf.requestFocus();
								}
							});
						}
					}
				}
			});
			this.setGraphic(textField);
		}

		@Override
		protected void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);        
			if(!empty) {
				// Retrieve the actual String Property that should be bound to the TextField
				// If the TextField is currently bound to a different StringProperty
				// Unbind the old property and rebind to the new one
				ObservableValue<String> ov = getTableColumn().getCellObservableValue(getIndex());
				SimpleStringProperty sp = (SimpleStringProperty)ov;

				if(this.boundToCurrently==null) {
					this.boundToCurrently = sp;
					this.textField.textProperty().bindBidirectional(sp);
				}
				else {
					if(this.boundToCurrently != sp) {
						this.textField.textProperty().unbindBidirectional(this.boundToCurrently);
						this.boundToCurrently = sp;
						this.textField.textProperty().bindBidirectional(this.boundToCurrently);
					}
				}
			}
			else {
				//this.setContentDisplay(ContentDisplay.TEXT_ONLY);
			}
		}
	}

	/**
	 * Valid entries are Strings that parse as integers and are >= 1.
	 * 
	 * @param TextField tf - TextField of TableView cell
	 * @param String value - entered value of TableView cell
	 * @param int index - row index of TableView cell
	 * @return true if entered value for locus is valid, otherwise returns false.
	 */
	private static boolean validEntry(TextField tf, String value, int index) {
		if (!value.trim().equals("")) {
			try {
				if (Integer.parseInt(value) < 1 || Integer.parseInt(value) > Constants.ANALYTICAL_THRESHOLD_MAX_VALUE) {
					Alert alert = new Alert(AlertType.ERROR, Constants.ANALYTICAL_THRESHOLD_ERROR_NUMBER_CHECK);
					alert.showAndWait();

					return false;
				}

				tf.setText(value);
			} catch (NumberFormatException nfe) {
				Alert alert = new Alert(AlertType.ERROR, Constants.NON_NUMERIC_ERROR_MESSAGE);
				alert.showAndWait();    			

				return false;
			}    		
		}

		return true;
	}	
}


