package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import yusaf.main.ui.EntityListView.EntityInformation;

public class IgnorableEntityHandler {
	private static List<String> ignorables = new ArrayList<>();
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

	public static void readIgnorables() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader("ignorables.txt"));
			ignorables = reader.lines().collect(Collectors.toList());
			reader.close();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public static void removeFromIgnorables(List<EntityInformation> items, boolean filterNonNumeric) {
		ignorables.removeAll(items.stream().filter(p -> {
			if (filterNonNumeric)
				return p.isNumeric();
			return true;
		}).map(m -> m.getName()).collect(Collectors.toList()));
		try {
			File file = new File("ignorables.txt");
			if (!file.exists()) {
				file.delete();
				file.createNewFile();
			}
			if (pw == null) {
				pw = new PrintWriter(new FileWriter(file, false));
			}
			ignorables.forEach(name -> {
				pw.append(name + "\n");
			});
			pw.flush();
			pw.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	public static List<String> ignorables() {
		return ignorables;
	}
}
