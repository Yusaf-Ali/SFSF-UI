module mainapp {
	requires transitive javafx.graphics;
	requires transitive javafx.controls;
	requires transitive java.xml;
	requires com.fasterxml.jackson.databind;

	exports utils;
	exports yusaf.main.ui;
	exports yusaf.main.ui.components;
}