package yusaf.sf.writeback;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.stream.Collectors;

import utils.SFSF;

public class ODataUpsertRequest extends ODataRequest {
	private String bodyString;
	private Map<String, String> headers;

	public ODataUpsertRequest(SFSF sfsf, String method, String endPoint, Map<String, String> headers, String bodyString) {
		this.sfsf = sfsf;
		this.method = method;
		this.endPoint = endPoint;
		this.headers = headers;
		this.bodyString = bodyString;
	}

	public String getBody() {
		return bodyString;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	@Override
	public String sendRequest() {
		HttpURLConnection urlCon = null;
		try {
			URL url = new URL(this.getUri());
			urlCon = (HttpURLConnection) url.openConnection();
			urlCon.setRequestMethod(this.getMethod());

			urlCon.addRequestProperty("Authorization", sfsf.getAuth());
			for (String headerPropertyKey : this.getHeaders().keySet()) {
				urlCon.addRequestProperty(headerPropertyKey, this.getHeaders().get(headerPropertyKey));
			}

			if (this.getBody() != null) {
				urlCon.setDoOutput(true);
				urlCon.setRequestProperty("Content-Length", Integer.toString(this.getBody().length()));
				urlCon.getOutputStream().write(this.getBody().getBytes("UTF-8"));
			}
			InputStream is = urlCon.getInputStream();
			if (is.available() > 0) {
				return new String(is.readAllBytes());
			}
		} catch (IOException io) {
			try {
				if (urlCon != null && urlCon.getErrorStream().available() > 0) {
					String error = new String(urlCon.getErrorStream().readAllBytes());
					System.err.println(error);
				}
			} catch (IOException e) {
				System.err.println("Error reading error response!");
			}
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Method: ");
		sb.append(method);
		sb.append("\t\t");
		sb.append("EndPoint: ");
		sb.append(endPoint);
		sb.append("\n");

		sb.append("Uri: ");
		sb.append(getUri());
		sb.append("\n");

		sb.append("Headers: ");
		sb.append("\n");
		sb.append(headers.entrySet().stream().map(entry -> {
			return entry.getKey() + ": " + entry.getValue();
		}).collect(Collectors.joining(", ")));
		sb.append("\n");

		sb.append("Body: ");
		sb.append("\n");
		sb.append(bodyString);

		return sb.toString();
	}
}