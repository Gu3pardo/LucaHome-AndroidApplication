package guepardoapps.lucahome.viewcontroller;

import android.content.Context;
import guepardoapps.lucahome.common.LucaHomeLogger;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.*;
import guepardoapps.lucahome.common.enums.LucaObject;
import guepardoapps.lucahome.common.enums.RaspberrySelection;
import guepardoapps.lucahome.dto.ScheduleDto;

public class ScheduleController {

	private static final String TAG = ScheduleController.class.getName();
	private LucaHomeLogger _logger;

	private Context _context;
	private ServiceController _serviceController;

	public ScheduleController(Context context) {
		_logger = new LucaHomeLogger(TAG);
		_context = context;
		_serviceController = new ServiceController(_context);
	}

	public void LoadSchedules() {
		_logger.Debug("LoadSchedules");
		_serviceController.StartRestService(Constants.SCHEDULE_DOWNLOAD, Constants.ACTION_GET_SCHEDULES,
				Constants.BROADCAST_DOWNLOAD_SCHEDULE_FINISHED, LucaObject.SCHEDULE, RaspberrySelection.BOTH);
	}

	public void SetSchedule(ScheduleDto schedule, boolean newState) {
		_logger.Debug("SetSchedule: " + schedule.GetName() + " to " + String.valueOf(newState));
		_serviceController.StartRestService(schedule.GetName(), schedule.GetCommandSet(newState),
				Constants.BROADCAST_RELOAD_SCHEDULE, LucaObject.SCHEDULE, RaspberrySelection.BOTH);
	}

	public void SetSchedule(String scheduleName, boolean newState) {
		_logger.Debug("SetSchedule: " + scheduleName + " to " + String.valueOf(newState));
		_serviceController.StartRestService(scheduleName,
				Constants.ACTION_SET_SCHEDULE + scheduleName + ((newState) ? Constants.STATE_ON : Constants.STATE_OFF),
				Constants.BROADCAST_RELOAD_SCHEDULE, LucaObject.SCHEDULE, RaspberrySelection.BOTH);
	}

	public void DeleteSchedule(ScheduleDto schedule) {
		_logger.Debug("DeleteSchedule");
		_serviceController.StartRestService(schedule.GetName(), schedule.GetCommandDelete(),
				Constants.BROADCAST_RELOAD_SCHEDULE, LucaObject.SCHEDULE, RaspberrySelection.BOTH);
	}
}
