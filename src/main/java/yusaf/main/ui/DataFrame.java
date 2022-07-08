package yusaf.main.ui;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import utils.IgnorableEntityHandler;
import utils.SFSF;
import utils.Utils;
import yusaf.main.ui.components.EntitiesView;
import yusaf.main.ui.components.EntityListView;

public class DataFrame {
	ProgressBar progressBar;
	private SFSF sfsf;
	private static EntityListView entityList;
	private static EntitiesView entitiesView;

	public DataFrame(SFSF sfsf) {
		this.sfsf = sfsf;
		progressBar = new ProgressBar();
		entityList = new EntityListView(progressBar);
		entityList.setSFSF(sfsf);
		entitiesView = new EntitiesView(entityList);
	}

	public Pane render(Stage stage) {
		try {
//			String content = meta.read(config.baseUrl);
//			writeToFile(content);
//			String content = readFromFile();
			MenuBar menuBar = menu();
			HBox statusBar = statusBar();

			entityList.initTableViewFromList(entitiesView.getDetailsActions());

			progressBar.setProgress(0.2);
			Pane panel = new HBox(1, entityList.createView(), entitiesView.createView());

			progressBar.setProgress(0.3);
			entityList.getTable().prefHeightProperty().bind(stage.heightProperty());
			entityList.getTable().prefWidthProperty().bind(stage.widthProperty().multiply(0.25));
			entitiesView.getDetailTable().prefHeightProperty().bind(stage.heightProperty());
			entitiesView.getDetailTable().prefWidthProperty().bind(stage.widthProperty().multiply(0.75));

			BorderPane mainPane = new BorderPane();
			mainPane.setTop(menuBar);
			mainPane.setBottom(statusBar);
			mainPane.setCenter(panel);

			Thread backgroundDataLoad = new Thread(() -> {
				// List<String> entities = preprocess();
				try {
					entityList.populateData(preprocess());
				} catch (SAXException | IOException | ParserConfigurationException e) {
					e.printStackTrace();
				}

				List<Thread> nonregisteredThreads = createTaskThreadsForEntityCount(entityList, sfsf, true);
				MainFrame.getThreadList().addAll(nonregisteredThreads);
				progressBar.setProgress(1);
			});
			backgroundDataLoad.start();

			/*
			 * // Run 20 threads in one second, gap evaluates to 1000 / 20 = 50; ThreadManager.runTasks(nonregisteredThreads, 1000 /
			 * 20, threadList, () -> { IgnorableEntityHandler.ignorables().forEach(ign -> { System.out.println(ign); });
			 * IgnorableEntityHandler.saveIgnorables(); return null; });
			 */
			return mainPane;
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return null;
	}

	private List<String> preprocess() throws SAXException, IOException, ParserConfigurationException {
		System.out.println("Reading entity names from content.xml");
		InputStream is = new FileInputStream("content.xml");
		Document doc = Utils.getDocument(is);
		List<String> entities = getEntityListFromFile(doc);
		return entities;
	}

	private MenuBar menu() {
		MenuBar topBar = new MenuBar();
		Menu optionsMenu = new Menu("Options");
		topBar.getMenus().add(optionsMenu);

		MenuItem refresh = new MenuItem("Refresh (Filtered)");
		MenuItem refreshIgnored = new MenuItem("Refresh Ignored (Filtered)");
		MenuItem refreshAll = new MenuItem("Refresh All");
		MenuItem clearIgnorables = new MenuItem("Clear Ignorables");

		optionsMenu.getItems().add(refresh);
		optionsMenu.getItems().add(refreshIgnored);
		optionsMenu.getItems().add(refreshAll);
		optionsMenu.getItems().add(new SeparatorMenuItem());
		optionsMenu.getItems().add(clearIgnorables);

		refresh.setOnAction(e -> entityList.refresh());
		refreshAll.setOnAction(e -> entityList.refreshAll());
		refreshIgnored.setOnAction(e -> entityList.refreshIgnored());
		clearIgnorables.setOnAction(e -> entityList.clearIgnorables());

		return topBar;
	}

	private HBox statusBar() {
		HBox bar = new HBox(progressBar);
		return bar;
	}

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