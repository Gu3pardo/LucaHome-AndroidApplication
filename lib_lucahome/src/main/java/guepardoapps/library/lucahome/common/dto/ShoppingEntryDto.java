package guepardoapps.library.lucahome.common.dto;

import java.io.Serializable;

import guepardoapps.library.lucahome.common.constants.ServerActions;
import guepardoapps.library.lucahome.common.enums.ShoppingEntryGroup;

public class ShoppingEntryDto implements Serializable {

	private static final long serialVersionUID = 1356205499060127468L;

	private static final String TAG = ShoppingEntryDto.class.getSimpleName();

	private int _id;
	private String _name;
	private ShoppingEntryGroup _group;
	private int _quantity;

	private boolean _bought;

	public ShoppingEntryDto(int id, String name, ShoppingEntryGroup group, int quantity, boolean bought) {
		_id = id;
		_name = name;
		_group = group;
		_quantity = quantity;

		_bought = bought;
	}

	public int GetId() {
		return _id;
	}

	public String GetName() {
		return _name;
	}

	public ShoppingEntryGroup GetGroup() {
		return _group;
	}

	public int GetQuantity() {
		return _quantity;
	}

	public boolean GetBought() {
		return _bought;
	}

	public void SetBought(boolean bought) {
		_bought = bought;
	}

	public void IncreaseQuantity() {
		_quantity++;
	}

	public void DecreaseQuantity() {
		_quantity--;
		if (_quantity < 0) {
			_quantity = 0;
		}
	}

	public String GetCommandAdd() {
		return String.format(ServerActions.ADD_SHOPPING_ENTRY_F, _id, _name, _group.toString(), _quantity);
	}

	public String GetCommandUpdate() {
		return String.format(ServerActions.UPDATE_SHOPPING_ENTRY_F, _id, _name, _group.toString(), _quantity);
	}

	public String GetCommandDelete() {
		return ServerActions.DELETE_SHOPPING_ENTRY + String.valueOf(_id);
	}

	public String GetCommandBoughtChanged() {
		return "ACTION:SET:SHOPPING:BOUGHT:" + String.valueOf(_name) + ":" + (_bought ? "1" : "0");
	}

	public String toString() {
		return "{" + TAG + ": {ID: " + String.valueOf(_id) + "};{Name: " + _name + "};{Group: " + _group.toString()
				+ "};{Quantity: " + String.valueOf(_quantity) + "}}";
	}
}
