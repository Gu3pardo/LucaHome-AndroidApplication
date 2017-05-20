package guepardoapps.lucahome.common.dto;

public class MainListViewItemDto {

	@SuppressWarnings("unused")
	private static final String TAG = MainListViewItemDto.class.getSimpleName();

	private int _imageResource;
	private String _text;

	public MainListViewItemDto(int imageResource, String text) {
		_imageResource = imageResource;
		_text = text;
	}

	public int GetImageResource() {
		return _imageResource;
	}

	public String GetText() {
		return _text;
	}
}
