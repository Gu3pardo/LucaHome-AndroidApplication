package guepardoapps.mediamirror.common.dto;

import guepardoapps.lucahome.common.tools.LucaHomeLogger;

public class YoutubeVideoDto {

	@SuppressWarnings("unused")
	private static final String TAG = YoutubeVideoDto.class.getName();
	@SuppressWarnings("unused")
	private LucaHomeLogger _logger;

	private String _youtubeId;
	private String _title;
	private String _description;

	public YoutubeVideoDto(String youtubeId, String title, String description) {
		_youtubeId = youtubeId;
		_title = title;
		_description = description;
	}

	public String GetYoutubeId() {
		return _youtubeId;
	}

	public String GetTitle() {
		return _title;
	}

	public String GetDescription() {
		return _description;
	}

	@Override
	public String toString() {
		return YoutubeVideoDto.class.getName() + ":{youtubeId: " + _youtubeId + ", title: " + _title + ", description: "
				+ _description + "}";
	}
}
