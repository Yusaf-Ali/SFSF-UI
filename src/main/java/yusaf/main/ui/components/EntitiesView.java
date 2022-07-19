package yusaf.main.ui.components;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
		entityCount.setAlignment(Pos.BASELINE_CENTER);
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

	public void populateDetailTable(List<Map<String, String>> listing, boolean append) {
		if (listing != null && listing.size() > 0) {
			if (!append) {
				if (filteredRows != null) {
					filteredRows.getSource().clear();
				} else {
					detailTable.getItems().clear();
				}
				detailTable.getColumns().clear();
				filterableFieldName.getItems().clear();
			}

			ObservableList<DynamicRow> rows = FXCollections.observableArrayList();
			if (append) {
				if (filteredRows != null) {
					rows.addAll(filteredRows.getSource());
				} else {
					rows.addAll(detailTable.getItems());
				}
			}

			if (append) {
				EntityInformation entityInfo = (EntityInformation) detailTable.getUserData();
				listing.stream().filter(newRow -> {
					for (DynamicRow existingRow : rows) {
						boolean matched = true;
						for (String key : entityInfo.getKeys()) {
							if (!existingRow.getFieldValues().get(key).equals(newRow.get(key))) {
								// When unmatched, it means this is distinct and should be retained.
								// Break this loop and set matched to false.
								matched = false;
								break;
							}
						}
						// If matched == false then do not break, check next record.
						// If matched == true then break because this record should not be included.
						if (matched)
							return false;
					}
					// If the whole loop ends, then simply return true because this record was not found.
					return true;
				}).forEach(itemRow -> {
					DynamicRow row = new DynamicRow(itemRow);
					rows.add(row);
				});
			} else {
				listing.forEach(itemRow -> {
					DynamicRow row = new DynamicRow(itemRow);
					rows.add(row);
				});
			}

			if (!append) {
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
			}

			filteredRows = new FilteredList<DynamicRow>(rows);
			SortedList<DynamicRow> sortedRows = new SortedList<>(filteredRows);
			sortedRows.comparatorProperty().bind(detailTable.comparatorProperty());

			detailTable.setItems(sortedRows);
			detailTable.refresh();

			filteredRows.addListener((ListChangeListener.Change<? extends DynamicRow> list) -> {
				updateEntityCount();
			});
			updateEntityCount();
			System.out.println("Cleared table and added " + listing.size() + " items to it from payload.");
		}
	}

	private void updateEntityCount() {
		try {
			int[] countArray = (int[]) entityCount.getUserData();
			entityCount.setText(detailTable.getItems().size() + " / " + countArray[1] + " / " + countArray[2]);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	private void showAlertNoRecordsFetched(String entityName) {
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle("Zero Fetch");
		alert.setHeaderText(entityName);
		alert.setContentText("There were no records in the response, if you have used a filter, try modifying it or this entity does not contain any record.");
		alert.showAndWait();
	}

	private EventHandler<MouseEvent> detailsActions = (event) -> {
		if (event.getButton() == MouseButton.PRIMARY) {
			EntityInformation selected = entityList.getTable().getSelectionModel().getSelectedItem();
			if (selected == null)
				return;
			EntityInformation currentEntity = (EntityInformation) detailTable.getUserData();
			String currentEntityName = currentEntity == null ? "" : currentEntity.getName();

			ViewEntityRecordsDialog input = new ViewEntityRecordsDialog(selected.getName(), currentEntityName);
			input.showAndWait();
			if (input.isCancelled() || !input.validateTopSkip() || input.getTop() == 0) {
				return;
			}

			Thread t = new Thread() {
				@Override
				public void run() {
					System.out.println(selected.getName() + "\t" + selected.getCount());

					List<String> selectList = selected.getAllFields().stream().collect(Collectors.toList());
					selectList.removeAll(selected.getIgnorables());

					boolean success = false;
					if (selected.getKeys().size() == 0 && selected.getAllFields().size() > 0) {
						String metadata = entityList.getSFSF().getEntityMetadata(selected.getName());
						try {
							Document doc = Utils.getDocument(metadata);
							NodeList keyTagList = doc.getElementsByTagName("Key");
							if (keyTagList.getLength() > 0) {
								Node keyTag = keyTagList.item(0);
								for (int index = 0; index < keyTag.getChildNodes().getLength(); index++) {
									Node node = keyTag.getChildNodes().item(index);
									String keyName = node.getAttributes().getNamedItem("Name").getTextContent();
									selected.getKeys().add(keyName);
								}
							}
							success = true;
						} catch (SAXException | IOException | ParserConfigurationException e) {
							System.err.println(e.getMessage());
						}
					} else {
						success = true;
					}

					for (int index = 0; index < selected.getKeys().size(); index++) {
						if (!selectList.contains(selected.getKeys().get(index))) {
							selectList.add(selected.getKeys().get(index));
						}
					}
					String selects = selectList.stream().collect(Collectors.joining(","));

					if (!success)
						return;
					String response = entityList.getSFSF()
							.getEntityRecords(selected.getName(), input.getTop(), input.getSkip(), selects, input.getFilter());
					if (response == null || response.equals("null") || response.startsWith("error:")) {
						System.err.println("Error response for " + selected.getName());
						String error = response == null ? "error:Response is null" : response;
						System.err.println(error.split("error:")[1]);

						Platform.runLater(() -> {
							Alert alertDialog = new Alert(AlertType.ERROR, error.split("error:")[1]);
							alertDialog.setHeaderText("There was error getting response from success factors!");
							alertDialog.showAndWait();
						});

						return;
					}

					int[] inlinecount = new int[] { 0, 0, 0 };
					List<Map<String, String>> list = SFSF.getResultsFromJsonInlineCount(response, inlinecount);
					if (list.size() > 0) {
						System.out.println("Inline count: " + inlinecount[2]);
						Platform.runLater(() -> {
							if (input.shouldAppend()) {
								inlinecount[1] += list.size();
							} else {
								inlinecount[1] = list.size();
							}
							inlinecount[0] = inlinecount[1];
							entityCount.setUserData(inlinecount);
							detailTable.setUserData(selected);
							populateDetailTable(list, input.shouldAppend());
						});
					} else {
						showAlertNoRecordsFetched(selected.getName());
					}
				}
			};
			t.start();
		}
	};

}