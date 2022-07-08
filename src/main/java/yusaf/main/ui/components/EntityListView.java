package yusaf.main.ui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import utils.IgnorableEntityHandler;
import utils.SFSF;
import utils.ThreadManager;
import utils.Utils;
import yusaf.main.ui.MainFrame;

public class EntityListView {
	private TableView<EntityInformation> table;
	ObservableList<EntityInformation> fullList;
	private SFSF sfsf;
	private TextField searchBar;
	private ProgressBar pb;
	private FilteredList<EntityInformation> filteredList;

	public EntityListView(ProgressBar pb) {
		this.pb = pb;
		searchBar = new TextField();
		searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
			filteredList.setPredicate(p -> {
				if (newValue == null || newValue.isBlank() || newValue.isEmpty())
					return true;
				return p.getName().toLowerCase().contains(newValue.toLowerCase());
			});
		});
	}

	public void setSFSF(SFSF sfsf) {
		this.sfsf = sfsf;
	}

	public SFSF getSFSF() {
		return sfsf;
	}

	public ObservableList<EntityInformation> getFullList() {
		return fullList;
	}

	public void initTableViewFromList(EventHandler<? super MouseEvent> onRowClicked) {
		TableColumn<EntityInformation, String> cName = new TableColumn<>("Name");
		TableColumn<EntityInformation, String> cCount = new TableColumn<>("Entity count");
		EntityListTableContextMenu contextMenu = new EntityListTableContextMenu(sfsf, this);

		cName.setCellValueFactory(new PropertyValueFactory<>("name"));
		cCount.setCellValueFactory(new PropertyValueFactory<>("count"));

		table = new TableView<>();
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
		table.setContextMenu(contextMenu.getMenu());
	}

	public void populateData(List<String> entities) {
		// Set data, Note: this is time consuming
		fullList = FXCollections.observableArrayList(entities.stream().map(m -> new EntityInformation(m, "Waiting")).collect(Collectors.toList()));
		searchability();
		Platform.runLater(() -> table.refresh());
	}

	private void searchability() {
		filteredList = new FilteredList<EntityListView.EntityInformation>(
				fullList);
		SortedList<EntityInformation> sortableFilteredList = new SortedList<>(filteredList);
		sortableFilteredList.comparatorProperty().bind(table.comparatorProperty());
		table.setItems(sortableFilteredList);
	}

	public Pane createView() {
		HBox topBar = null;
		topBar = new HBox(searchBar);

		VBox box = new VBox(topBar, table);
		return box;
	}

	public void refresh() {
		List<Thread> registeredThreads = new ArrayList<>();
		table.getItems().forEach(item -> {
			if (!IgnorableEntityHandler.ignorables().contains(item.getName())) {
				Thread registeredThread = createTaskThread(item);
				registeredThreads.add(registeredThread);
			} else {
				System.out.println("Ignored: " + item.getName());
			}
		});
		ThreadManager.runTasks(registeredThreads, 1000 / 20, MainFrame.getThreadList(), pb);
	}

	public void refreshIgnored() {
		List<Thread> registeredThreads = new ArrayList<>();
		table.getItems().forEach(item -> {
			if (IgnorableEntityHandler.ignorables().contains(item.getName())) {
				// If the entity is in ignored list
				Thread registeredThread = createTaskThread(item);
				registeredThreads.add(registeredThread);
			}
		});
		ThreadManager.runTasks(registeredThreads, 1000 / 20, MainFrame.getThreadList(), pb);
	}

	public void refreshAll() {
		List<Thread> registeredThreads = new ArrayList<>();
		fullList.forEach(item -> {
			Thread registeredThread = createTaskThread(item);
			registeredThreads.add(registeredThread);
		});
		ThreadManager.runTasks(registeredThreads, 1000 / 20, MainFrame.getThreadList(), pb);
	}

	public void clearIgnorables() {
		Thread thread = new Thread() {
			@Override
			public void run() {
				table.getItems().forEach(item -> {
					if (item.getCount().equalsIgnoreCase("Ignored")) {
						item.setCount("N/A");
					}
				});
				IgnorableEntityHandler.removeFromIgnorables(table.getItems(), false);
				Platform.runLater(() -> {
					table.refresh();
				});
			}
		};
		thread.start();
	}

	private Thread createTaskThread(EntityInformation item) {
		Thread registeredThread = new Thread(() -> {
			item.setCount("Retrieving...");
			table.refresh();
			String content = sfsf.getEntityCount(item.getName());

			if (Utils.isInteger(content)) {
				item.setCount(content);
				item.setNumeric(true);
				IgnorableEntityHandler.removeFromIgnorables(List.of(item), true);
			} else {
				item.setCount("N/A");
				item.setNumeric(false);
				IgnorableEntityHandler.addInIgnorables(item.getName());
			}
			table.refresh();
		});
		return registeredThread;
	}

	public TableView<EntityInformation> getTable() {
		return table;
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
}