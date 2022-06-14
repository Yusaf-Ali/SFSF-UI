package yusaf.main.ui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseButton;
import javafx.stage.Screen;
import javafx.stage.Stage;
import utils.DefaultConfiguration;
import utils.IgnorableEntityHandler;
import utils.SFSF;
import utils.ThreadManager;
import utils.Utils;
import yusaf.main.ui.EntityListView.EntityInformation;

public class MainFrame extends Application {
	private static Scene scene;
	private static List<Thread> threadList = new ArrayList<>();
	private static ProgressBar progressBar = new ProgressBar();

	@Override
	public void start(Stage stage) throws Exception {
		DefaultConfiguration config = new DefaultConfiguration();
		SFSF sfsf = new SFSF(config);
		IgnorableEntityHandler.readIgnorables();
		try {
//			String content = meta.read(config.baseUrl);
//			writeToFile(content);
//			String content = readFromFile();

			progressBar.setProgress(0);
			InputStream is = new FileInputStream("content.xml");
			Document doc = getDocument(is);
			List<String> entities = getEntityListFromFile(doc);
			progressBar.setProgress(1);

			EntityListView entityList = new EntityListView();
			entityList.setSFSF(sfsf);
			entityList.setRefreshEventHandler(new CustomEventHandler(entityList, sfsf));
			entityList.setClearEventHandler((e) -> {
				entityList.getTable().getItems().forEach(item -> {
					if (item.getCount().equalsIgnoreCase("Ignored")) {
						item.setCount("N/A");
						entityList.getTable().refresh();
					}
				});
				IgnorableEntityHandler.removeFromIgnorables(entityList.getTable().getItems(), false);
			});
			entityList.createEntityTableViewFromList(entities, event -> {
				if (event.getButton() == MouseButton.PRIMARY) {
					Thread t = new Thread() {
						@Override
						public void run() {
							EntityInformation selected = entityList.getTable().getSelectionModel().getSelectedItem();
							if (selected == null)
								return;
							System.out.println(selected.getName() + "\t" + selected.getCount());
							String response = sfsf.getEntityRecords(selected.getName());
							if (response == null || response.equals("null")) {
								System.err.println("Error response for " + selected.getName());
								System.err.println(response);
								return;
							}

							List<Map<String, String>> list = SFSF.getResultsFromJson(response);
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									entityList.populateDetailTable(list);
								}
							});
						}
					};
					t.start();
				}
			});
			progressBar.setProgress(0);
			entityList.createEmptyDetailTable();
			MainFrame.scene = entityList.createSceneWithDetailView(entityList.getTable(), progressBar);
			entityList.getTable().prefHeightProperty().bind(stage.heightProperty());
			entityList.getTable().prefWidthProperty().bind(stage.widthProperty().multiply(0.25));
			entityList.getDetailTable().prefHeightProperty().bind(stage.heightProperty());
			entityList.getDetailTable().prefWidthProperty().bind(stage.widthProperty().multiply(0.75));
			progressBar.setProgress(1);

			List<Thread> nonregisteredThreads = createTaskThreads(entityList, sfsf, true);
			threadList.addAll(nonregisteredThreads);

			System.out.println("Sending requests");
			// Run 20 threads in one second, gap evaluates to 1000 / 20 = 50;
			ThreadManager.runTasks(nonregisteredThreads, 1000 / 20, threadList);

			stage.setTitle("SFSF UI");
			stage.setScene(scene);
			stage.setMinHeight(720);
			stage.setMinWidth(Screen.getPrimary().getBounds().getWidth() / 2);
			stage.setWidth(Screen.getPrimary().getBounds().getWidth() - 200);
			stage.setHeight(Screen.getPrimary().getBounds().getHeight() - 100);
//			stage.setX(200 / 2);
//			stage.setY((Screen.getPrimary().getBounds().getHeight() - 720) / 2);
			stage.centerOnScreen();
//		stage.setScene(loginScene);
			stage.setOnCloseRequest(x -> onClose());
			stage.show();
		} catch (SAXException | ParserConfigurationException e) {
			System.err.println("SAX error: " + e.getMessage());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void onClose() {
		ThreadManager.shutdown();
		threadList.forEach(thread -> {
			thread.interrupt();
		});
		if (pw != null) {
			pw.flush();
			pw.close();
		}
		System.exit(0);
	}

	public static ProgressBar getProgressBar() {
		return progressBar;
	}

	public static void main(String[] args) {
		MainFrame.launch();
	}

	private static PrintWriter pw;

	private static Document getDocument(InputStream source)
			throws SAXException, IOException, ParserConfigurationException {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(source);
		return doc;
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

	private static List<Thread> createTaskThreads(EntityListView entityList, SFSF sfsf, boolean shouldIgnore) {
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

	private static class CustomEventHandler implements EventHandler<ActionEvent> {
		private EntityListView entityList;
		private SFSF sfsf;

		public CustomEventHandler(EntityListView entityList, SFSF sfsf) {
			this.entityList = entityList;
			this.sfsf = sfsf;
		}

		@Override
		public void handle(ActionEvent event) {
			List<Thread> nonregisteredThreads = createTaskThreads(entityList, sfsf, false);
			threadList.addAll(nonregisteredThreads);
			ThreadManager.runTasks(nonregisteredThreads, 1000 / 20, threadList);
		}
	}
}
