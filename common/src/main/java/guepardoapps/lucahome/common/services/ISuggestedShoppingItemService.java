package guepardoapps.lucahome.common.services;

import guepardoapps.lucahome.common.classes.SuggestedShoppingItem;

public interface ISuggestedShoppingItemService extends ILucaService<SuggestedShoppingItem> {
    String SuggestedShoppingItemDownloadFinishedBroadcast = "guepardoapps.lucahome.common.services.suggestedshoppingitem.download.finished";
    String SuggestedShoppingItemAddFinishedBroadcast = "guepardoapps.lucahome.common.services.suggestedshoppingitem.add.finished";
    String SuggestedShoppingItemUpdateFinishedBroadcast = "guepardoapps.lucahome.common.services.suggestedshoppingitem.update.finished";
    String SuggestedShoppingItemDeleteFinishedBroadcast = "guepardoapps.lucahome.common.services.suggestedshoppingitem.delete.finished";

    String SuggestedShoppingItemDownloadFinishedBundle = "SuggestedShoppingItemDownloadFinishedBundle";
    String SuggestedShoppingItemAddFinishedBundle = "SuggestedShoppingItemAddFinishedBundle";
    String SuggestedShoppingItemUpdateFinishedBundle = "SuggestedShoppingItemUpdateFinishedBundle";
    String SuggestedShoppingItemDeleteFinishedBundle = "SuggestedShoppingItemDeleteFinishedBundle";
}
