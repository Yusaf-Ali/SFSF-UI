package yusaf.main.ui;

import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class EntityListView {
	private TableView<EntityInformation> table;
	private TableView<Map<String, String>> detailTable;

	public TableView<EntityInformation> createEntityTableViewFromList(List<String> entities) {
		TableColumn<EntityInformation, String> cName = new TableColumn<>("Name");
		TableColumn<EntityInformation, String> cCount = new TableColumn<>("Entity count");

		// TODO set better heights and widths
		cName.setMinWidth(300);
		cCount.setMinWidth(200);
		cName.setPrefWidth(500 - 200 - 15); // 15 scrollbar
		cName.setCellValueFactory(new PropertyValueFactory<>("name"));
		cCount.setCellValueFactory(new PropertyValueFactory<>("count"));

		ObservableList<EntityInformation> list = FXCollections.observableArrayList();
		table = new TableView<>();
		table.setItems(list);
		// Set columns
		table.getColumns().add(cName);
		table.getColumns().add(cCount);

		// Set data
		entities.forEach(entity -> {
			list.add(new EntityInformation(entity, "Waiting"));
		});
		return table;
	}

	public Scene createSceneWithTableView(TableView<EntityInformation> table) {
		VBox box = new VBox(table);
		table.setMinHeight(500);
		table.setMinWidth(500);
		Scene scene = new Scene(box, 500, 500);
		return scene;
	}

	public Scene createSceneWithDetailView(TableView<EntityInformation> table) {
		table.setMinHeight(500);
		table.setMinWidth(500);
		detailTable.setMinHeight(500);
		detailTable.setMinWidth(500);

		HBox box = new HBox(table, detailTable);
		Scene scene = new Scene(box, 1000, 500);
		return scene;
	}

	public void createEmptyDetailTable() {
		detailTable = new TableView<>();
		TableColumn<Map<String, String>, String> column = new TableColumn<>();
		column.setText("Select an Item to load all its entities.");
		column.setMinWidth(500);
		detailTable.getColumns().add(column);
		detailTable.setPrefWidth(500);
	}

	public TableView<EntityInformation> getTable() {
		return table;
	}

	public TableView<Map<String, String>> getDetailTable() {
		return detailTable;
	}

	public static class EntityInformation {
		private String name;
		private String count;

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
	}
}