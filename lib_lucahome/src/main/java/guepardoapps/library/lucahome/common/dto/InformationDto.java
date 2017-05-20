package guepardoapps.library.lucahome.common.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class InformationDto implements Serializable {

	private static final long serialVersionUID = 2257042096692273681L;

	private Map<String, String> _informationList;

	public InformationDto(String author, String company, String contact, String buildDate, String serverVersion,
			String websiteVersion, String temperatureLogVersion, String androidAppVersion, String androidWearVersion,
			String androidAccessVersion, String mediaServerVersion) {
		_informationList = new HashMap<>();

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

	public String GetAuthor() {
		return _informationList.get("author");
	}

	public String GetCompany() {
		return _informationList.get("company");
	}

	public String GetContact() {
		return _informationList.get("contact");
	}

	public String GetBuildDate() {
		return _informationList.get("buildDate");
	}

	public String GetServerVersion() {
		return _informationList.get("serverVersion");
	}

	public String GetWebsiteVersion() {
		return _informationList.get("websiteVersion");
	}

	public String GetTemperatureLogVersion() {
		return _informationList.get("temperatureLogVersion");
	}

	public String GetAndroidAppVersion() {
		return _informationList.get("androidAppVersion");
	}

	public String GetAndroidWearVersion() {
		return _informationList.get("androidWearVersion");
	}

	public String GetAndroidAccessVersion() {
		return _informationList.get("androidAccessVersion");
	}

	public String GetMediaServerVersion() {
		return _informationList.get("mediaServerVersion");
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
