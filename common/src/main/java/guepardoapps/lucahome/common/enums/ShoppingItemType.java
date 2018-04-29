package guepardoapps.lucahome.common.enums;

import java.io.Serializable;

import guepardoapps.lucahome.common.R;

@SuppressWarnings({"unused"})
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

    private String _title;
    private int _id;
    private int _drawable;

    ShoppingItemType(String title, int id, int drawable) {
        _title = title;
        _id = id;
        _drawable = drawable;
    }

    @Override
    public String toString() {
        return _title;
    }

    public int GetId() {
        return _id;
    }

    public int GetDrawable() {
        return _drawable;
    }

    public static ShoppingItemType GetById(int value) {
        for (ShoppingItemType item : values()) {
            if (item._id == value) {
                return item;
            }
        }
        return OTHER;
    }

    public static ShoppingItemType GetByTitle(String value) {
        for (ShoppingItemType item : values()) {
            if (item._title.contains(value)) {
                return item;
            }
        }
        return OTHER;
    }
}
