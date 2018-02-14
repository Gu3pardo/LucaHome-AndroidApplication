package guepardoapps.lucahome.common.services;

import java.util.ArrayList;

import guepardoapps.lucahome.common.classes.ShoppingItem;

public interface IShoppingItemService extends ILucaService<ShoppingItem> {
    String ShoppingItemDownloadFinishedBroadcast = "guepardoapps.lucahome.common.services.shoppingitem.download.finished";
    String ShoppingItemAddFinishedBroadcast = "guepardoapps.lucahome.common.services.shoppingitem.add.finished";
    String ShoppingItemUpdateFinishedBroadcast = "guepardoapps.lucahome.common.services.shoppingitem.update.finished";
    String ShoppingItemDeleteFinishedBroadcast = "guepardoapps.lucahome.common.services.shoppingitem.delete.finished";

    String ShoppingItemDownloadFinishedBundle = "ShoppingItemDownloadFinishedBundle";
    String ShoppingItemAddFinishedBundle = "ShoppingItemAddFinishedBundle";
    String ShoppingItemUpdateFinishedBundle = "ShoppingItemUpdateFinishedBundle";
    String ShoppingItemDeleteFinishedBundle = "ShoppingItemDeleteFinishedBundle";

    ArrayList<String> GetDetailList();

    void ClearShoppingList();

    void ShareShoppingItemList();
}
