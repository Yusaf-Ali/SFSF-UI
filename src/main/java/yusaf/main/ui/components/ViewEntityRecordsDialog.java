package yusaf.main.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.Utils;

public class ViewEntityRecordsDialog {
	private Stage window;
	private Label filterLabel, topLabel, skipLabel, filterInstruction, topSkipInstruction;
	private TextField filter, top, skip;
	private CheckBox append;
	private Button fetch;
	private Button cancel;
	private boolean cancelled;

	public ViewEntityRecordsDialog(String entityName, String populatedEntity) {
		window = new Stage();
		window.setTitle("View: " + entityName);
		filterLabel = new Label("Filter:");
		topLabel = new Label("Top:");
		skipLabel = new Label("Skip:");
		filterInstruction = new Label("Use Success factors api filter format, if empty, will be ignored");
		topSkipInstruction = new Label("Must be greater than zero, otherwise will be ignored!");

		filter = new TextField();
		top = new TextField("5");
		skip = new TextField("0");

		append = new CheckBox("Append to existing records");
		append.setDisable(!entityName.equals(populatedEntity));

		fetch = new Button("Fetch");
		cancel = new Button("Cancel");
		cancel.setOnAction((e) -> {
			cancelled = true;
			window.close();
		});
		fetch.setOnAction((e) -> {
			cancelled = false;
			window.close();
		});
		window.setOnCloseRequest((e) -> {
			cancelled = false;
		});

		otherSettings();

		HBox.setHgrow(filterLabel, Priority.ALWAYS);
		HBox.setHgrow(topLabel, Priority.ALWAYS);
		HBox.setHgrow(skipLabel, Priority.ALWAYS);

		Region spacing1 = new Region();
		Region spacing2 = new Region();
		Region spacing3 = new Region();
		setWidth(spacing1, 10);
		setWidth(spacing2, 10);
		setWidth(spacing3, 10);

		HBox filterBox = new HBox(filterLabel, spacing1, filter);
		filterBox.setSpacing(5);
		HBox topBox = new HBox(topLabel, spacing2, top);
		topBox.setSpacing(5);
		HBox skipBox = new HBox(skipLabel, spacing3, skip);
		skipBox.setSpacing(5);
		HBox buttonBox = new HBox(fetch, cancel);
		buttonBox.setSpacing(5);
		buttonBox.setAlignment(Pos.CENTER_RIGHT);

		VBox box = new VBox(filterInstruction, filterBox, topSkipInstruction, topBox, skipBox, append, buttonBox);
		box.setPadding(new Insets(10));
		window.setScene(new Scene(box));
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public String getFilter() {
		return filter.getText();
	}

	public int getTop() {
		return Integer.parseInt(top.getText());
	}

	public int getSkip() {
		return Integer.parseInt(skip.getText());
	}

	public boolean shouldAppend() {
		return append.isSelected();
	}

	public boolean validateTopSkip() {
		if (Utils.isInteger(top.getText()) && Utils.isInteger(skip.getText())) {
			int tc = Integer.parseInt(top.getText());
			int sc = Integer.parseInt(skip.getText());
			if (tc < 0 || sc < 0)
				return false;
			else if (tc >= 0 && sc >= 0)
				return true;
		}
		return false;
	}

	public void showAndWait() {
		window.showAndWait();
	}

	private void setWidth(Region control, int width) {
		control.setMaxWidth(width);
		control.setMinWidth(width);
		control.setPrefWidth(width);
	}

	private void otherSettings() {
		setWidth(filter, 400);
		setWidth(top, 400);
		setWidth(skip, 400);
		setWidth(filterLabel, 50);
		setWidth(topLabel, 50);
		setWidth(skipLabel, 50);
		filterLabel.setAlignment(Pos.BASELINE_RIGHT);
		topLabel.setAlignment(Pos.BASELINE_RIGHT);
		skipLabel.setAlignment(Pos.BASELINE_RIGHT);

		top.textProperty().addListener((e, o, n) -> {
			if (n.length() != 0 && (!Utils.isInteger(n) || Integer.parseInt(n) < 0)) {
				top.setText(o);
			}
		});
		skip.textProperty().addListener((e, o, n) -> {
			if (n.length() != 0 && (!Utils.isInteger(n) || Integer.parseInt(n) < 0)) {
				top.setText(o);
			}
		});
	}
}