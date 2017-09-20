package guepardoapps.mediamirrorv2.updater;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.classes.LucaBirthday;
import guepardoapps.lucahome.common.service.BirthdayService;
import guepardoapps.mediamirrorv2.common.constants.Broadcasts;
import guepardoapps.mediamirrorv2.common.constants.Bundles;

public class BirthdayUpdater {
    private static final String TAG = BirthdayUpdater.class.getSimpleName();
    private Logger _logger;

    private static final int MAX_BIRTHDAY_COUNT = 1;

    private BroadcastController _broadcastController;
    private ReceiverController _receiverController;

    private BirthdayService _birthdayService;

    private boolean _isRunning;

    private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_updateReceiver");
            BirthdayService.BirthdayDownloadFinishedContent result =
                    (BirthdayService.BirthdayDownloadFinishedContent) intent.getSerializableExtra(BirthdayService.BirthdayDownloadFinishedBundle);

            if (result != null) {
                SerializableList<LucaBirthday> loadedBirthdayList = result.BirthdayList;
                SerializableList<LucaBirthday> _nextBirthdaysList = new SerializableList<>();

                if (loadedBirthdayList != null) {
                    if (loadedBirthdayList.getSize() == 0) {
                        _logger.Warning("loadedBirthdayList size is 0!");
                        return;
                    }

                    if (loadedBirthdayList.getSize() == MAX_BIRTHDAY_COUNT) {
                        _nextBirthdaysList = loadedBirthdayList;
                    } else if (loadedBirthdayList.getSize() < MAX_BIRTHDAY_COUNT) {
                        int count = loadedBirthdayList.getSize();
                        for (int index = 0; index < count - 1; index++) {
                            _nextBirthdaysList.setValue(index, loadedBirthdayList.getValue(index));
                        }
                        for (int index = 2; index > count - 1; index--) {
                            _nextBirthdaysList.setValue(index, null);
                        }
                    } else if (loadedBirthdayList.getSize() > MAX_BIRTHDAY_COUNT) {
                        Calendar today = Calendar.getInstance();
                        ArrayList<LucaBirthday> nextDateList = new ArrayList<>();
                        ArrayList<LucaBirthday> prevDateList = new ArrayList<>();

                        _logger.Information("Today:");
                        _logger.Information(today.toString());

                        for (int index = 0; index < loadedBirthdayList.getSize(); index++) {
                            LucaBirthday entry = loadedBirthdayList.getValue(index);

                            _logger.Information("Entry:" + entry.toString());

                            switch (entry.CurrentBirthdayType()) {
                                case UPCOMING:
                                case TODAY:
                                    _logger.Information("Next: " + entry.GetName());
                                    nextDateList.add(entry);
                                    break;
                                case PREVIOUS:
                                    _logger.Information("Prev:" + entry.GetName());
                                    prevDateList.add(entry);
                                    break;
                                default:
                                    _logger.Error("Not supported BirthdayDateType: " + entry.CurrentBirthdayType());
                                    break;
                            }
                        }

                        int nextDateCount = nextDateList.size();
                        _logger.Debug("BirthdayList nextDateCount" + String.valueOf(nextDateCount));
                        if (nextDateCount >= MAX_BIRTHDAY_COUNT) {
                            _logger.Debug("Size of nextDateCount is bigger or same as MAX_BIRTHDAY_COUNT");
                            for (int index = 0; index < MAX_BIRTHDAY_COUNT; index++) {
                                _logger.Debug("Adding entry to _nextBirthdaysList");
                                _nextBirthdaysList.setValue(index, nextDateList.get(index));
                                _logger.Debug("Added entry to list: " + _nextBirthdaysList.getValue(index).toString());
                            }
                        } else {
                            _logger.Debug("Size of nextDateCount is lower then MAX_BIRTHDAY_COUNT");
                            for (int index = 0; index < nextDateCount; index++) {
                                _nextBirthdaysList.setValue(index, nextDateList.get(index));
                            }
                            if (prevDateList.size() > MAX_BIRTHDAY_COUNT - nextDateCount) {
                                for (int index = nextDateCount; index < MAX_BIRTHDAY_COUNT; index++) {
                                    _nextBirthdaysList.setValue(index, prevDateList.get(index));
                                }
                            } else {
                                for (int index = nextDateCount; index < prevDateList.size() + nextDateCount; index++) {
                                    _nextBirthdaysList.setValue(index, prevDateList.get(index));
                                }
                                for (int index = 2; index > prevDateList.size() + nextDateCount - 1; index--) {
                                    _nextBirthdaysList.setValue(index, null);
                                }
                            }
                        }
                    }
                } else {
                    _logger.Warning("loadedBirthdayList is null!");
                }

                _logger.Debug("Sending current _nextBirthdaysList: " + _nextBirthdaysList.toString());
                _broadcastController.SendSerializableBroadcast(
                        Broadcasts.SHOW_BIRTHDAY_MODEL,
                        Bundles.BIRTHDAY_MODEL,
                        _nextBirthdaysList);
            }
        }
    };

    private BroadcastReceiver _performUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_performUpdateReceiver onReceive");
            DownloadBirthdays();
        }
    };

    public BirthdayUpdater(@NonNull Context context) {
        _logger = new Logger(TAG);

        _broadcastController = new BroadcastController(context);
        _receiverController = new ReceiverController(context);

        _birthdayService = BirthdayService.getInstance();
        _birthdayService.Initialize(context, null, false, true, 6 * 60);
    }

    public void Start() {
        _logger.Debug("Initialize");
        if (_isRunning) {
            _logger.Warning("Already running!");
            return;
        }

        _receiverController.RegisterReceiver(_updateReceiver, new String[]{BirthdayService.BirthdayDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_performUpdateReceiver, new String[]{Broadcasts.PERFORM_BIRTHDAY_UPDATE});

        _isRunning = true;
        DownloadBirthdays();
    }

    public void Dispose() {
        _logger.Debug("Dispose");
        _receiverController.Dispose();
        _isRunning = false;
    }

    public void DownloadBirthdays() {
        _logger.Debug("startDownloadBirthdays");
        _birthdayService.LoadData();
    }
}
