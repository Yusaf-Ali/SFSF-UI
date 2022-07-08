package yusaf.main.ui.components;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import utils.SFSF;
import utils.Utils;
import yusaf.main.ui.components.EntityListView.EntityInformation;

public class EntitiesView {
	private EntityListView entityList;
	private TableView<DynamicRow> detailTable;

	public EntitiesView(EntityListView entityList) {
		this.entityList = entityList;
		detailTable = new TableView<>();
		TableColumn<DynamicRow, String> column = new TableColumn<>();
		column.setText("Select an Item to load some of its entities.");
		column.setMinWidth(500);
		detailTable.getColumns().add(column);
		searchBar = new TextField();
		searchBar.setMaxWidth(400);
		searchBar.setPromptText("Type in something");
		searchBar.setDisable(true);
		detailTable.itemsProperty().addListener((observable, oldValue, newValue) -> {
			if (detailTable.getItems().isEmpty()) {
				searchBar.setDisable(true);
			} else {
				searchBar.setDisable(false);
			}
		});
	}

	public TableView<DynamicRow> getDetailTable() {
		return detailTable;
	}

	public EventHandler<MouseEvent> getDetailsActions() {
		return detailsActions;
	}

	public Pane createView() {
		VBox pane = new VBox(searchBar, detailTable);
		return pane;
	}

	public void populateDetailTable(List<Map<String, String>> listing) {
		if (listing != null && listing.size() > 0) {
			System.out.println("Cleared table and added " + listing.size() + " items to it from payload.");
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

	private EventHandler<MouseEvent> detailsActions = (event) -> {
		if (event.getButton() == MouseButton.PRIMARY) {
			TextInputDialog numberOfRecords = new TextInputDialog("10");
			numberOfRecords.setTitle("View");
			numberOfRecords.setHeaderText("Select $top");
			numberOfRecords.setContentText("Number must be greater than 0");
			numberOfRecords.getEditor().textProperty().addListener((e, o, n) -> {
				if (!Utils.isInteger(n) || Integer.parseInt(n) < 1) {
					numberOfRecords.getEditor().setText(o);
				}
			});
			Optional<String> showAndWait = numberOfRecords.showAndWait();
			int top[] = new int[] { 1 };
			if (showAndWait.isPresent()) {
				try {
					top[0] = Integer.parseInt(showAndWait.get());
				} catch (NumberFormatException ex) {
					System.err.println("Invalid input! " + ex.getMessage());
				}
			}
			Thread t = new Thread() {
				@Override
				public void run() {
					EntityInformation selected = entityList.getTable().getSelectionModel().getSelectedItem();
					if (selected == null)
						return;
					System.out.println(selected.getName() + "\t" + selected.getCount());

					List<String> selectList = selected.getAllFields().stream().collect(Collectors.toList());
					selectList.removeAll(selected.getIgnorables());
					String selects = selectList.stream().collect(Collectors.joining(","));

					String response = entityList.getSFSF().getEntityRecords(selected.getName(), top[0], 0, selects);
					if (response == null || response.equals("null") || response.startsWith("error:")) {
						System.err.println("Error response for " + selected.getName());
						System.err.println(response.split("error:")[1]);
						Platform.runLater(() -> {
							Alert alertDialog = new Alert(AlertType.ERROR, response.split("error:")[1]);
							alertDialog.setHeaderText("There was error getting resposne from success factors!");
							alertDialog.showAndWait();
						});

						return;
					}

					int[] inlinecount = new int[] { 0 };
					List<Map<String, String>> list = SFSF.getResultsFromJsonInlineCount(response, inlinecount);
					System.out.println(inlinecount[0]);
					Platform.runLater(() -> populateDetailTable(list));
				}
			};
			t.start();
		}
	};
	private TextField searchBar;

	public static class DynamicRow {
		Map<String, String> fieldValues;

		public DynamicRow(Map<String, String> fieldValues) {
			this.fieldValues = fieldValues;
		}

		public Map<String, String> getFieldValues() {
			return fieldValues;
		}
	}
}