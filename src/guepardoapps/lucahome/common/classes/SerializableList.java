package guepardoapps.lucahome.common.classes;

import java.io.Serializable;
import java.util.ArrayList;

public class SerializableList<T> implements Serializable {

	private static final long serialVersionUID = -7933419990581963085L;

	private ArrayList<T> _dataList;

	public SerializableList() {
		_dataList = new ArrayList<T>();
	}

	public void addValue(T newValue) {
		if (_dataList == null) {
			_dataList = new ArrayList<T>();
		}
		_dataList.add(newValue);
	}

	public void setValue(int index, T value) {
		if (index >= getSize()) {
			return;
		}
		_dataList.set(index, value);
	}

	public void removeValue(T removeValue) {
		if (_dataList == null) {
			_dataList = new ArrayList<T>();
			return;
		}
		_dataList.remove(removeValue);
	}

	public void removeValue(int index) {
		if (_dataList == null) {
			_dataList = new ArrayList<T>();
			return;
		}
		_dataList.remove(index);
	}

	public boolean HasValue(T checkValue) {
		if (_dataList == null) {
			_dataList = new ArrayList<T>();
			return false;
		}

		for (int index = 0; index < getSize(); index++) {
			if (checkValue == getValue(index)) {
				return true;
			}
		}

		return false;
	}

	public T getValue(int index) {
		if (index >= getSize()) {
			return null;
		}
		return _dataList.get(index);
	}

	public int getSize() {
		if (_dataList == null) {
			return -1;
		}
		return _dataList.size();
	}

	public int getIndex(T entry) {
		for (int index = 0; index < getSize(); index++) {
			if (entry == getValue(index)) {
				return index;
			}
		}

		return -1;
	}

	public void clear() {
		if (_dataList == null) {
			_dataList = new ArrayList<T>();
			return;
		}
		_dataList.clear();
	}

	public String toString() {
		if (_dataList == null) {
			return "-1";
		}

		String response = "";
		for (int index = 0; index < getSize(); index++) {
			response += getValue(index).toString();
		}

		return response;
	}
}
