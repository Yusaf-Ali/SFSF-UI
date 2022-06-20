package yusaf.sf.writeback;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import utils.SFSF;

public class ODataDeleteRequest extends ODataRequest {
	private String key;

	public ODataDeleteRequest(SFSF sfsf, String method, String endPoint, String key) {
		this.sfsf = sfsf;
		this.method = method;
		this.endPoint = endPoint;
		this.key = key;
	}

	@Override
	public String getUri() {
		return sfsf.getConfig().getBaseUrl() + "/" + endPoint + "(" + key + ")";
	}

	public String getKey() {
		return key;
	}

	@Override
	public String sendRequest() {
		HttpURLConnection urlCon = null;
		try {
			URL url = new URL(this.getUri());
			urlCon = (HttpURLConnection) url.openConnection();
			urlCon.setRequestMethod(this.getMethod());

			urlCon.addRequestProperty("Authorization", sfsf.getAuth());

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
		sb.append("\t");
		sb.append("EndPoint: ");
		sb.append(endPoint);
		sb.append("\t");
		sb.append("Key: ");
		sb.append(key);
		return sb.toString();
	}
}