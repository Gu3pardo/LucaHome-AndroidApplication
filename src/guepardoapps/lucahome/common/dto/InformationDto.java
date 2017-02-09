package guepardoapps.lucahome.common.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class InformationDto implements Serializable {

	private static final long serialVersionUID = 2257042096692273681L;

	private Map<String, String> _informationList;

	public InformationDto(String author, String company, String contact, String builddate, String serverversion,
			String websiteversion, String temperaturelogversion, String androidappversion, String androidwearversion,
			String androidaccessversion) {
		_informationList = new HashMap<String, String>();

		_informationList.put("author", author);
		_informationList.put("company", company);
		_informationList.put("contact", contact);
		_informationList.put("builddate", builddate);
		_informationList.put("serverversion", serverversion);
		_informationList.put("websiteversion", websiteversion);
		_informationList.put("temperaturelogversion", temperaturelogversion);
		_informationList.put("androidappversion", androidappversion);
		_informationList.put("androidwearversion", androidwearversion);
		_informationList.put("androidaccessversion", androidaccessversion);
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
		return _informationList.get("builddate");
	}

	public String GetServerVersion() {
		return _informationList.get("serverversion");
	}

	public String GetWebsiteVersion() {
		return _informationList.get("websiteversion");
	}

	public String GetTemperatureLogVersion() {
		return _informationList.get("temperaturelogversion");
	}

	public String GetAndroidAppVersion() {
		return _informationList.get("androidappversion");
	}

	public String GetAndroidWearVersion() {
		return _informationList.get("androidwearversion");
	}

	public String GetAndroidAccessVersion() {
		return _informationList.get("androidaccessversion");
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
