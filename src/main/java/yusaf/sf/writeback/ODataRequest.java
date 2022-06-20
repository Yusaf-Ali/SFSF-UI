package yusaf.sf.writeback;

import utils.SFSF;

public abstract class ODataRequest {
	protected SFSF sfsf;
	protected String method;
	protected String endPoint;

	public String getMethod() {
		return method;
	}

	public String getEndPoint() {
		return endPoint;
	}

	public String getUri() {
		return sfsf.getConfig().getBaseUrl() + "/" + endPoint;
	}

	public abstract String sendRequest();
}
