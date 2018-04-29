package guepardoapps.lucahome.common.services;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.MoneyLog;
import guepardoapps.lucahome.common.classes.MoneyLogItem;

@SuppressWarnings({"unused"})
public interface IMoneyLogService extends ILucaService<MoneyLogItem> {
    String MoneyLogItemDownloadFinishedBroadcast = "guepardoapps.lucahome.common.services.moneylogitem.download.finished";
    String MoneyLogItemAddFinishedBroadcast = "guepardoapps.lucahome.common.services.moneylogitem.add.finished";
    String MoneyLogItemUpdateFinishedBroadcast = "guepardoapps.lucahome.common.services.moneylogitem.update.finished";
    String MoneyLogItemDeleteFinishedBroadcast = "guepardoapps.lucahome.common.services.moneylogitem.delete.finished";

    String MoneyLogItemDownloadFinishedBundle = "MoneyLogItemDownloadFinishedBundle";
    String MoneyLogItemAddFinishedBundle = "MoneyLogItemAddFinishedBundle";
    String MoneyLogItemUpdateFinishedBundle = "MoneyLogItemUpdateFinishedBundle";
    String MoneyLogItemDeleteFinishedBundle = "MoneyLogItemDeleteFinishedBundle";

    ArrayList<MoneyLogItem> GetByTypeUuid(@NonNull UUID typeUuid);

    ArrayList<MoneyLog> GetMoneyLogList();

    ArrayList<UUID> GetTypeUuidList();
}
