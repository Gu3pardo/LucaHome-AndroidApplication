package guepardoapps.lucahome.common.services;

import android.support.annotation.NonNull;

import java.util.ArrayList;

import guepardoapps.lucahome.common.classes.MeterLog;
import guepardoapps.lucahome.common.classes.MeterLogItem;

public interface IMeterLogService extends ILucaService<MeterLogItem> {
    String MeterLogItemDownloadFinishedBroadcast = "guepardoapps.lucahome.common.services.meterlogitem.download.finished";
    String MeterLogItemAddFinishedBroadcast = "guepardoapps.lucahome.common.services.meterlogitem.add.finished";
    String MeterLogItemUpdateFinishedBroadcast = "guepardoapps.lucahome.common.services.meterlogitem.update.finished";
    String MeterLogItemDeleteFinishedBroadcast = "guepardoapps.lucahome.common.services.meterlogitem.delete.finished";

    String MeterLogItemDownloadFinishedBundle = "MeterLogItemDownloadFinishedBundle";
    String MeterLogItemAddFinishedBundle = "MeterLogItemAddFinishedBundle";
    String MeterLogItemUpdateFinishedBundle = "MeterLogItemUpdateFinishedBundle";
    String MeterLogItemDeleteFinishedBundle = "MeterLogItemDeleteFinishedBundle";

    ArrayList<MeterLogItem> GetByMeterId(@NonNull String meterId);

    ArrayList<MeterLog> GetMeterLogList();

    ArrayList<String> GetMeterIdList();
}
