package guepardoapps.library.lucahome.common.dto;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class InformationDto implements Serializable {

	private static final long serialVersionUID = 2257042096692273681L;

	private Map<String, String> _informationList = new HashMap<>();

	public InformationDto(
			@NonNull String author,
			@NonNull String company,
			@NonNull String contact,
			@NonNull String buildDate,
			@NonNull String serverVersion,
			@NonNull String websiteVersion,
			@NonNull String temperatureLogVersion,
			@NonNull String androidAppVersion,
			@NonNull String androidWearVersion,
			@NonNull String androidAccessVersion,
			@NonNull String mediaServerVersion) {
		_informationList.put("author", author);
		_informationList.put("company", company);
		_informationList.put("contact", contact);
		_informationList.put("buildDate", buildDate);
		_informationList.put("serverVersion", serverVersion);
		_informationList.put("websiteVersion", websiteVersion);
		_informationList.put("temperatureLogVersion", temperatureLogVersion);
		_informationList.put("androidAppVersion", androidAppVersion);
		_informationList.put("androidWearVersion", androidWearVersion);
		_informationList.put("androidAccessVersion", androidAccessVersion);
		_informationList.put("mediaServerVersion", mediaServerVersion);
	}

	public Map<String, String> GetInformationList() {
		return _informationList;
	}

	public String GetKey(int index) {
		int compareIndex = 0;
		for (String key : _informationList.keySet()) {
			if (compareIndex == index) {
				return key;
			}
			compareIndex++;
		}
		return "";
	}

	public String GetValue(int index) {
		int compareIndex = 0;
		for (String key : _informationList.keySet()) {
			if (compareIndex == index) {
				return _informationList.get(key);
			}
			compareIndex++;
		}
		return "";
	}

	public String toString() {
		String information = "{Information: ";
		for (String key : _informationList.keySet()) {
			information += "{" + key + ": " + _informationList.get(key) + "};";
		}
		information += "}";
		return information;
	}
}
