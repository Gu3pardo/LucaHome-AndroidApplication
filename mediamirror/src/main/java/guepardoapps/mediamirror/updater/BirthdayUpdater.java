package guepardoapps.mediamirror.updater;

import java.util.ArrayList;

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
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;

public class BirthdayUpdater {
    private static final String TAG = BirthdayUpdater.class.getSimpleName();

    private static final int MAX_BIRTHDAY_COUNT = 1;

    private BroadcastController _broadcastController;
    private ReceiverController _receiverController;

    private boolean _isRunning;

    private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ObjectChangeFinishedContent result = (ObjectChangeFinishedContent) intent.getSerializableExtra(BirthdayService.BirthdayDownloadFinishedBundle);

            if (result != null) {
                SerializableList<LucaBirthday> loadedBirthdayList = BirthdayService.getInstance().GetRemindMeList();
                SerializableList<LucaBirthday> _nextBirthdaysList = new SerializableList<>();

                if (loadedBirthdayList != null) {
                    if (loadedBirthdayList.getSize() == 0) {
                        Logger.getInstance().Warning(TAG, "loadedBirthdayList size is 0!");
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
                        ArrayList<LucaBirthday> nextDateList = new ArrayList<>();
                        ArrayList<LucaBirthday> prevDateList = new ArrayList<>();

                        for (int index = 0; index < loadedBirthdayList.getSize(); index++) {
                            LucaBirthday entry = loadedBirthdayList.getValue(index);

                            switch (entry.CurrentBirthdayType()) {
                                case UPCOMING:
                                case TODAY:
                                    nextDateList.add(entry);
                                    break;
                                case PREVIOUS:
                                    prevDateList.add(entry);
                                    break;
                                default:
                                    Logger.getInstance().Error(TAG, "Not supported BirthdayDateType: " + entry.CurrentBirthdayType());
                                    break;
                            }
                        }

                        int nextDateCount = nextDateList.size();
                        if (nextDateCount >= MAX_BIRTHDAY_COUNT) {
                            for (int index = 0; index < MAX_BIRTHDAY_COUNT; index++) {
                                _nextBirthdaysList.setValue(index, nextDateList.get(index));
                            }
                        } else {
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
                    Logger.getInstance().Warning(TAG, "loadedBirthdayList is null!");
                }

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
            DownloadBirthdays();
        }
    };

    public BirthdayUpdater(@NonNull Context context) {
        _broadcastController = new BroadcastController(context);
        _receiverController = new ReceiverController(context);
        BirthdayService.getInstance().Initialize(context, null, false, true, 6 * 60);
    }

    public void Start() {
        if (_isRunning) {
            Logger.getInstance().Warning(TAG, "Already running!");
            return;
        }

        _receiverController.RegisterReceiver(_updateReceiver, new String[]{BirthdayService.BirthdayDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_performUpdateReceiver, new String[]{Broadcasts.PERFORM_BIRTHDAY_UPDATE});

        _isRunning = true;
        DownloadBirthdays();
    }

    public void Dispose() {
        BirthdayService.getInstance().Dispose();
        _receiverController.Dispose();
        _isRunning = false;
    }

    public void DownloadBirthdays() {
        BirthdayService.getInstance().LoadData();
    }
}
