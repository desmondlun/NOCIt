package edu.rutgers.NOCIt;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.util.Callback;

/**
 * Default behavior of a TableView cell in JavaFX requires the Enter key to be pressed 
 * to commit an entry. This class overrides the 
 * default behavior and edits are committed when the cell loses focus.
 * Based on http://docs.oracle.com/javafx/2/ui_controls/table-view.htm.
 * 
 * @author James Kelley
 * @author Desmond Lun
 */
public class EditingTableCellFactory  
implements Callback<TableColumn<ObservableList<?>,String>,TableCell<ObservableList<?>,String>> {

	@Override
	public TableCell<ObservableList<?>, String> call(TableColumn<ObservableList<?>, String> param) {
		EditingCell editingCell = new EditingCell();
		return editingCell;
	}

	public static class EditingCell extends TableCell<ObservableList<?>,String> {
		private TextField textField;

		public EditingCell() {
		}

		@Override
		public void startEdit() {
			if (!isEmpty()) {
				super.startEdit();
				createTextField();
				setText(null);
				setGraphic(textField);
				textField.selectAll();
				textField.requestFocus();
			}
		}

		@Override
		public void cancelEdit() {
			super.cancelEdit();

			setText((String) getItem());
			setGraphic(null);
		}

		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);

			if (empty) {
				setText(null);
				setGraphic(null);
			} else {
				if (isEditing()) {
					if (textField != null) {
						textField.setText(getString());
					}
					setText(null);
					setGraphic(textField);
				} else {
					setText(getString());
					if (getString() != null && getString().length() > 0) {
						setTooltip(new Tooltip(getString()));
					} else {
						setTooltip(null);
					}
					setGraphic(null);
				}
			}
		}

		private void createTextField() {
			textField = new TextField(getString());
			textField.setMinWidth(this.getWidth() - this.getGraphicTextGap()* 2);
			textField.focusedProperty().addListener(new ChangeListener<Boolean>(){
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, 
						Boolean arg1, Boolean arg2) {
					if (!arg2) {
						commitEdit(textField.getText());
					}
				}
			});
			textField.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent actionEvent) {
					commitEdit(textField.getText());
				}
			});
		}

		private String getString() {
			return getItem() == null ? "" : getItem().toString();
		}
	}

}


