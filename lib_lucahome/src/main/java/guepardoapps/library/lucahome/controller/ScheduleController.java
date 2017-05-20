package guepardoapps.library.lucahome.controller;

import android.content.Context;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.constants.Constants;
import guepardoapps.library.lucahome.common.constants.ServerActions;
import guepardoapps.library.lucahome.common.dto.ScheduleDto;
import guepardoapps.library.lucahome.common.enums.LucaObject;
import guepardoapps.library.lucahome.common.enums.RaspberrySelection;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

public class ScheduleController {

    private static final String TAG = ScheduleController.class.getSimpleName();
    private LucaHomeLogger _logger;

    private ServiceController _serviceController;

    public ScheduleController(Context context) {
        _logger = new LucaHomeLogger(TAG);
        _serviceController = new ServiceController(context);
    }

    public void LoadSchedules() {
        _logger.Debug("LoadSchedules");
        _serviceController.StartRestService(Bundles.SCHEDULE_DOWNLOAD, ServerActions.GET_SCHEDULES,
                Broadcasts.DOWNLOAD_SCHEDULE_FINISHED, LucaObject.SCHEDULE, RaspberrySelection.BOTH);
    }

    public void SetSchedule(ScheduleDto schedule, boolean newState) {
        _logger.Debug("SetSchedule: " + schedule.GetName() + " to " + String.valueOf(newState));
        _serviceController.StartRestService(schedule.GetName(), schedule.GetCommandSet(newState),
                Broadcasts.RELOAD_SCHEDULE, LucaObject.SCHEDULE, RaspberrySelection.BOTH);
    }

    public void SetSchedule(String scheduleName, boolean newState) {
        _logger.Debug("SetSchedule: " + scheduleName + " to " + String.valueOf(newState));
        _serviceController.StartRestService(scheduleName,
                ServerActions.SET_SCHEDULE + scheduleName + ((newState) ? Constants.STATE_ON : Constants.STATE_OFF),
                Broadcasts.RELOAD_SCHEDULE, LucaObject.SCHEDULE, RaspberrySelection.BOTH);
    }

    public void DeleteSchedule(ScheduleDto schedule) {
        _logger.Debug("DeleteSchedule");
        _serviceController.StartRestService(schedule.GetName(), schedule.GetCommandDelete(), Broadcasts.RELOAD_SCHEDULE,
                LucaObject.SCHEDULE, RaspberrySelection.BOTH);
    }
}
