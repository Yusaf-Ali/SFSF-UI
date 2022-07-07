package yusaf.main.ui;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import utils.IgnorableEntityHandler;
import utils.SFSF;
import utils.ThreadManager;
import utils.Utils;
import yusaf.main.ui.components.EntitiesView;
import yusaf.main.ui.components.EntityListView;
import yusaf.main.ui.components.EntityListView.EntityInformation;

public class DataFrame {
	ProgressBar progressBar;
	private SFSF sfsf;
	private static EntityListView entityList;
	private static EntitiesView entitiesView;

	public DataFrame(ProgressBar progressBar) {
		this.progressBar = progressBar;
	}

	public Pane render(Stage stage, SFSF sfsf) {
		this.sfsf = sfsf;
		try {
//			String content = meta.read(config.baseUrl);
//			writeToFile(content);
//			String content = readFromFile();

			System.out.println("Reading entity names from content.xml");
			progressBar.setProgress(0);
			InputStream is = new FileInputStream("content.xml");
			Document doc = Utils.getDocument(is);
			List<String> entities = getEntityListFromFile(doc);
			progressBar.setProgress(1);

			entityList = new EntityListView();
			entityList.setSFSF(sfsf);
			entityList.createEntityTableViewFromList(entities, detailsActions);
			progressBar.setProgress(0);
			entityList.createEmptyDetailTable();

			Pane panel = entityList.createSceneWithDetailView(entityList.getTable(), progressBar);
			entityList.getTable().prefHeightProperty().bind(stage.heightProperty());
			entityList.getTable().prefWidthProperty().bind(stage.widthProperty().multiply(0.25));
			entityList.getDetailTable().prefHeightProperty().bind(stage.heightProperty());
			entityList.getDetailTable().prefWidthProperty().bind(stage.widthProperty().multiply(0.75));
			progressBar.setProgress(1);

			// As this takes time to run.
			Thread t = new Thread() {
				@Override
				public void run() {
					List<Thread> nonregisteredThreads = createTaskThreadsForEntityCount(entityList, sfsf, true);
					MainFrame.getThreadList().addAll(nonregisteredThreads);
				}
			};
			t.start();

			/*
			 * // Run 20 threads in one second, gap evaluates to 1000 / 20 = 50; ThreadManager.runTasks(nonregisteredThreads, 1000 /
			 * 20, threadList, () -> { IgnorableEntityHandler.ignorables().forEach(ign -> { System.out.println(ign); });
			 * IgnorableEntityHandler.saveIgnorables(); return null; });
			 */
			return panel;
		} catch (Exception e) {
		}
		return null;
	}

	public void refresh() {
		List<Thread> registeredThreads = new ArrayList<>();
		entityList.getTable().getItems().forEach(item -> {
			if (!IgnorableEntityHandler.ignorables().contains(item.getName())) {
				Thread registeredThread = createTaskThread(item);
				registeredThreads.add(registeredThread);
			} else {
				System.out.println("Ignored: " + item.getName());
			}
		});
		ThreadManager.runTasks(registeredThreads, 1000 / 20, MainFrame.getThreadList());
	}

	public void refreshIgnored() {
		List<Thread> registeredThreads = new ArrayList<>();
		entityList.getTable().getItems().forEach(item -> {
			if (IgnorableEntityHandler.ignorables().contains(item.getName())) {
				// If the entity is in ignored list
				Thread registeredThread = createTaskThread(item);
				registeredThreads.add(registeredThread);
			}
		});
		ThreadManager.runTasks(registeredThreads, 1000 / 20, MainFrame.getThreadList());
	}

	public void refreshAll() {
		List<Thread> registeredThreads = new ArrayList<>();
		entityList.getFullList().forEach(item -> {
			Thread registeredThread = createTaskThread(item);
			registeredThreads.add(registeredThread);
		});
		ThreadManager.runTasks(registeredThreads, 1000 / 20, MainFrame.getThreadList());
	}

	public void clearIgnorables() {
		Thread thread = new Thread() {
			@Override
			public void run() {
				entityList.getTable().getItems().forEach(item -> {
					if (item.getCount().equalsIgnoreCase("Ignored")) {
						item.setCount("N/A");
					}
				});
				IgnorableEntityHandler.removeFromIgnorables(entityList.getTable().getItems(), false);
				Platform.runLater(() -> {
					entityList.getTable().refresh();
				});
			}
		};
		thread.start();
	}

	private Thread createTaskThread(EntityInformation item) {
		Thread registeredThread = new Thread(() -> {
			item.setCount("Retrieving...");
			entityList.getTable().refresh();
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
			entityList.getTable().refresh();
		});
		return registeredThread;
	}

	EventHandler<MouseEvent> detailsActions = (event) -> {
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
					Platform.runLater(() -> entityList.populateDetailTable(list));
				}
			};
			t.start();
		}
	};

	private static List<String> getEntityListFromFile(Document doc) {
		NodeList elementsByTagName = doc.getElementsByTagName("collection");
		List<String> entities = new ArrayList<>(elementsByTagName.getLength());
		for (int index = 0; index < elementsByTagName.getLength(); index++) {
			Node item = elementsByTagName.item(index);
			entities.add(item.getTextContent());
		}
		return entities;
	}

	private static List<Thread> createTaskThreadsForEntityCount(EntityListView entityList, SFSF sfsf, boolean shouldIgnore) {
		List<Thread> registeredThreads = new ArrayList<>();
		entityList.getTable().getItems().forEach(item -> {
			// Avoid proceeding if the thread is interrupted
			if (Thread.currentThread().isInterrupted())
				return;
			if (shouldIgnore && IgnorableEntityHandler.ignorables().contains(item.getName())) {
				item.setCount("Ignored");
				entityList.getTable().refresh();
				return;
			}
			Thread registeredThread = new Thread(() -> {
				item.setCount("Retrieving...");
				entityList.getTable().refresh();
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
				entityList.getTable().refresh();
			});
			registeredThreads.add(registeredThread);
		});
		return registeredThreads;
	}
}