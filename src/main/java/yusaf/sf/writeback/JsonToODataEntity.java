package yusaf.sf.writeback;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;

import utils.Utils;
import utils.ValueResolver;

public class JsonToODataEntity {
	@SuppressWarnings("unchecked")
	public static List<ODataEntity> convert(String jsonString) {
		Map<String, Map<String, List<Object>>> json = Utils.jsonToMap(jsonString, new TypeReference<Map<String, Map<String, List<Object>>>>() {
		});
		List<ODataEntity> odataEntities = new ArrayList<>();
		List<Object> entities = json.get("d").get("results");
		entities.forEach(entity -> {
			ODataEntity odata = new ODataEntity();

			Map<String, Object> m = (Map<String, Object>) entity;
			Map<String, String> metadata = (HashMap<String, String>) m.get("__metadata");

			odata.setKeys(getKeysFromUri(metadata, m));

			// Find a way to easily convert this.
			odata.setFields(m.entrySet().stream()
					.filter(f -> !odata.getKeys().containsKey(f.getKey()))
					.collect(Collectors.toMap(
							e -> e.getKey(),
							e -> ValueResolver.convert(e.getValue()),
							(oldV, newV) -> oldV,
							LinkedHashMap::new)));
			odataEntities.add(odata);
		});
		return odataEntities;
	}

	@SuppressWarnings("unchecked")
	public static <T extends ODataEntity> List<T> convert(String jsonString, Class<T> t) {
		Map<String, Map<String, List<Object>>> json = Utils.jsonToMap(jsonString, new TypeReference<Map<String, Map<String, List<Object>>>>() {
		});
		List<T> odataEntities = new ArrayList<>();
		List<Object> entities = json.get("d").get("results");
		entities.forEach(entity -> {
			try {
				T odata = t.getConstructor().newInstance();

				Map<String, Object> m = (Map<String, Object>) entity;
				Map<String, String> metadata = (HashMap<String, String>) m.get("__metadata");

				odata.setKeys(getKeysFromUri(metadata, m));

				// Find a way to easily convert this.
				odata.setFields(m.entrySet().stream()
						.filter(f -> !odata.getKeys().containsKey(f.getKey()))
						.collect(Collectors.toMap(
								e -> e.getKey(),
								e -> ValueResolver.convert(e.getValue()),
								(oldV, newV) -> oldV,
								LinkedHashMap::new)));
				odataEntities.add(odata);
			} catch (InstantiationException e1) {
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
			} catch (NoSuchMethodException e1) {
				e1.printStackTrace();
			} catch (SecurityException e1) {
				e1.printStackTrace();
			}
		});
		return odataEntities;
	}

	public static Map<String, String> getKeysFromUri(Map<String, String> metadata, Map<String, Object> m) {
		// https://apisalesdemo4.successfactors.com:443/odata/v2/PerPerson('109033')
		// https://apisalesdemo4.successfactors.com:443/odata/v2/Position(code='temppos0617',effectiveStartDate=datetime'2019-01-01T00:00:00')
		String uri = metadata.get("uri");
		String type = getType(metadata.get("type"));
		String keyValueString = uri.split(type)[1];
		String keyValues[] = evaluateKeyArray(keyValueString);
		if (keyValues.length == 1) {
			// Need to check which field is the key.
			Optional<String> optionalKey = m.entrySet().stream().filter(e -> {
				for (String key : keyValues) {
					if (e.getValue().equals(key)) {
						return true;
					}
				}
				return false;
			}).map(e -> e.getKey()).findFirst();
			if (optionalKey.isPresent()) {
				Map<String, String> keyMap = new LinkedHashMap<>();
				keyMap.put(optionalKey.get(), (String) m.get(optionalKey.get()));
				return keyMap;
			}
		} else {
			// If there are multiple keys, then they will be with their names.
			Map<String, String> keyMap = new LinkedHashMap<>();
			for (String keyValue : keyValues) {
				String key = keyValue.split("=")[0];
				String value = keyValue.split("=")[1];
				if (value.startsWith("datetime'") && value.endsWith("'")) {
				} else {
					value = value.replaceAll("'", "");
				}
				keyMap.put(key, value);
			}
			return keyMap;
		}
		// As the map is already there (be it empty), we must avoid returning null.
		return new LinkedHashMap<>();
	}

	public static String[] evaluateKeyArray(String keyValueArrayString) {
		String keyValues[] = null;
		if (keyValueArrayString.startsWith("('") && keyValueArrayString.endsWith("')")) {
			// There is only one key.
			keyValues = new String[] { keyValueArrayString.substring(2, keyValueArrayString.length() - 2) };
		} else {
			keyValueArrayString = keyValueArrayString.substring(1, keyValueArrayString.length() - 1);
			keyValues = keyValueArrayString.split(",");
		}
		return keyValues;
	}

	private static String getType(String value) {
		return value.replaceAll("SFOData.", "");
	}
}