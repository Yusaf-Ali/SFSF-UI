package yusaf.main.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.DefaultConfiguration;
import utils.SFSF;
import utils.ThreadManager;

public class MainFrame extends Application {
	private static Scene scene;
	private static List<Thread> threadList = new ArrayList<>();
	private static List<String> ignorables = new ArrayList<>();

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("SFSF UI");
		primaryStage.setScene(scene);
//		primaryStage.setScene(loginScene);
		primaryStage.setOnCloseRequest(x -> onClose());
		primaryStage.show();
	}

	private void onClose() {
		ThreadManager.shutdown();
		threadList.forEach(thread -> {
			thread.interrupt();
		});
		if (pw != null) {
			pw.close();
		}
	}

	public static void main(String[] args) {
		DefaultConfiguration config = new DefaultConfiguration();
		SFSF sfsf = new SFSF(config);
		populateIgnorables();
		try {
//			String content = meta.read(config.baseUrl);
//			writeToFile(content);
//			String content = readFromFile();
			InputStream is = new FileInputStream("content.xml");
			Document doc = getDocument(is);
			List<String> entities = getEntityListFromFile(doc);

			EntityListView entityList = new EntityListView();
			entityList.createEntityTableViewFromList(entities);
			entityList.createEmptyDetailTable();
			MainFrame.scene = entityList.createSceneWithDetailView(entityList.getTable());

			List<Thread> registeredThreads = new ArrayList<>();
			entityList.getTable().getItems().forEach(item -> {
				// Avoid proceeding if the thread is interrupted
				if (Thread.currentThread().isInterrupted())
					return;
				if (ignorables.contains(item.getName())) {
					item.setCount("Ignored");
					entityList.getTable().refresh();
					return;
				}
				Thread registeredThread = new Thread(() -> {
					item.setCount("Retrieving...");
					String content = sfsf.getEntityCount(item.getName());
					entityList.getTable().refresh();
					try {
						// Try parsing the value if possible
						Integer.valueOf(content);
						item.setCount(content);
					} catch (NumberFormatException nfe) {
						if (content != null)
							item.setCount(content);
						else
							item.setCount("N/A");
						addInIgnorables(item.getName());
					}
					entityList.getTable().refresh();
				});
				registeredThreads.add(registeredThread);
			});
			System.out.println("Sending requests");
			threadList.addAll(registeredThreads);
			// Run 20 threads in one second, gap evaluates to 1000 / 20 = 50;
			ThreadManager.runTasks(registeredThreads, 1000 / 20);

			MainFrame.launch();
		} catch (SAXException | ParserConfigurationException e) {
			System.err.println("SAX error: " + e.getMessage());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static PrintWriter pw;

	public static void addInIgnorables(String name) {
		try {
			File file = new File("ignorables.txt");
			if (!file.exists()) {
				file.createNewFile();
			}
			if (pw == null) {
				pw = new PrintWriter(new FileWriter("ignorables.txt", true));
			}
			pw.append(name + "\n");
			pw.flush();
		} catch (Exception e) {
			System.err.println("Unable to update ignorables list " + name);
		}
	};

	public static void populateIgnorables() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader("ignorables.txt"));
			ignorables = reader.lines().collect(Collectors.toList());
			reader.close();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

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
}
