package yusaf.sf.writeback;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;

import utils.SFSF;
import utils.Utils;
import utils.ValueResolver;

public class ODataWriter {
	private SFSF sfsf;
	private ODataRequest request;

	public ODataWriter(SFSF sfsf) {
		this.sfsf = sfsf;
	}

	public String sendRequest() {
		System.out.println("Sending request to uri: " + request.getUri());
		return request.sendRequest();
	}

	public ODataWriter createUpsertRequest(ODataEntity entity) throws JsonProcessingException {
		request = createRequest(entity, "upsert");
		return this;
	}

	public ODataWriter createDeleteRequest(ODataEntity entity) {
		String keys = entity.getKeys().entrySet().stream().map(entry -> {
			return entry.getKey() + "=" + ValueResolver.resolveKeyValue(entry.getValue());
		}).collect(Collectors.joining(","));
		request = new ODataDeleteRequest(sfsf, "DELETE", entity.getType(), keys);
		return this;
	}

	private ODataRequest createRequest(ODataEntity entity, String endPoint) throws JsonProcessingException {
		Map<String, Object> body = new HashMap<>();
		body.putAll(entity.getFields());
		body.putAll(entity.getKeys());
		body.put("__metadata", entity.getMetadata(sfsf.getConfig().getBaseUrl()));

		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");

		return new ODataUpsertRequest(sfsf, "POST", endPoint, headers, Utils.mapToJson(body));
	}

	@Override
	public String toString() {
		return request == null ? super.toString() : request.toString();
	}
}