package guepardoapps.lucahome.common.converter.json;

import guepardoapps.lucahome.common.classes.SerializableList;
import guepardoapps.lucahome.common.dto.MovieDto;
import guepardoapps.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.lucahome.common.tools.StringHelper;

public final class JsonDataToMovieConverter {

	private static final String TAG = JsonDataToMovieConverter.class.getName();
	private static LucaHomeLogger _logger;

	private static String _searchParameter = "{movie:";

	public static SerializableList<MovieDto> GetList(String[] stringArray) {
		if (StringHelper.StringsAreEqual(stringArray)) {
			return ParseStringToList(stringArray[0]);
		} else {
			String usedEntry = StringHelper.SelectString(stringArray, _searchParameter);
			return ParseStringToList(usedEntry);
		}
	}

	public static MovieDto Get(String value) {
		if (StringHelper.GetStringCount(value, _searchParameter) == 1) {
			if (value.contains(_searchParameter)) {
				value = value.replace(_searchParameter, "").replace("};};", "");

				String[] data = value.split("\\};");
				MovieDto newValue = ParseStringToValue(data);
				if (newValue != null) {
					return newValue;
				}
			}
		}

		if (_logger == null) {
			_logger = new LucaHomeLogger(TAG);
		}
		_logger.Error(value + " has an error!");

		return null;
	}

	private static SerializableList<MovieDto> ParseStringToList(String value) {
		if (StringHelper.GetStringCount(value, _searchParameter) > 1) {
			if (value.contains(_searchParameter)) {
				SerializableList<MovieDto> list = new SerializableList<MovieDto>();

				String[] entries = value.split("\\" + _searchParameter);
				for (String entry : entries) {
					entry = entry.replace(_searchParameter, "").replace("};};", "");

					String[] data = entry.split("\\};");
					MovieDto newValue = ParseStringToValue(data);
					if (newValue != null) {
						list.addValue(newValue);
					}
				}
				return list;
			}
		}

		if (_logger == null) {
			_logger = new LucaHomeLogger(TAG);
		}
		_logger.Error(value + " has an error!");

		return null;
	}

	private static MovieDto ParseStringToValue(String[] data) {
		if (data.length == 6) {
			if (data[0].contains("{Title:") && data[1].contains("{Genre:") && data[2].contains("{Description:")
					&& data[3].contains("{Rating:") && data[4].contains("{Watched:") && data[5].contains("{Sockets:")) {

				String Title = data[0].replace("{Title:", "").replace("};", "");
				String Genre = data[1].replace("{Genre:", "").replace("};", "");
				String Description = data[2].replace("{Description:", "").replace("};", "");

				String RatingString = data[3].replace("{Rating:", "").replace("};", "");
				int rating = Integer.parseInt(RatingString);

				String WatchedString = data[4].replace("{Watched:", "").replace("};", "");
				int watched = Integer.parseInt(WatchedString);

				String SocketString = data[5].replace("{Sockets:", "").replace("};", "");
				String[] sockets = SocketString.split("\\|");

				MovieDto newValue = new MovieDto(Title, Genre, Description, rating, watched, sockets);
				return newValue;
			}
		}

		if (_logger == null) {
			_logger = new LucaHomeLogger(TAG);
		}
		_logger.Error("Data has an error!");

		return null;
	}
}