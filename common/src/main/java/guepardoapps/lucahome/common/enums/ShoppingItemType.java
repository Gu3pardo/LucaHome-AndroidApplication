package guepardoapps.lucahome.common.enums;

import java.io.Serializable;

import guepardoapps.lucahome.common.R;

public enum ShoppingItemType implements Serializable {

    OTHER("Other", 0, R.drawable.shopping_other),
    FRUIT("Fruit", 1, R.drawable.shopping_fruits),
    VEGETABLE("Vegetable", 2, R.drawable.shopping_vegetables),
    SALAD("Salad", 3, R.drawable.shopping_salad),
    SPREAD("Spread", 4, R.drawable.shopping_spread),
    DRINKS("Drinks", 5, R.drawable.shopping_drinks),
    CEREALS("Cereals", 6, R.drawable.shopping_cereals),
    BREAD("Bread", 7, R.drawable.shopping_bread),
    BAKING("Baking", 8, R.drawable.shopping_baking),
    SPICES("Spices", 9, R.drawable.shopping_spices),
    OIL_VINEGAR("Oil_and_vinegar", 10, R.drawable.shopping_oil_vinegar),
    NOODLES("Noodles", 11, R.drawable.shopping_noodles),
    CANDY("Candy", 12, R.drawable.shopping_candy),
    CLEANING_MATERIAL("Cleaning_material", 13, R.drawable.shopping_cleaning),
    BATH_UTILITY("Bath_utility", 14, R.drawable.shopping_bath),
    LEISURE("Leisure", 15, R.drawable.shopping_leisure);

    private String _string;
    private int _int;
    private int _drawable;

    ShoppingItemType(String stringValue, int intValue, int drawable) {
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

    public static ShoppingItemType GetById(int value) {
        for (ShoppingItemType item : values()) {
            if (item._int == value) {
                return item;
            }
        }
        return OTHER;
    }

    public static ShoppingItemType GetByString(String value) {
        for (ShoppingItemType item : values()) {
            if (item._string.contains(value)) {
                return item;
            }
        }
        return OTHER;
    }
}
