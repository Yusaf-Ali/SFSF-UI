package utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.type.TypeReference;

public class SFSF {
	private Configuration config;
	public static DateTimeFormatter sfsfDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

	public SFSF(Configuration config) {
		this.config = config;
	}

	private String combineCreds(String user, String pp) {
		String usernameAndPP = user + ":" + pp;
		usernameAndPP = "Basic " + Base64.getEncoder().encodeToString(usernameAndPP.getBytes());
		return usernameAndPP;
	}

	public String getCountUrl(String forEntity) {
		return config.getBaseUrl() + "/" + forEntity + "/$count";
	}

	public String getEntityCount(String entity) {
		return read(getCountUrl(entity));
	}

	public String getEntityRecords(String entity) {
		return getEntityRecords(entity, 200, 0, "");
	}

	public String getEntityRecords(String entity, int top, int skip) {
		return getEntityRecords(entity, top, skip, "");
	}

	public String getEntityRecords(String entity, int top, int skip, String select) {
		String url = config.getBaseUrl() + "/" + entity;
		if (top > 0 && skip > 0 && (select != null && !select.isEmpty()))
			url += "?$top=" + top + "&$skip=" + skip + "$select=" + select;
		else if (top > 0 && skip > 0)
			url += "?$top=" + top + "&$skip=" + skip;
		else if (top > 0 && (select != null && !select.isEmpty()))
			url += "?$top=" + top + "&$select=" + select;
		else if (top > 0)
			url += "?$top=" + top;
		else if (skip > 0)
			url += "?$skip=" + skip;
		else if (select != null && !select.isEmpty())
			url += "?$select=" + select;
		return readJson(url);
	}

	public String getEntityMetadata(String entity) {
		return read(config.getBaseUrl() + "/" + entity + "/$metadata");
	}

	public String read(String url) {
		HttpURLConnection ss = null;
		try {
			URL u = new URL(url);
			System.out.println("read: " + url);
			ss = (HttpURLConnection) u.openConnection();
			ss.setRequestProperty("Authorization", combineCreds(config.getUsernameAndCompany(), config.getPp()));
			InputStream responseStream = ss.getInputStream();
			if (responseStream.available() > 0) {
				String content = new String(responseStream.readAllBytes());
				return content;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			try {
				if (ss != null && ss.getErrorStream() != null) {
					String response = new String(ss.getErrorStream().readAllBytes());
					System.err.print("Response: ");
					System.err.println(response);
					String error = processServerError(response);
					if (error != null)
						return error;
				}
			} catch (IOException fx) {
				fx.printStackTrace();
			}
		}
		return null;
	}

	public String readJson(String url) {
		HttpURLConnection ss = null;
		try {
			if (url.contains("?"))
				url += "&$format=json";
			else
				url += "$format=json";
			URL u = new URL(url);
			System.out.println("readJson: " + url);
			ss = (HttpURLConnection) u.openConnection();
			ss.setRequestProperty("Authorization", combineCreds(config.getUsernameAndCompany(), config.getPp()));
			InputStream responseStream = ss.getInputStream();
			if (responseStream.available() > 0) {
				String content = new String(responseStream.readAllBytes());
				return content;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			try {
				if (ss != null && ss.getErrorStream() != null) {
					String response = new String(ss.getErrorStream().readAllBytes());
					System.err.print("Response: ");
					System.err.println(response);
					String error = processServerError(response);
					if (error != null)
						return error;
				}
			} catch (IOException fx) {
				fx.printStackTrace();
			}
		}
		return null;
	}

	private String processServerError(String response) {
		InputSource is = new InputSource(new StringReader(response));
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
			if (doc.getElementsByTagName("code").getLength() > 0) {
				return doc.getElementsByTagName("code").item(0).getTextContent();
			}
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static List<Map<String, String>> getResultsFromJson(String response) {
		Map<String, Map<String, List<Object>>> responseMap = Utils.jsonToMap(response,
				new TypeReference<Map<String, Map<String, List<Object>>>>() {
				});
		List<Object> list = responseMap.get("d").get("results");
		List<Map<String, String>> newList = list.stream().map(m -> {
			@SuppressWarnings("unchecked")
			Map<String, String> n = ((Map<String, Object>) m).entrySet().stream().map(e -> {
				if (e.getKey().contains("Nav") && e.getValue() instanceof LinkedHashMap<?, ?>) {
					return Map.entry(e.getKey(), "Ignored");
				}
				return Map.entry(e.getKey(), ValueResolver.convert(e.getValue()));
			}).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
			return n;
		}).collect(Collectors.toList());
		System.out.println("Data acquired from server contains " + list.size() + " records");
		return newList;
	}
}