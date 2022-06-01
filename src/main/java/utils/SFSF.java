package utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SFSF {
	private Configuration config;

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

	public String read(String url) {
		HttpURLConnection ss = null;
		try {
			URL u = new URL(url);
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
}
