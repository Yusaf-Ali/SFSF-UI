package yusaf.sf.writeback;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import utils.Utils;

public class ODataWriterResponse {
	private String key;
	private String status;
	private String editStatus;
	private String message;
	private String index;
	private String httpCode;
	private String inlineResults;

	public ODataWriterResponse read(String responseString) throws SAXException, IOException, ParserConfigurationException {
		try {
			Document doc = Utils.getDocument(responseString);
			key = getText(doc, "d:key");
			status = getText(doc, "d:status");
			editStatus = getText(doc, "d:editStatus");
			message = getText(doc, "d:message");
			index = getText(doc, "d:index");
			httpCode = getText(doc, "d:httpCode");
			inlineResults = getText(doc, "d:inlineResults");
		} catch (Exception ex) {
			System.err.println("Unable to parse response: " + responseString);
			System.err.println(ex.getMessage());
		}
		return this;
	}

	public ODataWriterResponse read(InputStream responseStream) throws SAXException, IOException, ParserConfigurationException {
		Document doc = Utils.getDocument(responseStream);
		key = getText(doc, "d:key");
		status = getText(doc, "d:status");
		editStatus = getText(doc, "d:editStatus");
		message = getText(doc, "d:message");
		index = getText(doc, "d:index");
		httpCode = getText(doc, "d:httpCode");
		inlineResults = getText(doc, "d:inlineResults");
		return this;
	}

	/**
	 * Avoid throwing exceptions
	 * 
	 * @return
	 */
	public static String getText(Document doc, String element) {
		try {
			NodeList nodeList = doc.getElementsByTagName(element);
			if (nodeList.getLength() > 0) {
				return nodeList.item(0).getTextContent();
			}
		} catch (Exception e) {
		}
		return null;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getEditStatus() {
		return editStatus;
	}

	public void setEditStatus(String editStatus) {
		this.editStatus = editStatus;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getHttpCode() {
		return httpCode;
	}

	public void setHttpCode(String httpCode) {
		this.httpCode = httpCode;
	}

	public String getInlineResults() {
		return inlineResults;
	}

	public void setInlineResults(String inlineResults) {
		this.inlineResults = inlineResults;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("---Response Start---\n");
		sb.append("Key: ");
		sb.append(key);
		sb.append(",\n");
		sb.append("HttpCode: ");
		sb.append(httpCode);
		sb.append(", ");
		sb.append("Status: ");
		sb.append(status);
		sb.append(", ");
		sb.append("EditStatus: ");
		sb.append(editStatus);
		sb.append(",\n");
		sb.append("Message: ");
		sb.append(message);
		sb.append("\n");
		sb.append("Index: ");
		sb.append(index);
		sb.append(", ");
		sb.append("InlineResults: ");
		sb.append(inlineResults);
		sb.append("\n");
		sb.append("---Response End---");
		return sb.toString();
	}
}
//<?xml version="1.0" encoding="utf-8"?>
//<feed xmlns="http://www.w3.org/2005/Atom" xmlns:m="http://schemas.microsoft.com/ado/2007/08/dataservices/metadata" xmlns:d="http://schemas.microsoft.com/ado/2007/08/dataservices">
//<entry>
//<content type="application/xml">
//<m:properties>
//<d:key>Position/code=temppos0617,Position/effectiveStartDate=2021-07-12T00:00:00.000-04:00</d:key>
//<d:status>OK</d:status>
//<d:editStatus>UPSERTED</d:editStatus>
//<d:message m:null="true"></d:message>
//<d:index m:type="Edm.Int32">0</d:index>
//<d:httpCode m:type="Edm.Int32">200</d:httpCode>
//<d:inlineResults m:type="Bag(SFOData.UpsertResult)"></d:inlineResults>
//</m:properties>
//</content>
//</entry>
//</feed>