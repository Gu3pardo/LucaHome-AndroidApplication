package guepardoapps.lucahome.view.controller;

import android.content.Context;

import guepardoapps.lucahome.common.constants.Broadcasts;
import guepardoapps.lucahome.common.constants.Bundles;
import guepardoapps.lucahome.common.constants.ServerActions;
import guepardoapps.lucahome.common.controller.*;
import guepardoapps.lucahome.common.dto.TimerDto;
import guepardoapps.lucahome.common.enums.LucaObject;
import guepardoapps.lucahome.common.enums.RaspberrySelection;
import guepardoapps.lucahome.common.tools.LucaHomeLogger;

public class TimerController {

	private static final String TAG = TimerController.class.getName();
	private LucaHomeLogger _logger;

	private Context _context;
	private ServiceController _serviceController;

	public TimerController(Context context) {
		_logger = new LucaHomeLogger(TAG);
		_context = context;
		_serviceController = new ServiceController(_context);
	}

	public void LoadTimer() {
		_logger.Debug("LoadTimer");
		_serviceController.StartRestService(Bundles.SCHEDULE_DOWNLOAD, ServerActions.GET_SCHEDULES,
				Broadcasts.DOWNLOAD_SCHEDULE_FINISHED, LucaObject.SCHEDULE, RaspberrySelection.BOTH);
	}

	public void Delete(TimerDto timer) {
		_logger.Debug("Delete: " + timer.toString());
		_serviceController.StartRestService(timer.GetName(), timer.GetCommandDelete(), Broadcasts.RELOAD_TIMER,
				LucaObject.TIMER, RaspberrySelection.BOTH);
	}
}
