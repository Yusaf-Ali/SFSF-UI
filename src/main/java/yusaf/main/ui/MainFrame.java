package yusaf.main.ui;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import utils.DefaultConfiguration;
import utils.IgnorableEntityHandler;
import utils.SFSF;
import utils.ThreadManager;

public class MainFrame extends Application {
	private static List<Thread> threadList = new ArrayList<>();

	@Override
	public void start(Stage stage) throws Exception {
		DefaultConfiguration config = new DefaultConfiguration();
		SFSF sfsf = new SFSF(config);

		IgnorableEntityHandler.readIgnorables();

		long start = System.currentTimeMillis();
		System.out.println("Stage 1 - Starting data frame render");

		DataFrame frame = new DataFrame(sfsf);
		System.out.println("Stage 2 - Data frame render: " + (System.currentTimeMillis() - start));
		Scene scene = new Scene(frame.render(stage));

		System.out.println("Stage 3 - Ending data frame render: " + (System.currentTimeMillis() - start));

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
		IgnorableEntityHandler.saveIgnorables(true);
		System.exit(0);
	}

	public static void main(String[] args) {
		MainFrame.launch();
	}
}