package edu.rutgers.NOCIt;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.util.Callback;
import org.controlsfx.control.CheckComboBox;

import edu.rutgers.NOCIt.Data.Constants;
import edu.rutgers.NOCIt.Data.UtilityMethods;

import java.util.ArrayList;
import java.util.List;

/**
 * Based on https://github.com/iMoHax/Trader/blob/master/client/src/main/java/ru/trader/view/support/cells/CheckComboBoxTableCell.java
 * @author James Kelley
 *
 * @param <S>
 * @param <T>
 */
public class CheckComboBoxTableCell<S,T> extends TableCell<ObservableList<?>, String> {
    private final CheckComboBox<String> box;
    private final ChangeListener<Boolean> onShowListener;
//    private ComboBox comboBox;

    public CheckComboBoxTableCell(final TableColumn<ObservableList<?>, String> column, 
    		final ObservableList<String> choiceList, ArrayList<Integer> ceesItNOCErrorRowList, 
    		ArrayList<Integer> ceesItDuplicateOutputRowList, 
    		ArrayList<Integer> ceesItExistingFileRowList, 
    		boolean startCEESItButtonClicked) {
        box = new CheckComboBox<>(choiceList);
        box.disableProperty().bind(column.editableProperty().not());
        box.setPrefWidth(Constants.KNOWN_CONTRIBUTORS_COMBO_WIDTH);
        
        onShowListener = (ov, o, n) -> {
            final TableView<ObservableList<?>> tableView = getTableView();
            if (n && !isEditing()) {
                tableView.getSelectionModel().select(getTableRow().getIndex());
                tableView.edit(tableView.getSelectionModel().getSelectedIndex(), column);
            } else {
                if (!n && isEditing()) {
                    cancelEdit();
                }
            }
        };
        
        box.getCheckModel().getCheckedItems().addListener((ListChangeListener<String>) c -> { 
        	final TableView<ObservableList<?>> tableView = getTableView();
        	ObservableList<?> row = (ObservableList<?>) tableView.getItems()
    				.get(getIndex());
        	updateTable(row, ceesItNOCErrorRowList, ceesItDuplicateOutputRowList, 
    				ceesItExistingFileRowList, startCEESItButtonClicked);
        	
        	// This is no longer necessary. Row can be updated without selecting row. Once thoroughly
        	// tested, this code below can be removed.
        	
//        	// Select the row to avoid null pointers
//        	tableView.getSelectionModel().select(getIndex());
//        	if (tableView.getSelectionModel().getSelectedIndex() > -1) {
//        		ObservableList<?> row = (ObservableList<?>) tableView.getItems()
//        				.get(tableView.getSelectionModel().getSelectedIndex());
//        		updateTable(row, ceesItNOCErrorRowList, ceesItDuplicateOutputRowList, 
//        				ceesItExistingFileRowList, startCEESItButtonClicked);
//        	} 
        });
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    public static <S,T> Callback<TableColumn<ObservableList<?>, String>, TableCell<ObservableList<?>, String>> forTableColumn(final TableColumn<ObservableList<?>, String> column, 
    		final ObservableList<T> choiceList, ArrayList<Integer> ceesItNOCErrorRowList, 
    		ArrayList<Integer> ceesItDuplicateOutputRowList, 
    		ArrayList<Integer> ceesItExistingFileRowList,
    		boolean startCEESItButtonClicked) {
    	return cell -> new CheckComboBoxTableCell<S, T>(cell, (ObservableList<String>) choiceList, 
    			ceesItNOCErrorRowList, ceesItDuplicateOutputRowList, ceesItExistingFileRowList, 
    			startCEESItButtonClicked);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        setText(null);
        if (empty || item == null) {
            setGraphic(null);
        } else {
            setGraphic(box);
            TableView<ObservableList<?>> tableView = getTableView();
        	ObservableList<?> row = (ObservableList<?>) tableView.getItems()
					.get(getIndex());
        	updateBox(row);
        	
        	// This is no longer necessary. Row can be updated without selecting row. Once thoroughly
        	// tested, this code below can be removed.
        	
//            if (getIndex() > -1) {
//            	//if (item != null && item.length() > 0) {
//            		TableView<ObservableList<?>> tableView = getTableView();
//                	ObservableList<?> row = (ObservableList<?>) tableView.getItems()
//        					.get(getIndex());
//                	int rowID = ((SimpleIntegerProperty) row.get(Constants.CEESIT_TABLE_COLUMN_ROW_ID_INDEX))
//							.get();
//                	System.out.println(rowID);
//                	updateBox(row);
//            	//}
//            }
        }
    }

    // Unsure if the commented out code below does anything.
    
//    private static ComboBox getComboBox(CheckComboBox box){
//        Skin skin = box.getSkin();
//        if (skin instanceof CheckComboBoxSkin){
//            Optional node = ((CheckComboBoxSkin) skin).getChildren().stream().findFirst();
//            if (node.isPresent() && node.get() instanceof ComboBox){
//                return (ComboBox) node.get();
//            }
//        }
//        return null;
//    }

//    @FunctionalInterface
//    public interface CheckedFunction<S, T> {
//
//        void apply(S entry, T item, boolean check);
//
//    }
    
    private void updateBox(ObservableList<?> row) {
    	String knownContributorsEntry = ((SimpleStringProperty) row.get(Constants.CEESIT_TABLE_COLUMN_KNOWN_CONTRIBUTORS_INDEX)).get();
		String[] knownContributorsArray = knownContributorsEntry.split(",");
		for (int i = 0; i < knownContributorsArray.length; i++) {
			boolean valid = true;
			try {
				box.getCheckModel().isChecked(knownContributorsArray[i]);
			} catch (Exception e) {
				valid = false;
			}
			if (valid) {
				if (!box.getCheckModel().isChecked(knownContributorsArray[i])) {
					box.getCheckModel().check(knownContributorsArray[i]);
				}
			}
		}
    }
    
    private void updateTable(ObservableList<?> row, ArrayList<Integer> ceesItNOCErrorRowList, 
    		ArrayList<Integer> ceesItDuplicateOutputRowList, 
    		ArrayList<Integer> ceesItExistingFileRowList, 
    		boolean startCEESItButtonClicked) {
    	// There should be some way to get the string displayed in the box, but until this
		// is figured out, this code generates a comma separated string that is the same
		// as the displayed string
    	String oldValue = ((SimpleStringProperty) row.get(Constants.CEESIT_TABLE_COLUMN_KNOWN_CONTRIBUTORS_INDEX)).get();
    	List<String> numbers = box.getCheckModel().getCheckedItems();
    	String listString = String.join(",", numbers);
    	((SimpleStringProperty) row.get(Constants.CEESIT_TABLE_COLUMN_KNOWN_CONTRIBUTORS_INDEX)).set(listString);
    	ObservableList<?> rowList = (ObservableList<?>) getTableView().getItems()
				.get(getIndex());
    	UtilityMethods.updateCEESItOutputName(getTableView(), rowList, getIndex());
    	updateBox(row);
    	// These checks are only run after start CEESIt button has been clicked, the value has been
    	// changed in the table, and the change requires the table to be refreshed to change color
    	// of text in one or more cells. Refreshing the table causes the combobox to close after
    	// checking only one entry.
    	if (!listString.equals(oldValue) && startCEESItButtonClicked) {
    		boolean change = false;
    		int rowID = ((SimpleIntegerProperty) getTableView().getItems().get(getIndex()).get(Constants.CEESIT_TABLE_COLUMN_ROW_ID_INDEX)).get();
    		//ceesItNOCErrorRowList.clear();
    		// Changing one file name will mess up the list of which files are duplicate. The entire
    		// set of files in the table could be checked but for now
    		// just clear the list. Hitting the Start button will run the check again. Existing
    		// file check has also been replaced with clearing the list.
    		if (ceesItDuplicateOutputRowList.size() > 0) {
    			ceesItDuplicateOutputRowList.clear();   
    			change = true;
    		}	
    		if (ceesItExistingFileRowList.size() > 0) {
    			ceesItExistingFileRowList.clear();   
    			change = true;
    		}
    		if (ceesItNOCErrorRowList.contains(rowID)) {
    			ceesItNOCErrorRowList.remove(ceesItNOCErrorRowList.indexOf(rowID));
    			change = true;
    		}
        	if (change) {
        		getTableView().refresh();
        	}
    	}   	
    }
}
