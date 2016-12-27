package guepardoapps.lucahome.common.enums;

import java.io.Serializable;

import android.Manifest;

public enum Permissions implements Serializable {

	NULL("", -1),
	READ_EXTERNAL_STORAGE(Manifest.permission.READ_EXTERNAL_STORAGE, 1000), 
	WRITE_EXTERNAL_STORAGE(Manifest.permission.WRITE_EXTERNAL_STORAGE, 1001);

	private String _permissionString;
	private int _requestId;

	private Permissions(String permissionString, int requestId) {
		_permissionString = permissionString;
		_requestId = requestId;
	}

	public String GetPermissionString() {
		return _permissionString;
	}

	public int GetRequestId() {
		return _requestId;
	}

	public static Permissions GetByRequestId(int requestId) {
		for (Permissions e : values()) {
			if (e._requestId == requestId) {
				return e;
			}
		}
		return null;
	}
}
