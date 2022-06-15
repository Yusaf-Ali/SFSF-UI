package yusaf.main.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import utils.SFSF;
import yusaf.main.ui.components.EntityListTableContextMenu;

public class EntityListView {
	private TableView<EntityInformation> table;
	private TableView<DynamicRow> detailTable;
	private EventHandler<ActionEvent> refreshEventHandler;
	private EventHandler<ActionEvent> clearEventHandler;
	private SFSF sfsf;

	public void setSFSF(SFSF sfsf) {
		this.sfsf = sfsf;
	}

	public SFSF getSFSF() {
		return sfsf;
	}

	public void setRefreshEventHandler(EventHandler<ActionEvent> eventHandler) {
		this.refreshEventHandler = eventHandler;
	}

	public void setClearEventHandler(EventHandler<ActionEvent> eventHandler) {
		this.clearEventHandler = eventHandler;
	}

	public TableView<EntityInformation> createEntityTableViewFromList(List<String> entities,
			EventHandler<? super MouseEvent> onRowClicked) {
		TableColumn<EntityInformation, String> cName = new TableColumn<>("Name");
		TableColumn<EntityInformation, String> cCount = new TableColumn<>("Entity count");
		EntityListTableContextMenu contextMenu = new EntityListTableContextMenu(sfsf, this);

		cName.setCellValueFactory(new PropertyValueFactory<>("name"));
		cCount.setCellValueFactory(new PropertyValueFactory<>("count"));

		ObservableList<EntityInformation> list = FXCollections.observableArrayList();
		table = new TableView<>();
		table.setItems(list);
		// Set columns
		table.getColumns().add(cName);
		table.getColumns().add(cCount);

		cName.prefWidthProperty().bind(table.widthProperty().multiply(0.75));
		cCount.prefWidthProperty().bind(table.widthProperty().multiply(0.2));

		table.setRowFactory(f -> {
			TableRow<EntityInformation> row = new TableRow<>();
			row.setOnMouseClicked(onRowClicked);
			return row;
		});

		// Set data
		entities.forEach(entity -> {
			list.add(new EntityInformation(entity, "Waiting"));
		});

		table.setContextMenu(contextMenu.getMenu());
		return table;
	}

	public VBox createTableAndSearchBarView(TableView<EntityInformation> table, ProgressBar progressBar) {
		FilteredList<EntityInformation> filteredList = new FilteredList<EntityListView.EntityInformation>(
				table.getItems());
		TextField searchBar = new TextField();
		searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
			filteredList.setPredicate(p -> {
				if (newValue == null || newValue.isBlank() || newValue.isEmpty())
					return true;
				return p.getName().toLowerCase().contains(newValue.toLowerCase());
			});
		});
		SortedList<EntityInformation> sortableFilteredList = new SortedList<>(filteredList);
		sortableFilteredList.comparatorProperty().bind(table.comparatorProperty());
		table.setItems(sortableFilteredList);

		Button clearIgnorables = new Button("Clear Ignorables");
		clearIgnorables.setOnAction(clearEventHandler);

		Button refreshButton = new Button("Refresh");
		refreshButton.setOnAction(refreshEventHandler);

		HBox topBar = null;
		if (progressBar == null) {
			topBar = new HBox(searchBar, refreshButton, clearIgnorables);
		} else {
			topBar = new HBox(searchBar, refreshButton, clearIgnorables, progressBar);
			progressBar.prefHeightProperty().bind(topBar.heightProperty());
		}

		VBox box = new VBox(topBar, table);
		return box;
	}

	public Scene createSceneWithTableView(TableView<EntityInformation> table) {
		VBox box = createTableAndSearchBarView(table, null);
		Scene scene = new Scene(box);
		return scene;
	}

	public Scene createSceneWithDetailView(TableView<EntityInformation> table, ProgressBar progressBar) {
		VBox vBox = createTableAndSearchBarView(table, progressBar);
		HBox box = new HBox(vBox, detailTable);
		Scene scene = new Scene(box);
		return scene;
	}

	public void createEmptyDetailTable() {
		detailTable = new TableView<>();
		TableColumn<DynamicRow, String> column = new TableColumn<>();
		column.setText("Select an Item to load some of its entities.");
		column.setMinWidth(500);
		detailTable.getColumns().add(column);
//		detailTable.setMinWidth(500);
	}

	public TableView<EntityInformation> getTable() {
		return table;
	}

	public TableView<DynamicRow> getDetailTable() {
		return detailTable;
	}

	public static class EntityInformation {
		private String name;
		private String count;
		private List<String> allFields = new ArrayList<>();
		private List<String> ignoredFields = new ArrayList<>();
		private boolean numeric;

		public EntityInformation(String name, String count) {
			this.name = name;
			this.count = count;
		}

		public String getName() {
			return name;
		}

		public String getCount() {
			return count;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setCount(String count) {
			this.count = count;
		}

		public List<String> getAllFields() {
			return allFields;
		}

		public List<String> getIgnorables() {
			return ignoredFields;
		}

		public void setAllFields(List<String> allFields) {
			this.allFields = allFields;
		}

		public void setIgnoredFields(List<String> ignoredFields) {
			this.ignoredFields = ignoredFields;
		}

		public void setNumeric(boolean is) {
			this.numeric = is;
		}

		public boolean isNumeric() {
			return numeric;
		}

		@Override
		public String toString() {
			return name + " " + count;
		}
	}

	public static class DynamicRow {
		Map<String, String> fieldValues;

		public DynamicRow(Map<String, String> fieldValues) {
			this.fieldValues = fieldValues;
		}

		public Map<String, String> getFieldValues() {
			return fieldValues;
		}
	}

	public void populateDetailTable(List<Map<String, String>> listing) {
		if (listing != null && listing.size() > 0) {
			System.out.println(listing.size());
			detailTable.getItems().clear();
			detailTable.getColumns().clear();

			ObservableList<DynamicRow> rows = FXCollections.observableArrayList();
			listing.forEach(itemRow -> {
				DynamicRow row = new DynamicRow(itemRow);
				rows.add(row);
			});

			Callback<CellDataFeatures<DynamicRow, String>, ObservableValue<String>> callback = new Callback<>() {
				@Override
				public ObservableValue<String> call(CellDataFeatures<DynamicRow, String> cell) {
					String fieldName = cell.getTableColumn().getText();
					SimpleStringProperty property = new SimpleStringProperty(
							cell.getValue().getFieldValues().get(fieldName));
					return property;
				};
			};

			Map<String, String> firstRow = listing.get(0);
			firstRow.keySet().forEach(fieldName -> {
				TableColumn<DynamicRow, String> col = new TableColumn<DynamicRow, String>(fieldName);
				col.setCellValueFactory(callback);
				detailTable.getColumns().add(col);
			});

			detailTable.setItems(rows);
			detailTable.refresh();
		}
	}
}