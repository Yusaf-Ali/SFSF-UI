package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import yusaf.main.ui.components.EntityListView.EntityInformation;

public class IgnorableEntityHandler {
	private static List<String> ignorables = new ArrayList<>();
	private static PrintWriter pw;

	public static void addInIgnorables(String name) {
		ignorables.add(name);
	}

	public static void saveIgnorables(boolean shouldCloseWriter) {
		try {
			File file = new File("ignorables.txt");
			if (!file.exists()) {
				file.createNewFile();
			}
			if (pw == null) {
				pw = new PrintWriter(new FileWriter("ignorables.txt", false));
			}
			ignorables.forEach(name -> {
				pw.append(name + "\n");
			});
			pw.flush();
			if (shouldCloseWriter)
				pw.close();
		} catch (Exception e) {
			System.err.println("Unable to save ignorables list");
		}
	};

	public static void readIgnorables() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader("ignorables.txt"));
			ignorables = reader.lines().collect(Collectors.toList());
			reader.close();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public static synchronized void removeFromIgnorables(List<EntityInformation> items, boolean filterNonNumeric) {
		// If the item is non numeric then remove it from ignorables.
		if (filterNonNumeric) {
			ignorables.removeAll(items.stream()
					.filter(p -> p.isNumeric())
					.map(x -> x.getName())
					.collect(Collectors.toList()));
		} else {
			ignorables.removeAll(items.stream()
					.map(x -> x.getName())
					.collect(Collectors.toList()));
		}
	}

	public static List<String> ignorables() {
		return ignorables;
	}

	public static PrintWriter getWriter() {
		return pw;
	}
}
