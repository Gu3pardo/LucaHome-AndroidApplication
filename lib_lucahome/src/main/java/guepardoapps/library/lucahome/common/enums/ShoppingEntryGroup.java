package guepardoapps.library.lucahome.common.enums;

import java.io.Serializable;

import guepardoapps.library.lucahome.R;

public enum ShoppingEntryGroup implements Serializable {

	OTHER("Other", 0, R.drawable.shopping),
	FRUIT("Fruit", 1, R.drawable.fruits), 
	VEGETABLE("Vegetable", 2, R.drawable.vegetables), 
	SALAD("Salat", 3, R.drawable.salad), 
	DRINKS("Drinks", 4, R.drawable.drinks), 
	OIL_VINEGAR("Oil_and_vinegar", 5, R.drawable.oil_vinegar), 
	NOODLES("Noodles", 6, R.drawable.noodles), 
	BREAD("Bread", 7, R.drawable.bread), 
	SPREAD("Spread", 8, R.drawable.spread), 
	MILK_PRODUCT("Milk_product", 9, R.drawable.milk),
	CANDY("Candy", 10, R.drawable.candy), 
	CLEANING_MATERIAL("Cleaning_material", 11, R.drawable.cleaning), 
	BATH_UTILITY("Bath_utility", 12, R.drawable.bath), 
	LEISURE("Leisure", 13, R.drawable.leisure), 
	BAKING("Baking", 14, R.drawable.baking), 
	CEREALS("Cereals", 15, R.drawable.cereals);

	private String _string;
	private int _int;
	private int _drawable;

	ShoppingEntryGroup(String stringValue, int intValue, int drawable) {
		_string = stringValue;
		_int = intValue;
		_drawable = drawable;
	}

	@Override
	public String toString() {
		return _string;
	}

	public int GetInt() {
		return _int;
	}

	public int GetDrawable() {
		return _drawable;
	}

	public static ShoppingEntryGroup GetById(int value) {
		for (ShoppingEntryGroup e : values()) {
			if (e._int == value) {
				return e;
			}
		}
		return OTHER;
	}

	public static ShoppingEntryGroup GetByString(String value) {
		for (ShoppingEntryGroup e : values()) {
			if (e._string.contains(value)) {
				return e;
			}
		}
		return OTHER;
	}
}
