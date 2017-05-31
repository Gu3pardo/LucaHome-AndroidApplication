package guepardoapps.library.lucahome.controller;

import android.content.Context;
import android.support.annotation.NonNull;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.constants.Constants;
import guepardoapps.library.lucahome.common.dto.ScheduleDto;
import guepardoapps.library.lucahome.common.enums.LucaServerAction;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

public class ScheduleController {

    private static final String TAG = ScheduleController.class.getSimpleName();
    private LucaHomeLogger _logger;

    private ServiceController _serviceController;

    public ScheduleController(@NonNull Context context) {
        _logger = new LucaHomeLogger(TAG);
        _serviceController = new ServiceController(context);
    }

    public void LoadSchedules() {
        _logger.Debug("LoadSchedules");
        _serviceController.StartRestService(
                Bundles.SCHEDULE_DOWNLOAD,
                LucaServerAction.GET_SCHEDULES.toString(),
                Broadcasts.DOWNLOAD_SCHEDULE_FINISHED);
    }

    public void SetSchedule(
            @NonNull ScheduleDto schedule,
            boolean newState) {
        _logger.Debug("SetSchedule: " + schedule.GetName() + " to " + String.valueOf(newState));
        _serviceController.StartRestService(
                schedule.GetName(),
                schedule.GetCommandSet(newState),
                Broadcasts.RELOAD_SCHEDULE);
    }

    public void SetSchedule(
            @NonNull String scheduleName,
            boolean newState) {
        _logger.Debug("SetSchedule: " + scheduleName + " to " + String.valueOf(newState));
        _serviceController.StartRestService(
                scheduleName,
                LucaServerAction.SET_SCHEDULE.toString() + scheduleName + ((newState) ? Constants.STATE_ON : Constants.STATE_OFF),
                Broadcasts.RELOAD_SCHEDULE);
    }

    public void DeleteSchedule(@NonNull ScheduleDto schedule) {
        _logger.Debug("DeleteSchedule");
        _serviceController.StartRestService(
                schedule.GetName(),
                schedule.GetCommandDelete(),
                Broadcasts.RELOAD_SCHEDULE);
    }
}
