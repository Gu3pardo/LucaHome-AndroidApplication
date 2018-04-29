package guepardoapps.lucahome.common.services;

import java.util.ArrayList;

import guepardoapps.lucahome.common.classes.SuggestedMeal;

@SuppressWarnings({"unused"})
public interface ISuggestedMealService extends ILucaService<SuggestedMeal> {
    String SuggestedMealDownloadFinishedBroadcast = "guepardoapps.lucahome.common.services.suggestedmeal.download.finished";
    String SuggestedMealAddFinishedBroadcast = "guepardoapps.lucahome.common.services.suggestedmeal.add.finished";
    String SuggestedMealUpdateFinishedBroadcast = "guepardoapps.lucahome.common.services.suggestedmeal.update.finished";
    String SuggestedMealDeleteFinishedBroadcast = "guepardoapps.lucahome.common.services.suggestedmeal.delete.finished";

    String SuggestedMealDownloadFinishedBundle = "SuggestedMealDownloadFinishedBundle";
    String SuggestedMealAddFinishedBundle = "SuggestedMealAddFinishedBundle";
    String SuggestedMealUpdateFinishedBundle = "SuggestedMealUpdateFinishedBundle";
    String SuggestedMealDeleteFinishedBundle = "SuggestedMealDeleteFinishedBundle";

    ArrayList<String> GetTitleList();

    ArrayList<String> GetDescriptionList();

    void ShareSuggestedMealList();
}
