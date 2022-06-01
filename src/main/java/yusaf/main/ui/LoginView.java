package yusaf.main.ui;

import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginView {
	private BorderPane pane;

	public LoginView(Stage stage) {
		ChoiceBox<String> sfsfUrls = new ChoiceBox<>();
		sfsfUrls.setMinWidth(300);
		sfsfUrls.getItems().add("Link 1");
		sfsfUrls.getItems().add("Link 2");
		sfsfUrls.getItems().add("Link 3");
		Button saveSFSFUrl = new Button("Save");
		saveSFSFUrl.setMinWidth(30);
		Button removeSFSFUrl = new Button("Remove");
		removeSFSFUrl.setMinWidth(30);
		HBox sfsfSection = new HBox(sfsfUrls, saveSFSFUrl, removeSFSFUrl);

		ChoiceBox<String> usernames = new ChoiceBox<>();
		usernames.setMinWidth(300);
		usernames.getItems().add("Username 1");
		usernames.getItems().add("Username 2");
		usernames.getItems().add("Username 3");
		Button saveUsernameUrl = new Button("Save");
		saveUsernameUrl.setMinWidth(30);
		Button removeUsernameUrl = new Button("Remove");
		removeUsernameUrl.setMinWidth(30);
		HBox usernameSection = new HBox(usernames, saveUsernameUrl, removeUsernameUrl);

		PasswordField passwordField = new PasswordField();
		Button login = new Button("Login");

		VBox verticalGrid = new VBox();
		verticalGrid.getChildren().add(sfsfSection);
		verticalGrid.getChildren().add(usernameSection);
		verticalGrid.getChildren().add(passwordField);
		verticalGrid.getChildren().add(login);

		pane = new BorderPane();
		pane.setCenter(verticalGrid);
	}

	public BorderPane getView() {
		return pane;
	}
}
