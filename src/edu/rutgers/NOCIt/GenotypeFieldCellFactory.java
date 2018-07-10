package edu.rutgers.NOCIt;

import edu.rutgers.NOCIt.Data.AMELAllele;
import edu.rutgers.NOCIt.Data.Allele;
import edu.rutgers.NOCIt.Data.Constants;
import edu.rutgers.NOCIt.Data.STRAllele;
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
 * amelIndex is set when table containing cells of this type is created.
 * 
 * @author James Kelley
 * @author Desmond Lun
 */
public class GenotypeFieldCellFactory  
implements Callback<TableColumn<ObservableList<?>,String>,TableCell<ObservableList<?>,String>> {

	private static int amelIndex = -1;

	public static void setAmelIndex(int amelIndex) {
		GenotypeFieldCellFactory.amelIndex = amelIndex;
	}
	
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
					if (!validEntry(tf, value, getIndex())) {
						// Returns focus to cell where invalid value was entered
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								tf.setText(oldCellValue);
								tf.requestFocus();
							}
						});
					}
					else {
						oldCellValue = ov.getValue();
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
	 * If Locus is AMEL, valid entries are "X" and "Y", where lower case
	 * characters are accepted as valid. If lower case 'x" or "y", value is
	 * changed to upper case. Otherwise valid entries are Strings that can be
	 * parsed to an STRAllele.
	 * 
	 * @param TextField
	 *            tf - TextField of TableView cell
	 * @param String
	 *            value - entered value of TableView cell
	 * @param int
	 *            index - row index of TableView cell
	 * @return true if entered value for locus is valid, otherwise returns
	 *         false.
	 * 
	 * @see STRAllele
	 */
    private static boolean validEntry(TextField tf, String value, int index) {
    	if (!value.trim().equals("")) {
    		Allele allele;
    		try {
    			if (index == amelIndex)
    				allele = new AMELAllele(value);
    			else
    				allele = new STRAllele(value);
    			tf.setText(allele.toString());
    		}
			catch (IllegalArgumentException e) {
				Alert alert;
				if (index == amelIndex)
					alert = new Alert(AlertType.ERROR, Constants.GENOTYPE_POPUP_ERROR_AMEL_ALLELE_CHECK);
				else
					alert = new Alert(AlertType.ERROR, Constants.GENOTYPE_POPUP_ERROR_STR_ALLELE_CHECK);
				
        		alert.showAndWait();
				return false;
			}		
		}
    	
		return true;
    }
}

