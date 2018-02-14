package guepardoapps.lucahome.common.services;

import android.support.annotation.NonNull;

import java.util.ArrayList;

import guepardoapps.lucahome.common.classes.Birthday;

public interface IBirthdayService extends ILucaService<Birthday> {
    String BirthdayDownloadFinishedBroadcast = "guepardoapps.lucahome.common.services.birthday.download.finished";
    String BirthdayAddFinishedBroadcast = "guepardoapps.lucahome.common.services.birthday.add.finished";
    String BirthdayUpdateFinishedBroadcast = "guepardoapps.lucahome.common.services.birthday.update.finished";
    String BirthdayDeleteFinishedBroadcast = "guepardoapps.lucahome.common.services.birthday.delete.finished";

    String BirthdayDownloadFinishedBundle = "BirthdayDownloadFinishedBundle";
    String BirthdayAddFinishedBundle = "BirthdayAddFinishedBundle";
    String BirthdayUpdateFinishedBundle = "BirthdayUpdateFinishedBundle";
    String BirthdayDeleteFinishedBundle = "BirthdayDeleteFinishedBundle";

    ArrayList<Birthday> GetRemindMeList();

    ArrayList<String> GetNameList();

    ArrayList<String> GetGroupList();

    ArrayList<Birthday> RetrieveContactPhotos(@NonNull ArrayList<Birthday> birthdayList);
}
