package guepardoapps.lucahome.enums;

import java.io.Serializable;

import guepardoapps.lucahome.views.*;

public enum TargetActivity implements Serializable {
	
	SOCKET("Sockets", SocketView.class),
	BIRTHDAYS("Birthdays", BirthdayView.class), 
	SHOPPING_LIST("ShoppingList", ShoppingListView.class),
	MEDIA_MIRROR("MediaMirror", MediaMirrorView.class),
	MENU("Menu", MenuView.class);

	private String _name;
	private Class<?> _activity;

	TargetActivity(String name, Class<?> activity) {
		_name = name;
		_activity = activity;
	}

	public String GetName() {
		return _name;
	}

	public Class<?> GetActivity() {
		return _activity;
	}

	public static TargetActivity GetByString(String value) {
		for (TargetActivity e : values()) {
			if (e._name.contains(value)) {
				return e;
			}
		}
		return null;
	}
}
