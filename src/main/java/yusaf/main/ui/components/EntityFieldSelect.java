package yusaf.main.ui.components;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EntityFieldSelect {
	private Stage window;
	private ObservableList<EntityFieldSelectItem> dataList;
	private Callable<Void> saveAction;
	private List<String> initIgnoredItems;
	private List<String> initAllFields;
	private ProgressBar progressBar = new ProgressBar();

	public ProgressBar getProgressBar() {
		return progressBar;
	}

	public EntityFieldSelect() {
		dataList = FXCollections.observableArrayList();
	}

	public void render() {
		window = new Stage();
		window.setMinHeight(500);
		window.setMinWidth(500);

	}

	public void populateData(List<String> fields) {
		initAllFields = fields;
		if (initIgnoredItems == null) {
			initAllFields.forEach(field -> {
				EntityFieldSelectItem item = new EntityFieldSelectItem(field, true);
				dataList.add(item);
			});
		} else {
			initAllFields.forEach(field -> {
				boolean isAlreadyIgnored = initIgnoredItems.stream().filter(f -> f.equals(field)).count() > 0;
				EntityFieldSelectItem item = new EntityFieldSelectItem(field, !isAlreadyIgnored);
				dataList.add(item);
			});
		}

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
				saveAction.call();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			window.close();
		});

		cancelButton.setOnAction((e) -> {
			window.close();
		});

		HBox buttons = new HBox(selectAll, clearAll);
		HBox footer = new HBox(saveButton, cancelButton, progressBar);
		VBox box = new VBox(buttons, table, footer);

		Scene scene = new Scene(box);
		window.setScene(scene);
	}

	public void setIgnoredItems(List<String> ignorables) {
		this.initIgnoredItems = ignorables;
	}

	public List<String> getIgnoredItems() {
		return dataList.stream().filter(f -> !f.isFetchable())
				.map(m -> m.getFieldName()).collect(Collectors.toList());
	}

	public void setSaveAction(Callable<Void> saveActionCallable) {
		this.saveAction = saveActionCallable;
	}

	public static class EntityFieldSelectItem {
		private String fieldName;
		private BooleanProperty fetchable = new SimpleBooleanProperty();

		EntityFieldSelectItem() {
		}

		EntityFieldSelectItem(String field) {
			this.fieldName = field;
			this.fetchable.setValue(false);
		}

		EntityFieldSelectItem(String field, boolean checked) {
			this.fieldName = field;
			this.fetchable.setValue(checked);
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