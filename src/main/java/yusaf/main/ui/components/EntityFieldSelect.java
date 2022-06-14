package yusaf.main.ui.components;

import java.util.List;
import java.util.concurrent.Callable;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EntityFieldSelect {
	private Stage window;

	public EntityFieldSelect(List<String> fields, Callable<Void> callable) {
		window = new Stage();
		window.setMinHeight(500);
		window.setMinWidth(500);

		ObservableList<EntityFieldSelectItem> dataList = FXCollections.observableArrayList();

		fields.forEach(field -> {
			EntityFieldSelectItem item = new EntityFieldSelectItem(field);
			dataList.add(item);
		});

		Button selectAll = new Button("Select All");
		Button clearAll = new Button("Clear All");
		Button saveButton = new Button("Save");
		Button cancelButton = new Button("Cancel");

		TableView<EntityFieldSelectItem> table = new TableView<>();
		TableColumn<EntityFieldSelectItem, Boolean> checkColumn = new TableColumn<>("Fetchable");
		TableColumn<EntityFieldSelectItem, String> nameColumn = new TableColumn<>("Field Name");

		checkColumn.setCellValueFactory(new PropertyValueFactory<>("fetchable"));
		checkColumn.setCellFactory(CheckBoxTableCell.forTableColumn(checkColumn));
		checkColumn.setEditable(true);

		nameColumn.setCellValueFactory(new PropertyValueFactory<>("fieldName"));
		nameColumn.setEditable(false);

		table.setEditable(true);
		table.getColumns().add(checkColumn);
		table.getColumns().add(nameColumn);
		table.setItems(dataList);

		selectAll.setOnAction((e) -> {
			table.getItems().forEach(item -> {
				item.fetchable.set(true);
			});
			table.refresh();
		});

		clearAll.setOnAction((e) -> {
			table.getItems().forEach(item -> {
				item.fetchable.set(false);
			});
			table.refresh();
		});

		saveButton.setOnAction((e) -> {
			try {
				callable.call();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			window.close();
		});

		cancelButton.setOnAction((e) -> {
			window.close();
		});

		HBox buttons = new HBox(selectAll, clearAll);
		HBox footer = new HBox(saveButton, cancelButton);
		VBox box = new VBox(buttons, table, footer);

		Scene scene = new Scene(box);
		window.setScene(scene);
	}

	public static class EntityFieldSelectItem {
		private String fieldName;
		private BooleanProperty fetchable = new SimpleBooleanProperty();

		EntityFieldSelectItem() {
		}

		EntityFieldSelectItem(String field) {
			this.fieldName = field;
			this.fetchable.setValue(false);
			;
		}

		public BooleanProperty fetchableProperty() {
			return fetchable;
		}

		public void setFetchable(boolean fetchable) {
			this.fetchable.set(fetchable);
		}

		public boolean isFetchable() {
			return this.fetchable.get();
		}

		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}

		public String getFieldName() {
			return fieldName;
		}
	}

	public void show() {
		window.centerOnScreen();
		window.show();
	}
}
