package utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
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
	/** yyyy-MM-dd'T'HH:mm:ss */
	public static DateTimeFormatter sfsfDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

	public SFSF(Configuration config) {
		this.config = config;
	}

	private String combineCreds(String user, String pp) {
		String usernameAndPP = user + ":" + pp;
		usernameAndPP = "Basic " + Base64.getEncoder().encodeToString(usernameAndPP.getBytes());
		return usernameAndPP;
	}

	public String getAuth() {
		return combineCreds(config.getUsernameAndCompany(), config.getPp());
	}

	public Configuration getConfig() {
		return config;
	}

	public String getCountUrl(String forEntity) {
		return config.getBaseUrl() + "/" + forEntity + "/$count";
	}

	public String getEntityCount(String entity) {
		return read(getCountUrl(entity));
	}

	public String getEntity(String entity, String filter) throws UnsupportedEncodingException {
		return getEntityRecords(entity, 1, 0, null, filter);
	}

	public String getEntityRecords(String entity) {
		try {
			return getEntityRecords(entity, 2, 0, null, null);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getEntityRecords(String entity, int top, int skip) {
		try {
			return getEntityRecords(entity, top, skip, null, null);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getEntityRecords(String entity, int top, int skip, String select) {
		try {
			return getEntityRecords(entity, top, skip, select, null);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getEntityRecords(String entity, int top, int skip, String select, String filter) throws UnsupportedEncodingException {
		// Always use inlineCount. This way we don't need to track '?' in URI string.
		String url = config.getBaseUrl() + "/" + entity;
		url += "?$inlinecount=allpages";

		if (top > 0)
			url += "&$top=" + top;
		if (skip > 0)
			url += "&$skip=" + skip;
		if (filter != null && !filter.isEmpty())
			url += "&$filter=" + URLEncoder.encode(filter, "UTF-8");
		if (select != null && !select.isEmpty())
			url += "&$select=" + select;

		// Also using fromDate and toDate to also get all effective dated entities. (This can be made configurable)
		url += "&fromDate=1900-01-01&toDate=9999-12-31";

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
			ss.setConnectTimeout(1000 * 60);
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
					String error = processJsonServerError(response);
					if (error != null)
						return "error:" + error; // It is necessary to return with error: to avoid rendering error on table.
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

	@SuppressWarnings("unchecked")
	private String processJsonServerError(String response) {
		/*
		 * { "error" : { "code" : "COE_UNSUPPORTED_FEATURE", "message" : { "lang" : "en-US", "value" :
		 * "[COE0025]Unsupported feature: You need to query base entity and expand with Permissions navigation." } } }
		 */
		Map<String, Map<String, Object>> map = Utils.jsonToMap(response, new TypeReference<Map<String, Map<String, Object>>>() {
		});
		Map<String, String> messageObject = (Map<String, String>) map.get("error").get("message");
		return messageObject.get("value");
	}

	/**
	 * 
	 * 
	 * @param response
	 * @param inlineCount An integer array of size 1.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String, String>> getResultsFromJsonInlineCount(String response, int[] inlineCount) {
		Map<String, Map<String, Object>> responseMap = Utils.jsonToMap(response,
				new TypeReference<Map<String, Map<String, Object>>>() {
				});
		List<Object> list = (List<Object>) responseMap.get("d").get("results");
		List<Map<String, String>> newList = list.stream().map(m -> {
			Map<String, String> n = ((Map<String, Object>) m).entrySet().stream().map(e -> {
				if (e.getKey().contains("Nav") && e.getValue() instanceof LinkedHashMap<?, ?>) {
					return Map.entry(e.getKey(), "Ignored");
				}
				return Map.entry(e.getKey(), ValueResolver.convert(e.getValue()));
			}).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
			return n;
		}).collect(Collectors.toList());
		try {
			inlineCount[0] = Integer.valueOf(responseMap.get("d").get("__count").toString());
		} catch (Exception e) {
			System.err.println("Unable to parse " + responseMap.get("d").get("__count"));
		}
		return newList;
	}

	@SuppressWarnings("unchecked")
	public static List<Map<String, String>> getResultsFromJson(String response) {
		Map<String, Map<String, List<Object>>> responseMap = Utils.jsonToMap(response,
				new TypeReference<Map<String, Map<String, List<Object>>>>() {
				});
		List<Object> list = responseMap.get("d").get("results");
		List<Map<String, String>> newList = list.stream().map(m -> {
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