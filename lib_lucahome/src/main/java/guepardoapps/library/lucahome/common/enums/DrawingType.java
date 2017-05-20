package guepardoapps.library.lucahome.common.enums;

import java.io.Serializable;

import guepardoapps.library.lucahome.R;

public enum DrawingType implements Serializable {

	NULL(0, "", R.drawable.drawing_raspberry), 
	RASPBERRY(1, "Raspberry", R.drawable.drawing_raspberry), 
	ARDUINO(2, "Arduino", R.drawable.drawing_arduino), 
	SOCKET(3, "Socket", R.drawable.drawing_socket_off), 
	TEMPERATURE(4, "Temperature", R.drawable.drawing_temperature), 
	MEDIASERVER(5, "MediaServer", R.drawable.drawing_mediamirror_off), 
	SHOPPING_LIST(6, "ShoppingList", R.drawable.drawing_shoppinglist), 
	MENU(7, "Menu", R.drawable.drawing_menu), 
	CAMERA(8, "Camera", R.drawable.drawing_menu);

	private String _type;
	private int _id;
	private int _drawable;

	DrawingType(int id, String type, int drawable) {
		_id = id;
		_type = type;
		_drawable = drawable;
	}

	public int GetId() {
		return _id;
	}

	public int GetDrawable() {
		return _drawable;
	}

	@Override
	public String toString() {
		return _type;
	}

	public static DrawingType GetById(int id) {
		for (DrawingType e : values()) {
			if (e._id == id) {
				return e;
			}
		}
		return NULL;
	}
}
