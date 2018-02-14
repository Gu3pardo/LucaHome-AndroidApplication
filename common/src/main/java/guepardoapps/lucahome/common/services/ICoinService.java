package guepardoapps.lucahome.common.services;

import android.support.annotation.NonNull;

import java.util.ArrayList;

import guepardoapps.lucahome.common.classes.Coin;

public interface ICoinService extends ILucaService<Coin> {
    int NotificationId = 1100017;

    String CoinDownloadFinishedBroadcast = "guepardoapps.lucahome.common.services.coin.download.finished";
    String CoinAddFinishedBroadcast = "guepardoapps.lucahome.common.services.coin.add.finished";
    String CoinUpdateFinishedBroadcast = "guepardoapps.lucahome.common.services.coin.update.finished";
    String CoinDeleteFinishedBroadcast = "guepardoapps.lucahome.common.services.coin.delete.finished";
    String CoinDataDownloadFinishedBroadcast = "guepardoapps.lucahome.common.services.coindata.download.finished";

    String CoinDownloadFinishedBundle = "CoinDownloadFinishedBundle";
    String CoinAddFinishedBundle = "CoinAddFinishedBundle";
    String CoinUpdateFinishedBundle = "CoinUpdateFinishedBundle";
    String CoinDeleteFinishedBundle = "CoinDeleteFinishedBundle";
    String CoinDataDownloadFinishedBundle = "CoinDataDownloadFinishedBundle";

    ArrayList<String> GetTypeList();

    double AllCoinsValue();

    double FilteredCoinsValue(@NonNull String searchKey);

    void LoadCoinData(@NonNull Coin coin);

    void SetCoinHoursTrend(int coinHoursTrend);

    int GetCoinHoursTrend();
}
