package yusaf.main.ui.components;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import utils.SFSF;
import yusaf.main.ui.pojos.EntityInformation;

public class EntityListTableContextMenu {
	private ContextMenu menu;

	public EntityListTableContextMenu(SFSF sfsf, EntityListView view) {
		menu = new ContextMenu();
		MenuItem selectMenuItem = new MenuItem("Configure Selects...");
		selectMenuItem.setOnAction((event) -> {
			EntityInformation s = view.getTable().getSelectionModel().getSelectedItem();
			if (s == null)
				return;
			if (s.getAllFields().size() > 0) {
				Platform.runLater(() -> {
					EntityFieldSelect popup = new EntityFieldSelect();
					popup.renderAndShow();
					popup.populateData(s.getAllFields());
					popup.setIgnoredItems(s.getIgnorables());
					popup.setSaveAction(() -> {
						s.setAllFields(s.getAllFields());
						s.setIgnoredFields(popup.getIgnoredItems());
						return null;
					});
				});
				return;
			}
			Thread t = new Thread() {
				public void run() {
					EntityFieldSelect popup = new EntityFieldSelect();
					popup.render();
					ProgressBar pb = popup.getProgressBar();
					pb.setProgress(0);
					pb.setVisible(true);
					String data = sfsf.getEntityMetadata(s.getName());
					pb.setProgress(0.5);
					try {
						Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
								.parse(new InputSource(new StringReader(data)));
						NodeList properties = doc.getElementsByTagName("Property");

						List<String> fields = new ArrayList<>();
						for (int index = 0; index < properties.getLength(); index++) {
							Node node = properties.item(index);
							Node attrFilterable = node.getAttributes().getNamedItem("sap:filterable");
							boolean isFilterable = attrFilterable.getTextContent().equalsIgnoreCase("true");
							Node attrName = node.getAttributes().getNamedItem("Name");
							if (isFilterable) {
								fields.add(attrName.getTextContent());
							} else {
								s.getIgnorables().add(attrName.getTextContent());
							}
						}
						pb.setProgress(0.7);

						Platform.runLater(() -> {
							popup.setSaveAction(() -> {
								s.setAllFields(fields);
								s.setIgnoredFields(popup.getIgnoredItems());
								return null;
							});
							popup.populateData(fields);
							popup.show();
							pb.setProgress(1);
							pb.setVisible(false);
						});
					} catch (SAXException | IOException | ParserConfigurationException e) {
						e.printStackTrace();
					}
				}
			};
			t.start();
		});
		menu.getItems().add(selectMenuItem);
		MenuItem loadMoreItems = new MenuItem("Load more");
		loadMoreItems.setOnAction((event) -> {
			EntityInformation information = view.getTable().getSelectionModel().getSelectedItem();
			if (information == null)
				return;
		});
	}

	public ContextMenu getMenu() {
		return menu;
	}
}