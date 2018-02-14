package guepardoapps.lucahome.common.services;

import java.util.ArrayList;

import guepardoapps.lucahome.common.classes.Meal;

public interface IMealService extends ILucaService<Meal> {
    String MealDownloadFinishedBroadcast = "guepardoapps.lucahome.common.services.meal.download.finished";
    String MealUpdateFinishedBroadcast = "guepardoapps.lucahome.common.services.meal.update.finished";
    String MealClearFinishedBroadcast = "guepardoapps.lucahome.common.services.meal.clear.finished";

    String MealDownloadFinishedBundle = "MealDownloadFinishedBundle";
    String MealUpdateFinishedBundle = "MealUpdateFinishedBundle";
    String MealClearFinishedBundle = "MealClearFinishedBundle";

    ArrayList<String> GetTitleList();

    ArrayList<String> GetDescriptionList();

    void ShareMenuList();
}
