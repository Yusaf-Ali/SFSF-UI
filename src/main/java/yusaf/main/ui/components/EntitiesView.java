package yusaf.main.ui.components;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import utils.SFSF;
import utils.Utils;
import yusaf.main.ui.pojos.DynamicRow;
import yusaf.main.ui.pojos.EntityInformation;

public class EntitiesView {
	private TextField searchBar;
	private Label entityCount;
	private EntityListView entityList;
	private TableView<DynamicRow> detailTable;
	private ChoiceBox<String> filterableFieldName;
	private FilteredList<DynamicRow> filteredRows;

	public EntitiesView(EntityListView entityList) {
		this.entityList = entityList;
		detailTable = new TableView<>();
		TableColumn<DynamicRow, String> column = new TableColumn<>();
		column.setText("Select an Item to load some of its entities.");
		column.setMinWidth(500);
		detailTable.getColumns().add(column);

		searchBar = new TextField();
		searchBar.setMaxWidth(500);
		searchBar.setMinWidth(400);
		searchBar.setPrefWidth(450);
		searchBar.setPromptText("Type in something");
		searchBar.setDisable(true);

		entityCount = new Label("0 / 0");
		entityCount.setMaxWidth(120);
		entityCount.setMinWidth(80);
		entityCount.setAlignment(Pos.BASELINE_RIGHT);
		entityCount.setStyle("-fx-border-color: black");

		filterableFieldName = new ChoiceBox<>();
		filterableFieldName.setMaxWidth(200);
		filterableFieldName.setPrefWidth(150);
		filterableFieldName.setMinWidth(100);
		filterableFieldName.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			updateSearchPredicate(searchBar.getText());
		});

		searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
			updateSearchPredicate(newValue);
		});

		detailTable.itemsProperty().addListener((observable, oldValue, newValue) -> {
			if (detailTable.getItems().isEmpty()) {
				searchBar.setDisable(true);
			} else {
				searchBar.setDisable(false);
			}
		});
	}

	private void updateSearchPredicate(String newValue) {
		if (filterableFieldName.getSelectionModel().getSelectedItem() == null)
			return;
		filteredRows.setPredicate(p -> {
			if (newValue == null || newValue.isBlank() || newValue.isEmpty())
				return true;
			String selectedColumn = filterableFieldName.getSelectionModel().getSelectedItem();
			String v = p.getFieldValues().get(selectedColumn);

			if (newValue.startsWith("!")) {
				if ((v == null || v.equalsIgnoreCase("null")) &&
						newValue.substring(1, newValue.length() - 1).equalsIgnoreCase("null")) {
					return false;
				}
			} else {
				if ((v == null || v.equalsIgnoreCase("null")) && newValue.equalsIgnoreCase("null")) {
					return true;
				} else if (newValue.startsWith("!")) {
					return !v.contains(newValue);
				} else {
					return v.contains(newValue);
				}
			}
			return true;
		});
	}

	public TableView<DynamicRow> getDetailTable() {
		return detailTable;
	}

	public EventHandler<MouseEvent> getDetailsActions() {
		return detailsActions;
	}

	public Pane createView() {
		Region khlaa = new Region();
		khlaa.setStyle("-fx-border-color: red");
		HBox top = new HBox(searchBar, filterableFieldName, khlaa, entityCount);
		entityCount.prefHeightProperty().bind(top.heightProperty());
		HBox.setHgrow(khlaa, Priority.ALWAYS);
		VBox pane = new VBox(top, detailTable);
		return pane;
	}

	public void populateDetailTable(List<Map<String, String>> listing) {
		if (listing != null && listing.size() > 0) {
			System.out.println("Cleared table and added " + listing.size() + " items to it from payload.");
			if (filteredRows != null)
				filteredRows.getSource().clear();
			else
				detailTable.getItems().clear();
			detailTable.getColumns().clear();
			filterableFieldName.getItems().clear();

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
				filterableFieldName.getItems().add(fieldName);
			});

			filteredRows = new FilteredList<DynamicRow>(rows);
			SortedList<DynamicRow> sortedRows = new SortedList<>(filteredRows);
			sortedRows.comparatorProperty().bind(detailTable.comparatorProperty());

			detailTable.setItems(sortedRows);
			detailTable.refresh();

			filteredRows.addListener((ListChangeListener.Change<? extends DynamicRow> list) -> {
				updateEntityCount();
			});
		}
	}

	private void updateEntityCount() {
		try {
			String entityCountText = entityCount.getText();
			if (entityCountText.length() > 0 && Utils.isInteger(entityCountText.split(" / ")[2])) {
				int totalCount = Integer.parseInt(entityCountText.split(" / ")[2]);
				int currentTotalCount = Integer.parseInt(entityCountText.split(" / ")[1]);
				entityCount.setText(detailTable.getItems().size() + " / " + currentTotalCount + " / " + totalCount);
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	private EventHandler<MouseEvent> detailsActions = (event) -> {
		if (event.getButton() == MouseButton.PRIMARY) {
			EntityInformation selected = entityList.getTable().getSelectionModel().getSelectedItem();
			if (selected == null)
				return;

			TextInputDialog numberOfRecords = new TextInputDialog("10");
			numberOfRecords.setTitle("View: " + selected.getName());
			numberOfRecords.setHeaderText("Select $top");
			numberOfRecords.setContentText("Number must be greater than 0");
			numberOfRecords.getEditor().textProperty().addListener((e, o, n) -> {
				if (n.length() != 0 && (!Utils.isInteger(n) || Integer.parseInt(n) < 0)) {
					numberOfRecords.getEditor().setText(o);
				}
			});
			Optional<String> showAndWait = numberOfRecords.showAndWait();
			int top[] = new int[] { 1 };
			if (showAndWait.isPresent()) {
				try {
					top[0] = Integer.parseInt(showAndWait.get());
					if (top[0] == 0)
						throw new NumberFormatException("0 is not selectable");
				} catch (NumberFormatException ex) {
					System.err.println("Invalid input! " + ex.getMessage());
				}
			} else {
				return;
			}
			Thread t = new Thread() {
				@Override
				public void run() {
					System.out.println(selected.getName() + "\t" + selected.getCount());

					List<String> selectList = selected.getAllFields().stream().collect(Collectors.toList());
					selectList.removeAll(selected.getIgnorables());
					String selects = selectList.stream().collect(Collectors.joining(","));

					String response = entityList.getSFSF().getEntityRecords(selected.getName(), top[0], 0, selects);
					if (response == null || response.equals("null") || response.startsWith("error:")) {
						System.err.println("Error response for " + selected.getName());
						String error = response == null ? "error:Response is null" : response;
						System.err.println(error.split("error:")[1]);

						Platform.runLater(() -> {
							Alert alertDialog = new Alert(AlertType.ERROR, error.split("error:")[1]);
							alertDialog.setHeaderText("There was error getting resposne from success factors!");
							alertDialog.showAndWait();
						});

						return;
					}

					int[] inlinecount = new int[] { 0 };
					List<Map<String, String>> list = SFSF.getResultsFromJsonInlineCount(response, inlinecount);
					System.out.println(inlinecount[0]);
					Platform.runLater(() -> {
						entityCount.setText(list.size() + " / " + list.size() + " / " + inlinecount[0]);
						populateDetailTable(list);
					});
				}
			};
			t.start();
		}
	};
}