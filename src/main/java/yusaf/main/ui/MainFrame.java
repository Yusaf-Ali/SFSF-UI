package yusaf.main.ui;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import utils.DefaultConfiguration;
import utils.IgnorableEntityHandler;
import utils.SFSF;
import utils.ThreadManager;

public class MainFrame extends Application {
	private static List<Thread> threadList = new ArrayList<>();
	private static ProgressBar progressBar = new ProgressBar();

	@Override
	public void start(Stage stage) throws Exception {
		DefaultConfiguration config = new DefaultConfiguration();
		SFSF sfsf = new SFSF(config);

		MenuBar topBar = new MenuBar();
		Menu optionsMenu = new Menu("Options");
		topBar.getMenus().add(optionsMenu);

		MenuItem refresh = new MenuItem("Refresh");
		MenuItem refreshIgnored = new MenuItem("Refresh Ignored");
		MenuItem refreshAll = new MenuItem("Refresh All");
		MenuItem clearIgnorables = new MenuItem("Clear Ignorables");

		optionsMenu.getItems().add(refresh);
		optionsMenu.getItems().add(refreshIgnored);
		optionsMenu.getItems().add(refreshAll);
		optionsMenu.getItems().add(new SeparatorMenuItem());
		optionsMenu.getItems().add(clearIgnorables);
		IgnorableEntityHandler.readIgnorables();

		long start = System.currentTimeMillis();
		System.out.println("Starting data frame render: 0");
		DataFrame frame = new DataFrame(progressBar);
		Pane dataPanel = frame.render(stage, sfsf);
		System.out.println("Ending data frame render: " + (System.currentTimeMillis() - start));

		refresh.setOnAction(ev -> {
			frame.refresh();
		});
		refreshAll.setOnAction(ev -> {
			frame.refreshAll();
		});
		refreshIgnored.setOnAction(ev -> {
			frame.refreshIgnored();
		});
		clearIgnorables.setOnAction(ev -> {
			frame.clearIgnorables();
		});

		BorderPane mainPane = new BorderPane();
		mainPane.setTop(topBar);
		mainPane.setCenter(dataPanel);
		Scene scene = new Scene(mainPane);

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
		System.out.println("Displaying time: " + (System.currentTimeMillis() - start));
		stage.show();

		// TODO need to add functionality to access more records, probably using a dialog box that asks for $top
		// TODO need to save selected fields in a way that it will be available after reopening the program
	}

	public static List<Thread> getThreadList() {
		return threadList;
	}

	private void onClose() {
		ThreadManager.shutdown();
		threadList.forEach(thread -> {
			thread.interrupt();
		});
		PrintWriter writer = IgnorableEntityHandler.getWriter();
		if (writer != null) {
			IgnorableEntityHandler.saveIgnorables();
			writer.close();
		}
		System.exit(0);
	}

	public static ProgressBar getProgressBar() {
		return progressBar;
	}

	public static void main(String[] args) {
		MainFrame.launch();
	}
}